package ui;

import algorithms.*;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Parada;
import model.Ruta;
import persistence.GestorArchivos;
import structure.GrafoTransporte;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TransporteVisual {
    @FXML private Canvas canvasEditor, canvasGPS;
    @FXML private TextArea log;
    @FXML private TextField txtOrigen, txtDestino;
    @FXML private ComboBox<String> cbCriterio, cbAlgoritmo;

    private static GrafoTransporte sistema = new GrafoTransporte();
    private AlgDijkstra dijkstra = new AlgDijkstra();
    private AlgBellmanFord bellmanFord = new AlgBellmanFord();

    private final String FILE_JSON = "transporte_datos.json";
    private final String FILE_MAPA = "mapa.jpg";
    private Parada paradaDesde;
    private final double RADIO_NODO = 16.0;

    private double mapOffsetX = 0, mapOffsetY = 0;
    private double lastMouseX, lastMouseY;
    private Image imagenMapa;

    @FXML
    public void initialize() {
        if (sistema.getGrafo().isEmpty()) {
            GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        }

        try {
            File file = new File(FILE_MAPA);
            if (file.exists()) {
                imagenMapa = new Image(file.toURI().toString());
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        if (cbCriterio != null) {
            cbCriterio.getItems().addAll("Tiempo", "Distancia", "Transbordos", "Costo");
            cbCriterio.setValue("Tiempo");
        }
        if (cbAlgoritmo != null) {
            cbAlgoritmo.getItems().addAll("Dijkstra", "Bellman-Ford");
            cbAlgoritmo.setValue("Dijkstra");
        }
        actualizarTodo();
    }

    private void actualizarTodo() {
        if (canvasEditor != null) dibujar(canvasEditor, null, null);
        if (canvasGPS != null) dibujar(canvasGPS, null, null);
    }

    private void dibujar(Canvas canvas, List<Parada> optima, List<Parada> segunda) {
        if (canvas == null) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (imagenMapa != null) {
            gc.drawImage(imagenMapa, mapOffsetX, mapOffsetY);
        }

        sistema.getGrafo().forEach((p, rutas) -> {
            for (Ruta r : rutas) {
                double x1 = p.getX() + mapOffsetX, y1 = p.getY() + mapOffsetY;
                double x2 = r.getDestino().getX() + mapOffsetX, y2 = r.getDestino().getY() + mapOffsetY;

                Color color = Color.BLACK;
                double ancho = 2.5;

                if (segunda != null && estaEnCamino(p, r.getDestino(), segunda)) {
                    color = Color.GOLD;
                    ancho = 8.0;
                }

                if (optima != null && estaEnCamino(p, r.getDestino(), optima)) {
                    color = Color.web("#32CD32");
                    ancho = 5.0;
                }

                dibujarAristaConFlecha(gc, x1, y1, x2, y2, color, ancho);
            }
        });

        for (Parada p : sistema.getGrafo().keySet()) {
            double px = p.getX() + mapOffsetX, py = p.getY() + mapOffsetY;

            Color c = Color.web("#0B5563");
            if (optima != null && optima.contains(p)) c = Color.web("#32CD32");
            else if (segunda != null && segunda.contains(p)) c = Color.GOLD;

            gc.setEffect(new DropShadow(10, Color.BLACK));
            gc.setFill(c);
            gc.fillOval(px - RADIO_NODO, py - RADIO_NODO, RADIO_NODO * 2, RADIO_NODO * 2);

            gc.setEffect(null);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2.5);
            gc.strokeOval(px - RADIO_NODO, py - RADIO_NODO, RADIO_NODO * 2, RADIO_NODO * 2);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
            String idNum = p.getId().replaceAll("[^0-9]", "");
            gc.fillText(idNum, px - 6, py + 5);

            gc.setFont(Font.font("System", FontWeight.BOLD, 11));
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.0);
            gc.strokeText(p.getNombre(), px - 20, py + 35);
            gc.setFill(Color.WHITE);
            gc.fillText(p.getNombre(), px - 20, py + 35);
        }
    }

    private void dibujarAristaConFlecha(GraphicsContext gc, double x1, double y1, double x2, double y2, Color c, double a) {
        double dx = x2 - x1, dy = y2 - y1;
        double angulo = Math.atan2(dy, dx);
        double tamFlecha = 14.0;

        double startX = x1 + RADIO_NODO * Math.cos(angulo);
        double startY = y1 + RADIO_NODO * Math.sin(angulo);
        double endX = x2 - RADIO_NODO * Math.cos(angulo);
        double endY = y2 - RADIO_NODO * Math.sin(angulo);

        gc.setStroke(c);
        gc.setLineWidth(a);
        gc.strokeLine(startX, startY, endX, endY);

        double xA = endX - tamFlecha * Math.cos(angulo - Math.PI / 6);
        double yA = endY - tamFlecha * Math.sin(angulo - Math.PI / 6);
        double xB = endX - tamFlecha * Math.cos(angulo + Math.PI / 6);
        double yB = endY - tamFlecha * Math.sin(angulo + Math.PI / 6);

        gc.setFill(c);
        gc.fillPolygon(new double[]{endX, xA, xB}, new double[]{endY, yA, yB}, 3);
    }

    @FXML
    public void ejecutarCalculo() {
        String ori = txtOrigen.getText();
        String dest = txtDestino.getText();
        String crit = cbCriterio.getValue();
        String alg = cbAlgoritmo.getValue();

        if (ori.isEmpty() || dest.isEmpty()) return;
        if (crit == null || alg == null) return;

        List<Parada> optima;
        List<Parada> segunda = new ArrayList<>();

        if (alg.equals("Dijkstra")) {
            optima = dijkstra.ejecutar(sistema, ori, dest, crit);
            List<List<Parada>> todos = encontrarTodosLosCaminos(ori, dest);
            todos.sort(Comparator.comparingDouble(c -> calcularPesoCamino(c, crit)));
            if (todos.size() > 1) segunda = todos.get(1);
        } else {
            optima = bellmanFord.ejecutar(sistema, ori, dest, crit);
        }

        dibujar(canvasGPS, optima, segunda);
        mostrarDetallesDual(optima, segunda, alg, crit);
    }

    private List<List<Parada>> encontrarTodosLosCaminos(String idOri, String idDest) {
        List<List<Parada>> resultados = new ArrayList<>();
        Parada origen = sistema.buscarParada(idOri);
        Parada destino = sistema.buscarParada(idDest);
        if (origen == null || destino == null) return resultados;
        buscarRecursivo(origen, destino, new ArrayList<>(), new ArrayList<>(), resultados);
        return resultados;
    }

    private void buscarRecursivo(Parada actual, Parada destino, List<Parada> camino, List<Parada> visitados, List<List<Parada>> res) {
        visitados.add(actual);
        camino.add(actual);
        if (actual.equals(destino)) {
            res.add(new ArrayList<>(camino));
        } else {
            for (Ruta r : sistema.getGrafo().getOrDefault(actual, new ArrayList<>())) {
                if (!visitados.contains(r.getDestino())) {
                    buscarRecursivo(r.getDestino(), destino, camino, visitados, res);
                }
            }
        }
        camino.remove(camino.size() - 1);
        visitados.remove(actual);
    }

    private double calcularPesoCamino(List<Parada> camino, String criterio) {
        double peso = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            Parada u = camino.get(i); Parada v = camino.get(i + 1);
            Ruta r = sistema.getGrafo().get(u).stream().filter(rt -> rt.getDestino().equals(v)).findFirst().orElse(null);
            if (r != null) peso += dijkstra.obtenerPeso(r, criterio);
        }
        return peso;
    }

    private void mostrarDetallesDual(List<Parada> optima, List<Parada> segunda, String alg, String crit) {
        log.clear();
        log.appendText("Algoritmo: " + alg + " (" + crit + ")\n");
        if (optima.isEmpty()) {
            log.appendText("No hay ruta disponible.");
            return;
        }
        double[] mOpt = obtenerMetricas(optima);
        log.appendText("MEJOR (VERDE): " + CalculadoraRutas.formatearTiempo(mOpt[0]) + " | " + String.format("%.2f", mOpt[1]) + "km | $" + String.format("%.1f", mOpt[2]) + " | Transbordos: " + (int)mOpt[3] + "\n");
        if (!segunda.isEmpty()) {
            double[] mSeg = obtenerMetricas(segunda);
            log.appendText("SEGUNDA (ORO): " + CalculadoraRutas.formatearTiempo(mSeg[0]) + " | " + String.format("%.2f", mSeg[1]) + "km | $" + String.format("%.1f", mSeg[2]) + " | Transbordos: " + (int)mSeg[3]);
        }
    }

    private double[] obtenerMetricas(List<Parada> camino) {
        double t = 0, d = 0, tr = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            Parada u = camino.get(i); Parada v = camino.get(i + 1);
            Ruta r = sistema.getGrafo().get(u).stream().filter(rt -> rt.getDestino().equals(v)).findFirst().orElse(null);
            if (r != null) {
                t += r.getTiempo();
                d += r.getDistancia();
                if(r.isRequiereTrasbordo()) tr++;
            }
        }
        return new double[]{t, d, CalculadoraRutas.calcularCosto(d, t), tr};
    }

    private boolean estaEnCamino(Parada u, Parada v, List<Parada> camino) {
        if (camino == null) return false;
        for (int i = 0; i < camino.size() - 1; i++) {
            if (camino.get(i).equals(u) && camino.get(i + 1).equals(v)) return true;
        }
        return false;
    }

    @FXML
    public void clicEditor(MouseEvent e) {
        double realX = e.getX() - mapOffsetX;
        double realY = e.getY() - mapOffsetY;
        Parada p = buscarParada(realX, realY);
        if (e.getButton() == MouseButton.PRIMARY && p == null && e.getClickCount() == 1) {
            TextInputDialog d = new TextInputDialog("P" + (sistema.getGrafo().size() + 1));
            d.showAndWait().ifPresent(n -> {
                sistema.agregarParada(new Parada("P" + (sistema.getGrafo().size() + 1), n, "Urbana", realX, realY));
                actualizarTodo();
            });
        } else if (e.getButton() == MouseButton.SECONDARY && p != null) {
            List<String> op = Arrays.asList("Cambiar Nombre", "Eliminar Parada", "Eliminar Ruta Específica");
            new ChoiceDialog<>(op.get(0), op).showAndWait().ifPresent(o -> {
                if (o.equals("Cambiar Nombre")) {
                    new TextInputDialog(p.getNombre()).showAndWait().ifPresent(nuevo -> {
                        sistema.modificarParada(p.getId(), nuevo);
                    });
                } else if (o.equals("Eliminar Parada")) {
                    sistema.eliminarParada(p.getId());
                } else {
                    TextInputDialog d = new TextInputDialog("");
                    d.setHeaderText("ID de la parada destino a desconectar:");
                    d.showAndWait().ifPresent(dest -> sistema.eliminarRuta(p.getId(), dest));
                }
                actualizarTodo();
            });
        }
    }

    @FXML public void presionarEditor(MouseEvent e) {
        lastMouseX = e.getX(); lastMouseY = e.getY();
        if (e.getButton() == MouseButton.PRIMARY) {
            paradaDesde = buscarParada(e.getX() - mapOffsetX, e.getY() - mapOffsetY);
        }
    }

    @FXML
    public void arrastrarEnEditor(MouseEvent e) {
        double deltaX = e.getX() - lastMouseX;
        double deltaY = e.getY() - lastMouseY;
        if (e.getButton() == MouseButton.SECONDARY) {
            mapOffsetX += deltaX; mapOffsetY += deltaY;
            actualizarTodo();
        } else if (paradaDesde != null && e.isShiftDown()) {
            paradaDesde.setX(paradaDesde.getX() + deltaX);
            paradaDesde.setY(paradaDesde.getY() + deltaY);
            actualizarTodo();
        }
        lastMouseX = e.getX(); lastMouseY = e.getY();
    }

    @FXML public void soltarEditor(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY && !e.isShiftDown()) {
            Parada hacia = buscarParada(e.getX() - mapOffsetX, e.getY() - mapOffsetY);
            if (paradaDesde != null && hacia != null && !paradaDesde.equals(hacia)) {
                double km = CalculadoraRutas.calcularDistanciaKM(paradaDesde, hacia);
                sistema.agregarRuta(paradaDesde.getId(), hacia.getId(), km * 2.5, km, CalculadoraRutas.calcularCosto(km, km*2.5), CalculadoraRutas.calcularTransbordos(km) > 0);
                actualizarTodo();
            }
        }
        paradaDesde = null;
    }

    @FXML public void seleccionarParadaMapa(MouseEvent e) {
        Parada p = buscarParada(e.getX() - mapOffsetX, e.getY() - mapOffsetY);
        if (p != null) {
            if (txtOrigen.getText().isEmpty() || (!txtOrigen.getText().isEmpty() && !txtDestino.getText().isEmpty())) {
                txtOrigen.setText(p.getNombre()); txtDestino.clear();
            } else { txtDestino.setText(p.getNombre()); }
            actualizarTodo();
        }
    }

    private Parada buscarParada(double x, double y) {
        return sistema.getGrafo().keySet().stream()
                .filter(p -> Math.hypot(x - p.getX(), y - p.getY()) < RADIO_NODO)
                .findFirst().orElse(null);
    }

    @FXML public void entrarAlGPS() throws IOException {
        AppTransporte.setRoot("UsuarioGPS.fxml", "GPS");
    }
    @FXML public void abrirEditor() throws IOException {
        AppTransporte.setRoot("EditorGPS.fxml", "Editor");
    }
    @FXML public void actualizarMapa() {
        GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        actualizarTodo();
    }
    @FXML public void volverAlInicio() throws IOException {
        AppTransporte.setRoot("MenuPrincipal.fxml", "Inicio");
    }
    @FXML public void guardarEditor() {
        GestorArchivos.guardarEnJson(sistema, FILE_JSON);
    }
    @FXML public void salirApp() {
        System.exit(0);
    }
}