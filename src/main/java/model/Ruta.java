package model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/*
   PLUGIN UTILIZADO: LOMBOK
   @Data: Genera automáticamente Getters, Setters, toString, equals y hashCode. (Pusimos estos ultimos 3 por si acaso)
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

}