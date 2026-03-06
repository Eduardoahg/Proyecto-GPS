package org.example;

import java.util.List;

public class MainBackend {
    public static void main(String[] args) {
        GrafoTransporte sistema = new GrafoTransporte();

        // 1. Población de Datos Iniciales
        sistema.agregarParada(new Parada("A", "Estación Central", "Centro Ciudad", 100.0, 300.0));
        sistema.agregarParada(new Parada("B", "Plaza Norte", "Zona Norte", 400.0, 100.0));
        sistema.agregarParada(new Parada("C", "Terminal Sur", "Zona Sur", 400.0, 500.0));
        sistema.agregarParada(new Parada("D", "Aeropuerto", "Afueras", 700.0, 300.0));

        // 2. Definición de Rutas
        sistema.agregarRuta("A", "B", 15.0, 5.0, 30.0, false);
        sistema.agregarRuta("A", "C", 20.0, 8.0, 25.0, false);
        sistema.agregarRuta("B", "D", 25.0, 15.0, 50.0, true);
        sistema.agregarRuta("C", "D", 10.0, 12.0, 40.0, false);
        sistema.agregarRuta("A", "D", 60.0, 20.0, 100.0, false);
        System.out.println("=== PRUEBAS DEL SISTEMA DE RUTAS BACKEND ===");

        // 2. Pruebas de Algoritmos (Dijkstra) [cite: 18, 27, 40]
        System.out.println("\n--- Dijkstra de Estación Central (A) a Aeropuerto (D) ---");
        System.out.println(sistema.dijkstra("A", "D", "tiempo")); // Debe ir por C -> D
        System.out.println(sistema.dijkstra("A", "D", "distancia"));
        System.out.println(sistema.dijkstra("A", "D", "transbordos"));

        // 3. Pruebas de Algoritmos (Dijkstra) [cite: 18, 27, 40, 118, 141]
        System.out.println("\n--- Dijkstra de Estación Central (A) a Aeropuerto (D) ---");
        System.out.println(sistema.dijkstra("A", "D", "tiempo"));
        System.out.println(sistema.dijkstra("A", "D", "distancia"));
        System.out.println(sistema.dijkstra("A", "D", "transbordos"));

        // 4. Rutas Alternativas (DFS) [cite: 28, 117]
        System.out.println("\n--- Rutas Alternativas de A hacia D ---");
        List<String> alternativas = sistema.buscarRutasAlternativas("A", "D");
        alternativas.forEach(System.out::println);

        // 5. Simulación en Tiempo Real (Tráfico) [cite: 34, 44, 45]
        System.out.println("\n--- Simulando Tráfico Pesado en ruta C -> D ---");
        sistema.actualizarTiempoRuta("C", "D", 50.0);
        System.out.println("Nuevo cálculo óptimo por tiempo:");
        System.out.println(sistema.dijkstra("A", "D", "tiempo"));

        // 6. Persistencia (VITAL para que AppTransporte vea los datos) [cite: 59, 143, 144]
        System.out.println("\n--- Guardando Datos en Archivos Locales ---");
        GestorArchivos.guardarDatos(sistema, "paradas.csv", "rutas.csv");
        System.out.println("Datos guardados con éxito.");
    }
}