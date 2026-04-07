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
import java.util.List;

/**
 * PROCESO: Clase controladora encargada de gestionar la edición y eliminación de rutas existentes.
 * Permite actualizar parámetros técnicos o remover conexiones del grafo de transporte.
 */
public class ModificarRutaVisual {

    @FXML
    private ComboBox<Parada> cbOrigen;
    @FXML
    private ComboBox<Ruta> cbRuta;
    @FXML
    private TextField txtTiempo;
    @FXML
    private TextField txtDistancia;
    @FXML
    private TextField txtCosto;

    private static GrafoTransporte sistema = new GrafoTransporte();
    private final String FILE_JSON = "transporte_datos.json";

    /**
     * PROCESO: Inicializa la pantalla configurando conversores y eventos de selección.
     * FLUJO DE LLAMADAS:
     * 1. Carga datos desde JSON.
     * 2. Configura StringConverters para mostrar nombres en lugar de direcciones de memoria.
     * 3. Define la lógica de cascada: al elegir origen, se filtran las rutas disponibles.
     */
    @FXML
    public void initialize() {
        if (sistema.getGrafo().isEmpty()) {
            GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        }

        // Conversor para que el origen muestre el nombre de la parada
        cbOrigen.setConverter(new StringConverter<Parada>() {
            @Override
            public String toString(Parada p) {
                return (p == null) ? "" : p.getNombre();
            }

            @Override
            public Parada fromString(String s) {
                return null;
            }
        });

        // Conversor para que la ruta muestre el destino y su distancia
        cbRuta.setConverter(new StringConverter<Ruta>() {
            @Override
            public String toString(Ruta r) {
                return (r == null) ? "" : "Hacia: " + r.getDestino().getNombre() + " (" + String.format("%.1f", r.getDistancia()) + " km)";
            }

            @Override
            public Ruta fromString(String s) {
                return null;
            }
        });

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
                txtTiempo.setText(String.format("%.2f", r.getTiempo()).replace(",", "."));
                txtDistancia.setText(String.format("%.2f", r.getDistancia()).replace(",", "."));
                txtCosto.setText(String.format("%.2f", r.getCosto()).replace(",", "."));
            }
        });

        cargarControles();
    }

    /**
     * PROCESO: Llena el selector de origen con todas las paradas que tienen rutas salientes.
     */
    private void cargarControles() {
        cbOrigen.getItems().clear();
        cbOrigen.getItems().addAll(sistema.getGrafo().keySet());
        cbRuta.getItems().clear();
        txtTiempo.clear();
        txtDistancia.clear();
        txtCosto.clear();
    }

    /**
     * PROCESO: Actualiza los valores de una ruta existente y guarda en el archivo JSON.
     */
    @FXML
    public void guardarCambios() {
        Ruta r = cbRuta.getValue();
        if (r == null) {
            mostrarAlerta("Error", "Seleccione una ruta para modificar.");
            return;
        }

        try {
            double t = Double.parseDouble(txtTiempo.getText());
            double d = Double.parseDouble(txtDistancia.getText());
            double c = Double.parseDouble(txtCosto.getText());

            if (t <= 0 || d <= 0) {
                mostrarAlerta("Error", "El tiempo y la distancia deben ser mayores a cero.");
                return;
            }

            r.setTiempo(t);
            r.setDistancia(d);
            r.setCosto(c);

            GestorArchivos.guardarEnJson(sistema, FILE_JSON);
            mostrarAlerta("Éxito", "Ruta actualizada correctamente.");
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Asegúrese de ingresar valores numéricos válidos.");
        }
    }

    /**
     * PROCESO: Elimina la ruta seleccionada del sistema de forma permanente.
     * FLUJO DE LLAMADAS:
     * 1. Obtiene la lista de rutas del origen seleccionado.
     * 2. Remueve el objeto Ruta de dicha lista.
     * 3. Sincroniza con el archivo JSON.
     */
    @FXML
    public void eliminarRuta() {
        Parada origen = cbOrigen.getValue();
        Ruta rutaAEliminar = cbRuta.getValue();

        if (origen != null && rutaAEliminar != null) {
            // Confirmación básica
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar esta ruta permanentemente?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    List<Ruta> rutas = sistema.getGrafo().get(origen);
                    rutas.remove(rutaAEliminar);

                    GestorArchivos.guardarEnJson(sistema, FILE_JSON);
                    mostrarAlerta("Eliminado", "La ruta ha sido removida del sistema.");
                    cargarControles(); // Refrescar interfaz
                }
            });
        } else {
            mostrarAlerta("Error", "Debe seleccionar un origen y una ruta específica.");
        }
    }

    @FXML
    public void actualizarDatos() {
        GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        cargarControles();
    }

    @FXML
    public void volver() throws IOException {
        AppTransporte.setRoot("MenuPrincipal.fxml", "Inicio");
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        try {
            ((Stage) alerta.getDialogPane().getScene().getWindow()).getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));
        } catch (Exception e) { /* Silenciar si no hay logo */ }
        alerta.showAndWait();
    }
}