package persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.Parada;
import model.Ruta;
import structure.GrafoTransporte;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * PROCESO: Clase encargada de la persistencia de datos. Permite guardar y cargar la configuración del grafo
 * en formato JSON para que la información no se pierda al cerrar la aplicación.
 */
public class GestorArchivos {
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * PROCESO: Convierte la estructura del grafo en una lista serializable y la escribe en un archivo físico.
     * * ENTRADAS:
     * - grafo: El sistema de transporte con todos sus datos actuales.
     * - ruta: Dirección del archivo en el disco donde se guardará la información.
     * * SALIDA: Ninguna (procedimiento de escritura).
     * * FLUJO DE LLAMADAS:
     * 1. Itera sobre el mapa del grafo usando un lambda forEach.
     * 2. Crea instancias de la clase interna EntradaGrafo para organizar los datos.
     * 3. Llama a mapper.writeValue() para realizar la conversión final a texto JSON.
     */
    public static void guardarEnJson(GrafoTransporte grafo, String ruta) {
        try {
            List<EntradaGrafo> datosSerializables = new ArrayList<>();
            grafo.getGrafo().forEach((parada, rutas) -> {
                datosSerializables.add(new EntradaGrafo(parada, rutas));
            });
            mapper.writeValue(new File(ruta), datosSerializables);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * PROCESO: Lee el archivo JSON y reconstruye el grafo en memoria, asegurando que las referencias entre paradas sean consistentes.
     * * ENTRADAS:
     * - grafo: Objeto GrafoTransporte vacío o existente que se desea poblar.
     * - ruta: Dirección del archivo JSON a leer.
     * * SALIDA: Ninguna (actualiza el estado del objeto grafo recibido).
     * * FLUJO DE LLAMADAS:
     * 1. Llama a mapper.readValue() para obtener la lista de entradas.
     * 2. Crea un masterMap temporal para indexar las paradas reales por su ID.
     * 3. Itera sobre las rutas para reemplazar los objetos "clonados" del JSON por las referencias únicas del masterMap.
     * 4. Llama a grafo.getGrafo().put() para reconstruir el mapa de adyacencia final.
     */
    public static void cargarDesdeJson(GrafoTransporte grafo, String ruta) {
        File f = new File(ruta);
        if (!f.exists()) return;

        try {
            List<EntradaGrafo> datosLeidos = mapper.readValue(f, new TypeReference<List<EntradaGrafo>>() {});
            grafo.getGrafo().clear();

            Map<String, Parada> masterMap = new HashMap<>();
            for (EntradaGrafo entrada : datosLeidos) {
                masterMap.put(entrada.getParada().getId(), entrada.getParada());
            }

            for (EntradaGrafo entrada : datosLeidos) {
                Parada realOrigen = masterMap.get(entrada.getParada().getId());
                List<Ruta> rutasReales = entrada.getRutas();

                for (Ruta r : rutasReales) {
                    Parada realDestino = masterMap.get(r.getDestino().getId());
                    if (realDestino != null) {
                        r.setDestino(realDestino);
                    }
                }
                grafo.getGrafo().put(realOrigen, rutasReales);
            }
        } catch (IOException e) {
            System.err.println("Error al leer el JSON.");
        }
    }

    /**
     * PROCESO: Clase contenedora para facilitar la serialización de la estructura Nodo-Aristas.
     */
    public static class EntradaGrafo {
        private Parada parada;
        private List<Ruta> rutas;

        public EntradaGrafo() {}

        public EntradaGrafo(Parada parada, List<Ruta> rutas) {
            this.parada = parada;
            this.rutas = rutas;
        }

        public Parada getParada() {
            return parada;
        }
        public void setParada(Parada parada) {
            this.parada = parada;
        }
        public List<Ruta> getRutas() {
            return rutas;
        }
        public void setRutas(List<Ruta> rutas) {
            this.rutas = rutas;
        }
    }
}