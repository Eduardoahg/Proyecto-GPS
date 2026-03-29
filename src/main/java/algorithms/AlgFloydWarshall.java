package algorithms;

import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;

import java.util.*;

public class AlgFloydWarshall {

    public List<Parada> ejecutar(GrafoTransporte grafo, String idOri, String idDest, String criterio) {
        Parada origen = grafo.buscarParada(idOri);
        Parada destino = grafo.buscarParada(idDest);
        if (origen == null || destino == null) return new ArrayList<>();

        int V = grafo.getGrafo().size();
        double[][] dist = new double[V][V];
        int[][] next = new int[V][V];
        List<Parada> paradas = new ArrayList<>(grafo.getGrafo().keySet());
        Map<Parada, Integer> pToIndex = new HashMap<>();

        for (int i = 0; i < V; i++) {
            pToIndex.put(paradas.get(i), i);
            Arrays.fill(dist[i], Double.MAX_VALUE);
            Arrays.fill(next[i], -1);
            dist[i][i] = 0;
            next[i][i] = i;
        }

        for (var entry : grafo.getGrafo().entrySet()) {
            int u = pToIndex.get(entry.getKey());
            for (Ruta r : entry.getValue()) {
                int v = pToIndex.get(r.getDestino());
                dist[u][v] = obtenerPeso(r, criterio);
                next[u][v] = v;
            }
        }

        for (int k = 0; k < V; k++) {
            for (int i = 0; i < V; i++) {
                for (int j = 0; j < V; j++) {
                    if (dist[i][k] != Double.MAX_VALUE && dist[k][j] != Double.MAX_VALUE) {
                        if (dist[i][k] + dist[k][j] < dist[i][j]) {
                            dist[i][j] = dist[i][k] + dist[k][j];
                            next[i][j] = next[i][k];
                        }
                    }
                }
            }
        }

        int u = pToIndex.get(origen);
        int v = pToIndex.get(destino);
        if (next[u][v] == -1) return new ArrayList<>();

        List<Parada> camino = new ArrayList<>();
        camino.add(paradas.get(u));
        while (u != v) {
            u = next[u][v];
            camino.add(paradas.get(u));
        }
        return camino;
    }

    private double obtenerPeso(Ruta r, String criterio) {
        return switch (criterio.toLowerCase()) {
            case "tiempo" -> r.getTiempo();
            case "distancia" -> r.getDistancia();
            case "transbordos" -> r.isRequiereTrasbordo() ? 9.0 : 1.0;
            case "costo" -> r.getCosto();
            default -> r.getDistancia();
        };
    }
}