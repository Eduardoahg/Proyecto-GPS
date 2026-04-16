package structure;

import model.Parada;
import model.Ruta;
import java.util.*;

/**
 * PROCESO: Representa la estructura de datos principal del sistema (Grafo Dirigido).
 * Utiliza una lista de adyacencia basada en un Map para gestionar las conexiones entre paradas.
 */
public class GrafoTransporte {
    private Map<Parada, List<Ruta>> adjList = new HashMap<>();

    /**
     * PROCESO: Registra una nueva parada en el sistema de transporte.
     * ENTRADAS: Objeto Parada con sus atributos básicos.
     * FLUJO DE LLAMADAS: Utiliza putIfAbsent() para asegurar que no se dupliquen nodos existentes.
     */
    public void agregarParada(Parada parada) {
        adjList.putIfAbsent(parada, new ArrayList<>());
    }

    /**
     * PROCESO: Actualiza el nombre de una parada identificada por su ID.
     * ENTRADAS: ID de la parada y el nuevo nombre.
     * FLUJO DE LLAMADAS: Llama internamente a buscarParada() para localizar el nodo antes de modificarlo.
     */
    public void modificarParada(String id, String nuevoNombre, String nuevaLoc, double nx, double ny) {
        Parada p = buscarParada(id);
        if (p != null) {
            p.setNombre(nuevoNombre);
            p.setLocalizacion(nuevaLoc);
            p.setX(nx);
            p.setY(ny);
        }
    }

    /**
     * PROCESO: Elimina una parada y todas las rutas que lleguen o salgan de ella.
     * ENTRADAS: ID único de la parada a remover.
     * FLUJO DE LLAMADAS:
     * 1. Llama a buscarParada() para validar existencia.
     * 2. Llama a adjList.remove() para quitar el nodo de salida.
     * 3. Itera sobre todos los destinos usando removeIf() para limpiar referencias entrantes.
     */
    public void eliminarParada(String id) {
        Parada p = buscarParada(id);
        if (p != null) {
            adjList.remove(p);
            adjList.values().forEach(rutas -> rutas.removeIf(r -> r.getDestino().equals(p)));
        }
    }

    /**
     * PROCESO: Crea una conexión dirigida entre dos paradas con sus métricas correspondientes.
     * ENTRADAS: IDs de origen y destino, tiempo, distancia, costo e indicador de transbordo.
     * FLUJO DE LLAMADAS: Llama a buscarParada() dos veces para validar los extremos antes de insertar la ruta.
     */
    public void agregarRuta(String idOri, String idDest, double t, double d, double c, boolean tr) {
        Parada origen = buscarParada(idOri);
        Parada destino = buscarParada(idDest);
        if (origen != null && destino != null) {
            adjList.get(origen).add(new Ruta(destino, t, d, c, tr));
        }
    }

    /**
     * PROCESO: Desconecta dos paradas eliminando el arco que las une.
     * ENTRADAS: IDs de las paradas involucradas.
     * FLUJO DE LLAMADAS: Utiliza removeIf() sobre la lista de adyacencia del origen.
     */
    public void eliminarRuta(String idOri, String idDest) {
        Parada origen = buscarParada(idOri);
        Parada destino = buscarParada(idDest);
        if (origen != null && destino != null && adjList.containsKey(origen)) {
            adjList.get(origen).removeIf(r -> r.getDestino().equals(destino));
        }
    }

    /**
     * PROCESO: Localiza un objeto Parada en el mapa basándose en su ID o en su Nombre.
     * ENTRADAS: Cadena de texto con el criterio de búsqueda.
     * SALIDA: El objeto Parada encontrado o nulo si no existe.
     * FLUJO DE LLAMADAS: Implementa un stream() con filtros ignorando mayúsculas/minúsculas.
     */
    public Parada buscarParada(String criterio) {
        return adjList.keySet().stream()
                .filter(p -> p.getId().equalsIgnoreCase(criterio) || p.getNombre().equalsIgnoreCase(criterio))
                .findFirst().orElse(null);
    }

    /**
     * PROCESO: Provee acceso directo a la estructura interna del grafo.
     * SALIDA: El Map que contiene la lista de adyacencia.
     */
    public Map<Parada, List<Ruta>> getGrafo() {
        return adjList;
    }

    /**
     * PROCESO: Modifica las métricas de una ruta existente entre dos paradas específicas.
     * ENTRADAS: IDs de origen y destino, y los nuevos valores de tiempo, distancia y costo.
     * FLUJO DE LLAMADAS:
     * 1. Llama a buscarParada() para localizar el nodo de origen.
     * 2. Recorre la lista de adyacencia del origen para encontrar la ruta hacia el destino por su ID.
     */
    public void modificarRuta(String idOri, String idDest, double nt, double nd, double nc) {
        Parada origen = buscarParada(idOri);
        if (origen != null && adjList.containsKey(origen)) {
            for (Ruta r : adjList.get(origen)) {
                if (r.getDestino().getId().equalsIgnoreCase(idDest)) {
                    r.setTiempo(nt);
                    r.setDistancia(nd);
                    r.setCosto(nc);
                    break;
                }
            }
        }
    }

    /**
     * PROCESO: Recupera la lista de rutas salientes de una parada específica.
     * ENTRADAS: El objeto Parada del cual se desean obtener las conexiones.
     * SALIDA: Una lista de objetos Ruta; devuelve una lista vacía si la parada no existe o no tiene conexiones.
     */
    public List<Ruta> obtenerRutasDe(Parada p) {
        if (p != null && adjList.containsKey(p)) {
            return adjList.get(p);
        }
        return new ArrayList<>();
    }
}