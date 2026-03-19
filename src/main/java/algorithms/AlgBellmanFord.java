package algorithms;

import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;

import java.util.*;

public class AlgBellmanFord {

    public String ejecutar(GrafoTransporte grafo, String idOri, String idDest) {
        Parada origen = grafo.buscarParada(idOri);
        Parada destino = grafo.buscarParada(idDest);
        if (origen == null || destino == null) return "Paradas no válidas.";

        Map<Parada, Double> costos = new HashMap<>();
        Map<Parada, Parada> padres = new HashMap<>();

        for (Parada p : grafo.getGrafo().keySet()) costos.put(p, Double.MAX_VALUE);
        costos.put(origen, 0.0);

        int V = grafo.getGrafo().size();
        for (int i = 1; i < V; i++) {
            for (var entry : grafo.getGrafo().entrySet()) {
                Parada u = entry.getKey();
                if (costos.get(u) == Double.MAX_VALUE) continue;
                for (Ruta r : entry.getValue()) {
                    if (costos.get(u) + r.getCosto() < costos.get(r.getDestino())) {
                        costos.put(r.getDestino(), costos.get(u) + r.getCosto());
                        padres.put(r.getDestino(), u);
                    }
                }
            }
        }

        // Detección de ciclos negativos
        for (var entry : grafo.getGrafo().entrySet()) {
            Parada u = entry.getKey();
            if (costos.get(u) == Double.MAX_VALUE) continue;
            for (Ruta r : entry.getValue()) {
                if (costos.get(u) + r.getCosto() < costos.get(r.getDestino())) {
                    return "El grafo contiene un ciclo de costo negativo.";
                }
            }
        }
        return formatearRuta(destino, padres, costos.get(destino));
    }

    private String formatearRuta(Parada dest, Map<Parada, Parada> padres, double total) {
        if (!padres.containsKey(dest)) return "No hay ruta disponible.";
        List<String> camino = new ArrayList<>();
        for (Parada p = dest; p != null; p = padres.get(p)) camino.add(0, p.getNombre());
        return "Ruta: " + String.join(" -> ", camino) + " | Costo: " + total;
    }
}