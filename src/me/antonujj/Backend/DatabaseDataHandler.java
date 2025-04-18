package me.antonujj.Backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DatabaseDataHandler {

	private String jdbcUrl;
	private String username;
	private String password;

	public DatabaseDataHandler(String jdbcUrl, String username, String password) {
		this.jdbcUrl = jdbcUrl;
		this.username = username;
		this.password = password;
	}

	public DatabaseDataHandler(String sqliteFilePath) {
		this.jdbcUrl = "jdbc:sqlite:" + sqliteFilePath;
		this.username = null;
		this.password = null;
	}

	public void ulozVsechnyStudenty(List<Student> studenti) {
		String deleteSql = "DELETE FROM student";
		String insertSql = "INSERT INTO student (id, jmeno, prijmeni, datum_narozeni, obor, znamky) VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection conn = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
				PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
				PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

			deleteStmt.executeUpdate();

			for (Student s : studenti) {
				insertStmt.setInt(1, s.getId());
				insertStmt.setString(2, s.getJmeno());
				insertStmt.setString(3, s.getPrijmeni());
				insertStmt.setDate(4, java.sql.Date.valueOf(s.getNarozeni()));
				insertStmt.setString(5, s.getObor().toString());

				StringBuilder znamkyText = new StringBuilder();
				for (int i = 0; i < s.getZnamky().size(); i++) {
					znamkyText.append(s.getZnamky().get(i));
					if (i < s.getZnamky().size() - 1) {
						znamkyText.append(",");
					}
				}

				insertStmt.setString(6, znamkyText.toString());
				insertStmt.executeUpdate();
			}

		} catch (SQLException e) {
			System.err.println("Chyba pri ukladani do db");
			e.printStackTrace();
		}
	}

	public List<Student> nactiVsechnyStudenty() {
		List<Student> studenti = new ArrayList<>();

		String sql = "SELECT id, jmeno, prijmeni, datum_narozeni, obor, znamky FROM student";

		try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				int id = rs.getInt("id");
				String jmeno = rs.getString("jmeno");
				String prijmeni = rs.getString("prijmeni");
				LocalDate narozeni = rs.getDate("datum_narozeni").toLocalDate();
				String oborText = rs.getString("obor").toUpperCase();

				String znamkyText = rs.getString("znamky");
				List<Integer> znamky = new ArrayList<>();
				if (znamkyText != null && !znamkyText.isBlank()) {
					for (String z : znamkyText.split(",")) {
						try {
							znamky.add(Integer.parseInt(z.trim()));
						} catch (NumberFormatException e) {
							System.err.println("Chybna znamka u studenta ID: " + id + ": " + z);
						}
					}
				}

				Student student;

				switch (oborText) {
				case "KYBERBEZPECNOST":
					student = new KyberbezpecnostStudent(id, prijmeni, jmeno, narozeni, Obor.KYBERBEZPECNOST, znamky);
					break;
				case "TELEKOMUNIKACE":
					student = new TelekomunikaceStudent(id, prijmeni, jmeno, narozeni, Obor.TELEKOMUNIKACE, znamky);
					break;
				default:
					System.err.println("Neznamy obor" + oborText + " student id: " + id);
					continue;
				}

				studenti.add(student);
			}

		} catch (SQLException e) {

			if (e.getMessage().contains("database disk image is malformed")) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Chyba databáze");
				alert.setHeaderText(null);
				alert.setContentText("Databáze je poškozena");
				alert.showAndWait();
				System.exit(1);
			}

			e.printStackTrace();
		}

		return studenti;
	}
}
