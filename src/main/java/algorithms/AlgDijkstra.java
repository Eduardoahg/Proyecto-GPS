package algorithms;

import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;
import java.util.*;

/**
 * PROCESO: Implementa el algoritmo de Dijkstra para encontrar la ruta más corta en un grafo de transporte urbano.
 * Se utiliza una cola de prioridad para optimizar la búsqueda basándose en el menor costo acumulado.
 */
public class AlgDijkstra {

    /**
     * PROCESO: Ejecuta la lógica de búsqueda de camino mínimo basándose en un criterio específico.
     * ENTRADAS:
     * - grafo: El mapa de transporte que contiene paradas y rutas.
     * - idOri: ID de la parada donde inicia el viaje.
     * - idDest: ID de la parada de destino final.
     * - criterio: Criterio de optimización (Tiempo, Distancia, Transbordos o Costo).
     * SALIDA: Una lista ordenada de objetos Parada que representan la ruta óptima.
     * FLUJO DE LLAMADAS:
     * 1. Llama a grafo.buscarParada() para validar origen y destino.
     * 2. Llama a distancias.put() y distancias.get() para gestionar los costos de los nodos.
     * 3. Dentro del ciclo, llama a obtenerPeso() para evaluar el costo de cada tramo adyacente.
     * 4. Al finalizar, invoca a reconstruirCamino() para armar la lista de retorno.
     */
    public List<Parada> ejecutar(GrafoTransporte grafo, String idOri, String idDest, String criterio) {
        Parada origen = grafo.buscarParada(idOri);
        Parada destino = grafo.buscarParada(idDest);
        if (origen == null || destino == null) return new ArrayList<>();

        Map<Parada, Double> distancias = new HashMap<>();
        Map<Parada, Parada> padres = new HashMap<>();
        PriorityQueue<Parada> pq = new PriorityQueue<>(Comparator.comparingDouble(distancias::get));

        grafo.getGrafo().keySet().forEach(p -> distancias.put(p, Double.MAX_VALUE));
        distancias.put(origen, 0.0);
        pq.add(origen);

        while (!pq.isEmpty()) {
            Parada u = pq.poll();
            if (u.equals(destino)) break;

            for (Ruta r : grafo.getGrafo().getOrDefault(u, new ArrayList<>())) {
                double peso = obtenerPeso(r, criterio);
                if (distancias.get(u) + peso < distancias.get(r.getDestino())) {
                    distancias.put(r.getDestino(), distancias.get(u) + peso);
                    padres.put(r.getDestino(), u);
                    pq.add(r.getDestino());
                }
            }
        }
        return reconstruirCamino(destino, padres);
    }

    /**
     * PROCESO: Obtiene el valor numérico de una ruta según el criterio de búsqueda seleccionado.
     * ENTRADAS:
     * - r: La ruta que se está evaluando.
     * - criterio: El parámetro de optimización elegido por el usuario.
     * SALIDA: El peso (double) que se sumará al costo acumulado en el algoritmo.
     */
    public double obtenerPeso(Ruta r, String criterio) {
        return switch (criterio.toLowerCase()) {
            case "tiempo" -> r.getTiempo();
            case "distancia" -> r.getDistancia();
            case "transbordos" -> r.isRequiereTrasbordo() ? 9.0 : 1.0;
            case "costo" -> r.getCosto();
            default -> r.getDistancia();
        };
    }

    /**
     * PROCESO: Crea la lista final de paradas recorriendo el mapa de padres desde el destino al origen.
     * ENTRADAS:
     * - destino: La parada final del viaje.
     * - padres: Estructura de datos que almacena la procedencia de cada nodo óptimo.
     * SALIDA: Lista de paradas en el orden correcto de inicio a fin.
     * FLUJO DE LLAMADAS:
     * - Llama a padres.containsKey() para validar si existe una ruta trazada.
     * - Llama a padres.get() de forma iterativa para retroceder en el grafo.
     * - Llama a camino.add(0, p) para insertar los elementos al principio de la lista.
     */
    private List<Parada> reconstruirCamino(Parada destino, Map<Parada, Parada> padres) {
        List<Parada> camino = new ArrayList<>();
        if (!padres.containsKey(destino)) return camino;
        for (Parada p = destino; p != null; p = padres.get(p)) camino.add(0, p);
        return camino;
    }
}