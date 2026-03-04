package org.example;
import java.util.*;

public class GrafoTransporte {

    // Lista de adyacencia, mapea una parada con la lista de rutas que salen de ella
    private Map<Parada, List<Ruta>> listaAdyacencia;

    public GrafoTransporte() {
        this.listaAdyacencia = new HashMap<>();
    }

    // METODOS PARA PARADAS

    public void agregarParada(Parada parada) {
        listaAdyacencia.putIfAbsent(parada, new ArrayList<>());
    }

    public void eliminarParada(Parada parada) {
        //Eliminar la parada como origen (su entrada en el mapa)
        listaAdyacencia.remove(parada);

        //Eliminar cualquier ruta que tuviera a esta parada como destino en el resto del grafo
        for (List<Ruta> rutas : listaAdyacencia.values()) {
            rutas.removeIf(ruta -> ruta.getDestino().equals(parada));
        }
    }

    public void modificarParada(String idParadaVieja, String nuevoNombre) {
        for (Parada p : listaAdyacencia.keySet()) {
            if (p.getId().equals(idParadaVieja)) {
                p.setNombre(nuevoNombre);
                return;
            }
        }
        System.out.println("Parada no encontrada.");
    }


    // METODOS PARA RUTAS

    public void agregarRuta(Parada origen, Parada destino, double tiempo, double distancia, double costo, boolean trasbordo) {
        // Nos aseguramos de que ambas paradas existan en el grafo
        agregarParada(origen);
        agregarParada(destino);

        Ruta nuevaRuta = new Ruta(destino, tiempo, distancia, costo, trasbordo);
        listaAdyacencia.get(origen).add(nuevaRuta);
    }

    public void eliminarRuta(Parada origen, Parada destino) {
        List<Ruta> rutas = listaAdyacencia.get(origen);
        if (rutas != null) {
            rutas.removeIf(ruta -> ruta.getDestino().equals(destino));
        }
    }

    public void modificarRuta(Parada origen, Parada destino, double nuevoTiempo, double nuevaDistancia, double nuevoCosto, boolean nuevoTrasbordo) {
        List<Ruta> rutas = listaAdyacencia.get(origen);
        if (rutas != null) {
            for (Ruta ruta : rutas) {
                if (ruta.getDestino().equals(destino)) {
                    ruta.setTiempo(nuevoTiempo);
                    ruta.setDistancia(nuevaDistancia);
                    ruta.setCosto(nuevoCosto);
                    ruta.setRequiereTrasbordo(nuevoTrasbordo);
                    return;
                }
            }
        }
        System.out.println("Ruta no encontrada.");
    }


    // METODO PARA VER EL GRAFO

    public void imprimirGrafo() {
        for (Map.Entry<Parada, List<Ruta>> entry : listaAdyacencia.entrySet()) {
            System.out.println("Parada Origen: " + entry.getKey().toString());
            for (Ruta ruta : entry.getValue()) {
                System.out.println("  -> " + ruta.toString());
            }
        }
    }
}