package org.example;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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