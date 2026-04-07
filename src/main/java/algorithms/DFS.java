package algorithms;

import model.Parada;
import model.Ruta;

import java.util.*;


/**
 * CLASE: DFS
 * OBJETIVO: Explorar rutas exhaustivamente y validar la conectividad de la red.
 */

public class DFS {


    /**
     * PROCESO: Utiliza recursividad para navegar por las ramas del grafo hasta encontrar el destino.
     * ENTRADAS:
     * - grafo: Estructura de datos que contiene las paradas y rutas.
     * - actual: El nodo donde se encuentra la exploración en este momento.
     * - destino: El nodo objetivo.
     * - visitados: Conjunto de paradas ya procesadas para evitar ciclos infinitos.
     * SALIDA: Valor booleano (true si existe un camino, false si no).
     */

    public static boolean hayConexionEntre(Map<Parada, List<Ruta>> grafo, Parada actual, Parada destino, Set<Parada> visitados) {
        // Caso base: llegamos al destino
        if (actual.equals(destino)) return true;

        visitados.add(actual);

        for (Ruta conexion : grafo.getOrDefault(actual, new ArrayList<>())) {
            Parada vecino = conexion.getDestino();
            if (!visitados.contains(vecino)) {
                if (hayConexionEntre(grafo, vecino, destino, visitados)) {
                    return true;
                }
            }
        }
        return false; // No se encontró camino por esta rama
    }
}