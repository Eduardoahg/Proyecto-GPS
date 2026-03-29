package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/*
   PLUGIN UTILIZADO: LOMBOK
   @Data: Genera automáticamente Getters, Setters, toString, equals y hashCode. (Pusimos estos ultimos 3 por si acaso)
   @AllArgsConstructor: Crea un constructor que incluye todos los atributos de la clase.
   @NoArgsConstructor: Genera un constructor vacío (sin parámetros), necesario para frameworks y persistencia.
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Parada {
    private String id;
    private String nombre;
    private String localizacion;
    private double x; 
    private double y;

    public Parada(String id, String nombre, String localizacion) {
        this.id = id;
        this.nombre = nombre;
        this.localizacion = localizacion;
        this.x = 0;
        this.y = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parada parada = (Parada) o;
        return Objects.equals(id, parada.id);
    }
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return nombre;
    }
}