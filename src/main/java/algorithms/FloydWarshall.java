package algorithms;

import model.Parada;
import model.Ruta;

import java.util.*;

/**
 * CLASE: FloydWarshall
 * OBJETIVO: Calcular la matriz de caminos mínimos entre todas las paradas del sistema.
 */
public class FloydWarshall {

    /**
     * PROCESO: Implementa el algoritmo de triple ciclo para encontrar el costo mínimo entre cada par de nodos.
     * ENTRADAS:
     * - listaParadas: Una lista indexada de todas las paradas (nodos) del grafo.
     * - grafo: El mapa de adyacencia que contiene las conexiones (Rutas) entre paradas.
     * - criterio: El parámetro a optimizar ("TIEMPO", "DISTANCIA" o "COSTO").
     * SALIDA: Una matriz bidimensional de doubles [n][n] con los resultados óptimos.
     */
    public static double[][] generarMatrizDistancias(List<Parada> listaParadas, Map<Parada, List<Ruta>> grafo, String criterio) {
        int n = listaParadas.size();
        double[][] matriz = new double[n][n];

        for (int i = 0; i < n; i++) {
            Arrays.fill(matriz[i], Double.MAX_VALUE);
            matriz[i][i] = 0;
        }

        for (int i = 0; i < n; i++) {
            Parada origen = listaParadas.get(i);
            List<Ruta> rutasDesdeOrigen = grafo.getOrDefault(origen, new ArrayList<>());

            for (Ruta r : rutasDesdeOrigen) {
                int j = listaParadas.indexOf(r.getDestino());
                if (j != -1) {
                    matriz[i][j] = r.getPeso(criterio);
                }
            }
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (matriz[i][k] != Double.MAX_VALUE && matriz[k][j] != Double.MAX_VALUE) {

                        if (matriz[i][k] + matriz[k][j] < matriz[i][j]) {
                            matriz[i][j] = matriz[i][k] + matriz[k][j];
                        }
                    }
                }
            }
        }
        return matriz;
    }

    /**
     * PROCESO: Imprime la matriz en consola (útil para que el profe vea que funciona).
     * ENTRADAS: La matriz generada y la lista de paradas para los nombres.
     * SALIDA: Impresión visual en la terminal.
     */
    public static void imprimirMatriz(double[][] matriz, List<Parada> listaParadas) {
        System.out.println("\n--- MATRIZ DE CAMINOS MÍNIMOS ---");
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                if (matriz[i][j] == Double.MAX_VALUE) System.out.print("INF\t");
                else System.out.printf("%.2f\t", matriz[i][j]);
            }
            System.out.println();
        }
    }
}