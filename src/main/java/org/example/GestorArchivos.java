package org.example;

import java.io.*;
import java.util.Map;
import java.util.List;

public class GestorArchivos {
    public static void guardarDatos(GrafoTransporte grafo, String rutaParadas, String rutaRutas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaParadas))) {
            for (Parada p : grafo.getGrafo().keySet()) {
                pw.println(p.getId() + "," + p.getNombre() + "," + p.getLocalizacion() + "," + p.getX() + "," + p.getY());
            }
        } catch (IOException e) {
            System.out.println("Error guardando paradas: " + e.getMessage());
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaRutas))) {
            for (Map.Entry<Parada, List<Ruta>> entry : grafo.getGrafo().entrySet()) {
                String idOri = entry.getKey().getId();
                for (Ruta r : entry.getValue()) {
                    pw.println(idOri + "," + r.getDestino().getId() + "," +
                            r.getTiempo() + "," + r.getDistancia() + "," +
                            r.getCosto() + "," + r.isRequiereTrasbordo());
                }
            }
        } catch (IOException e) {
            System.out.println("Error guardando rutas: " + e.getMessage());
        }
    }

    public static void cargarDatos(GrafoTransporte grafo, String rutaParadas, String rutaRutas) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaParadas))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] d = linea.split(",");
                Parada p = new Parada(d[0], d[1], d[2], Double.parseDouble(d[3]), Double.parseDouble(d[4]));
                grafo.agregarParada(p);
            }
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Archivo de paradas vacío o desactualizado.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(rutaRutas))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] d = linea.split(",");
                grafo.agregarRuta(d[0], d[1], Double.parseDouble(d[2]), Double.parseDouble(d[3]),
                        Double.parseDouble(d[4]), Boolean.parseBoolean(d[5]));
            }
        } catch (IOException e) {
            System.out.println("ERROR!");
            System.out.println("No se encontro archivo de rutas");

        }
    }
}