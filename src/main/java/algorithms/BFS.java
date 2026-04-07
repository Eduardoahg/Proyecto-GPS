package algorithms;

import model.Parada;
import model.Ruta;

import java.util.*;

/**
 * CLASE: BFS
 * OBJETIVO: Encontrar la ruta con la menor cantidad de conexiones (transbordos).
 */

public class BFS {

    /**
     * PROCESO: Explora el grafo nivel por nivel para encontrar el camino más corto en términos de saltos.
     * ENTRADAS:
     * - grafo: El mapa de conexiones del sistema.
     * - inicio: La parada de origen.
     * - destino: La parada a la que se desea llegar.
     * SALIDA: Una lista de paradas que representan el camino con menos estaciones intermedias.
     */
    public static List<Parada> calcularRutaMinimosSaltos(Map<Parada, List<Ruta>> grafo, Parada inicio, Parada destino) {
        Queue<Parada> cola = new LinkedList<>();
        Map<Parada, Parada> rastroPadres = new HashMap<>();
        Set<Parada> visitados = new HashSet<>();

        cola.add(inicio);
        visitados.add(inicio);

        while (!cola.isEmpty()) {
            Parada actual = cola.poll();

            if (actual.equals(destino)) {
                return reconstruirCamino(rastroPadres, destino);
            }

            for (Ruta conexion : grafo.getOrDefault(actual, new ArrayList<>())) {
                Parada vecino = conexion.getDestino();
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    rastroPadres.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * PROCESO: Rastrea hacia atrás desde el destino usando el mapa de padres para armar la ruta final.
     * ENTRADAS:
     * - padres: Mapa que relaciona cada nodo con el nodo que lo descubrió.
     * - destino: El nodo final del trayecto.
     * SALIDA: Lista ordenada de paradas desde el inicio hasta el destino.
     */
    private static List<Parada> reconstruirCamino(Map<Parada, Parada> padres, Parada destino) {
        LinkedList<Parada> camino = new LinkedList<>();
        for (Parada at = destino; at != null; at = padres.get(at)) {
            camino.addFirst(at);
        }
        return camino;
    }
}