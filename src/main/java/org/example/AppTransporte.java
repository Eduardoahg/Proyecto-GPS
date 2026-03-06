package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class AppTransporte extends Application {
    private GrafoTransporte sistema = new GrafoTransporte();
    private Canvas canvas = new Canvas(800, 600);
    private GraphicsContext gc;

    @Override
    public void start(Stage stage) {
        gc = canvas.getGraphicsContext2D();

        GestorArchivos.cargarDatos(sistema, "paradas.csv", "rutas.csv");

        BorderPane root = new BorderPane();
        VBox menu = new VBox(10);
        menu.setStyle("-fx-padding: 10; -fx-background-color: #f4f4f4;");

        Label label = new Label("Haga clic en el mapa para\nagregar una parada.");
        Button btnLimpiar = new Button("Limpiar Pantalla");
        TextArea log = new TextArea();
        log.setPrefHeight(200);

        // Evento para click
        canvas.setOnMouseClicked(e -> {
            String id = "P" + (sistema.getGrafo().size() + 1);
            Parada nueva = new Parada(id, "Parada " + id, "Ubicación", e.getX(), e.getY());
            sistema.agregarParada(nueva);
            log.appendText("Agregada: " + nueva.getNombre() + "\n");
            dibujarMapa();
        });

        menu.getChildren().addAll(label, btnLimpiar, log);
        root.setLeft(menu);
        root.setCenter(canvas);

        dibujarMapa();

        Scene scene = new Scene(root, 1050, 650);
        stage.setTitle("GPS ");
        stage.setScene(scene);
        stage.show();
    }

    private void dibujarMapa() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // 1. Dibujar Conexiones (Rutas)
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);
        for (Map.Entry<Parada, List<Ruta>> entrada : sistema.getGrafo().entrySet()) {
            Parada origen = entrada.getKey();
            for (Ruta ruta : entrada.getValue()) {
                Parada destino = ruta.getDestino();
                // Dibujamos la línea de parada a parada [cite: 14, 30]
                gc.strokeLine(origen.getX(), origen.getY(), destino.getX(), destino.getY());
            }
        }

        // 2. Dibujar Paradas (Nodos)
        for (Parada p : sistema.getGrafo().keySet()) {
            gc.setFill(Color.RED);
            // Dibujamos el círculo en la coordenada guardada [cite: 13, 30]
            gc.fillOval(p.getX() - 10, p.getY() - 10, 20, 20);
            gc.setFill(Color.BLACK);
            gc.fillText(p.getNombre(), p.getX() + 12, p.getY() + 5);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}