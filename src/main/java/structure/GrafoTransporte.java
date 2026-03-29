package structure;

import model.Parada;
import model.Ruta;

import java.util.*;

public class GrafoTransporte {
    private Map<Parada, List<Ruta>> adjList = new HashMap<>();

    public void agregarParada(Parada parada) {
        adjList.putIfAbsent(parada, new ArrayList<>());
    }

    public void modificarParada(String id, String nuevoNombre) {
        Parada p = buscarParada(id);
        if (p != null) {
            p.setNombre(nuevoNombre);
        }
    }

    public void eliminarParada(String id) {
        Parada p = buscarParada(id);
        if (p != null) {
            adjList.remove(p);
            adjList.values().forEach(rutas -> rutas.removeIf(r -> r.getDestino().equals(p)));
        }
    }

    public void agregarRuta(String idOri, String idDest, double t, double d, double c, boolean tr) {
        Parada origen = buscarParada(idOri);
        Parada destino = buscarParada(idDest);
        if (origen != null && destino != null) {
            adjList.get(origen).add(new Ruta(destino, t, d, c, tr));
        }
    }

    public void eliminarRuta(String idOri, String idDest) {
        Parada origen = buscarParada(idOri);
        Parada destino = buscarParada(idDest);
        if (origen != null && destino != null && adjList.containsKey(origen)) {
            adjList.get(origen).removeIf(r -> r.getDestino().equals(destino));
        }
    }

    public Parada buscarParada(String criterio) {
        return adjList.keySet().stream()
                .filter(p -> p.getId().equalsIgnoreCase(criterio) || p.getNombre().equalsIgnoreCase(criterio))
                .findFirst().orElse(null);
    }

    public Map<Parada, List<Ruta>> getGrafo() {
        return adjList;
    }
}