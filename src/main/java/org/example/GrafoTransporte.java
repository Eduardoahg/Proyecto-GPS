package org.example;

import java.util.*;

/*
   CLASE: GrafoTransporte
   Argumentos: ninguno.
   Objetivo: Constructor para inicializar el mapa de adyacencia del grafo.
   Retorno: instancia de GrafoTransporte.
*/
public class GrafoTransporte {

    private Map<Parada, List<Ruta>> adjList;

    public GrafoTransporte() {
        this.adjList = new HashMap<>();
    }

    /*
       Función: agregarParada
       Argumentos:
          Parada parada: Objeto parada a incluir en el grafo.
       Objetivo: Añadir un nuevo nodo al grafo si no existe previamente.
       Retorno: ninguno.
    */
    public void agregarParada(Parada parada) {
        adjList.putIfAbsent(parada, new ArrayList<>());
    }

    /*
       Función: eliminarParada
       Argumentos:
          String id: Identificador único de la parada a borrar.
       Objetivo: Eliminar una parada del grafo y todas las rutas que apunten a ella.
       Retorno: ninguno.
    */
    public void eliminarParada(String id) {
        Parada p = buscarParada(id);
        if (p != null) {
            adjList.remove(p);
            for (List<Ruta> rutas : adjList.values()) {
                rutas.removeIf(r -> r.getDestino().equals(p));
            }
        }
    }

    /*
       Función: agregarRuta
       Argumentos:
          String idOrigen: ID de la parada inicial.
          String idDestino: ID de la parada final.
          double tiempo: Duración del trayecto.
          double distancia: Longitud de la ruta.
          double costo: Precio del viaje.
          boolean trasbordo: Indica si requiere cambio de vehículo.
       Objetivo: Crear una conexión dirigida entre dos paradas con sus atributos.
       Retorno: ninguno.
    */
    public void agregarRuta(String idOrigen, String idDestino, double tiempo, double distancia, double costo, boolean trasbordo) {
        Parada origen = buscarParada(idOrigen);
        Parada destino = buscarParada(idDestino);
        if (origen != null && destino != null) {
            adjList.get(origen).add(new Ruta(destino, tiempo, distancia, costo, trasbordo));
        }
    }

    /*
       Función: modificarParada
       Argumentos:
          String id: ID de la parada a modificar.
          String nuevoNombre: El nombre actualizado.
       Objetivo: Cambiar el nombre de una parada existente.
       Retorno: ninguno.
    */
    public void modificarParada(String id, String nuevoNombre) {
        Parada p = buscarParada(id);
        if (p != null) {
            p.setNombre(nuevoNombre);
        }
    }

    /*
       Función: actualizarTiempoRuta
       Argumentos:
          String idOrigen: ID del origen.
          String idDestino: ID del destino.
          double nuevoTiempo: El nuevo valor de tiempo.
       Objetivo: Actualizar el tiempo de una ruta específica entre dos paradas.
       Retorno: ninguno.
    */
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

    /*
       Función: buscarParada
       Argumentos:
          String criterio: ID o nombre de la parada a buscar.
       Objetivo: Localizar una parada en el grafo por sus atributos.
       Retorno: Objeto Parada o null si no se encuentra.
    */
    public Parada buscarParada(String criterio) {
        return adjList.keySet().stream()
                .filter(p -> p.getId().equalsIgnoreCase(criterio) || p.getNombre().equalsIgnoreCase(criterio))
                .findFirst()
                .orElse(null);
    }

    /*
       Función: getGrafo
       Argumentos: ninguno.
       Objetivo: Obtener la estructura completa del mapa de adyacencia.
       Retorno: Map que representa el grafo.
    */
    public Map<Parada, List<Ruta>> getGrafo() {
        return adjList;
    }

    /*
       Función: calcularRutaDijkstra
       Argumentos:
          String idOri: ID origen.
          String idDest: ID destino.
          String criterio: Atributo a optimizar (tiempo, costo, etc).
       Objetivo: Calcular el camino más corto utilizando el algoritmo de Dijkstra.
       Retorno: Lista de paradas que forman el camino óptimo.
    */
    public List<Parada> calcularRutaDijkstra(String idOri, String idDest, String criterio) {
        Parada origen = buscarParada(idOri);
        Parada destino = buscarParada(idDest);
        if (origen == null || destino == null) return new ArrayList<>();

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
        List<Parada> camino = new ArrayList<>();
        if (!padres.containsKey(destino) && !origen.equals(destino)) return camino;
        for (Parada p = destino; p != null; p = padres.get(p)) camino.add(0, p);
        return camino;
    }

    /*
       Función: obtenerCaminosAlternativos
       Argumentos:
          String idOri: ID origen.
          String idDest: ID destino.
       Objetivo: Encontrar múltiples rutas posibles entre dos puntos.
       Retorno: Lista de listas de paradas.
    */
    public List<List<Parada>> obtenerCaminosAlternativos(String idOri, String idDest) {
        Parada origen = buscarParada(idOri);
        Parada destino = buscarParada(idDest);
        List<List<Parada>> todosLosCaminos = new ArrayList<>();
        if (origen != null && destino != null) {
            dfsTodosLosCaminos(origen, destino, new HashSet<>(), new ArrayList<>(List.of(origen)), todosLosCaminos);
        }
        return todosLosCaminos;
    }

