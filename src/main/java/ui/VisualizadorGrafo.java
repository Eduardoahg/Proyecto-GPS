package ui;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;

import java.util.List;


// Esta clase conecta tu lógica de transporte con la librería visual
public class VisualizadorGrafo {

    private final SmartGraphPanel<Parada, Ruta> graphView;
    private final Graph<Parada, Ruta> modelGraph;

    public VisualizadorGrafo(GrafoTransporte miGrafo) {
        // 1. Creamos el repositorio de datos que exige la librería
        this.modelGraph = new GraphEdgeList<>();

        // 2. Pasamos tus Paradas al modelo visual
        for (Parada p : miGrafo.getGrafo().keySet()) {
            modelGraph.insertVertex(p);
        }

        // 3. Pasamos tus Rutas (Aristas) al modelo visual
        for (Parada origen : miGrafo.getGrafo().keySet()) {
            for (Ruta ruta : miGrafo.getGrafo().get(origen)) {
                try {
                    // insertEdge necesita: (Origen, Destino, ObjetoRuta)
                    modelGraph.insertEdge(origen, ruta.getDestino(), ruta);
                } catch (Exception e) {
                    // Si la ruta ya existe (en grafos bidireccionales), la ignoramos
                }
            }
        }

        // 4. Estrategia de colocación (Circular para que se vea ordenado al inicio)
        SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();

        // Creamos el panel que hereda de Node (se puede añadir a cualquier Scene)
        this.graphView = new SmartGraphPanel<>(modelGraph, strategy);

        // Activamos que los nodos se muevan automáticamente si los arrastras
        graphView.setAutomaticLayout(true);
    }

    // Este método devuelve el componente de JavaFX para ponerlo en tu ventana
    public SmartGraphPanel<Parada, Ruta> getPanel() {
        return graphView;
    }

    public void resaltarCamino(List<Parada> camino) {
        if (camino == null || camino.isEmpty()) return;

        for (Parada p : camino) {
            try {
                // Le añadimos una etiqueta CSS en lugar de pintarlo directamente
                graphView.getStylableVertex(p).addStyleClass("camino-dorado");
            } catch (Exception e) {
            }
        }
    }
}