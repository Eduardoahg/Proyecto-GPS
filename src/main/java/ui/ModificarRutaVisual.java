package ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;
import persistence.GestorArchivos;
import java.io.IOException;

/**
 * PROCESO: Clase controladora encargada de gestionar la edición de rutas existentes.
 * Permite actualizar el tiempo, distancia y costo de una conexión específica entre dos nodos.
 */
public class ModificarRutaVisual {

    @FXML private ComboBox<Parada> cbOrigen;
    @FXML private ComboBox<Ruta> cbRuta;
    @FXML private TextField txtTiempo;
    @FXML private TextField txtDistancia;
    @FXML private TextField txtCosto;

    private static GrafoTransporte sistema = new GrafoTransporte();
    private final String FILE_JSON = "transporte_datos.json";

    /**
     * PROCESO: Inicializa la pantalla cargando los datos y configurando los listeners de los ComboBox.
     * FLUJO DE LLAMADAS:
     * 1. Llama a GestorArchivos.cargarDesdeJson() si la estructura estática está vacía.
     * 2. Llama a cargarControles() para llenar las opciones de origen.
     * 3. Configura un listener en cbOrigen que limpia cbRuta y lo puebla con las rutas del nodo seleccionado.
     * 4. Configura un listener en cbRuta que rellena los campos de texto con los valores actuales de la ruta.
     * 5. Establece un StringConverter para que cbRuta muestre el nombre del destino y la distancia de forma legible.
     */
    @FXML
    public void initialize() {
        if (sistema.getGrafo().isEmpty()) {
            GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        }
        cargarControles();

        cbOrigen.setOnAction(e -> {
            cbRuta.getItems().clear();
            Parada seleccionada = cbOrigen.getValue();
            if (seleccionada != null && sistema.getGrafo().containsKey(seleccionada)) {
                cbRuta.getItems().addAll(sistema.getGrafo().get(seleccionada));
            }
        });

        cbRuta.setOnAction(e -> {
            Ruta r = cbRuta.getValue();
            if (r != null) {
                txtTiempo.setText(String.format("%.2f", r.getTiempo()));
                txtDistancia.setText(String.format("%.2f", r.getDistancia()));
                txtCosto.setText(String.format("%.2f", r.getCosto()));
            }
        });

        cbRuta.setConverter(new StringConverter<Ruta>() {
            @Override public String toString(Ruta r) {
                return (r == null) ? "" : "Hacia: " + r.getDestino().getNombre() + " (" + String.format("%.1f", r.getDistancia()) + " km)";
            }
            @Override public Ruta fromString(String s) { return null; }
        });
    }

    /**
     * PROCESO: Refresca el contenido de los selectores visuales y limpia los campos de edición.
     * FLUJO DE LLAMADAS: Llama a sistema.getGrafo().keySet() para obtener todas las paradas registradas.
     */
    private void cargarControles() {
        cbOrigen.getItems().clear();
        cbOrigen.getItems().addAll(sistema.getGrafo().keySet());
        cbRuta.getItems().clear();
        txtTiempo.clear(); txtDistancia.clear(); txtCosto.clear();
    }

    /**
     * PROCESO: Sincroniza la aplicación con los datos almacenados en el archivo JSON.
     * FLUJO DE LLAMADAS:
     * 1. Llama a GestorArchivos.cargarDesdeJson() para sobreescribir la memoria con el disco.
     * 2. Llama a cargarControles() para actualizar la interfaz visual.
     */
    @FXML
    public void actualizarDatos() {
        GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        cargarControles();
    }

    /**
     * PROCESO: Valida los cambios realizados por el usuario y los guarda permanentemente.
     * FLUJO DE LLAMADAS:
     * 1. Verifica la validez numérica de los campos de texto.
     * 2. Actualiza los atributos del objeto Ruta seleccionado.
     * 3. Llama a GestorArchivos.guardarEnJson() para asegurar la persistencia en el archivo físico.
     * 4. Llama a mostrarAlerta() para notificar el éxito o error al usuario.
     */
    @FXML
    public void guardarCambios() {
        Ruta r = cbRuta.getValue();
        if (r != null) {
            try {
                double t = Double.parseDouble(txtTiempo.getText().replace(",", "."));
                double d = Double.parseDouble(txtDistancia.getText().replace(",", "."));
                double c = Double.parseDouble(txtCosto.getText().replace(",", "."));

                if (t <= 0 || d <= 0) {
                    mostrarAlerta("Error", "Valores mayores a 0.");
                    return;
                }

                r.setTiempo(t);
                r.setDistancia(d);
                r.setCosto(c);

                GestorArchivos.guardarEnJson(sistema, FILE_JSON);
                mostrarAlerta("Éxito", "Ruta actualizada.");
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "Números válidos requeridos.");
            }
        }
    }

    /**
     * PROCESO: Navega de regreso a la vista del Menú Principal.
     * FLUJO DE LLAMADAS: Llama a AppTransporte.setRoot() con el nombre del archivo FXML correspondiente.
     */
    @FXML
    public void volver() throws IOException {
        AppTransporte.setRoot("MenuPrincipal.fxml", "Inicio");
    }

    /**
     * PROCESO: Despliega una ventana emergente de información.
     * ENTRADAS: Título de la alerta y mensaje descriptivo.
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