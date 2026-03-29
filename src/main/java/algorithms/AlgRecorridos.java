package algorithms;

import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;

import java.util.*;

public class AlgRecorridos {

    public List<Parada> ejecutarBFS(GrafoTransporte grafo, String idOri, String idDest) {
        Parada origen = grafo.buscarParada(idOri);
        Parada destino = grafo.buscarParada(idDest);
        if (origen == null || destino == null) return new ArrayList<>();

        Queue<Parada> cola = new LinkedList<>();
        Map<Parada, Parada> padres = new HashMap<>();
        Set<Parada> visitados = new HashSet<>();

        cola.add(origen);
        visitados.add(origen);

        while (!cola.isEmpty()) {
            Parada u = cola.poll();
            if (u.equals(destino)) break;

            for (Ruta r : grafo.getGrafo().getOrDefault(u, new ArrayList<>())) {
                Parada v = r.getDestino();
                if (!visitados.contains(v)) {
                    visitados.add(v);
                    padres.put(v, u);
                    cola.add(v);
                }
            }
        }
        return reconstruirCamino(destino, padres);
    }

    public List<Parada> ejecutarDFS(GrafoTransporte grafo, String idOri, String idDest) {
        Parada origen = grafo.buscarParada(idOri);
        Parada destino = grafo.buscarParada(idDest);
        if (origen == null || destino == null) return new ArrayList<>();

        Map<Parada, Parada> padres = new HashMap<>();
        Set<Parada> visitados = new HashSet<>();
        dfsRecursivo(grafo, origen, destino, visitados, padres);

        return reconstruirCamino(destino, padres);
    }

    private boolean dfsRecursivo(GrafoTransporte grafo, Parada actual, Parada destino, Set<Parada> visitados, Map<Parada, Parada> padres) {
        visitados.add(actual);
        if (actual.equals(destino)) return true;

        for (Ruta r : grafo.getGrafo().getOrDefault(actual, new ArrayList<>())) {
            Parada v = r.getDestino();
            if (!visitados.contains(v)) {
                padres.put(v, actual);
                if (dfsRecursivo(grafo, v, destino, visitados, padres)) {
                    return true;
                }
            }
        }
        return false;
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