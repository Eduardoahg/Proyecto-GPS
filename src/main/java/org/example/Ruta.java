package org.example;

public class Ruta {
    private Parada destino;
    private double tiempo;
    private double distancia;
    private double costo;
    private boolean requiereTrasbordo;

    public Ruta(Parada destino, double tiempo, double distancia, double costo, boolean requiereTrasbordo) {
        this.destino = destino;
        this.tiempo = tiempo;
        this.distancia = distancia;
        this.costo = costo;
        this.requiereTrasbordo = requiereTrasbordo;
    }

    public Parada getDestino() {

        return destino;
    }
    public double getTiempo() {

        return tiempo;
    }
    public void setTiempo(double tiempo) {

        this.tiempo = tiempo;
    }
    public double getDistancia() {

        return distancia;
    }
    public double getCosto() {

        return costo;
    }
    public boolean isRequiereTrasbordo() {

        return requiereTrasbordo;
    }
}