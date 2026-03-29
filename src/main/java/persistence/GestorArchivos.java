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

public class GestorArchivos {
    // Configuramos Jackson para que soporte objetos complejos como llaves de mapa
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void guardarEnJson(GrafoTransporte grafo, String ruta) {
        try {
            // En lugar de guardar un Map<Parada, List<Ruta>>,
            // guardamos una Lista de objetos "EntradaGrafo" que es 100% compatible con JSON
            List<EntradaGrafo> datosSerializables = new ArrayList<>();
            grafo.getGrafo().forEach((parada, rutas) -> {
                datosSerializables.add(new EntradaGrafo(parada, rutas));
            });

            mapper.writeValue(new File(ruta), datosSerializables);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void cargarDesdeJson(GrafoTransporte grafo, String ruta) {
        File f = new File(ruta);
        if (!f.exists()) return; // Si no existe, no hacemos nada

        try {
            // Leemos la lista de entradas
            List<EntradaGrafo> datosLeidos = mapper.readValue(f, new TypeReference<List<EntradaGrafo>>() {
            });

            grafo.getGrafo().clear();
            // Reconstruimos el Mapa
            for (EntradaGrafo entrada : datosLeidos) {
                grafo.getGrafo().put(entrada.getParada(), entrada.getRutas());
            }
        } catch (IOException e) {
            System.err.println("Error al leer el JSON. Iniciando con mapa vacío.");
        }
    }

    // --- CLASE AUXILIAR PARA JACKSON ---
    // Esta clase envuelve la Parada y sus Rutas para que Jackson no se confunda
    public static class EntradaGrafo {
        private Parada parada;
        private List<Ruta> rutas;

        public EntradaGrafo() {
        } // Para Jackson

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