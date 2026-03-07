package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppTransporte extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/AppTransporte.fxml"));
        stage.setTitle("GPS Transporte Público");
        stage.setScene(new Scene(root, 1100, 700));
        stage.show();//hola
    }

    public static void main(String[] args) {
        launch(args);
    }
}