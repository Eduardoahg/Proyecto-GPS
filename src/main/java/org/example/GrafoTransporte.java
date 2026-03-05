package org.example;

import java.util.*;

public class GrafoTransporte {

    // Implementación del grafo mediante listas de adyacencia
    private Map<Parada, List<Ruta>> adjList;

    public GrafoTransporte() {
        this.adjList = new HashMap<>();
    }

    // ==========================================
    // GESTIÓN DE PARADAS Y RUTAS (CRUD)
    // ==========================================

    public void agregarParada(Parada parada) {
        adjList.putIfAbsent(parada, new ArrayList<>());
    }

    public void eliminarParada(String id) {
        Parada p = buscarParada(id);
        if (p != null) {
            adjList.remove(p);
            // Eliminar rutas huérfanas
            for (List<Ruta> rutas : adjList.values()) {
                rutas.removeIf(r -> r.getDestino().equals(p));
            }
        }
    }

    public void agregarRuta(String idOrigen, String idDestino, double tiempo, double distancia, double costo, boolean trasbordo) {
        Parada origen = buscarParada(idOrigen);
        Parada destino = buscarParada(idDestino);
        if (origen != null && destino != null) {
            adjList.get(origen).add(new Ruta(destino, tiempo, distancia, costo, trasbordo));
        }
    }

    // Simulación en Tiempo Real: Ajustar tiempos por tráfico [cite: 34, 44, 45]
    public void actualizarTiempoRuta(String idOrigen, String idDestino, double nuevoTiempo) {
        Parada origen = buscarParada(idOrigen);
        if (origen != null) {
            for (Ruta r : adjList.get(origen)) {
                if (r.getDestino().getId().equals(idDestino)) {
                    r.setTiempo(nuevoTiempo);
                    break;
                }
            }
        }
    }

