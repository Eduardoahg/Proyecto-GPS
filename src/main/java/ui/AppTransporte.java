package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class AppTransporte extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));

        setRoot("MenuPrincipal.fxml", "GPS Transporte Urbano - PUCMM");

        stage.setMaximized(true);
        stage.show();
    }

    public static void setRoot(String fxml, String titulo) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppTransporte.class.getResource("/" + fxml));
        Parent root = loader.load();

        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root));
        } else {
            primaryStage.getScene().setRoot(root);
        }

        primaryStage.setTitle(titulo);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}