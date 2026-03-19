package ui;

import algorithms.AlgDijkstra;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.Parada;
import model.Ruta;
import persistence.GestorArchivos;
import structure.GrafoTransporte;

import java.util.List;

public class TransporteVisual {
    @FXML private VBox panelInicio;
    @FXML private BorderPane panelGPS, panelEditor;
    @FXML private Canvas canvasMapa, canvasEditor;
    @FXML private TextArea log;
    @FXML private TextField txtOrigen, txtDestino;
    @FXML private ComboBox<String> cbCriterio;

    private GrafoTransporte sistema = new GrafoTransporte();
    private AlgDijkstra buscador = new AlgDijkstra();
    private Parada paradaDesde;
    private Parada paradaSeleccionada;
    private boolean hayCambiosSinGuardar = false;
    private final String FILE_JSON = "transporte_datos.json";

    @FXML
    public void initialize() {
        GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        cbCriterio.getItems().addAll("Tiempo", "Distancia", "Transbordos", "Costo");
        volverAlInicio();
    }

    public boolean isHayCambiosSinGuardar() {
        return hayCambiosSinGuardar;
    }

    @FXML
    private void ejecutarCalculo() {
        String ori = txtOrigen.getText();
        String dest = txtDestino.getText();
        String crit = cbCriterio.getValue();

        if (ori.isEmpty() || dest.isEmpty() || crit == null) {
            log.appendText("Error: Complete los campos.\n");
            return;
        }

        List<Parada> camino = buscador.ejecutar(sistema, ori, dest, crit);
        log.clear();
        if (camino.isEmpty()) {
            log.appendText("No se encontró ruta.\n");
        } else {
            log.appendText("Ruta óptima hallada.\n");
        }
        dibujarGrafo(canvasMapa, camino);
    }

    // --- MÉTODOS DEL EDITOR (INTERACTIVIDAD) ---

    @FXML
    private void clicEditor(MouseEvent e) {
        Parada pBuscada = buscarParadaPorCoordenada(e.getX(), e.getY());

        // ELIMINAR: Clic derecho
        if (e.getButton() == MouseButton.SECONDARY && pBuscada != null) {
            sistema.eliminarParada(pBuscada.getId());
            hayCambiosSinGuardar = true;
            log.appendText("Parada eliminada: " + pBuscada.getNombre() + "\n");
            dibujarGrafo(canvasEditor, null);
            return;
        }

        // MODIFICAR: Doble clic
        if (e.getClickCount() == 2 && pBuscada != null) {
            TextInputDialog dialog = new TextInputDialog(pBuscada.getNombre());
            dialog.setTitle("Modificar Parada");
            dialog.setContentText("Nuevo nombre:");
            dialog.showAndWait().ifPresent(nuevo -> {
                sistema.modificarParada(pBuscada.getId(), nuevo);
                hayCambiosSinGuardar = true;
                dibujarGrafo(canvasEditor, null);
            });
            return;
        }

        // AGREGAR: Clic izquierdo en vacío
        if (pBuscada == null && e.getButton() == MouseButton.PRIMARY) {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Nueva Parada");
            dialog.setContentText("Nombre:");
            dialog.showAndWait().ifPresent(nombre -> {
                String id = "P" + (sistema.getGrafo().size() + 1);
                sistema.agregarParada(new Parada(id, nombre, "Urbana", e.getX(), e.getY()));
                hayCambiosSinGuardar = true;
                dibujarGrafo(canvasEditor, null);
            });
        }
    }

    @FXML
    private void presionarEditor(MouseEvent e) {
        paradaSeleccionada = buscarParadaPorCoordenada(e.getX(), e.getY());
        paradaDesde = paradaSeleccionada;
    }

    @FXML
    private void arrastrarEnEditor(MouseEvent e) {
        if (paradaSeleccionada != null) {
            paradaSeleccionada.setX(e.getX());
            paradaSeleccionada.setY(e.getY());
            hayCambiosSinGuardar = true;
            dibujarGrafo(canvasEditor, null);
        }
    }

    @FXML
    private void soltarEditor(MouseEvent e) {
        Parada paradaHacia = buscarParadaPorCoordenada(e.getX(), e.getY());
        // Crear ruta si soltamos sobre otra parada
        if (paradaDesde != null && paradaHacia != null && !paradaDesde.equals(paradaHacia)) {
            sistema.agregarRuta(paradaDesde.getId(), paradaHacia.getId(), 10.0, 5.0, 80.0, false);
            hayCambiosSinGuardar = true;
            log.appendText("Ruta conectada.\n");
            dibujarGrafo(canvasEditor, null);
        }
        paradaSeleccionada = null;
        paradaDesde = null;
    }

    @FXML
    private void seleccionarParada(MouseEvent event) {
        Parada p = buscarParadaPorCoordenada(event.getX(), event.getY());
        if (p != null) {
            if (txtOrigen.getText().isEmpty()) {
                txtOrigen.setText(p.getNombre());
            } else {
                txtDestino.setText(p.getNombre());
            }
        }
    }

    private Parada buscarParadaPorCoordenada(double x, double y) {
        return sistema.getGrafo().keySet().stream()
                .filter(p -> Math.sqrt(Math.pow(x - p.getX(), 2) + Math.pow(y - p.getY(), 2)) < 15)
                .findFirst().orElse(null);
    }

    private void dibujarGrafo(Canvas canvas, List<Parada> resaltar) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        sistema.getGrafo().forEach((p, rutas) -> {
            for (Ruta r : rutas) {
                gc.strokeLine(p.getX(), p.getY(), r.getDestino().getX(), r.getDestino().getY());
            }
        });

        for (Parada p : sistema.getGrafo().keySet()) {
            gc.setFill(resaltar != null && resaltar.contains(p) ? Color.LIMEGREEN : Color.RED);
            gc.fillOval(p.getX() - 10, p.getY() - 10, 20, 20);
            gc.setFill(Color.BLACK);
            gc.fillText(p.getNombre(), p.getX() + 12, p.getY() + 5);
        }
    }

    @FXML
    private void guardarEditor() {
        GestorArchivos.guardarEnJson(sistema, FILE_JSON);
        hayCambiosSinGuardar = false;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText("Cambios guardados con éxito.");
        alert.showAndWait();
    }

    @FXML private void abrirEditor() { panelInicio.setVisible(false); panelEditor.setVisible(true); dibujarGrafo(canvasEditor, null); }
    @FXML private void volverAlInicio() { panelGPS.setVisible(false); panelEditor.setVisible(false); panelInicio.setVisible(true); }
    @FXML private void entrarAlGPS() { panelInicio.setVisible(false); panelGPS.setVisible(true); dibujarGrafo(canvasMapa, null); }
}
