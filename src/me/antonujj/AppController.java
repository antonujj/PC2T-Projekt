package me.antonujj;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import me.antonujj.Backend.*;

public class AppController {

	@FXML
	private TableView<Student> tableView;

	@FXML
	private TableColumn<Student, Integer> studID;
	@FXML
	private TableColumn<Student, String> studPrijmeni;
	@FXML
	private TableColumn<Student, String> studJmeno;
	@FXML
	private TableColumn<Student, String> studNarozeni;
	@FXML
	private TableColumn<Student, String> studZnamky;
	@FXML
	private TableColumn<Student, String> studObor;
	@FXML
	private TableColumn<Student, String> studPrumer;
	@FXML
	private RadioButton vse;

	private List<Student> studenti;

	DatabaseDataHandler databaze;

	@FXML
	private void initialize() {

		vse.setSelected(true);

		Platform.runLater(() -> {

			studenti = databaze.nactiVsechnyStudenty();
			updateTableView(studenti);
			tableView.refresh();

		});

	}

	@FXML
	private void handleAbout(ActionEvent event) {
		Dialog<Void> dialog = new Dialog<>();
		dialog.setTitle("O Aplikaci");
		dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

		String content = """
				Autor: Antonů Jakub Jan
				VUT ID: 271323
				Program vytvořen jako projekt pro předmět PC2T v letním semestru 2024/2025

				Užité knihovny:
				- JavaFX
				  https://openjfx.io/
				  Licence: GPL v2

				- PostgreSQL JDBC Driver
				  https://github.com/pgjdbc/pgjdbc
				  Licence: BSD-2-Clause

				- SQLite JDBC Driver
				  https://github.com/xerial/sqlite-jdbc
				  Licence: Apache License 2.0
				""";

		TextArea textArea = new TextArea(content);
		textArea.setWrapText(true);
		textArea.setEditable(false);
		textArea.setPrefWidth(450);
		textArea.setPrefHeight(250);

		dialog.getDialogPane().setContent(textArea);
		dialog.showAndWait();
	}

