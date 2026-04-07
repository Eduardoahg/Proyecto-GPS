package ui;

import algorithms.*;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Parada;
import model.Ruta;
import persistence.GestorArchivos;
import structure.GrafoTransporte;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TransporteVisual {

    @FXML
    private Canvas canvasGPS;
    @FXML
    private TextArea log;
    @FXML
    private TextField txtOrigen, txtDestino;
    @FXML
    private ComboBox<String> cbCriterio, cbAlgoritmo;

    private static GrafoTransporte sistema = new GrafoTransporte();
    private AlgDijkstra dijkstra = new AlgDijkstra();
    private AlgBellmanFord bellmanFord = new AlgBellmanFord();

    private final String FILE_JSON = "transporte_datos.json";
    private final double RADIO_NODO = 16.0;

    /**
     * PROCESO: Prepara la interfaz de usuario al cargar la vista. Configura los selectores y la lógica de bloqueo.
     * FLUJO DE LLAMADAS:
     * 1. Llama a GestorArchivos.cargarDesdeJson() si el grafo está vacío.
     * 2. Configura los items de cbCriterio y cbAlgoritmo.
     * 3. Añade un Listener para desactivar cbCriterio cuando se elige BFS o Auditoría.
     */
    @FXML
    public void initialize() {
        if (sistema.getGrafo().isEmpty()) {
            GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        }

        if (cbCriterio != null) {
            cbCriterio.getItems().setAll("Tiempo", "Distancia", "Costo", "Mínimos Trasbordos");
            cbCriterio.setValue("Tiempo");
            cbCriterio.setPrefWidth(150);
        }

        if (cbAlgoritmo != null) {
            cbAlgoritmo.getItems().setAll(
                    "Dijkstra",
                    "Bellman-Ford",
                    "Menos Paradas (BFS)",
                    "Auditar Red (DFS)",
                    "Matriz de Caminos (Floyd)"
            );
            cbAlgoritmo.setValue("Dijkstra");
            cbAlgoritmo.setPrefWidth(200);  

            cbAlgoritmo.valueProperty().addListener((obs, viejo, nuevo) -> {
                if (nuevo == null) return;

                boolean desactivar = nuevo.equals("Menos Paradas (BFS)") ||
                        nuevo.equals("Auditar Red (DFS)");

                cbCriterio.setDisable(desactivar);
                cbCriterio.setOpacity(desactivar ? 0.5 : 1.0);
            });
        }

        
        if (log != null) {
            log.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");
            log.setPrefHeight(200);
            log.setPrefWidth(200);
            log.setWrapText(false);
        }

        actualizarTodo();
    }


    /**
     * PROCESO: Refresca la vista principal del sistema.
     * FLUJO DE LLAMADAS: Llama a dibujar() asegurándose de limpiar cualquier ruta previa.
     */
    private void actualizarTodo() {
        if (canvasGPS != null) {
            dibujar(canvasGPS, null, null);
        }
    }

    /**
     * PROCESO: Orquestador central de inteligencia de transporte.
     * Gestiona la ejecución de algoritmos de optimización (Dijkstra/Bellman),
     * conectividad (BFS) y auditoría profunda (DFS).
     * * FLUJO DE LLAMADAS:
     * 1. Recupera datos de los campos txtOrigen, txtDestino y ComboBoxes.
     * 2. Según el algoritmo:
     * - Optimización: Usa cbCriterio (Tiempo, Distancia, Costo).
     * - Estructura: Ignora criterios (BFS/DFS).
     * 3. Actualiza el Canvas y el TextArea de registro (log).
     */

    @FXML
    public void ejecutarCalculo() {
        String algoritmo = cbAlgoritmo.getValue();
        String criterio = cbCriterio.isDisable() ? "Saltos" : cbCriterio.getValue();

        if (algoritmo == null) {
            log.setText("ERROR: Debe seleccionar un algoritmo.");
            return;
        }

        if (algoritmo.equals("Matriz de Caminos (Floyd)")) {
            ejecutarFloydWarshall();
            return;
        }

        String idOri = txtOrigen.getText().trim();
        String idDest = txtDestino.getText().trim();

        if (algoritmo.equals("Auditar Red (DFS)")) {
            if (idOri.isEmpty()) {
                log.setText("ERROR: Seleccione una parada de origen para iniciar la auditoría (DFS).");
            } else {
                ejecutarAuditoriaDFS(idOri);
            }
            return;
        }

        if (idOri.isEmpty() || idDest.isEmpty()) {
            log.setText("ERROR: Para " + algoritmo + " se requiere origen y destino.");
            return;
        }

        List<Parada> optima = new ArrayList<>();
        List<Parada> segunda = new ArrayList<>();

        try {
            switch (algoritmo) {
                case "Dijkstra":
                    if (criterio.equals("Mínimos Trasbordos")) {
                        optima = calcularRutaPorSaltos(idOri, idDest);
                    } else {
                        optima = dijkstra.ejecutar(sistema, idOri, idDest, criterio);
                        List<List<Parada>> todosLosCaminos = encontrarTodosLosCaminos(idOri, idDest);
                        if (todosLosCaminos.size() > 1) {
                            todosLosCaminos.sort(Comparator.comparingDouble(cam -> calcularPesoCamino(cam, criterio)));
                            segunda = todosLosCaminos.get(1);
                        }
                    }
                    break;

                case "Bellman-Ford":
                    optima = bellmanFord.ejecutar(sistema, idOri, idDest, criterio);
                    break;

                case "Menos Paradas (BFS)":
                    optima = calcularRutaPorSaltos(idOri, idDest);
                    break;
            }

            if (optima != null && !optima.isEmpty()) {
                dibujar(canvasGPS, optima, segunda);
                mostrarDetallesDual(optima, segunda, algoritmo, criterio);
            } else {
                log.setText("No existe una ruta disponible entre " + idOri + " y " + idDest);
                dibujar(canvasGPS, null, null);
            }

        } catch (Exception e) {
            log.setText("Error en la ejecución: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * PROCESO: Calcula la ruta con menos transbordos utilizando BFS.
     * Extraído como metodo independiente para mantener la estructura original.
     */
    private List<Parada> calcularRutaPorSaltos(String idOri, String idDest) {
        if (idDest.isEmpty()) return null;

        Parada pStart = sistema.buscarParada(idOri);
        Parada pEnd = sistema.buscarParada(idDest);
        if (pStart != null && pEnd != null) {
            return BFS.calcularRutaMinimosSaltos(sistema.getGrafo(), pStart, pEnd);
        }
        return new ArrayList<>();
    }

    /**
     * PROCESO: Realiza el mapeo de conectividad profunda (DFS) y genera el reporte de auditoría.
     */
    private void ejecutarAuditoriaDFS(String idOrigen) {
        Parada inicio = sistema.buscarParada(idOrigen);
        if (inicio == null) {
            log.setText("Origen no encontrado para auditoría.");
            return;
        }

        List<Parada> visitados = new ArrayList<>();
        StringBuilder reporte = new StringBuilder();
        reporte.append("=== AUDITORÍA ESTRATÉGICA DE COBERTURA (DFS) ===\n");
        reporte.append("Punto inicial: ").append(inicio.getNombre()).append("\n");
        reporte.append("------------------------------------------------\n");

        realizarMapeoDFS(inicio, visitados, reporte, 0);

        log.setText(reporte.toString());
        dibujar(canvasGPS, visitados, null);
    }

    /**
     * PROCESO: Método recursivo para explorar la red y dar formato jerárquico al reporte.
     */
    private void realizarMapeoDFS(Parada actual, List<Parada> visitados, StringBuilder sb, int nivel) {
        visitados.add(actual);
        sb.append("  ".repeat(nivel)).append("└─ ").append(actual.getNombre()).append(" (ID: ").append(actual.getId()).append(")\n");

        for (Ruta r : sistema.getGrafo().getOrDefault(actual, new ArrayList<>())) {
            if (!visitados.contains(r.getDestino())) {
                realizarMapeoDFS(r.getDestino(), visitados, sb, nivel + 1);
            }
        }
    }

    /**
     * PROCESO: Renderiza gráficamente las paradas y rutas en el Canvas. Resalta los caminos óptimos y secundarios.
     * ENTRADAS: Canvas destino, lista de parada óptima y lista de parada secundaria.
     * FLUJO DE LLAMADAS:
     * 1. Itera sobre sistema.getGrafo() para dibujar cada conexión.
     * 2. Llama a estaEnCamino() para decidir el color y grosor de la línea.
     * 3. Llama a dibujarAristaConFlecha() para representar el sentido de la ruta.
     */

    private void dibujar(Canvas canvas, List<Parada> optima, List<Parada> segunda) {
        if (canvas == null) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        sistema.getGrafo().forEach((p, rutas) -> {
            for (Ruta r : rutas) {
                double x1 = p.getX();
                double y1 = p.getY();
                double x2 = r.getDestino().getX();
                double y2 = r.getDestino().getY();

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
            double px = p.getX();
            double py = p.getY();

            Color c = Color.web("#0B5563");
            if (optima != null && optima.contains(p)) {
                c = Color.web("#32CD32");
            } else if (segunda != null && segunda.contains(p)) {
                c = Color.GOLD;
            }

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

    /**
     * PROCESO: Dibuja una línea con una punta de flecha para indicar la dirección de la ruta.
     * ENTRADAS: Contexto gráfico, coordenadas de inicio y fin, color y ancho de línea.
     * SALIDA: Representación visual de una arista dirigida en el Canvas.
     */

    private void dibujarAristaConFlecha(GraphicsContext gc, double x1, double y1, double x2, double y2, Color c, double a) {
        double dx = x2 - x1;
        double dy = y2 - y1;
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

    /**
     * PROCESO: Localiza todos los caminos posibles entre dos puntos mediante una búsqueda en profundidad (DFS).
     * ENTRADAS: IDs de origen y destino.
     * SALIDA: Una lista de listas de paradas.
     * FLUJO DE LLAMADAS: Llama a sistema.buscarParada() e inicia la recursión con buscarRecursivo().
     */
    private List<List<Parada>> encontrarTodosLosCaminos(String idOri, String idDest) {
        List<List<Parada>> resultados = new ArrayList<>();
        Parada origen = sistema.buscarParada(idOri);
        Parada destino = sistema.buscarParada(idDest);

        if (origen == null || destino == null) {
            return resultados;
        }

        buscarRecursivo(origen, destino, new ArrayList<>(), new ArrayList<>(), resultados);
        return resultados;
    }

    /**
     * PROCESO: Método recursivo para explorar todas las ramas del grafo sin repetir nodos en el mismo camino.
     */
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

    /**
     * PROCESO: Calcula el peso total de un camino sumando el costo de cada arista individual según el criterio.
     */
    private double calcularPesoCamino(List<Parada> camino, String criterio) {
        double peso = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            Parada u = camino.get(i);
            Parada v = camino.get(i + 1);
            Ruta r = sistema.getGrafo().get(u).stream()
                    .filter(rt -> rt.getDestino().equals(v))
                    .findFirst()
                    .orElse(null);
            if (r != null) {
                peso += dijkstra.obtenerPeso(r, criterio);
            }
        }
        return peso;
    }

    /**
     * PROCESO: Imprime en el TextArea las métricas comparativas de las rutas calculadas.
     */
    private void mostrarDetallesDual(List<Parada> optima, List<Parada> segunda, String alg, String crit) {
        log.clear();
        log.appendText("Algoritmo: " + alg + (crit.equals("Saltos") ? "" : " (" + crit + ")") + "\n");

        if (optima.isEmpty()) {
            log.appendText("No hay ruta disponible.");
            return;
        }

        double[] mOpt = obtenerMetricas(optima);
        log.appendText("MEJOR (VERDE): " + CalculadoraRutas.formatearTiempo(mOpt[0]) + " | " +
                String.format("%.2f", mOpt[1]) + "km | $" +
                String.format("%.2f", mOpt[2]) + " | Transbordos: " + (int) mOpt[3] + "\n");

        if (!segunda.isEmpty()) {
            double[] mSeg = obtenerMetricas(segunda);
            log.appendText("SEGUNDA (ORO): " + CalculadoraRutas.formatearTiempo(mSeg[0]) + " | " +
                    String.format("%.2f", mSeg[1]) + "km | $" +
                    String.format("%.1f", mSeg[2]) + " | Transbordos: " + (int) mSeg[3]);
        }
    }

    /**
     * PROCESO: Suma las métricas físicas (tiempo, distancia, costo, transbordos) de una secuencia de paradas.
     */
    private double[] obtenerMetricas(List<Parada> camino) {
        double t = 0, d = 0, c = 0, tr = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            Parada u = camino.get(i);
            Parada v = camino.get(i + 1);
            Ruta r = sistema.getGrafo().get(u).stream()
                    .filter(rt -> rt.getDestino().equals(v))
                    .findFirst()
                    .orElse(null);
            if (r != null) {
                t += r.getTiempo();
                d += r.getDistancia();
                c += r.getCosto();
                tr += CalculadoraRutas.calcularTransbordos(r.getDistancia());
            }
        }
        return new double[]{t, d, c, tr};
    }

    /**
     * PROCESO: Verifica si un segmento (u -> v) pertenece a un camino calculado.
     */
    private boolean estaEnCamino(Parada u, Parada v, List<Parada> camino) {
        if (camino == null) return false;
        for (int i = 0; i < camino.size() - 1; i++) {
            if (camino.get(i).equals(u) && camino.get(i + 1).equals(v)) {
                return true;
            }
        }
        return false;
    }

    /**
     * PROCESO: Detecta clics en el canvas para asignar automáticamente el origen y el destino.
     */
    @FXML
    public void seleccionarParadaMapa(MouseEvent e) {
        Parada p = buscarParada(e.getX(), e.getY());
        if (p != null) {
            if (txtOrigen.getText().isEmpty() || (!txtOrigen.getText().isEmpty() && !txtDestino.getText().isEmpty())) {
                txtOrigen.setText(p.getNombre());
                txtDestino.clear();
            } else {
                txtDestino.setText(p.getNombre());
            }
            actualizarTodo();
        }
    }

    /**
     * PROCESO: Calcula si existe una parada dentro del radio de detección de un punto dado.
     */
    private Parada buscarParada(double x, double y) {
        return sistema.getGrafo().keySet().stream()
                .filter(p -> Math.hypot(x - p.getX(), y - p.getY()) < RADIO_NODO)
                .findFirst()
                .orElse(null);
    }

    /**
     * PROCESO: Navegación entre las diferentes vistas de la aplicación.
     */
    @FXML
    public void entrarAlGPS() throws IOException {
        AppTransporte.setRoot("UsuarioGPS.fxml", "GPS");
    }

    @FXML
    public void abrirGestionParadas() throws IOException {
        AppTransporte.setRoot("GestionParadas.fxml", "Gestión de Paradas");
    }

    /**
     * PROCESO: Despliega un diálogo de elección para dirigir al usuario a la creación o modificación de rutas.
     */
    @FXML
    public void abrirGestionRutas() throws IOException {
        List<String> choices = Arrays.asList("Crear Nuevas Rutas", "Modificar Rutas Existentes");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Crear Nuevas Rutas", choices);
        dialog.setTitle("Selección de Tarea");
        dialog.setHeaderText("¿Qué desea realizar con las rutas?");

        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (result.get().equals("Crear Nuevas Rutas")) {
                AppTransporte.setRoot("CrearRuta.fxml", "Creación de Rutas");
            } else {
                AppTransporte.setRoot("GestionRutas.fxml", "Modificación de Rutas");
            }
        }
    }

    @FXML
    public void actualizarMapa() {
        GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        actualizarTodo();
    }

    @FXML
    public void volverAlInicio() throws IOException {
        AppTransporte.setRoot("MenuPrincipal.fxml", "Inicio");
    }

    @FXML
    public void salirApp() {
        System.exit(0);
    }

    /**
     * PROCESO: Genera y muestra una tabla con los costos mínimos entre todas las paradas.
     * * FLUJO DE LLAMADAS:
     * 1. Obtiene las paradas del sistema para definir filas y columnas.
     * 2. Calcula la matriz de distancias según el criterio (Tiempo, Distancia o Costo).
     * 3. Formatea el texto con columnas alineadas para que sea legible.
     * * INTERPRETACIÓN:
     * - FILAS: Punto de Origen.
     * - COLUMNAS: Punto de Destino.
     * - CELDAS: El camino más corto entre ambos (∞ si no hay conexión).
     * * COMPLEJIDAD: Temporal O(V³) | Espacial O(V²)
     */
    private void ejecutarFloydWarshall() {
        List<Parada> listaParadas = new ArrayList<>(sistema.getGrafo().keySet());
        String criterio = cbCriterio.getValue();

        double[][] matriz = FloydWarshall.generarMatrizDistancias(listaParadas, sistema.getGrafo(), criterio);

        log.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        log.setWrapText(false);

        StringBuilder sb = new StringBuilder();
        sb.append("═════════════════════════════════════════════════\n");
        sb.append(" REPORTE ESTRATÉGICO: COSTOS MÍNIMOS GLOBAL\n");
        sb.append("═════════════════════════════════════════════════\n");
        sb.append("Interpretación: FILAS = Origen  |  COLUMNAS = Destino\n");
        sb.append("Criterio: ").append(criterio.toUpperCase()).append("\n\n");

        sb.append(String.format("%-12s", "ORI\\DEST"));
        for (Parada p : listaParadas) {
            sb.append(String.format("%-10s", "[" + p.getId() + "]"));
        }
        sb.append("\n").append("═".repeat(12 + listaParadas.size() * 10)).append("\n");


        for (int i = 0; i < matriz.length; i++) {
            sb.append(String.format("%-12s", "[" + listaParadas.get(i).getId() + "]"));

            for (int j = 0; j < matriz[i].length; j++) {
                if (matriz[i][j] == Double.MAX_VALUE || matriz[i][j] >= 999999) {
                    sb.append(String.format("%-10s", "  ∞"));
                } else {
                    sb.append(String.format("%-10.1f", matriz[i][j]));
                }
            }
            sb.append("\n");
        }

        log.setText(sb.toString());
        dibujar(canvasGPS, null, null);
    }
}