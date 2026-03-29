package ui;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.*;
import javafx.application.Platform;
import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class VisualizadorGrafo {

    private SmartGraphPanel<Parada, Ruta> graphView;
    private Digraph<Parada, Ruta> modelGraph;
    private final GrafoTransporte miGrafoOriginal;
    private final Consumer<Parada> alSeleccionar;
    private Map<Parada, Vertex<Parada>> verticesMap = new HashMap<>();
    private boolean inicializado = false;

    public VisualizadorGrafo(GrafoTransporte miGrafo, Consumer<Parada> alSeleccionar) {
        this.miGrafoOriginal = miGrafo;
        this.alSeleccionar = alSeleccionar;
        reconstruirGrafo();
    }

    public void reconstruirGrafo() {
        this.modelGraph = new DigraphEdgeList<>();
        this.verticesMap.clear();
        this.inicializado = false;

        for (Parada p : miGrafoOriginal.getGrafo().keySet()) {
            verticesMap.put(p, modelGraph.insertVertex(p));
        }

        for (Parada origen : miGrafoOriginal.getGrafo().keySet()) {
            for (Ruta ruta : miGrafoOriginal.getGrafo().get(origen)) {
                try {
                    if (verticesMap.containsKey(ruta.getDestino())) {
                        modelGraph.insertEdge(origen, ruta.getDestino(), ruta);
                    }
                } catch (Exception e) {}
            }
        }

        SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();
        try {
            URL cssUrl = getClass().getResource("/smartgraph.css");
            URL propUrl = getClass().getResource("/smartgraph.properties");
            if (propUrl != null && cssUrl != null) {
                SmartGraphProperties prop = new SmartGraphProperties(propUrl.openStream());
                this.graphView = new SmartGraphPanel<>(modelGraph, prop, strategy, cssUrl.toURI());
            } else {
                this.graphView = new SmartGraphPanel<>(modelGraph, strategy);
            }
        } catch (Exception e) {
            this.graphView = new SmartGraphPanel<>(modelGraph, strategy);
        }

        graphView.setAutomaticLayout(true);

        graphView.setVertexDoubleClickAction(graphVertex -> {
            alSeleccionar.accept(graphVertex.getUnderlyingVertex().element());
        });
    }

    public void inicializar() {
        if (!inicializado) {
            graphView.init();
            inicializado = true;
            Platform.runLater(() -> {
                fijarPosiciones();
            });
        }
    }

    public void fijarPosiciones() {
        for (Parada p : miGrafoOriginal.getGrafo().keySet()) {
            Vertex<Parada> v = verticesMap.get(p);
            if (v != null) {
                graphView.setVertexPosition(v, p.getX(), p.getY());
            }
        }
        graphView.update();
    }

    public void resaltarCaminos(List<Parada> optima, List<Parada> alternativa) {
        limpiarEstilos();
        if (optima != null) {
            aplicarEstiloCamino(optima, "-fx-stroke: limegreen; -fx-stroke-width: 6;");
            aplicarEstiloNodos(optima, "-fx-fill: limegreen; -fx-stroke: black; -fx-stroke-width: 2;");
        }
        if (alternativa != null) {
            aplicarEstiloCamino(alternativa, "-fx-stroke: gold; -fx-stroke-width: 4;");
            aplicarEstiloNodos(alternativa, "-fx-fill: gold; -fx-stroke: black; -fx-stroke-width: 2;");
        }
    }

    private void aplicarEstiloCamino(List<Parada> camino, String estilo) {
        for (int i = 0; i < camino.size() - 1; i++) {
            Parada u = camino.get(i);
            Parada v = camino.get(i + 1);
            modelGraph.edges().stream()
                    .filter(e -> e.vertices()[0].element().equals(u) && e.vertices()[1].element().equals(v))
                    .forEach(e -> ((javafx.scene.Node) graphView.getStylableEdge(e)).setStyle(estilo));
        }
    }

    private void aplicarEstiloNodos(List<Parada> camino, String estilo) {
        for (Parada p : camino) {
            Vertex<Parada> v = verticesMap.get(p);
            if (v != null) {
                ((javafx.scene.Node) graphView.getStylableVertex(v)).setStyle(estilo);
            }
        }
    }

    private void limpiarEstilos() {
        modelGraph.edges().forEach(e -> {
            ((javafx.scene.Node) graphView.getStylableEdge(e)).setStyle("-fx-stroke: black; -fx-stroke-width: 2;");
        });
        modelGraph.vertices().forEach(v -> {
            ((javafx.scene.Node) graphView.getStylableVertex(v)).setStyle("-fx-fill: red; -fx-stroke: black; -fx-stroke-width: 2;");
        });
    }

    public SmartGraphPanel<Parada, Ruta> getPanel() {
        return graphView;
    }
}