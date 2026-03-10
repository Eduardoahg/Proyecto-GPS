package org.example;

public class MainBackend {
    public static void main(String[] args) {
        GrafoTransporte sistema = new GrafoTransporte();

        sistema.agregarParada(new Parada("A", "Estacion Central", "Centro", 100.0, 300.0));
        sistema.agregarParada(new Parada("B", "Plaza Norte", "Norte", 400.0, 100.0));
        sistema.agregarParada(new Parada("C", "Terminal Sur", "Sur", 400.0, 500.0));
        sistema.agregarParada(new Parada("D", "Aeropuerto", "Este", 700.0, 300.0));
        sistema.agregarParada(new Parada("E", "Parque Independencia", "Oeste", 250.0, 200.0));
        sistema.agregarParada(new Parada("F", "Villa Mella", "Norte Extremo", 250.0, 400.0));
        sistema.agregarParada(new Parada("G", "Zona Colonial", "Centro Historico", 550.0, 200.0));
        sistema.agregarParada(new Parada("H", "Malecon", "Costa", 550.0, 400.0));
        sistema.agregarParada(new Parada("I", "Piantini", "Centro Comercial", 400.0, 300.0));
        sistema.agregarParada(new Parada("J", "Boca Chica", "Playa", 900.0, 300.0));

        sistema.agregarRuta("A", "B", 15.0, 5.0, 30.0, false);
        sistema.agregarRuta("A", "C", 20.0, 8.0, 25.0, false);
        sistema.agregarRuta("B", "D", 25.0, 15.0, 50.0, true);
        sistema.agregarRuta("C", "D", 10.0, 12.0, 40.0, false);
        sistema.agregarRuta("A", "D", 60.0, 20.0, 100.0, false);

        sistema.agregarRuta("A", "E", 10.0, 3.0, 20.0, false);
        sistema.agregarRuta("E", "B", 12.0, 4.0, 15.0, false);
        sistema.agregarRuta("A", "F", 18.0, 6.0, 25.0, false);
        sistema.agregarRuta("F", "C", 14.0, 5.5, 20.0, false);
        sistema.agregarRuta("B", "G", 22.0, 10.0, 45.0, true);
        sistema.agregarRuta("G", "D", 15.0, 7.0, 35.0, false);
        sistema.agregarRuta("C", "H", 12.0, 6.0, 20.0, false);
        sistema.agregarRuta("H", "D", 18.0, 8.0, 30.0, false);
        sistema.agregarRuta("I", "B", 8.0, 2.5, 10.0, false);
        sistema.agregarRuta("I", "C", 9.0, 3.0, 10.0, false);
        sistema.agregarRuta("D", "J", 45.0, 30.0, 150.0, false);

        GestorArchivos.guardarDatos(sistema, "paradas.csv", "rutas.csv");
        System.out.println("Archivos guardados :)");
    }
}