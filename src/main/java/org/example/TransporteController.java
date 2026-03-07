package org.example;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransporteController {
    @FXML private VBox panelInicio;
    @FXML private BorderPane panelGPS;
    @FXML private Canvas canvasMapa;
    @FXML private TextArea log;
    @FXML private TextField txtOrigen, txtDestino;
    @FXML private ComboBox<String> cbCriterio;
    @FXML private BorderPane panelEditor;
    @FXML private Canvas canvasEditor;
    private Parada paradaDesde;

    private GrafoTransporte sistema = new GrafoTransporte();
    private List<Parada> rutaResaltada = new ArrayList<>();

    @FXML
    public void initialize() {
        GestorArchivos.cargarDatos(sistema, "paradas.csv", "rutas.csv");
        cbCriterio.getItems().addAll("Tiempo", "Distancia", "Transbordos", "Costo");
        panelGPS.setVisible(false);
        panelEditor.setVisible(false);
    }

    @FXML
    private void entrarAlGPS() {
        log.clear();
        panelInicio.setVisible(false);
        panelGPS.setVisible(true);
        dibujarEnCanvas(canvasMapa, null);
    }

    @FXML
    private void ejecutarCalculo() {
        String origen = txtOrigen.getText();
        String destino = txtDestino.getText();
        String criterio = cbCriterio.getValue();

        // 1. VALIDACIÓN: Ahora incluimos el Costo en el mensaje
        if (origen == null || origen.isEmpty()) {
            log.appendText("Error: Debe seleccionar una parada de origen.\n");
            return;
        }
        if (destino == null || destino.isEmpty()) {
            log.appendText("Error: Debe seleccionar una parada de destino.\n");
            return;
        }
        if (criterio == null) {
            log.appendText("Error: Seleccione un criterio (Tiempo, Distancia, Transbordos o Costo).\n");
            return;
        }

        rutaResaltada = sistema.calcularRutaDijkstra(origen, destino, criterio);

        if (rutaResaltada.isEmpty()) {
            log.appendText("No hay conexión entre paradas.\n");
        } else {
            double valorTotal = sistema.calcularPesoTotalCamino(rutaResaltada, criterio);

            String unidad = "";
            switch (criterio.toLowerCase()) {
                case "tiempo":
                    unidad = " min";
                    break;
                case "distancia":
                    unidad = " km";
                    break;
                case "transbordos":
                    unidad = " transbordos";
                    break;
                case "costo":
                    unidad = " DOP";
                    break;
                default:
                    unidad = " unidades";
            }

            log.appendText("\n--- RESULTADO ---\n");
            log.appendText("Ruta: ");
            for (int i = 0; i < rutaResaltada.size(); i++) {
                log.appendText(rutaResaltada.get(i).getNombre() + (i < rutaResaltada.size() - 1 ? " -> " : ""));
            }

            log.appendText("\nTotal " + criterio + ": " + String.format("%.2f", valorTotal) + unidad + "\n");
        }
        dibujarEnCanvas(canvasMapa, rutaResaltada);
    }

    @FXML
    private void seleccionarParada(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();
        for (Parada p : sistema.getGrafo().keySet()) {
            double dist = Math.sqrt(Math.pow(mouseX - p.getX(), 2) + Math.pow(mouseY - p.getY(), 2));
            if (dist < 15) {
                if (txtOrigen.getText().isEmpty() || (!txtOrigen.getText().isEmpty() && !txtDestino.getText().isEmpty())) {
                    txtOrigen.setText(p.getNombre());
                    txtDestino.clear();
                } else {
                    txtDestino.setText(p.getNombre());
                }
                break;
            }
        }
    }

    private void dibujarEnCanvas(Canvas canvasDestino, List<Parada> camino) {
        GraphicsContext gc = canvasDestino.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasDestino.getWidth(), canvasDestino.getHeight());

        sistema.getGrafo().forEach((p, rutas) -> {
            for (Ruta r : rutas) {
                if (camino != null && estaEnCamino(p, r.getDestino(), camino)) {
                    gc.setStroke(Color.LIMEGREEN);
                    gc.setLineWidth(6);
                } else {
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(2);
                }
                gc.strokeLine(p.getX(), p.getY(), r.getDestino().getX(), r.getDestino().getY());
            }
        });

        for (Parada p : sistema.getGrafo().keySet()) {
            if (camino != null && camino.contains(p)) {
                gc.setFill(Color.LIMEGREEN);
                gc.fillOval(p.getX() - 15, p.getY() - 15, 30, 30);
            } else {
                gc.setFill(Color.RED);
                gc.fillOval(p.getX() - 12, p.getY() - 12, 24, 24);
            }
            gc.setFill(Color.BLACK);
            gc.setFont(new javafx.scene.text.Font("System Bold", 12));
            gc.fillText(p.getNombre(), p.getX() + 18, p.getY() + 5);
        }
    }

    private boolean estaEnCamino(Parada u, Parada v, List<Parada> camino) {
        for (int i = 0; i < camino.size() - 1; i++) {
            if (camino.get(i).equals(u) && camino.get(i + 1).equals(v)) return true;
        }
        return false;
    }

    @FXML
    private void abrirEditor() {
        log.clear();
        panelInicio.setVisible(false);
        panelEditor.setVisible(true);
        dibujarEnCanvas(canvasEditor, null);
    }

    @FXML
    private void clicEditor(MouseEvent e) {
        if (e.getButton() == MouseButton.SECONDARY) {
            Parada aBorrar = buscarParadaPorCoordenada(e.getX(), e.getY());
            if (aBorrar != null) {
                sistema.eliminarParada(aBorrar.getId());
                log.appendText("Eliminada: " + aBorrar.getNombre() + "\n");
                dibujarEnCanvas(canvasEditor, null);
            }
            return;
        }

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Gestión de Parada");
        dialog.setHeaderText("Nueva Parada");
        dialog.setContentText("Ingrese el nombre de la parada:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nombre -> {
            if (nombre.trim().isEmpty()) {
                log.appendText("Error: El nombre de la parada no puede estar vacío.\n");
                return;
            }

            String id = "P" + (sistema.getGrafo().size() + 1);
            Parada nueva = new Parada(id, nombre.trim(), "Ubicación", e.getX(), e.getY());
            sistema.agregarParada(nueva);
            dibujarEnCanvas(canvasEditor, null);
        });
    }

    @FXML
    private void presionarEditor(MouseEvent e) {
        paradaDesde = buscarParadaPorCoordenada(e.getX(), e.getY());
    }

    @FXML
    private void soltarEditor(MouseEvent e) {
        Parada paradaHacia = buscarParadaPorCoordenada(e.getX(), e.getY());
        if (paradaDesde != null && paradaHacia != null && !paradaDesde.equals(paradaHacia)) {
            double distPixeles = Math.sqrt(Math.pow(paradaDesde.getX() - paradaHacia.getX(), 2) +
                    Math.pow(paradaDesde.getY() - paradaHacia.getY(), 2));

            double distReal = distPixeles / 20.0;
            double tiempo = (distReal / 30.0) * 60.0;
            double costo = distReal * 5.0;
            boolean necesitaTrasbordo = distReal > 10.0;

            if (distReal > 0 && tiempo > 0) {
                sistema.agregarRuta(paradaDesde.getId(), paradaHacia.getId(), tiempo, distReal, costo, necesitaTrasbordo);
                log.appendText("Ruta creada: " + paradaDesde.getNombre() + " -> " + paradaHacia.getNombre() + " (" + String.format("%.2f", distReal) + " km)\n");
                dibujarEnCanvas(canvasEditor, null);
            } else {
                log.appendText("Error: La distancia entre paradas es demasiado corta.\n");
            }
        }
    }

    private Parada buscarParadaPorCoordenada(double x, double y) {
        return sistema.getGrafo().keySet().stream()
                .filter(p -> Math.sqrt(Math.pow(x - p.getX(), 2) + Math.pow(y - p.getY(), 2)) < 15)
                .findFirst().orElse(null);
    }

    @FXML
    private void guardarEditor() {
        GestorArchivos.guardarDatos(sistema, "paradas.csv", "rutas.csv");
        log.appendText("Cambios guardados con éxito.\n");
    }

    @FXML
    private void volverAlInicio() {
        panelGPS.setVisible(false);
        panelEditor.setVisible(false);
        panelInicio.setVisible(true);
    }
}