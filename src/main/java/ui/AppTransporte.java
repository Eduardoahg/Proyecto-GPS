package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * PROCESO: Clase principal de la aplicación que hereda de Application. Es el punto de entrada
 * que inicializa el entorno gráfico de JavaFX y gestiona el escenario (Stage) principal.
 */
public class AppTransporte extends Application {
    private static Stage primaryStage;

    /**
     * PROCESO: Configura e inicia el escenario principal de la aplicación.
     * ENTRADAS:
     * - stage: El contenedor principal proporcionado por la plataforma JavaFX.
     * FLUJO DE LLAMADAS:
     * 1. Almacena la referencia del stage en la variable estática primaryStage.
     * 2. Carga el icono de la aplicación desde los recursos.
     * 3. Llama a setRoot() para cargar la vista inicial (MenuPrincipal.fxml).
     */
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/LOGO.jpg")));

        setRoot("MenuPrincipal.fxml", "GPS Transporte Urbano - PUCMM");

        stage.setMaximized(true);
        stage.show();
    }

    /**
     * PROCESO: Cambia la escena actual del escenario principal cargando un nuevo archivo FXML.
     * ENTRADAS:
     * - fxml: Nombre del archivo de vista a cargar.
     * - titulo: Texto que se mostrará en la barra de título de la ventana.
     * FLUJO DE LLAMADAS:
     * 1. Llama a FXMLLoader para procesar el archivo FXML indicado.
     * 2. Actualiza el contenido del escenario usando setRoot o creando una nueva Scene.
     * 3. Llama a stage.setTitle() para actualizar el encabezado de la ventana.
     */
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

    /**
     * PROCESO: Provee acceso global al escenario principal de la aplicación.
     * SALIDA: El objeto Stage que representa la ventana principal.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * PROCESO: Método principal de Java que sirve como punto de entrada al programa.
     * FLUJO DE LLAMADAS: Llama internamente al método launch() de la clase Application para iniciar el ciclo de vida de JavaFX.
     */
    public static void main(String[] args) {
        launch(args);
    }
}