	@FXML
	private void studAdd(ActionEvent event) {
		Dialog<Student> dialog = new Dialog<>();
		dialog.setTitle("Přidat nového studenta");

		TextField prijmeniField = new TextField();
		prijmeniField.setPromptText("Příjmení");

		TextField jmenoField = new TextField();
		jmenoField.setPromptText("Jméno");

		DatePicker datumNarozeniPicker = new DatePicker();

		datumNarozeniPicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
			@Override
			public DateCell call(DatePicker picker) {
				return new DateCell() {
					@Override
					public void updateItem(LocalDate date, boolean empty) {
						super.updateItem(date, empty);
						if (empty || date.isAfter(LocalDate.now())) {
							setDisable(true);
						}
					}
				};
			}
		});

		ComboBox<String> oborCombo = new ComboBox<>();
		oborCombo.getItems().addAll("KYBERBEZPECNOST", "TELEKOMUNIKACE");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

		grid.add(new Label("Příjmení:"), 0, 0);
		grid.add(prijmeniField, 1, 0);
		grid.add(new Label("Jméno:"), 0, 1);
		grid.add(jmenoField, 1, 1);
		grid.add(new Label("Datum narození:"), 0, 2);
		grid.add(datumNarozeniPicker, 1, 2);
		grid.add(new Label("Obor:"), 0, 3);
		grid.add(oborCombo, 1, 3);

		dialog.getDialogPane().setContent(grid);

		ButtonType pridatButtonType = new ButtonType("Přidat", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(pridatButtonType, ButtonType.CANCEL);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == pridatButtonType) {
				try {
					LocalDate narozeni = datumNarozeniPicker.getValue();
					if (narozeni == null) {
						throw new IllegalArgumentException("Datum narození je povinné.");
					}

					String jmeno = jmenoField.getText().trim();
					String prijmeni = prijmeniField.getText().trim();
					String oborText = oborCombo.getValue();

					if (jmeno.isEmpty() || prijmeni.isEmpty() || oborText == null) {
						throw new IllegalArgumentException("Chybějící údaje.");
					}

					int id = 0;
					for (Student student : studenti) {
						if (student.getId() > id) {
							id = student.getId();
						}
					}
					id++;

					List<Integer> prazdneZnamky = new ArrayList<>();
					if (oborText.equals("KYBERBEZPECNOST")) {
						return new KyberbezpecnostStudent(id, prijmeni, jmeno, narozeni, Obor.KYBERBEZPECNOST,
								prazdneZnamky);
					} else {
						return new TelekomunikaceStudent(id, prijmeni, jmeno, narozeni, Obor.TELEKOMUNIKACE,
								prazdneZnamky);
					}

				} catch (Exception e) {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Chyba");
					alert.setContentText(e.getMessage());
					alert.showAndWait();
					return null;
				}
			}
			return null;
		});

		Student result = dialog.showAndWait().orElse(null);

		if (result != null) {
			studenti.add(result);
			updateTableView(studenti);
		}
	}

	@FXML
	private void studDel(ActionEvent event) {
		TextInputDialog inputDialog = new TextInputDialog();
		inputDialog.setTitle("Smazat studenta");
		inputDialog.setHeaderText("Zadejte ID studenta, kterého chcete smazat:");
		inputDialog.setContentText("ID:");

		String studentIdInput = inputDialog.showAndWait().orElse(null);
		if (studentIdInput == null || studentIdInput.isEmpty()) {
			return;
		}

		try {
			int studentId = Integer.parseInt(studentIdInput);

			Student studentToDelete = null;
			for (Student student : studenti) {
				if (student.getId() == studentId) {
					studentToDelete = student;
					break;
				}
			}

			if (studentToDelete == null) {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Chyba");
				alert.setHeaderText("Student nebyl nalezen");
				alert.setContentText("Student s tímto ID neexistuje.");
				alert.showAndWait();
				return;
			}

			Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
			confirmAlert.setTitle("Potvrzení smazání");
			confirmAlert.setHeaderText("Opravdu chcete smazat studenta?");
			confirmAlert.setContentText("Student: " + studentToDelete.getJmeno() + " " + studentToDelete.getPrijmeni());

			ButtonType buttonSmazat = new ButtonType("Smazat");
			ButtonType buttonZrusit = new ButtonType("Zrušit", ButtonData.CANCEL_CLOSE);

			confirmAlert.getButtonTypes().setAll(buttonSmazat, buttonZrusit);

			ButtonType result = confirmAlert.showAndWait().orElse(buttonZrusit);

			if (result == buttonSmazat) {
				studenti.remove(studentToDelete);
				updateTableView(studenti);
				System.out.println("Student ID" + studentId + " byl pridan");
			} else {
				System.out.println("Smazani zruseno");
			}

		} catch (NumberFormatException e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Chyba");
			alert.setHeaderText("Neplatné ID");
			alert.setContentText("Zadané ID není platné číslo.");
			alert.showAndWait();
		}
	}

	@FXML
	private void studAddMark(ActionEvent event) {
		TextInputDialog inputDialog = new TextInputDialog();
		inputDialog.setTitle("Zadání známky");
		inputDialog.setHeaderText("Zadejte ID studenta, kterému chcete přiřadit známku:");
		inputDialog.setContentText("ID:");

		String studentIdInput = inputDialog.showAndWait().orElse(null);
		if (studentIdInput == null || studentIdInput.isEmpty()) {
			return;
		}

		try {
			int studentId = Integer.parseInt(studentIdInput);

			Student studentToUpdate = null;
			for (Student student : studenti) {
				if (student.getId() == studentId) {
					studentToUpdate = student;
					break;
				}
			}

			if (studentToUpdate == null) {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Chyba");
				alert.setHeaderText("Student nebyl nalezen");
				alert.setContentText("Student s tímto ID neexistuje.");
				alert.showAndWait();
				return;
			}

			TextInputDialog markDialog = new TextInputDialog();
			markDialog.setTitle("Zadání známky");
			markDialog.setHeaderText("Zadejte známku pro studenta " + studentToUpdate.getJmeno() + " "
					+ studentToUpdate.getPrijmeni() + ":");
			markDialog.setContentText("Známka:");

			String markInput = markDialog.showAndWait().orElse(null);
			if (markInput == null || markInput.isEmpty()) {
				return;
			}

			try {
				int znamka = Integer.parseInt(markInput);
				if (znamka < 1 || znamka > 5) {
					throw new IllegalArgumentException("Známka musí být mezi 1 a 5.");
				}

				studentToUpdate.getZnamky().add(znamka);
				updateTableView(studenti);
				tableView.refresh();
				System.out.println("Znamka " + znamka + " pridana studentovi " + studentToUpdate.getJmeno() + " "
						+ studentToUpdate.getPrijmeni());

			} catch (NumberFormatException e) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Chyba");
				alert.setHeaderText("Neplatná známka");
				alert.setContentText("Známka musí být číslo.");
				alert.showAndWait();
			} catch (IllegalArgumentException e) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Chyba");
				alert.setHeaderText("Neplatná známka");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}

		} catch (NumberFormatException e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Chyba");
			alert.setHeaderText("Neplatné ID");
			alert.setContentText("Zadané ID není platné číslo.");
			alert.showAndWait();
		}
	}

	@FXML
	private void studGetByID(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Zadejte ID studenta");
		dialog.setHeaderText("Zadejte ID studenta pro zobrazení informací.");
		dialog.setContentText("ID:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			try {
				int id = Integer.parseInt(result.get());

				Student student = null;
				for (Student s : studenti) {
					if (s.getId() == id) {
						student = s;
						break;
					}
				}

				if (student == null) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Student nenalezen");
					alert.setHeaderText(null);
					alert.setContentText("Student s tímto ID neexistuje.");
					alert.showAndWait();
				} else {
					String studentInfo = "ID: " + student.getId() + "\n" + "Obor: " + student.getObor() + "\n"
							+ "Příjmení: " + student.getPrijmeni() + "\n" + "Jméno: " + student.getJmeno() + "\n"
							+ "Datum narození: " + student.getNarozeni() + "\n" + "Známky: " + student.getZnamky()
							+ "\n" + "Průměr: " + student.getPrumer();

					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Informace o studentovi");
					alert.setHeaderText("Informace o studentovi s ID " + student.getId());
					TextArea textArea = new TextArea(studentInfo);
					textArea.setEditable(false);
					textArea.setWrapText(true);
					textArea.setPrefSize(400, 200);
					alert.getDialogPane().setContent(textArea);
					alert.showAndWait();
				}
			} catch (NumberFormatException e) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Chyba");
				alert.setHeaderText("Neplatné ID");
				alert.setContentText("Zadali jste neplatné ID. Zkuste to znovu.");
				alert.showAndWait();
			}
		}
	}

	@FXML
	private void studAbility(ActionEvent event) {
		TextInputDialog inputDialog = new TextInputDialog();
		inputDialog.setTitle("Spuštění dovednosti");
		inputDialog.setHeaderText("Zadejte ID studenta pro spuštění dovednosti:");
		inputDialog.setContentText("ID:");

		String studentIdInput = inputDialog.showAndWait().orElse(null);
		if (studentIdInput == null || studentIdInput.isEmpty()) {
			return;
		}

		try {
			int id = Integer.parseInt(studentIdInput);

			Student student = null;
			for (Student s : studenti) {
				if (s.getId() == id) {
					student = s;
					break;
				}
			}

			if (student == null) {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Chyba");
				alert.setHeaderText("Student nebyl nalezen");
				alert.setContentText("Student s tímto ID neexistuje.");
				alert.showAndWait();
				return;
			}

			if (student.getObor() == Obor.TELEKOMUNIKACE) {
				String morse = Morse.toMorse(student.getJmeno() + " " + student.getPrijmeni());
				Alert morseAlert = new Alert(Alert.AlertType.INFORMATION);
				morseAlert.setTitle("Morseova abeceda");
				morseAlert.setHeaderText("Jméno a příjmení v Morseově abecedě:");
				TextArea textArea = new TextArea(student.getPrijmeni() + " " + student.getJmeno() + "\n" + morse);
				textArea.setEditable(false);
				textArea.setWrapText(true);
				textArea.setPrefSize(400, 200);
				morseAlert.getDialogPane().setContent(textArea);
				morseAlert.showAndWait();
			}

			if (student.getObor() == Obor.KYBERBEZPECNOST) {
				String hash = Hash.hash(student.getJmeno() + " " + student.getPrijmeni());
				Alert hashAlert = new Alert(Alert.AlertType.INFORMATION);
				hashAlert.setTitle("Hash");
				hashAlert.setHeaderText("Jméno a příjmení jako hash (SHA-256):");
				TextArea textArea = new TextArea(student.getPrijmeni() + " " + student.getJmeno() + "\n" + hash);
				textArea.setEditable(false);
				textArea.setWrapText(true);
				textArea.setPrefSize(400, 200);
				hashAlert.getDialogPane().setContent(textArea);
				hashAlert.showAndWait();

			}

		} catch (NumberFormatException e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Chyba");
			alert.setHeaderText("Neplatné ID");
			alert.setContentText("Zadané ID není platné číslo.");
			alert.showAndWait();
		}
	}

	@FXML
	private void studAverage(ActionEvent event) {
		double celkovyPrumer = 0;
		double prumerTelekomunikace = 0;
		double prumerKyberbezpecnost = 0;
		int pocetStudentuTelekomunikace = 0;
		int pocetStudentuKyberbezpecnost = 0;
		int pocetStudentu = 0;

		for (Student student : studenti) {
			if (student.getZnamky() != null && !student.getZnamky().isEmpty()) {
				double prumer = student.getZnamky().stream().mapToInt(Integer::intValue).average().orElse(0);
				celkovyPrumer += prumer;
				pocetStudentu++;

				if (student.getObor() == Obor.TELEKOMUNIKACE) {
					prumerTelekomunikace += prumer;
					pocetStudentuTelekomunikace++;
				}

				if (student.getObor() == Obor.KYBERBEZPECNOST) {
					prumerKyberbezpecnost += prumer;
					pocetStudentuKyberbezpecnost++;
				}
			}
		}

		if (pocetStudentu > 0) {
			celkovyPrumer /= pocetStudentu;
		}
		if (pocetStudentuTelekomunikace > 0) {
			prumerTelekomunikace /= pocetStudentuTelekomunikace;
		}
		if (pocetStudentuKyberbezpecnost > 0) {
			prumerKyberbezpecnost /= pocetStudentuKyberbezpecnost;
		}

		String prumerText = "Průměr celkově: " + String.format("%.2f", celkovyPrumer) + "\n" + "Průměr telekomunikace: "
				+ String.format("%.2f", prumerTelekomunikace) + "\n" + "Průměr kyberbezpečnost: "
				+ String.format("%.2f", prumerKyberbezpecnost);

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Průměry");
		alert.setHeaderText("Studijní průměry");
		TextArea textArea = new TextArea(prumerText);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setPrefSize(400, 200);
		alert.getDialogPane().setContent(textArea);
		alert.showAndWait();
	}

	@FXML
	private void studStatTotal(ActionEvent event) {
		int pocetTelekomunikace = 0;
		int pocetKyberbezpecnost = 0;
		int pocetCelkem = 0;

		for (Student student : studenti) {
			if (student.getObor() == Obor.TELEKOMUNIKACE) {
				pocetTelekomunikace++;
			} else if (student.getObor() == Obor.KYBERBEZPECNOST) {
				pocetKyberbezpecnost++;
			}
		}
		pocetCelkem = pocetKyberbezpecnost + pocetTelekomunikace;

		String studentStat = "Počet studentů celkem: " + pocetCelkem + "\n" + "Počet studentů telekomunikací: "
				+ pocetTelekomunikace + "\n" + "Počet studentů kyberbezpečnosti: " + pocetKyberbezpecnost;

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Počet studentů");
		alert.setHeaderText("Statistiky počtu studentů");
		TextArea textArea = new TextArea(studentStat);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setPrefSize(400, 200);
		alert.getDialogPane().setContent(textArea);
		alert.showAndWait();
	}

	@FXML
	private void studSaveToFile(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Zadejte ID studenta");
		dialog.setHeaderText("Zadejte ID studenta pro uložení do souboru.");
		dialog.setContentText("ID: ");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			try {
				int id = Integer.parseInt(result.get());

				Student selectedStudent = null;
				for (Student s : studenti) {
					if (s.getId() == id) {
						selectedStudent = s;
						break;
					}
				}

				if (selectedStudent == null) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Student nenalezen");
					alert.setHeaderText(null);
					alert.setContentText("Student s tímto ID neexistuje.");
					alert.showAndWait();
					return;
				}

				FileChooser fileChooser = new FileChooser();
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
				String fileName = selectedStudent.getId() + "_" + selectedStudent.getPrijmeni() + "_"
						+ selectedStudent.getJmeno() + ".csv";
				fileChooser.setInitialFileName(fileName);

				File file = fileChooser.showSaveDialog(null);
				if (file != null) {
					try {
						StudentFileIO.saveStudentToFile(selectedStudent, file);
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("Úspěch");
						alert.setHeaderText("Soubor byl úspěšně uložen");
						alert.setContentText("Student byl uložen do souboru: " + file.getName());
						alert.showAndWait();
					} catch (IOException e) {
						e.printStackTrace();
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Chyba");
						alert.setHeaderText("Chyba při ukládání");
						alert.setContentText("Došlo k chybě při ukládání souboru.");
						alert.showAndWait();
					}
				}

			} catch (NumberFormatException e) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Chyba");
				alert.setHeaderText("Neplatné ID");
				alert.setContentText("Zadali jste neplatné ID. Zkuste to znovu.");
				alert.showAndWait();
			}
		}
	}

	@FXML
	private void studLoadFromFile(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
		File file = fileChooser.showOpenDialog(null);

		if (file != null) {
			int newId = 0;
			for (Student s : studenti) {
				if (s.getId() > newId) {
					newId = s.getId();
				}
			}
			newId = newId + 1;

			Student student = StudentFileIO.importStudentFromFile(file, newId);

			if (student != null) {
				studenti.add(student);

				String info = "ID: " + student.getId() + "\n" + "Obor: " + student.getObor() + "\n" + "Příjmení: "
						+ student.getPrijmeni() + "\n" + "Jméno: " + student.getJmeno() + "\n" + "Datum narození: "
						+ student.getNarozeni() + "\n" + "Známky: " + student.getZnamky() + "\n" + "Průměr: "
						+ student.getPrumer();

				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Načtený student");
				alert.setHeaderText("Student byl úspěšně načten.");
				TextArea textArea = new TextArea(info);
				textArea.setEditable(false);
				textArea.setWrapText(true);
				textArea.setPrefSize(400, 200);
				alert.getDialogPane().setContent(textArea);
				updateTableView(studenti);
				tableView.refresh();
				alert.showAndWait();
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Chyba");
				alert.setHeaderText("Načítání se nepovedlo");
				alert.setContentText("Zkontrolujte soubor nebo formát.");
				alert.showAndWait();
			}
		}
	}

	@FXML
	private void saveDB(ActionEvent event) {
		Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
		confirmDialog.setTitle("Potvrzení");
		confirmDialog.setHeaderText("Opravdu chcete uložit všechny studenty do databáze?");
		confirmDialog.setContentText("Tímto se přepíšou všechna data v databázi.");

		Optional<ButtonType> result = confirmDialog.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			databaze.ulozVsechnyStudenty(studenti);

			Alert successDialog = new Alert(Alert.AlertType.INFORMATION);
			successDialog.setTitle("Hotovo");
			successDialog.setHeaderText("Uložení do databáze proběhlo úspěšně.");
			successDialog.setContentText("Všichni studenti byli zapsáni.");
			successDialog.showAndWait();
		}
	}

	@FXML
	private void exit(ActionEvent event) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Ukončit aplikaci");
		alert.setHeaderText("Opravdu chcete ukončit aplikaci?");
		alert.setContentText("Veškeré neuložené změny budou zahozeny.");

		ButtonType buttonAno = new ButtonType("Ano");
		ButtonType buttonNe = new ButtonType("Ne", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonAno, buttonNe);

		ButtonType vysledek = alert.showAndWait().get();
		if (vysledek == buttonAno)
			System.exit(0);
	}

	@FXML
	private void displayAll(ActionEvent event) {
		List<Student> filteredStudents = filterByObor("VSECHNO");
		updateTableView(filteredStudents);
	}

	@FXML
	private void displayKyberbezpecnost(ActionEvent event) {
		zobrazStudentyPodleOboru("KYBERBEZPECNOST");
	}

	@FXML
	private void displayTelekomunikace(ActionEvent event) {
		zobrazStudentyPodleOboru("TELEKOMUNIKACE");
	}

	private void zobrazStudentyPodleOboru(String obor) {
		List<Student> filteredStudents = filterByObor(obor);
		updateTableView(filteredStudents);
	}

	private void updateTableView(List<Student> students) {
		ObservableList<Student> studentData = FXCollections.observableArrayList(students);

		studID.setCellValueFactory(new PropertyValueFactory<>("id"));
		studPrijmeni.setCellValueFactory(new PropertyValueFactory<>("prijmeni"));
		studJmeno.setCellValueFactory(new PropertyValueFactory<>("jmeno"));
		studNarozeni.setCellValueFactory(new PropertyValueFactory<>("narozeni"));

		studZnamky.setCellValueFactory(cellData -> {
			List<Integer> znamky = cellData.getValue().getZnamky();
			String z = "";
			if (znamky != null && !znamky.isEmpty()) {
				for (int i = 0; i < znamky.size(); i++) {
					z += znamky.get(i);
					if (i != znamky.size() - 1) {
						z += ", ";
					}
				}
			} else {
				z = "-";
			}
			return new javafx.beans.property.SimpleStringProperty(z);
		});

		studObor.setCellValueFactory(cellData -> {
			return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getObor().toString());
		});

		studPrumer.setCellValueFactory(cellData -> {
			List<Integer> znamky = cellData.getValue().getZnamky();
			if (znamky == null || znamky.isEmpty()) {
				return new javafx.beans.property.SimpleStringProperty("-");
			}
			double sum = 0;
			for (int z : znamky) {
				sum += z;
			}
			double average = sum / znamky.size();
			return new javafx.beans.property.SimpleStringProperty(String.format("%.2f", average));
		});

		tableView.setItems(studentData);
	}

	private List<Student> filterByObor(String obor) {
		List<Student> filteredStudents = new ArrayList<>();
		for (Student student : studenti) {
			if (obor.equals("VSECHNO") || student.getObor().toString().equals(obor)) {
				filteredStudents.add(student);
			}
		}
		return filteredStudents;
	}

}
