package org.example;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        GrafoTransporte sistema = new GrafoTransporte();

        System.out.println("=== PRUEBA DE ESTRÉS: SISTEMA DE GESTIÓN DE RUTAS (40%) ===");

        //Datos Iniciales
        Parada p1 = new Parada("P01", "Parada La Mora");
        Parada p2 = new Parada("P02", "Parada Los Halamos");
        Parada p3 = new Parada("P03", "Parada San Juan");
        Parada p4 = new Parada("P04", "Parada La Vieja");

        sistema.agregarParada(p1);
        sistema.agregarParada(p2);
        sistema.agregarParada(p3);
        sistema.agregarParada(p4);

        // Creacion de la red para probar lo que la lista de adyacnecia
        // PRA-MA -> PRA-HL, PRA-MA -> PRA-SJ, PRA-MA -> PRA-VJ  (Una parada con muchos destinos)
        sistema.agregarRuta(p1, p2, 10.0, 5.0, 25.0, false);
        sistema.agregarRuta(p1, p3, 15.0, 7.5, 30.0, true);
        sistema.agregarRuta(p1, p4, 25.0, 12.0, 50.0, false);

        // Conexiones de retorno y transversales
        sistema.agregarRuta(p2, p1, 12.0, 5.0, 25.0, false);
        sistema.agregarRuta(p3, p4, 10.0, 4.0, 20.0, false);

        System.out.println("\n ESTADO 1: Red creada ");
        sistema.imprimirGrafo();

        // Pruebas de las modificaciones y validaciones [cite: 16, 17]
        System.out.println("\n ESTADO 2: Pruebas de Modificación");

        // Modificacion exitosa
        System.out.println("Modificando P02 ...");
        sistema.modificarParada("P02", "Parada La Herradura");
        System.out.println("Modificando P01 ...");
        sistema.modificarParada("P01", "Parada Las Palmas");

        // Validacion de que no se pueda modificar una parada que no existe
        System.out.println("Intentando modificar parada 'P05' (No existe):");
        sistema.modificarParada("P05", "Parada que no existe");
        System.out.println("Intentando modificar parada 'P06' (No existe):");
        sistema.modificarParada("P06", "Parada que no existe");

        // Eliminacion de dependencias [cite: 11, 45]
        // Si eliminamos P04 se deberia:
        // Borrar la entrada de P04 en el mapa.
        // Borrar la ruta P01 -> P04 y P03 -> P04 .
        System.out.println("\n ESTADO 3: Eliminando 'Parada La Vieja' (P04) y verificando rutas huerfanas ");
        sistema.eliminarParada(p4);

        sistema.imprimirGrafo();

        // Prueba de gestiond e rutas [cite: 15, 58]
        System.out.println("\n ESTADO 4: Eliminacion y Modificación de Rutas ");

        // Eliminar una ruta especifica sin borrar la parada
        System.out.println("Eliminando ruta específica: Parada La Mora -> Parada San Juan");
        sistema.eliminarRuta(p1, p3);

        // Modificar una ruta existente
        System.out.println("Actualizando costos de ruta: Parada La Herradura -> Parada La Mora");
        sistema.modificarRuta(p2, p1, 14.0, 5.0, 45.0, true);

        System.out.println("\n Resultado final");
        sistema.imprimirGrafo();
    }
}