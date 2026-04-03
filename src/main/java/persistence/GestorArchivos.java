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
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

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