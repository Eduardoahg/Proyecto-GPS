package algorithms;

import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;

import java.util.*;

public class AlgFloydWarshall {

    public double[][] ejecutar(GrafoTransporte grafo) {
        int V = grafo.getGrafo().size();
        double[][] dist = new double[V][V];
        List<Parada> paradas = new ArrayList<>(grafo.getGrafo().keySet());
        Map<Parada, Integer> pToIndex = new HashMap<>();

        for (int i = 0; i < V; i++) {
            pToIndex.put(paradas.get(i), i);
            Arrays.fill(dist[i], Double.MAX_VALUE);
            dist[i][i] = 0;
        }

        for (var entry : grafo.getGrafo().entrySet()) {
            int u = pToIndex.get(entry.getKey());
            for (Ruta r : entry.getValue()) {
                int v = pToIndex.get(r.getDestino());
                dist[u][v] = r.getTiempo();
            }
        }

        for (int k = 0; k < V; k++) {
            for (int i = 0; i < V; i++) {
                for (int j = 0; j < V; j++) {
                    if (dist[i][k] != Double.MAX_VALUE && dist[k][j] != Double.MAX_VALUE) {
                        dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                    }
                }
            }
        }
        return dist;
    }
}