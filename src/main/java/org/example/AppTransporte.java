package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
   CLASE: AppTransporte
   Argumentos: ninguno.
   Objetivo: Clase principal que extiende de Application para iniciar el ciclo de vida de JavaFX.
   Retorno: instancia de la aplicación.
*/

public class AppTransporte extends Application {

    /*
   Función: start
   Argumentos:
      Stage stage: El escenario principal de la aplicación.
   Objetivo: Cargar el archivo FXML, configurar la escena (título, dimensiones) y mostrar la ventana al usuario.
   Retorno: ninguno.
*/
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/AppTransporte.fxml"));
        stage.setTitle("GPS Transporte Publico");
        stage.setScene(new Scene(root, 1100, 700));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}