package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

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

        return nombre + " (" + id + ")";
    }
}