package me.antonujj;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.antonujj.Backend.DatabaseDataHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgressController {

	@FXML
	TextField IPTextField;

	@FXML
	TextField DatabaseTextField;

	@FXML
	TextField PortTextField;

	@FXML
	TextField UserTextField;

	@FXML
	TextField PasswordTextField;

	@FXML
	Label ConnInfoLabel;

	@FXML
	Button BackToChoiceButton;

	DatabaseDataHandler databaze;

	public void backToChoiceAction(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("DbDialog.fxml"));
		((Node) event.getSource()).getScene().setRoot(root);
	}

	public void connectDB(ActionEvent event) throws IOException {
		String jdbcUrl = "jdbc:postgresql://" + IPTextField.getText() + ":" + PortTextField.getText() + "/"
				+ DatabaseTextField.getText();
		String username = UserTextField.getText();
		String password = PasswordTextField.getText();

		Connection connection = null;

		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
			if (connection != null) {
				BackToChoiceButton.setVisible(false);
				ConnInfoLabel.setTextFill(Color.GREEN);
				ConnInfoLabel.setText("Připojeno k databázi");
				ConnInfoLabel.setVisible(true);

				PauseTransition delay = new PauseTransition(Duration.seconds(1));
				delay.setOnFinished(eventdelay -> {
					try {
						FXMLLoader loader = new FXMLLoader(getClass().getResource("App.fxml"));
						Parent root = loader.load();
						AppController controller = loader.getController();

						controller.databaze = new DatabaseDataHandler(jdbcUrl, username, password);

						Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
			ConnInfoLabel.setText("Chyba připojení");
			ConnInfoLabel.setVisible(true);
			e.printStackTrace();
		} finally {
			try {
				if (connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
}
