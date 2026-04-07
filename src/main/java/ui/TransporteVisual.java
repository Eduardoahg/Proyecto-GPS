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
    @FXML private Canvas canvasGPS;
    @FXML private TextArea log;
    @FXML private TextField txtOrigen, txtDestino;
    @FXML private ComboBox<String> cbCriterio, cbAlgoritmo;

    private static GrafoTransporte sistema = new GrafoTransporte();
    private AlgDijkstra dijkstra = new AlgDijkstra();
    private AlgBellmanFord bellmanFord = new AlgBellmanFord();

    private final String FILE_JSON = "transporte_datos.json";
    private final double RADIO_NODO = 16.0;

    /**
     * PROCESO: Prepara la interfaz de usuario al cargar la vista. Configura los selectores y carga los datos iniciales.
     * FLUJO DE LLAMADAS:
     * 1. Llama a GestorArchivos.cargarDesdeJson() si el grafo está vacío.
     * 2. Configura los items de cbCriterio y cbAlgoritmo.
     * 3. Llama a actualizarTodo() para renderizar el mapa inicial.
     */
    @FXML
    public void initialize() {
        if (sistema.getGrafo().isEmpty()) {
            GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
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

    /**
     * PROCESO: Refresca la vista principal del sistema.
     * FLUJO DE LLAMADAS: Llama a dibujar() asegurándose de limpiar cualquier ruta previa.
     */
    private void actualizarTodo() {
        if (canvasGPS != null) dibujar(canvasGPS, null, null);
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
                double x1 = p.getX(), y1 = p.getY();
                double x2 = r.getDestino().getX(), y2 = r.getDestino().getY();

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
            double px = p.getX(), py = p.getY();

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

    /**
     * PROCESO: Dibuja una línea con una punta de flecha para indicar la dirección de la ruta.
     * ENTRADAS: Contexto gráfico, coordenadas de inicio y fin, color y ancho de línea.
     * SALIDA: Representación visual de una arista dirigida en el Canvas.
     */
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

    /**
     * PROCESO: Orquestador del cálculo de rutas. Obtiene los parámetros de la UI y ejecuta la búsqueda.
     * FLUJO DE LLAMADAS:
     * 1. Obtiene los valores de txtOrigen, txtDestino, cbCriterio y cbAlgoritmo.
     * 2. Llama a dijkstra.ejecutar() o bellmanFord.ejecutar() según la selección.
     * 3. Si usa Dijkstra, llama a encontrarTodosLosCaminos() y calcularPesoCamino() para hallar la segunda mejor ruta.
     * 4. Llama a dibujar() para pintar los resultados en el mapa.
     * 5. Llama a mostrarDetallesDual() para actualizar el log de texto.
     */
    @FXML
    public void ejecutarCalculo() {
        String ori = txtOrigen.getText();
        String dest = txtDestino.getText();
        String crit = cbCriterio.getValue();
        String alg = cbAlgoritmo.getValue();

        if (ori.isEmpty() || dest.isEmpty() || crit == null || alg == null) return;

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
        if (origen == null || destino == null) return resultados;
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
     * FLUJO DE LLAMADAS: Llama a dijkstra.obtenerPeso() para cada conexión del camino.
     */
    private double calcularPesoCamino(List<Parada> camino, String criterio) {
        double peso = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            Parada u = camino.get(i); Parada v = camino.get(i + 1);
            Ruta r = sistema.getGrafo().get(u).stream().filter(rt -> rt.getDestino().equals(v)).findFirst().orElse(null);
            if (r != null) peso += dijkstra.obtenerPeso(r, criterio);
        }
        return peso;
    }

    /**
     * PROCESO: Imprime en el TextArea las métricas comparativas de las rutas calculadas.
     * FLUJO DE LLAMADAS: Llama a obtenerMetricas() para cada ruta y a CalculadoraRutas.formatearTiempo().
     */
    private void mostrarDetallesDual(List<Parada> optima, List<Parada> segunda, String alg, String crit) {
        log.clear();
        log.appendText("Algoritmo: " + alg + " (" + crit + ")\n");
        if (optima.isEmpty()) {
            log.appendText("No hay ruta disponible.");
            return;
        }
        double[] mOpt = obtenerMetricas(optima);
        log.appendText("MEJOR (VERDE): " + CalculadoraRutas.formatearTiempo(mOpt[0]) + " | " + String.format("%.2f", mOpt[1]) + "km | $" + String.format("%.2f", mOpt[2]) + " | Transbordos: " + (int)mOpt[3] + "\n");
        if (!segunda.isEmpty()) {
            double[] mSeg = obtenerMetricas(segunda);
            log.appendText("SEGUNDA (ORO): " + CalculadoraRutas.formatearTiempo(mSeg[0]) + " | " + String.format("%.2f", mSeg[1]) + "km | $" + String.format("%.1f", mSeg[2]) + " | Transbordos: " + (int)mSeg[3]);
        }
    }

    /**
     * PROCESO: Suma las métricas físicas (tiempo, distancia, costo, transbordos) de una secuencia de paradas.
     * FLUJO DE LLAMADAS: Llama a CalculadoraRutas.calcularTransbordos() para cada segmento.
     */
    private double[] obtenerMetricas(List<Parada> camino) {
        double t = 0, d = 0, c = 0, tr = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            Parada u = camino.get(i); Parada v = camino.get(i + 1);
            Ruta r = sistema.getGrafo().get(u).stream().filter(rt -> rt.getDestino().equals(v)).findFirst().orElse(null);
            if (r != null) {
                t += r.getTiempo(); d += r.getDistancia(); c += r.getCosto();
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
            if (camino.get(i).equals(u) && camino.get(i + 1).equals(v)) return true;
        }
        return false;
    }

    /**
     * PROCESO: Detecta clics en el canvas para asignar automáticamente el origen y el destino.
     * FLUJO DE LLAMADAS: Llama a buscarParada() y a actualizarTodo().
     */
    @FXML public void seleccionarParadaMapa(MouseEvent e) {
        Parada p = buscarParada(e.getX(), e.getY());
        if (p != null) {
            if (txtOrigen.getText().isEmpty() || (!txtOrigen.getText().isEmpty() && !txtDestino.getText().isEmpty())) {
                txtOrigen.setText(p.getNombre()); txtDestino.clear();
            } else { txtDestino.setText(p.getNombre()); }
            actualizarTodo();
        }
    }

    /**
     * PROCESO: Calcula si existe una parada dentro del radio de detección de un punto dado.
     */
    private Parada buscarParada(double x, double y) {
        return sistema.getGrafo().keySet().stream()
                .filter(p -> Math.hypot(x - p.getX(), y - p.getY()) < RADIO_NODO)
                .findFirst().orElse(null);
    }

    /**
     * PROCESO: Navegación entre las diferentes vistas de la aplicación.
     * FLUJO DE LLAMADAS: Llaman a AppTransporte.setRoot().
     */
    @FXML public void entrarAlGPS() throws IOException {
        AppTransporte.setRoot("UsuarioGPS.fxml", "GPS");
    }

    @FXML public void abrirGestionParadas() throws IOException {
        AppTransporte.setRoot("GestionParadas.fxml", "Gestión de Paradas");
    }

    /**
     * PROCESO: Despliega un diálogo de elección para dirigir al usuario a la creación o modificación de rutas.
     */
    @FXML public void abrirGestionRutas() throws IOException {
        List<String> choices = Arrays.asList("Crear Nuevas Rutas", "Modificar Rutas Existentes");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Crear Nuevas Rutas", choices);
        dialog.setTitle("Selección de Tarea");
        dialog.setHeaderText("¿Qué desea realizar con las rutas?");
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            if (result.get().equals("Crear Nuevas Rutas")) {
                AppTransporte.setRoot("CrearRuta.fxml", "Creación de Rutas");
            } else {
                AppTransporte.setRoot("GestionRutas.fxml", "Modificación de Rutas");
            }
        }
    }

    /**
     * PROCESO: Sincroniza el mapa visual con el estado actual del archivo JSON.
     * FLUJO DE LLAMADAS: Llama a GestorArchivos.cargarDesdeJson().
     */
    @FXML public void actualizarMapa() {
        GestorArchivos.cargarDesdeJson(sistema, FILE_JSON);
        actualizarTodo();
    }

    @FXML public void volverAlInicio() throws IOException {
        AppTransporte.setRoot("MenuPrincipal.fxml", "Inicio");
    }

    @FXML public void salirApp() {
        System.exit(0);
    }
}