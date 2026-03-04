package org.example;

public class Ruta {

    //Representa las arista
    private Parada destino; // Hacia donde va la ruta
    private double tiempo;
    private double distancia;
    private double costo;
    private boolean requiereTrasbordo; // true si implica un cambio de línea o vehículo

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

    public void setDestino(Parada destino) {
        this.destino = destino;
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

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public boolean isRequiereTrasbordo() {
        return requiereTrasbordo;
    }

    public void setRequiereTrasbordo(boolean requiereTrasbordo) {
        this.requiereTrasbordo = requiereTrasbordo;
    }

    @Override
    public String toString() {
        return "Ruta hacia " + destino.getNombre() +
                " [Distancia: " + distancia + ", Tiempo: " + tiempo +
                ", Costo: " + costo + ", Trasbordo: " + requiereTrasbordo + "]";
    }
}