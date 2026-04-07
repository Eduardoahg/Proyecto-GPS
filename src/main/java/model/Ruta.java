package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/*
   PLUGIN UTILIZADO: LOMBOK
   @Data: Genera automáticamente Getters, Setters, toString, equals y hashCode.
   @AllArgsConstructor: Crea un constructor que incluye todos los atributos de la clase.
   @NoArgsConstructor: Genera un constructor vacío (sin parámetros), necesario para frameworks y persistencia.
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ruta {
    private Parada destino;
    private double tiempo;
    private double distancia;
    private double costo;
    private boolean requiereTrasbordo;

    /**
     * PROCESO: Obtiene el valor numérico de la ruta según el criterio de búsqueda seleccionado.
     * ENTRADAS:
     * - criterio: El parámetro de optimización elegido por el usuario ("TIEMPO", "DISTANCIA" o "COSTO").
     * SALIDA: El peso (double) que se sumará al costo acumulado en el algoritmo.
     */
    public double getPeso(String criterio) {
        return switch (criterio.toUpperCase()) {
            case "TIEMPO" -> this.tiempo;
            case "COSTO" -> this.costo;
            default -> this.distancia;
        };
    }
}

