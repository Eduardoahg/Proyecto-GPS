package ui;

import algorithms.AlgDijkstra;
import javafx.application.Platform;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransporteVisual {
    @FXML private VBox panelOpciones;
    @FXML private BorderPane panelGPS;
    @FXML private Canvas canvasEditor;
    @FXML private TextArea log;
    @FXML private TextField txtOrigen, txtDestino;
    @FXML private ComboBox<String> cbCriterio;

    private static GrafoTransporte sistema = new GrafoTransporte();
    private static boolean hayCambiosSinGuardar = false;

    private AlgDijkstra buscador = new AlgDijkstra();
    private VisualizadorGrafo visualizador;
    private final String FILE_JSON = "transporte_datos.json";

    private Parada paradaDesde;

    @FXML
    public void initialize() {
        if (sistema.getGrafo().isEmpty()) {
            GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        }

        if (cbCriterio != null) {
            cbCriterio.getItems().addAll("Tiempo", "Distancia", "Transbordos", "Costo");
        }

        visualizador = new VisualizadorGrafo(sistema, p -> {
            if (txtOrigen != null && txtDestino != null) {
                if (txtOrigen.getText().isEmpty()) txtOrigen.setText(p.getNombre());
                else txtDestino.setText(p.getNombre());
            }
        });

        if (canvasEditor != null) {
            dibujar();
        }
    }

    @FXML
    private void entrarAlGPS() throws IOException {
        AppTransporte.setRoot("UsuarioGPS.fxml", "Modo GPS - Usuario");
    }

    @FXML
    private void abrirEditor() throws IOException {
        AppTransporte.setRoot("EditorGPS.fxml", "Editor de Red de Transporte");
    }

    @FXML
    private void ejecutarCalculo() {
        String ori = txtOrigen.getText();
        String dest = txtDestino.getText();
        String crit = cbCriterio.getValue();

        if (ori.isEmpty() || dest.isEmpty() || crit == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona origen, destino y criterio").show();
            return;
        }

        List<Parada> optima = buscador.ejecutar(sistema, ori, dest, crit);
        List<Parada> alternativa = buscarRutaAlternativa(ori, dest, crit, optima);

        visualizador.resaltarCaminos(optima, alternativa);
        mostrarDetalles(optima);
    }

    private List<Parada> buscarRutaAlternativa(String ori, String dest, String crit, List<Parada> optima) {
        if (optima.size() < 2) return new ArrayList<>();
        Parada p1 = optima.get(0);
        Parada p2 = optima.get(1);

        List<Ruta> rutas = sistema.getGrafo().get(p1);
        Ruta temp = rutas.stream().filter(r -> r.getDestino().equals(p2)).findFirst().orElse(null);

        rutas.remove(temp);
        List<Parada> alt = buscador.ejecutar(sistema, ori, dest, crit);
        if (temp != null) rutas.add(temp);
        return alt;
    }

    private void mostrarDetalles(List<Parada> camino) {
        log.clear();
        if (camino.isEmpty()) {
            log.appendText("No hay ruta disponible\n");
            return;
        }
        double distPX = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            Parada a = camino.get(i);
            Parada b = camino.get(i + 1);
            distPX += Math.hypot(b.getX() - a.getX(), b.getY() - a.getY());
        }

        double km = distPX / 100.0;
        double min = km * 1.5;
        int transbordos = (int) (km / 9);
        double costo = 80 + (km * 12) + (min * 1.1);

        log.appendText("--- ANÁLISIS DE RUTA ---\n");
        log.appendText("Distancia: " + String.format("%.2f", km) + " km\n");
        log.appendText("Tiempo: " + (min > 60 ? (int)min/60 + "h " + (int)min%60 + "m" : (int)min + "m") + "\n");
        log.appendText("Costo: $" + String.format("%.2f", costo) + " DOP\n");
        log.appendText("Transbordos: " + transbordos + "\n");
    }

    @FXML
    private void actualizarMapa() {
        if (canvasEditor != null) {
            GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
            dibujar();
        }

        if (panelGPS != null) {
            visualizador.reconstruirGrafo();
            panelGPS.setCenter(visualizador.getPanel());

            Platform.runLater(() -> {
                if (panelGPS.getWidth() > 0) {
                    visualizador.inicializar();
                } else {
                    panelGPS.widthProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal.doubleValue() > 0) {
                            visualizador.inicializar();
                        }
                    });
                }
            });
        }
    }

    @FXML
    private void volverAlInicio() throws IOException {
        AppTransporte.setRoot("MenuPrincipal.fxml", "Inicio - GPS");
    }

    @FXML
    private void salirApp() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void guardarEditor() {
        GestorArchivos.guardarEnJson(sistema, FILE_JSON);
        hayCambiosSinGuardar = false;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText("¡Datos guardados con éxito!");
        alert.show();
    }

    @FXML
    private void clicEditor(MouseEvent e) {
        Parada p = buscarParada(e.getX(), e.getY());
        if (e.getButton() == MouseButton.PRIMARY) {
            if (p == null) {
                TextInputDialog dialog = new TextInputDialog("Parada " + (sistema.getGrafo().size() + 1));
                dialog.setTitle("Nueva Parada");
                dialog.setHeaderText("Configuración de Parada");
                dialog.setContentText("Ingrese el nombre:");
                dialog.showAndWait().ifPresent(name -> {
                    String id = "P" + (sistema.getGrafo().size() + 1);
                    sistema.agregarParada(new Parada(id, name, "Urbana", e.getX(), e.getY()));
                    hayCambiosSinGuardar = true;
                    dibujar();
                });
            }
        } else if (e.getButton() == MouseButton.SECONDARY) {
            if (p != null) {
                List<String> opciones = Arrays.asList("Modificar Nombre", "Eliminar Parada", "Eliminar Rutas de Salida");
                ChoiceDialog<String> dialog = new ChoiceDialog<>("Modificar Nombre", opciones);
                dialog.setTitle("Gestión de Parada");
                dialog.setHeaderText("Opciones para: " + p.getNombre());
                dialog.setContentText("Selecciona una acción:");

                dialog.showAndWait().ifPresent(opcion -> {
                    if (opcion.equals("Modificar Nombre")) {
                        TextInputDialog editDialog = new TextInputDialog(p.getNombre());
                        editDialog.setTitle("Modificar Parada");
                        editDialog.setHeaderText(null);
                        editDialog.setContentText("Nuevo nombre:");
                        editDialog.showAndWait().ifPresent(newName -> {
                            sistema.modificarParada(p.getId(), newName);
                            hayCambiosSinGuardar = true;
                            dibujar();
                        });
                    } else if (opcion.equals("Eliminar Parada")) {
                        sistema.eliminarParada(p.getId());
                        hayCambiosSinGuardar = true;
                        dibujar();
                    } else if (opcion.equals("Eliminar Rutas de Salida")) {
                        sistema.getGrafo().get(p).clear();
                        hayCambiosSinGuardar = true;
                        dibujar();
                    }
                });
            }
        }
    }

    @FXML
    private void presionarEditor(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            paradaDesde = buscarParada(e.getX(), e.getY());
        }
    }

    @FXML
    private void soltarEditor(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            Parada hacia = buscarParada(e.getX(), e.getY());
            if (paradaDesde != null && hacia != null && !paradaDesde.equals(hacia)) {
                sistema.agregarRuta(paradaDesde.getId(), hacia.getId(), 10, 5, 80, false);
                hayCambiosSinGuardar = true;
                dibujar();
            }
            paradaDesde = null;
        }
    }

    @FXML
    private void arrastrarEnEditor(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY && paradaDesde != null) {
            if (!e.isShiftDown()) {
                paradaDesde.setX(e.getX());
                paradaDesde.setY(e.getY());
                hayCambiosSinGuardar = true;
                dibujar();
            }
        }
    }

    private Parada buscarParada(double x, double y) {
        return sistema.getGrafo().keySet().stream()
                .filter(p -> Math.hypot(x - p.getX(), y - p.getY()) < 15)
                .findFirst().orElse(null);
    }

    private void dibujar() {
        if (canvasEditor == null) return;
        GraphicsContext gc = canvasEditor.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasEditor.getWidth(), canvasEditor.getHeight());

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1.5);
        sistema.getGrafo().forEach((p, rutas) -> {
            rutas.forEach(r -> gc.strokeLine(p.getX(), p.getY(), r.getDestino().getX(), r.getDestino().getY()));
        });

        for (Parada p : sistema.getGrafo().keySet()) {
            gc.setFill(Color.BLACK);
            gc.fillOval(p.getX() - 10, p.getY() - 10, 20, 20);
            gc.setFill(Color.BLACK);
            gc.fillText(p.getNombre(), p.getX() + 12, p.getY() + 5);
        }
    }

    public boolean isHayCambiosSinGuardar() {
        return hayCambiosSinGuardar;
    }
}