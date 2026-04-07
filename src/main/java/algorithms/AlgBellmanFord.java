package algorithms;

import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;
import java.util.*;

public class AlgBellmanFord {

    /**
     * PROCESO: Calcula la ruta óptima utilizando el algoritmo de Bellman-Ford.
     * * ENTRADAS:
     * - grafo: Estructura que contiene el mapa de adyacencia del sistema.
     * - idOri: Identificador de la parada de origen.
     * - idDest: Identificador de la parada de destino.
     * - criterio: Variable de optimización (Tiempo, Distancia, Costo o Transbordos).
     * * SALIDA: Una lista de paradas que forman el camino más corto.
     * * FLUJO DE LLAMADAS:
     * 1. Llama a grafo.buscarParada() para validar los puntos de inicio y fin.
     * 2. Llama a costos.put() y padres.put() para inicializar las estructuras de control.
     * 3. Llama a AlgDijkstra.obtenerPeso() para calcular el costo de cada tramo según el criterio.
     * 4. Llama a reconstruirCamino() para procesar el mapa de padres y generar el resultado final.
     */
    public List<Parada> ejecutar(GrafoTransporte grafo, String idOri, String idDest, String criterio) {
        Parada origen = grafo.buscarParada(idOri);
        Parada destino = grafo.buscarParada(idDest);
        if (origen == null || destino == null) return new ArrayList<>();

        Map<Parada, Double> costos = new HashMap<>();
        Map<Parada, Parada> padres = new HashMap<>();

        grafo.getGrafo().keySet().forEach(p -> costos.put(p, Double.MAX_VALUE));
        costos.put(origen, 0.0);

        int V = grafo.getGrafo().size();
        for (int i = 1; i < V; i++) {
            for (var entry : grafo.getGrafo().entrySet()) {
                Parada u = entry.getKey();
                if (costos.get(u) == Double.MAX_VALUE) continue;
                for (Ruta r : entry.getValue()) {
                    double peso = new AlgDijkstra().obtenerPeso(r, criterio);
                    if (costos.get(u) + peso < costos.get(r.getDestino())) {
                        costos.put(r.getDestino(), costos.get(u) + peso);
                        padres.put(r.getDestino(), u);
                    }
                }
            }
        }
        return reconstruirCamino(destino, padres);
    }

    /**
     * PROCESO: Reconstruye la secuencia de paradas desde el destino hacia el origen.
     * * ENTRADAS:
     * - destino: Nodo final de la ruta buscada.
     * - padres: Mapa que contiene la jerarquía de nodos calculada por el algoritmo.
     * * SALIDA: Lista de paradas en el orden correcto de viaje.
     * * FLUJO DE LLAMADAS:
     * - Llama a padres.containsKey() para verificar si existe un camino válido al destino.
     * - Llama a camino.add(0, p) repetidamente para invertir el orden del rastro.
     */
    private List<Parada> reconstruirCamino(Parada destino, Map<Parada, Parada> padres) {
        List<Parada> camino = new ArrayList<>();
        if (!padres.containsKey(destino)) return camino;
        for (Parada p = destino; p != null; p = padres.get(p)) camino.add(0, p);
        return camino;
    }
}