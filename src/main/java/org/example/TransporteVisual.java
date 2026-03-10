package org.example;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TransporteVisual {
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
    private List<Parada> rutaMejor = new ArrayList<>();
    private List<Parada> rutaSegunda = new ArrayList<>();

    @FXML
    public void initialize() {
        GestorArchivos.cargarDatos(sistema, "paradas.csv", "rutas.csv");
        cbCriterio.getItems().addAll("Tiempo", "Distancia", "Transbordos", "Costo");
        panelGPS.setVisible(false);
        panelEditor.setVisible(false);
    }

    @FXML private void entrarAlGPS() {
        log.clear();
        panelInicio.setVisible(false);
        panelGPS.setVisible(true);
        dibujarDual(canvasMapa, new ArrayList<>(), new ArrayList<>());
    }

    @FXML
    private void ejecutarCalculo() {
        String ori = txtOrigen.getText();
        String dest = txtDestino.getText();
        String crit = cbCriterio.getValue();

        if (ori.isEmpty() || dest.isEmpty() || crit == null) {
            log.appendText("Favor completar los campos y/o criterio.\n");
            return;
        }

        rutaMejor = sistema.calcularRutaDijkstra(ori, dest, crit);

        List<List<Parada>> todas = sistema.obtenerCaminosAlternativos(ori, dest);
        todas.sort(Comparator.comparingDouble(c -> sistema.calcularPesoTotalCamino(c, crit)));

        rutaSegunda = (todas.size() > 1) ? todas.get(1) : new ArrayList<>();

        log.clear();
        if (rutaMejor.isEmpty()) {
            log.appendText("No se encontro una ruta entre las paradas seleccionadas.\n");
        } else {
            imprimirRuta("MEJOR RUTA (VERDE)", rutaMejor, crit);
            if (!rutaSegunda.isEmpty()) {
                imprimirRuta("SEGUNDA MEJOR (AMARILLO)", rutaSegunda, crit);
            }
        }
        dibujarDual(canvasMapa, rutaMejor, rutaSegunda);
    }

    private void imprimirRuta(String titulo, List<Parada> camino, String crit) {
        double peso = sistema.calcularPesoTotalCamino(camino, crit);
        String unidad = switch (crit.toLowerCase()) {
            case "tiempo" -> " min";
            case "distancia" -> " km";
            case "transbordos" -> " transbordos";
            case "costo" -> " DOP";
            default -> " unidades";
        };

        log.appendText("--- " + titulo + " ---\n");
        for (int i = 0; i < camino.size(); i++) {
            log.appendText(camino.get(i).getNombre() + (i < camino.size() - 1 ? " -> " : ""));
        }
        log.appendText("\nTotal: " + String.format("%.2f", peso) + unidad + "\n\n");
    }

    private void dibujarDual(Canvas canvas, List<Parada> principal, List<Parada> secundaria) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setLineWidth(2);
        gc.setStroke(Color.BLACK);
        sistema.getGrafo().forEach((p, rutas) -> {
            for (Ruta r : rutas) {
                gc.strokeLine(p.getX(), p.getY(), r.getDestino().getX(), r.getDestino().getY());
            }
        });

        if (secundaria != null && !secundaria.isEmpty()) {
            gc.setStroke(Color.GOLD);
            gc.setLineWidth(8);
            for (int i = 0; i < secundaria.size() - 1; i++) {
                gc.strokeLine(secundaria.get(i).getX(), secundaria.get(i).getY(),
                        secundaria.get(i+1).getX(), secundaria.get(i+1).getY());
            }
        }

        if (principal != null && !principal.isEmpty()) {
            gc.setStroke(Color.LIMEGREEN);
            gc.setLineWidth(5);
            for (int i = 0; i < principal.size() - 1; i++) {
                gc.strokeLine(principal.get(i).getX(), principal.get(i).getY(),
                        principal.get(i+1).getX(), principal.get(i+1).getY());
            }
        }

        for (Parada p : sistema.getGrafo().keySet()) {
            if (principal != null && principal.contains(p)) {
                gc.setFill(Color.LIMEGREEN);
                gc.fillOval(p.getX() - 15, p.getY() - 15, 30, 30);
            } else if (secundaria != null && secundaria.contains(p)) {
                gc.setFill(Color.GOLD);
                gc.fillOval(p.getX() - 14, p.getY() - 14, 28, 28);
            } else {
                gc.setFill(Color.RED);
                gc.fillOval(p.getX() - 12, p.getY() - 12, 24, 24);
            }
            gc.setFill(Color.BLACK);
            gc.setFont(new javafx.scene.text.Font("System Bold", 12));
            gc.fillText(p.getNombre(), p.getX() + 18, p.getY() + 5);
        }
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

    @FXML private void abrirEditor() {
        log.clear();
        panelInicio.setVisible(false);
        panelEditor.setVisible(true);
        dibujarDual(canvasEditor, new ArrayList<>(), new ArrayList<>());
    }

    @FXML
    private void clicEditor(MouseEvent e) {
        Parada pBuscada = buscarParadaPorCoordenada(e.getX(), e.getY());

        if (e.getButton() == MouseButton.SECONDARY && pBuscada != null) {
            sistema.eliminarParada(pBuscada.getId());
            log.appendText("Eliminada :) " + pBuscada.getNombre() + "\n");
            dibujarDual(canvasEditor, new ArrayList<>(), new ArrayList<>());
            return;
        }

        if (e.getClickCount() == 2 && pBuscada != null) {
            TextInputDialog dialog = new TextInputDialog(pBuscada.getNombre());
            dialog.setTitle("Modificar Parada");
            dialog.setHeaderText("Cambiando nombre a: " + pBuscada.getId());
            dialog.setContentText("Nuevo nombre:");
            dialog.showAndWait().ifPresent(nuevo -> {
                if (!nuevo.trim().isEmpty()) {
                    sistema.modificarParada(pBuscada.getId(), nuevo.trim());
                    dibujarDual(canvasEditor, new ArrayList<>(), new ArrayList<>());
                }
            });
            return;
        }

        if (e.getClickCount() == 1 && pBuscada == null && e.getButton() == MouseButton.PRIMARY) {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Nueva Parada");
            dialog.setHeaderText("Creando parada en (" + (int)e.getX() + ", " + (int)e.getY() + ")");
            dialog.setContentText("Nombre:");
            dialog.showAndWait().ifPresent(nombre -> {
                if (!nombre.trim().isEmpty()) {
                    String id = "P" + (sistema.getGrafo().size() + 1);
                    sistema.agregarParada(new Parada(id, nombre.trim(), "Ubicacion", e.getX(), e.getY()));
                    dibujarDual(canvasEditor, new ArrayList<>(), new ArrayList<>());
                }
            });
        }
    }

    @FXML private void presionarEditor(MouseEvent e) { paradaDesde = buscarParadaPorCoordenada(e.getX(), e.getY()); }

    @FXML
    private void soltarEditor(MouseEvent e) {
        Parada paradaHacia = buscarParadaPorCoordenada(e.getX(), e.getY());
        if (paradaDesde != null && paradaHacia != null && !paradaDesde.equals(paradaHacia)) {
            double distPixeles = Math.sqrt(Math.pow(paradaDesde.getX() - paradaHacia.getX(), 2) + Math.pow(paradaDesde.getY() - paradaHacia.getY(), 2));
            double distReal = distPixeles / 20.0;
            double tiempo = (distReal / 30.0) * 60.0;
            double costo = (distReal <= 7.0) ? 80.0 : 80.0 + (distReal - 7.0) * 15.0;
            boolean necesitaTrasbordo = distReal > 10.0;

            sistema.agregarRuta(paradaDesde.getId(), paradaHacia.getId(), tiempo, distReal, costo, necesitaTrasbordo);
            log.appendText("Ruta creada: " + paradaDesde.getNombre() + " -> " + paradaHacia.getNombre() + "\n");
            dibujarDual(canvasEditor, new ArrayList<>(), new ArrayList<>());
        }
    }

    private Parada buscarParadaPorCoordenada(double x, double y) {
        return sistema.getGrafo().keySet().stream()
                .filter(p -> Math.sqrt(Math.pow(x - p.getX(), 2) + Math.pow(y - p.getY(), 2)) < 15)
                .findFirst().orElse(null);
    }

    @FXML private void guardarEditor() {
        GestorArchivos.guardarDatos(sistema, "paradas.csv", "rutas.csv");
        log.appendText("Cambios guardados.\n");
    }

    @FXML private void volverAlInicio() {
        panelGPS.setVisible(false);
        panelEditor.setVisible(false);
        panelInicio.setVisible(true);
    }
}