package ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.Parada;
import structure.GrafoTransporte;
import persistence.GestorArchivos;
import java.io.IOException;

/**
 * PROCESO: Clase controladora de la interfaz de gestión de paradas. Maneja las operaciones CRUD
 * (Crear, Leer, Actualizar, Borrar) sobre los nodos del grafo de transporte.
 */
public class GestionParadasVisual {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtLocalizacion;
    @FXML private TextField txtX;
    @FXML private TextField txtY;
    @FXML private ListView<Parada> listParadas;

    private static GrafoTransporte sistema = new GrafoTransporte();
    private final String FILE_JSON = "transporte_datos.json";

    /**
     * PROCESO: Inicializa la vista cargando los datos y configurando el comportamiento de selección de la lista.
     * FLUJO DE LLAMADAS:
     * 1. Llama a GestorArchivos.cargarDesdeJson() para poblar el sistema al iniciar.
     * 2. Llama a actualizarLista() para mostrar las paradas en el ListView.
     * 3. Configura un listener que detecta cambios en la selección y llena los campos de texto automáticamente.
     */
    @FXML
    public void initialize() {
        if (sistema.getGrafo().isEmpty()) {
            GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        }
        actualizarLista();

        listParadas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtId.setText(newVal.getId());
                txtNombre.setText(newVal.getNombre());
                txtLocalizacion.setText(newVal.getLocalizacion());
                txtX.setText(String.valueOf(newVal.getX()));
                txtY.setText(String.valueOf(newVal.getY()));
                txtId.setEditable(false);
            }
        });
    }

    /**
     * PROCESO: Refresca el contenido visual del ListView con las paradas actuales del grafo.
     * FLUJO DE LLAMADAS: Llama a sistema.getGrafo().keySet() para obtener los nodos registrados.
     */
    private void actualizarLista() {
        listParadas.getItems().clear();
        listParadas.getItems().addAll(sistema.getGrafo().keySet());
    }

    /**
     * PROCESO: Fuerza una recarga completa de la información desde el archivo físico JSON.
     * FLUJO DE LLAMADAS:
     * 1. Llama a GestorArchivos.cargarDesdeJson().
     * 2. Llama a actualizarLista() y limpiarCampos() para sincronizar la interfaz.
     */
    @FXML
    public void actualizarDatos() {
        GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        actualizarLista();
        limpiarCampos();
    }

    /**
     * PROCESO: Recolecta los datos de los formularios, valida la información e inserta una nueva parada.
     * FLUJO DE LLAMADAS:
     * 1. Llama a sistema.buscarParada() para evitar duplicados de ID.
     * 2. Llama a sistema.agregarParada() para insertar el nuevo objeto.
     * 3. Llama a GestorArchivos.guardarEnJson() para persistir el cambio.
     * 4. Llama a actualizarLista() y limpiarCampos() tras el éxito.
     */
    @FXML
    public void crearParada() {
        String id = txtId.getText();
        String nombre = txtNombre.getText();
        String loc = txtLocalizacion.getText();

        if (id.isEmpty() || nombre.isEmpty()) {
            mostrarAlerta("Error", "El ID y el nombre son obligatorios.");
            return;
        }

        if (sistema.buscarParada(id) != null) {
            mostrarAlerta("Error", "Ya existe una parada con ese ID.");
            return;
        }

        int randomX = (int) (100 + (Math.random() * 1100));
        int randomY = (int) (100 + (Math.random() * 500));

        txtX.setText(String.valueOf(randomX));
        txtY.setText(String.valueOf(randomY));

        sistema.agregarParada(new Parada(id, nombre, loc, randomX, randomY));
        GestorArchivos.guardarEnJson(sistema, FILE_JSON);
        actualizarLista();
        limpiarCampos();
    }

    /**
     * PROCESO: Actualiza los atributos de la parada que se encuentra actualmente seleccionada en la lista.
     * FLUJO DE LLAMADAS:
     * 1. Llama a GestorArchivos.guardarEnJson() después de modificar los setters del objeto.
     * 2. Llama a listParadas.refresh() para actualizar el nombre visual en la interfaz.
     */
    @FXML
    public void modificarParada() {
        Parada seleccionada = listParadas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            try {
                String nombre = txtNombre.getText();
                String loc = txtLocalizacion.getText();
                double x = Double.parseDouble(txtX.getText());
                double y = Double.parseDouble(txtY.getText());

                sistema.modificarParada(seleccionada.getId(), nombre, loc, x, y);

                GestorArchivos.guardarEnJson(sistema, FILE_JSON);
                listParadas.refresh();
                mostrarAlerta("Éxito", "Parada modificada correctamente.");
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "Coord. inválidas.");
            }
        }
    }

    /**
     * PROCESO: Solicita confirmación y procede a borrar permanentemente una parada y sus conexiones.
     * FLUJO DE LLAMADAS:
     * 1. Llama a sistema.eliminarParada().
     * 2. Llama a GestorArchivos.guardarEnJson() para actualizar el archivo físico.
     * 3. Llama a actualizarLista() y limpiarCampos() para remover el elemento de la vista.
     */
    @FXML
    public void eliminarParada() {
        Parada seleccionada = listParadas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que quieres eliminar la parada " + seleccionada.getNombre() + "?", ButtonType.YES, ButtonType.NO);
            ((Stage) confirm.getDialogPane().getScene().getWindow()).getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.YES) {
                    sistema.eliminarParada(seleccionada.getId());
                    GestorArchivos.guardarEnJson(sistema, FILE_JSON);
                    actualizarLista();
                    limpiarCampos();
                }
            });
        }
    }

    /**
     * PROCESO: Limpia todos los campos de texto y restablece el estado de los controles de edición.
     */
    @FXML
    public void limpiarCampos() {
        txtId.clear();
        txtNombre.clear();
        txtLocalizacion.clear();
        txtX.clear();
        txtY.clear();
        txtId.setEditable(true);
        listParadas.getSelectionModel().clearSelection();
    }

    /**
     * PROCESO: Cambia la vista actual al Menú Principal.
     * FLUJO DE LLAMADAS: Llama a AppTransporte.setRoot().
     */
    @FXML
    public void volver() throws IOException {
        AppTransporte.setRoot("MenuPrincipal.fxml", "Inicio");
    }

    /**
     * PROCESO: Crea y despliega una ventana de diálogo de tipo información.
     * ENTRADAS: Título y contenido del mensaje.
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