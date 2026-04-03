package algorithms;

import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;
import java.util.*;

public class AlgDijkstra {
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

    public double obtenerPeso(Ruta r, String criterio) {
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
        for (Parada p = destino; p != null; p = padres.get(p)) camino.add(0, p);
        return camino;
    }
}