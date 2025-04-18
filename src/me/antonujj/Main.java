package me.antonujj;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = (BorderPane) FXMLLoader.load(getClass().getResource("DbDialog.fxml"));
			Scene scene = new Scene(root, 640, 360);
			primaryStage.getIcons().add(new Image(getClass().getResource("logo.png").toExternalForm()));
			primaryStage.setTitle("Semestrální Projekt PC2T");
			primaryStage.setResizable(true);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
