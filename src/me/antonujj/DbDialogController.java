package me.antonujj;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

public class DbDialogController {

	public void postgresqlConnect(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("Postgress.fxml"));
		((Node) event.getSource()).getScene().setRoot(root);
	}

	public void sqliteConnect(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("Sqlite.fxml"));
		((Node) event.getSource()).getScene().setRoot(root);
	}
}
