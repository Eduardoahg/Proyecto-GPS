package ui;

import algorithms.CalculadoraRutas;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.Parada;
import structure.GrafoTransporte;
import persistence.GestorArchivos;
import java.io.IOException;

/**
 * PROCESO: Controlador encargado de la interfaz de creación de nuevas conexiones (aristas) entre paradas existentes.
 * Permite al usuario definir métricas personalizadas o utilizar el auto-cálculo basado en la posición de los nodos.
 */
public class CrearRutaVisual {

    @FXML private ComboBox<Parada> cbOrigen;
    @FXML private ComboBox<Parada> cbDestino;
    @FXML private TextField txtTiempo;
    @FXML private TextField txtDistancia;
    @FXML private TextField txtCosto;

    private static GrafoTransporte sistema = new GrafoTransporte();
    private final String FILE_JSON = "transporte_datos.json";

    /**
     * PROCESO: Configura el estado inicial de la pantalla y establece la lógica de auto-completado.
     * FLUJO DE LLAMADAS:
     * 1. Llama a GestorArchivos.cargarDesdeJson() si la memoria del sistema está vacía.
     * 2. Llama a sistema.getGrafo().keySet() para poblar los selectores de paradas.
     * 3. Establece un listener que llama a CalculadoraRutas.calcularDistanciaKM() y calcularCosto() para sugerir valores automáticos.
     */
    @FXML
    public void initialize() {
        if (sistema.getGrafo().isEmpty()) {
            GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        }
        cbOrigen.getItems().addAll(sistema.getGrafo().keySet());
        cbDestino.getItems().addAll(sistema.getGrafo().keySet());

        cbDestino.setOnAction(e -> {
            Parada ori = cbOrigen.getValue();
            Parada dest = cbDestino.getValue();
            if (ori != null && dest != null && !ori.equals(dest)) {
                double km = CalculadoraRutas.calcularDistanciaKM(ori, dest);
                txtDistancia.setText(String.format("%.2f", km));
                txtTiempo.setText(String.format("%.2f", km * 2.5));
                txtCosto.setText(String.format("%.2f", CalculadoraRutas.calcularCosto(km, km * 2.5)));
            }
        });
    }

    /**
     * PROCESO: Valida los datos ingresados y registra formalmente la nueva ruta en el grafo.
     * FLUJO DE LLAMADAS:
     * 1. Llama a CalculadoraRutas.calcularTransbordos() para determinar si el tramo es directo o no.
     * 2. Llama a sistema.agregarRuta() para insertar la conexión en la lista de adyacencia.
     * 3. Llama a GestorArchivos.guardarEnJson() para asegurar que la ruta persista en el disco.
     * 4. Llama a mostrarAlerta() y limpiar() para finalizar el proceso.
     */
    @FXML
    public void crearRuta() {
        Parada ori = cbOrigen.getValue();
        Parada dest = cbDestino.getValue();

        if (ori == null || dest == null || ori.equals(dest)) {
            mostrarAlerta("Error", "Selecciona dos paradas distintas.");
            return;
        }

        try {
            double t = Double.parseDouble(txtTiempo.getText().replace(",", "."));
            double d = Double.parseDouble(txtDistancia.getText().replace(",", "."));
            double c = Double.parseDouble(txtCosto.getText().replace(",", "."));

            sistema.agregarRuta(ori.getId(), dest.getId(), t, d, c, CalculadoraRutas.calcularTransbordos(d) > 0);
            GestorArchivos.guardarEnJson(sistema, FILE_JSON);
            mostrarAlerta("Éxito", "Ruta creada correctamente.");
            limpiar();
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Ingresa valores numéricos válidos.");
        }
    }

    /**
     * PROCESO: Fuerza la recarga de paradas disponibles desde el archivo fuente.
     * FLUJO DE LLAMADAS:
     * 1. Llama a GestorArchivos.cargarDesdeJson() para refrescar el objeto sistema.
     * 2. Llama a sistema.getGrafo().keySet() para actualizar las opciones de los ComboBox.
     */
    @FXML
    public void actualizarDatos() {
        GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        cbOrigen.getItems().clear();
        cbDestino.getItems().clear();
        cbOrigen.getItems().addAll(sistema.getGrafo().keySet());
        cbDestino.getItems().addAll(sistema.getGrafo().keySet());
    }

    /**
     * PROCESO: Restablece todos los controles de la interfaz a su estado original vacío.
     */
    private void limpiar() {
        cbOrigen.getSelectionModel().clearSelection();
        cbDestino.getSelectionModel().clearSelection();
        txtTiempo.clear(); txtDistancia.clear(); txtCosto.clear();
    }

    /**
     * PROCESO: Navega de regreso a la pantalla principal del sistema.
     * FLUJO DE LLAMADAS: Llama a AppTransporte.setRoot() con el FXML del menú.
     */
    @FXML
    public void volver() throws IOException {
        AppTransporte.setRoot("MenuPrincipal.fxml", "Inicio");
    }

    /**
     * PROCESO: Despliega una ventana emergente para informar al usuario sobre el resultado de sus acciones.
     * ENTRADAS: Título de la ventana y mensaje descriptivo.
     */
    private void mostrarAlerta(String titulo, String contenido) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        ((Stage) alerta.getDialogPane().getScene().getWindow()).getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));
        alerta.showAndWait();
    }
}