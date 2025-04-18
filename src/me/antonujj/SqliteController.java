package me.antonujj;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.antonujj.Backend.DatabaseDataHandler;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteController {

	@FXML
	Label ConnInfoLabel;

	@FXML
	Button BackToChoiceButton;

	DatabaseDataHandler databaze;

	public void backToChoiceAction(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("DbDialog.fxml"));
		((Node) event.getSource()).getScene().setRoot(root);
	}

	public void connectDB(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Vyberte SQLite databázi");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite soubory", "*.db", "*.sqlite"));

		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		File selectedFile = fileChooser.showOpenDialog(stage);

		if (selectedFile == null) {
			ConnInfoLabel.setTextFill(Color.RED);
			ConnInfoLabel.setText("Nebyl vybrán žádný soubor");
			ConnInfoLabel.setVisible(true);
			return;
		}

		String dbPath = selectedFile.getAbsolutePath();
		databaze = new DatabaseDataHandler(dbPath);

		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
			if (connection != null) {
				BackToChoiceButton.setVisible(false);
				ConnInfoLabel.setTextFill(Color.GREEN);
				ConnInfoLabel.setText("Připojeno k databázi");
				ConnInfoLabel.setVisible(true);

				PauseTransition delay = new PauseTransition(Duration.seconds(1));
				delay.setOnFinished(eventDelay -> {
					try {
						FXMLLoader loader = new FXMLLoader(getClass().getResource("App.fxml"));
						Parent root = loader.load();
						AppController controller = loader.getController();

						controller.databaze = databaze;

						stage.setWidth(1280);
						stage.setHeight(720);
						stage.getScene().setRoot(root);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				delay.play();
			}
		} catch (SQLException e) {
			ConnInfoLabel.setTextFill(Color.RED);
			ConnInfoLabel.setText("Chyba připojení k databázi");
			ConnInfoLabel.setVisible(true);
			e.printStackTrace();
		}
	}
}
