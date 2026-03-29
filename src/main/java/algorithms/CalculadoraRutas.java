package algorithms;

import model.Parada;

public class CalculadoraRutas {

    // Escala: 100 píxeles = 1 kilómetro
    private static final double ESCALA_PX_KM = 40.0;

    public static double calcularDistanciaKM(Parada a, Parada b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        return Math.sqrt(dx * dx + dy * dy) / ESCALA_PX_KM;
    }

    public static String formatearTiempo(double minutos) {
        int h = (int) minutos / 60;
        int m = (int) minutos % 60;
        return (h > 0) ? h + "h " + m + "min" : m + "min";
    }

    public static double calcularCosto(double km, double minutos) {
        // Base 80 + incremento por distancia y tiempo
        return 80 + (km * 12) + (minutos * 1.5);
    }

    public static int calcularTransbordos(double km) {
        // 0 transbordos iniciales, +1 cada 9km
        return (int) (km / 9);
    }
}