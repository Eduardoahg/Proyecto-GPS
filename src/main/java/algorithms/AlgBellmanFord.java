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

        Map<Parada, Double> distancias = new HashMap<>();
        Map<Parada, Parada> padres = new HashMap<>();

        for (Parada p : grafo.getGrafo().keySet()) distancias.put(p, Double.MAX_VALUE);
        distancias.put(origen, 0.0);

        int V = grafo.getGrafo().size();
        for (int i = 1; i < V; i++) {
            for (var entry : grafo.getGrafo().entrySet()) {
                Parada u = entry.getKey();
                if (distancias.get(u) == Double.MAX_VALUE) continue;
                for (Ruta r : entry.getValue()) {
                    double peso = obtenerPeso(r, criterio);
                    if (distancias.get(u) + peso < distancias.get(r.getDestino())) {
                        distancias.put(r.getDestino(), distancias.get(u) + peso);
                        padres.put(r.getDestino(), u);
                    }
                }
            }
        }

        return reconstruirCamino(destino, padres);
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

    private List<Parada> reconstruirCamino(Parada destino, Map<Parada, Parada> padres) {
        List<Parada> camino = new ArrayList<>();
        if (!padres.containsKey(destino)) return camino;
        for (Parada p = destino; p != null; p = padres.get(p)) {
            camino.add(0, p);
        }
        return camino;
    }
}