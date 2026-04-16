package algorithms;

import model.Parada;

/**
 * PROCESO: Clase de utilidad encargada de realizar todos los cálculos métricos, financieros y temporales del sistema.
 * Centraliza las fórmulas para asegurar que el costo, el tiempo y las distancias sean coherentes en toda la aplicación.
 */
public class CalculadoraRutas {

    // Escala definida: 50 píxeles en el canvas representan 1 kilómetro real.
    private static final double ESCALA_PX_KM = 50.0;

    /**
     * PROCESO: Calcula la distancia geométrica entre dos puntos y la traduce a kilómetros reales.
     * ENTRADAS:
     * - a: Parada de origen con coordenadas X e Y.
     * - b: Parada de destino con coordenadas X e Y.
     * SALIDA: Valor decimal (double) que representa la distancia en kilómetros.
     * FLUJO DE LLAMADAS: Utiliza fórmulas matemáticas de la librería estándar de Java (Math.sqrt).
     */
    public static double calcularDistanciaKM(Parada a, Parada b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        return Math.sqrt(dx * dx + dy * dy) / ESCALA_PX_KM;
    }

    /**
     * PROCESO: Convierte una cantidad de minutos en un formato de lectura humana (horas y minutos).
     * ENTRADAS:
     * - minutos: Valor total del tiempo de viaje en formato decimal.
     * SALIDA: Una cadena de texto formateada (ej: "1h 05min" o "45min").
     */
    public static String formatearTiempo(double minutos) {
        int h = (int) minutos / 60;
        int m = (int) minutos % 60;
        return (h > 0) ? h + "h " + m + "min" + " |" : m + "min" + " |";
    }

    /**
     * PROCESO: Determina el precio del viaje basándose en una tarifa base y recargos por uso de recursos.
     * ENTRADAS:
     * - km: Distancia total recorrida.
     * - minutos: Tiempo total estimado del trayecto.
     * SALIDA: El costo total del viaje en pesos dominicanos.
     */
    public static double calcularCosto(double km, double minutos) {
        // Fórmula: Tarifa Base 80 + (12 pesos por km) + (1.5 pesos por minuto).
        return 80 + (km * 12) + (minutos * 1.5);
    }

    /**
     * PROCESO: Calcula la cantidad de transbordos necesarios según la longitud del trayecto.
     * ENTRADAS:
     * - km: Distancia total del tramo.
     * SALIDA: Número entero que representa los transbordos (uno por cada 9km recorridos).
     */
    public static int calcularTransbordos(double km) {
        return (int) (km / 9);
    }
}