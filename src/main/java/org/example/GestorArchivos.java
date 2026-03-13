package org.example;

import java.io.*;
import java.util.Map;
import java.util.List;

public class GestorArchivos {

    /*
       Función: guardarDatos
       Argumentos:
          GrafoTransporte grafo: El objeto que contiene la red de transporte.
          String rutaParadas: Ruta del archivo para almacenar las paradas.
          String rutaRutas: Ruta del archivo para almacenar las conexiones.
       Objetivo: Persistir la información del grafo en archivos de texto plano.
       Retorno: ninguno.
    */
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

    /*
       Función: cargarDatos
       Argumentos:
          GrafoTransporte grafo: El grafo donde se cargarán los datos.
          String rutaParadas: Ruta del archivo de origen de las paradas.
          String rutaRutas: Ruta del archivo de origen de las conexiones.
       Objetivo: Leer los archivos y reconstruir la estructura del grafo en memoria.
       Retorno: ninguno.
    */
    public static void cargarDatos(GrafoTransporte grafo, String rutaParadas, String rutaRutas) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaParadas))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] d = linea.split(",");
                Parada p = new Parada(d[0], d[1], d[2], Double.parseDouble(d[3]), Double.parseDouble(d[4]));
                grafo.agregarParada(p);
            }
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Error! Archivo de paradas vacio o desactualizado.");
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