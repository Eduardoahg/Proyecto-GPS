package algorithms;

import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;
import java.util.ArrayList;
import java.util.List;

/**
 * PROCESO: Clase especializada en exploraciones profundas (DFS) dentro del grafo.
 */
public class AlgDFS {

    /**
     * PROCESO: Localiza todos los caminos posibles entre dos puntos mediante una búsqueda en profundidad (DFS).
     * ENTRADAS: IDs de origen y destino.
     * SALIDA: Una lista de listas de paradas.
     * FLUJO DE LLAMADAS: Llama a sistema.buscarParada() e inicia la recursión con buscarRecursivo().
     */
    public List<List<Parada>> encontrarTodosLosCaminos(GrafoTransporte sistema, String idOri, String idDest) {
        List<List<Parada>> resultados = new ArrayList<>();
        Parada origen = sistema.buscarParada(idOri);
        Parada destino = sistema.buscarParada(idDest);

        if (origen != null && destino != null) {
            buscarRecursivo(sistema, origen, destino, new ArrayList<>(), new ArrayList<>(), resultados);
        }
        return resultados;
    }

    /**
     * PROCESO: Metodo recursivo para explorar todas las ramas del grafo sin repetir nodos en el mismo camino.
     */
    private void buscarRecursivo(GrafoTransporte sistema, Parada actual, Parada destino,
                                 List<Parada> camino, List<Parada> visitados, List<List<Parada>> res) {
        visitados.add(actual);
        camino.add(actual);

        if (actual.equals(destino)) {
            res.add(new ArrayList<>(camino));
        } else {
            List<Ruta> rutas = sistema.obtenerRutasDe(actual);
            for (Ruta r : rutas) {
                if (!visitados.contains(r.getDestino())) {
                    buscarRecursivo(sistema, r.getDestino(), destino, camino, visitados, res);
                }
            }
        }
        camino.remove(camino.size() - 1);
        visitados.remove(actual);
    }

}