    public Parada buscarParada(String id) {
        return adjList.keySet().stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    public Map<Parada, List<Ruta>> getGrafo() {
        return adjList;
    }

    // ==========================================
    // ALGORITMOS DE OPTIMIZACIÓN [cite: 17, 56, 115]
    // ==========================================

    // 1. DIJKSTRA: Ruta más corta por tiempo, distancia o transbordos [cite: 18, 27, 40, 118]
    // Complejidad Espacial: O(V)
    public String dijkstra(String idOrigen, String idDestino, String criterio) {
        Parada origen = buscarParada(idOrigen);
        Parada destino = buscarParada(idDestino);
        if (origen == null || destino == null) return "Paradas no válidas.";

        Map<Parada, Double> distancias = new HashMap<>();
        Map<Parada, Parada> padres = new HashMap<>();
        PriorityQueue<Parada> pq = new PriorityQueue<>(Comparator.comparingDouble(distancias::get));

        for (Parada p : adjList.keySet()) distancias.put(p, Double.MAX_VALUE);
        distancias.put(origen, 0.0);
        pq.add(origen);

        while (!pq.isEmpty()) {
            Parada u = pq.poll();
            if (u.equals(destino)) break;

            for (Ruta r : adjList.getOrDefault(u, new ArrayList<>())) {
                double peso = obtenerPeso(r, criterio);
                if (distancias.get(u) + peso < distancias.get(r.getDestino())) {
                    distancias.put(r.getDestino(), distancias.get(u) + peso);
                    padres.put(r.getDestino(), u);
                    pq.add(r.getDestino());
                }
            }
        }
        return construirStringCamino("Dijkstra (" + criterio + ")", destino, padres, distancias.get(destino));
    }

    // 2. BELLMAN-FORD: Útil si existen costos/tarifas negativas (descuentos) [cite: 19, 119]
    public String bellmanFord(String idOrigen, String idDestino) {
        Parada origen = buscarParada(idOrigen);
        Parada destino = buscarParada(idDestino);
        if (origen == null || destino == null) return "Paradas no válidas.";

        Map<Parada, Double> costos = new HashMap<>();
        Map<Parada, Parada> padres = new HashMap<>();
        for (Parada p : adjList.keySet()) costos.put(p, Double.MAX_VALUE);
        costos.put(origen, 0.0);

        int V = adjList.size();
        for (int i = 1; i < V; i++) {
            for (Map.Entry<Parada, List<Ruta>> entry : adjList.entrySet()) {
                Parada u = entry.getKey();
                if (costos.get(u) == Double.MAX_VALUE) continue;
                for (Ruta r : entry.getValue()) {
                    Parada v = r.getDestino();
                    if (costos.get(u) + r.getCosto() < costos.get(v)) {
                        costos.put(v, costos.get(u) + r.getCosto());
                        padres.put(v, u);
                    }
                }
            }
        }

        // Verificación de ciclos negativos
        for (Map.Entry<Parada, List<Ruta>> entry : adjList.entrySet()) {
            Parada u = entry.getKey();
            if (costos.get(u) == Double.MAX_VALUE) continue;
            for (Ruta r : entry.getValue()) {
                if (costos.get(u) + r.getCosto() < costos.get(r.getDestino())) {
                    return "El grafo contiene un ciclo de costo negativo.";
                }
            }
        }
        return construirStringCamino("Bellman-Ford (Costo)", destino, padres, costos.get(destino));
    }

    // 3. FLOYD-WARSHALL: Rutas más cortas entre todas las paradas [cite: 20, 120]
    public void floydWarshall() {
        int V = adjList.size();
        double[][] dist = new double[V][V];
        List<Parada> indexToParada = new ArrayList<>(adjList.keySet());
        Map<Parada, Integer> paradaToIndex = new HashMap<>();

        for (int i = 0; i < V; i++) {
            paradaToIndex.put(indexToParada.get(i), i);
            Arrays.fill(dist[i], Double.MAX_VALUE);
            dist[i][i] = 0;
        }

        for (Map.Entry<Parada, List<Ruta>> entry : adjList.entrySet()) {
            int u = paradaToIndex.get(entry.getKey());
            for (Ruta r : entry.getValue()) {
                int v = paradaToIndex.get(r.getDestino());
                dist[u][v] = r.getTiempo(); // Ejemplo priorizando tiempo
            }
        }

        for (int k = 0; k < V; k++) {
            for (int i = 0; i < V; i++) {
                for (int j = 0; j < V; j++) {
                    if (dist[i][k] != Double.MAX_VALUE && dist[k][j] != Double.MAX_VALUE && dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }
        System.out.println("Matriz Floyd-Warshall calculada (Impresión omitida por espacio).");
    }

    // 4. BÚSQUEDA DE RUTAS ALTERNATIVAS (Basado en DFS)
    public List<String> buscarRutasAlternativas(String idOrigen, String idDestino) {
        Parada origen = buscarParada(idOrigen);
        Parada destino = buscarParada(idDestino);
        List<String> caminos = new ArrayList<>();
        if (origen != null && destino != null) {
            dfsAlternativas(origen, destino, new HashSet<>(), new ArrayList<>(Arrays.asList(origen.getNombre())), caminos);
        }
        return caminos;
    }

    private void dfsAlternativas(Parada actual, Parada destino, Set<Parada> visitados, List<String> caminoActual, List<String> caminos) {
        if (actual.equals(destino)) {
            caminos.add(String.join(" -> ", caminoActual));
            return;
        }
        visitados.add(actual);
        for (Ruta r : adjList.getOrDefault(actual, new ArrayList<>())) {
            if (!visitados.contains(r.getDestino())) {
                caminoActual.add(r.getDestino().getNombre());
                dfsAlternativas(r.getDestino(), destino, visitados, caminoActual, caminos);
                caminoActual.remove(caminoActual.size() - 1); // Backtracking
            }
        }
        visitados.remove(actual);
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================
    private double obtenerPeso(Ruta r, String criterio) {
        switch (criterio.toLowerCase()) {
            case "tiempo":
                return r.getTiempo(); //[cite:27]
            case "distancia":
                return r.getDistancia(); //[cite:27]
            case "transbordos":
                return r.isRequiereTrasbordo() ? 1.0 : 0.0; //[cite:27]
            default:
                return r.getDistancia();
        }
    }

    private String construirStringCamino(String alg, Parada dest, Map<Parada, Parada> padres, double total) {
        if (!padres.containsKey(dest) && total == Double.MAX_VALUE) return alg + ": No hay ruta disponible.";
        List<String> camino = new ArrayList<>();
        for (Parada p = dest; p != null; p = padres.get(p)) camino.add(0, p.getNombre());
        return alg + " -> Camino: " + String.join(" -> ", camino) + " | Peso total: " + total;
    }
}