    /*
       Función: dfsTodosLosCaminos
       Argumentos:
          Parada actual: Nodo donde se encuentra la búsqueda.
          Parada destino: Nodo final.
          Set visitados: Conjunto de nodos ya explorados.
          List caminoActual: Ruta que se está construyendo.
          List resultados: Acumulador de rutas encontradas.
       Objetivo: Realizar una búsqueda en profundidad para hallar todos los caminos.
       Retorno: ninguno.
    */
    private void dfsTodosLosCaminos(Parada actual, Parada destino, Set<Parada> visitados, List<Parada> caminoActual, List<List<Parada>> resultados) {
        if (resultados.size() > 15) return;
        if (actual.equals(destino)) {
            resultados.add(new ArrayList<>(caminoActual));
            return;
        }
        visitados.add(actual);
        for (Ruta r : adjList.getOrDefault(actual, new ArrayList<>())) {
            if (!visitados.contains(r.getDestino())) {
                caminoActual.add(r.getDestino());
                dfsTodosLosCaminos(r.getDestino(), destino, visitados, caminoActual, resultados);
                caminoActual.remove(caminoActual.size() - 1);
            }
        }
        visitados.remove(actual);
    }

    /*
       Función: bellmanFord
       Argumentos:
          String idOrigen: ID origen.
          String idDestino: ID destino.
       Objetivo: Encontrar la ruta de costo mínimo permitiendo pesos negativos.
       Retorno: String con el resultado del camino o error de ciclos negativos.
    */
    public String bellmanFord(String idOrigen, String idDestino) {
        Parada origen = buscarParada(idOrigen);
        Parada destino = buscarParada(idDestino);
        if (origen == null || destino == null) return "Paradas no validas.";

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

        for (Map.Entry<Parada, List<Ruta>> entry : adjList.entrySet()) {
            Parada u = entry.getKey();
            if (costos.get(u) == Double.MAX_VALUE) continue;
            for (Ruta r : entry.getValue()) {
                if (costos.get(u) + r.getCosto() < costos.get(r.getDestino())) {
                    return "El grafo contiene uno costo negativo.";
                }
            }
        }
        return construirStringCamino("Bellman-Ford (Costo)", destino, padres, costos.get(destino));
    }

    /*
       Función: floydWarshall
       Argumentos: ninguno.
       Objetivo: Calcular todos los caminos más cortos entre todos los pares de nodos.
       Retorno: ninguno.
    */
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
                dist[u][v] = r.getTiempo();
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
    }

    /*
       Función: obtenerPeso
       Argumentos:
          Ruta r: La ruta a evaluar.
          String criterio: Tipo de peso a extraer.
       Objetivo: Determinar el valor numérico del peso basado en un criterio.
       Retorno: double con el peso.
    */
    private double obtenerPeso(Ruta r, String criterio) {
        switch (criterio.toLowerCase()) {
            case "tiempo":
                return r.getTiempo();
            case "distancia":
                return r.getDistancia();
            case "transbordos":
                return r.isRequiereTrasbordo() ? 9.0 : 1.0;
            case "costo":
                return r.getCosto();
            default:
                return r.getDistancia();
        }
    }

    /*
            METODOS AUXILIARES
     */

    /*
       Función: calcularPesoTotalCamino
       Argumentos:
          List camino: Lista de paradas recorridas.
          String criterio: Atributo a sumar.
       Objetivo: Sumar el peso total de una ruta completa.
       Retorno: double con el acumulado.
    */
    public double calcularPesoTotalCamino(List<Parada> camino, String criterio) {
        double total = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            Parada actual = camino.get(i);
            Parada siguiente = camino.get(i + 1);
            List<Ruta> rutas = adjList.get(actual);
            for (Ruta r : rutas) {
                if (r.getDestino().equals(siguiente)) {
                    if (criterio.equalsIgnoreCase("transbordos")) {
                        if (r.isRequiereTrasbordo()) total += 1;
                    } else {
                        total += obtenerPeso(r, criterio);
                    }
                    break;
                }
            }
        }
        return total;
    }

    /*
       Función: construirStringCamino
       Argumentos:
          String alg: Nombre del algoritmo usado.
          Parada dest: Parada de destino.
          Map padres: Mapeo de predecesores.
          double total: Peso acumulado.
       Objetivo: Formatear el resultado del camino en una cadena legible.
       Retorno: String con la ruta formateada.
    */
    private String construirStringCamino(String alg, Parada dest, Map<Parada, Parada> padres, double total) {
        if (!padres.containsKey(dest) && total == Double.MAX_VALUE) return alg + ": No hay ruta disponible.";
        List<String> camino = new ArrayList<>();
        for (Parada p = dest; p != null; p = padres.get(p)) camino.add(0, p.getNombre());
        return alg + " -> Camino: " + String.join(" -> ", camino) + " | Peso total: " + total;
    }
}