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
    private VisualizadorGrafo visualizador;

    private Parada paradaDesde;
    private Parada paradaSeleccionada;
    private boolean hayCambiosSinGuardar = false;
    private final String FILE_JSON = "transporte_datos.json";

    @FXML
    public void initialize() {
        GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        cbCriterio.getItems().addAll("Tiempo", "Distancia", "Transbordos", "Costo");

        // Inicializamos el visualizador de SmartGraph
        visualizador = new VisualizadorGrafo(sistema);

        // Lo colocamos en el centro para que brille
        panelGPS.setCenter(visualizador.getPanel());

        volverAlInicio();
    }

    @FXML
    private void ejecutarCalculo() {
        String ori = txtOrigen.getText();
        String dest = txtDestino.getText();
        String crit = cbCriterio.getValue();

        if (ori.isEmpty() || dest.isEmpty() || crit == null) {
            log.appendText("Error: Llena todos los campos, montro.\n");
            return;
        }

        List<Parada> camino = buscador.ejecutar(sistema, ori, dest, crit);
        log.clear();

        if (camino.isEmpty()) {
            log.appendText("No hay ruta de " + ori + " a " + dest + ".\n");
        } else {
            log.appendText("¡Ruta óptima hallada!\n");
            log.appendText("Paradas: " + camino.size() + "\n");
            visualizador.resaltarCamino(camino);
        }
    }

    @FXML
    private void entrarAlGPS() {
        panelInicio.setVisible(false);
        panelGPS.setVisible(true);
        // Despierta la física del grafo
        visualizador.getPanel().init();
    }

    // --- EL FIX: Este es el método que te faltaba y hacía que la app crasheara ---
    @FXML
    private void seleccionarParada(MouseEvent event) {
        // Buscamos si el clic fue cerca de una parada (usando la lógica del Canvas)
        Parada p = buscarParadaPorCoordenada(event.getX(), event.getY());
        if (p != null) {
            if (txtOrigen.getText().isEmpty()) {
                txtOrigen.setText(p.getNombre());
            } else {
                txtDestino.setText(p.getNombre());
            }
        }
    }

    // --- MÉTODOS DEL EDITOR ---

    @FXML
    private void clicEditor(MouseEvent e) {
        Parada pBuscada = buscarParadaPorCoordenada(e.getX(), e.getY());

        if (e.getButton() == MouseButton.SECONDARY && pBuscada != null) {
            sistema.eliminarParada(pBuscada.getId());
            hayCambiosSinGuardar = true;
            log.appendText("Eliminada: " + pBuscada.getNombre() + "\n");
            actualizarGrafos();
            return;
        }

        if (e.getClickCount() == 2 && pBuscada != null) {
            TextInputDialog dialog = new TextInputDialog(pBuscada.getNombre());
            dialog.setTitle("Modificar Parada");
            dialog.setContentText("Nuevo nombre:");
            dialog.showAndWait().ifPresent(nuevo -> {
                sistema.modificarParada(pBuscada.getId(), nuevo);
                hayCambiosSinGuardar = true;
                actualizarGrafos();
            });
            return;
        }

        if (pBuscada == null && e.getButton() == MouseButton.PRIMARY) {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Nueva Parada");
            dialog.setContentText("Nombre:");
            dialog.showAndWait().ifPresent(nombre -> {
                String id = "P" + (sistema.getGrafo().size() + 1);
                sistema.agregarParada(new Parada(id, nombre, "Urbana", e.getX(), e.getY()));
                hayCambiosSinGuardar = true;
                actualizarGrafos();
            });
        }
    }

    private void actualizarGrafos() {
        dibujarGrafo(canvasEditor, null);
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
        if (paradaDesde != null && paradaHacia != null && !paradaDesde.equals(paradaHacia)) {
            sistema.agregarRuta(paradaDesde.getId(), paradaHacia.getId(), 10.0, 5.0, 80.0, false);
            hayCambiosSinGuardar = true;
            actualizarGrafos();
        }
        paradaSeleccionada = null;
        paradaDesde = null;
    }

    private Parada buscarParadaPorCoordenada(double x, double y) {
        return sistema.getGrafo().keySet().stream()
                .filter(p -> Math.sqrt(Math.pow(x - p.getX(), 2) + Math.pow(y - p.getY(), 2)) < 15)
                .findFirst().orElse(null);
    }

    private void dibujarGrafo(Canvas canvas, List<Parada> resaltar) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.web("#f4f4f4"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        sistema.getGrafo().forEach((p, rutas) -> {
            for (Ruta r : rutas) {
                gc.strokeLine(p.getX(), p.getY(), r.getDestino().getX(), r.getDestino().getY());
            }
        });

        for (Parada p : sistema.getGrafo().keySet()) {
            gc.setFill(resaltar != null && resaltar.contains(p) ? Color.LIMEGREEN : Color.TOMATO);
            gc.fillOval(p.getX() - 8, p.getY() - 8, 16, 16);
            gc.setFill(Color.BLACK);
            gc.fillText(p.getNombre(), p.getX() + 10, p.getY() + 3);
        }
    }

    @FXML
    private void guardarEditor() {
        GestorArchivos.guardarEnJson(sistema, FILE_JSON);
        hayCambiosSinGuardar = false;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("¡Datos guardados!");
        alert.show();
    }

    public boolean isHayCambiosSinGuardar() {
        return hayCambiosSinGuardar;
    }

    @FXML
    private void abrirEditor() {
        panelInicio.setVisible(false);
        panelEditor.setVisible(true);
        dibujarGrafo(canvasEditor, null);
    }

    @FXML
    private void volverAlInicio() {
        panelGPS.setVisible(false);
        panelEditor.setVisible(false);
        panelInicio.setVisible(true);
    }
}