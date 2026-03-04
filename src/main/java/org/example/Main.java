package org.example;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        GrafoTransporte sistema = new GrafoTransporte();

        // 1. Creación de Paradas (Nodos) [cite: 10, 16]
        Parada p1 = new Parada("ESCT", "Estacion Central");
        Parada p2 = new Parada("PAGB", "Parada Gurabo");
        Parada p3 = new Parada("CTHO", "Centro Histórico");

        // 2. Agregar Paradas al sistema
        sistema.agregarParada(p1);
        sistema.agregarParada(p2);
        sistema.agregarParada(p3);

        // 3. Agregar Rutas (Aristas) con sus atributos [cite: 9, 10, 11]
        // Ruta: p1 -> p2 (Distancia: 5km, Tiempo: 10min, Costo: 25.0, Sin trasbordo)
        sistema.agregarRuta(p1, p2, 10.0, 5.0, 25.0, false);

        // Ruta: p2 -> p3 (Distancia: 3km, Tiempo: 15min, Costo: 35.0, Con trasbordo)
        sistema.agregarRuta(p2, p3, 15.0, 3.0, 35.0, true);

        System.out.println("--- Red de Transporte Inicial ---");
        sistema.imprimirGrafo();

        // 4. Modificación de Datos (Hito de Gestión 20%) [cite: 11, 15, 16]
        System.out.println("\n--- Modificando nombre de Parada S02 y tiempo de Ruta p1->p2 ---");
        sistema.modificarParada("S02", "Terminal Norte Principal");
        sistema.modificarRuta(p1, p2, 12.0, 5.0, 25.0, false); // Aumentó el tráfico (12 min)

        // 5. Verificación de eliminación
        System.out.println("\n--- Eliminando Parada Centro Histórico (S03) ---");
        sistema.eliminarParada(p3);

        // Mostrar estado final para verificar consistencia
        System.out.println("\n--- Estado Final del Sistema ---");
        sistema.imprimirGrafo();
    }
}