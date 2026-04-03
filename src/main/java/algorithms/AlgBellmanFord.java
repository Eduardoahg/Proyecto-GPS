package algorithms;

import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;
import java.util.*;

public class AlgBellmanFord {
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

    private List<Parada> reconstruirCamino(Parada destino, Map<Parada, Parada> padres) {
        List<Parada> camino = new ArrayList<>();
        if (!padres.containsKey(destino)) return camino;
        for (Parada p = destino; p != null; p = padres.get(p)) camino.add(0, p);
        return camino;
    }
}