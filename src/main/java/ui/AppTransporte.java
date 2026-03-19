package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public class AppTransporte extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AppTransporte.fxml"));
        Parent root = loader.load();

        TransporteVisual controller = loader.getController();

        stage.setOnCloseRequest(event -> {
            if (controller.isHayCambiosSinGuardar()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmación de salida");
                alert.setHeaderText("Tienes cambios sin guardar.");
                alert.setContentText("¿Seguro que quieres salir?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    stage.close();
                } else {
                    event.consume();
                }
            }
        });

        stage.setTitle("GPS Transporte Urbano - PUCMM");
        stage.setScene(new Scene(root, 1100, 700));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}