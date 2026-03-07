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

public class TransporteController {
    @FXML private VBox panelInicio;
    @FXML private BorderPane panelGPS;
    @FXML private Canvas canvasMapa;
    @FXML private TextArea log;
    @FXML private TextField txtOrigen, txtDestino;
    @FXML private ComboBox<String> cbCriterio;

    private GrafoTransporte sistema = new GrafoTransporte();

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
        dibujar();
    }

    @FXML
    private void ejecutarCalculo() {
        String res = sistema.dijkstra(txtOrigen.getText(), txtDestino.getText(), cbCriterio.getValue());
        log.appendText(res + "\n");
    }

    private void dibujar() {
        GraphicsContext gc = canvasMapa.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasMapa.getWidth(), canvasMapa.getHeight());

        gc.setStroke(Color.BLACK);
        sistema.getGrafo().forEach((p, rutas) -> {
            for (Ruta r : rutas) {
                gc.strokeLine(p.getX(), p.getY(), r.getDestino().getX(), r.getDestino().getY());
            }
        });

        for (Parada p : sistema.getGrafo().keySet()) {
            gc.setFill(Color.RED);
            gc.fillOval(p.getX() - 10, p.getY() - 10, 20, 20);
            gc.setFill(Color.BLACK);
            gc.fillText(p.getNombre(), p.getX() + 12, p.getY() + 5);
        }
    }
}