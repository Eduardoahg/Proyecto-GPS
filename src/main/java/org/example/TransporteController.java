package org.example;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class TransporteController {
    @FXML private VBox panelInicio;
    @FXML private BorderPane panelGPS;
    @FXML private Canvas canvasMapa;
    @FXML private TextArea log;
    @FXML private TextField txtOrigen, txtDestino;
    @FXML private ComboBox<String> cbCriterio;

    private GrafoTransporte sistema = new GrafoTransporte();
    private List<Parada> rutaResaltada = new ArrayList<>();

    @FXML
    public void initialize() {
        GestorArchivos.cargarDatos(sistema, "paradas.csv", "rutas.csv");
        cbCriterio.getItems().addAll("Tiempo", "Distancia", "Transbordos");
        panelGPS.setVisible(false);
    }

    @FXML
    private void entrarAlGPS() {
        panelInicio.setVisible(false);
        panelGPS.setVisible(true);
        dibujar(null);
    }

    @FXML
    private void ejecutarCalculo() {
        String origen = txtOrigen.getText();
        String destino = txtDestino.getText();
        String criterio = cbCriterio.getValue();

        if (origen.isEmpty() || destino.isEmpty() || criterio == null) {
            log.appendText("Error: Faltan datos.\n");
            return;
        }

        rutaResaltada = sistema.calcularRutaDijkstra(origen, destino, criterio);

        if (rutaResaltada.isEmpty()) {
            log.appendText("No hay conexión entre paradas.\n");
        } else {
            double valorTotal = sistema.calcularPesoTotalCamino(rutaResaltada, criterio);
            String unidad = criterio.equalsIgnoreCase("tiempo") ? " min" : (criterio.equalsIgnoreCase("distancia") ? " km" : " transbordos");

            log.appendText("\n--- RESULTADO ---\n");
            log.appendText("Ruta: ");
            for (int i = 0; i < rutaResaltada.size(); i++) {
                log.appendText(rutaResaltada.get(i).getNombre() + (i < rutaResaltada.size() - 1 ? " -> " : ""));
            }
            log.appendText("\nTotal " + criterio + ": " + String.format("%.2f", valorTotal) + unidad + "\n");
        }
        dibujar(rutaResaltada);
    }

    @FXML
    private void seleccionarParada(javafx.scene.input.MouseEvent event) {
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

    private void dibujar(List<Parada> camino) {
        GraphicsContext gc = canvasMapa.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasMapa.getWidth(), canvasMapa.getHeight());

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
}