package com.example.demo2;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Mandelbrot extends Application {

    static final double WIDTH = Screen.getPrimary().getVisualBounds().getMaxX();
    static final double HEIGHT = Screen.getPrimary().getVisualBounds().getMaxY();
    final WritableImage IMAGE = new WritableImage((int) WIDTH, (int) HEIGHT);
    final ImageView IMAGEVIEW = new ImageView(IMAGE);
    final Group ROOT = new Group(IMAGEVIEW);
    double initialX;
    double initialY;
    double offsetX;
    double offsetY;
    int x = 0;
    int y = 0;
    
    static final int THREADS = 7;//Parameter
    final double ITERATIONSINCREASERATE = 1.2;//Parameter
    double iterations = 20;//Parameter
    double scale = .003;//Parameter
    final double ZOOMRATE = 2;//Parameter

    static Complex function(Complex z, Complex c) {
        return Complex.add(Complex.square(z), c);
    }

    void generateImage() {
        double centerX = WIDTH / 2 - offsetX;
        double centerY = HEIGHT / 2 - offsetY;
        Loader[] temp = new Loader[THREADS];
        for (int i = 0; i < THREADS; i++) {
            temp[i] = new Loader(i, THREADS, IMAGE, WIDTH, HEIGHT, scale, iterations, centerX, centerY);
            temp[i].start();
        }
        for (int i = 0; i < THREADS; i++) {
            try {
                temp[i].join();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void start(Stage stage) {
        long start = System.nanoTime();
        generateImage();
        long end = System.nanoTime();
        long result = end-start;
        System.out.println(result/1000000000.0);
        Scene scene = new Scene(ROOT, 0, 0);
        scene.setOnKeyPressed(event -> {
            KeyCode pressed = event.getCode();
            if (pressed == KeyCode.LEFT || pressed == KeyCode.A) {
                offsetX -= 100;
                generateImage();
            } else if (pressed == KeyCode.RIGHT || pressed == KeyCode.D) {
                offsetX += 100;
                generateImage();
            } else if (pressed == KeyCode.UP || pressed == KeyCode.W) {
                offsetY -= 100;
                generateImage();
            } else if (pressed == KeyCode.DOWN || pressed == KeyCode.S) {
                offsetY += 100;
                generateImage();
            } else if (pressed == KeyCode.EQUALS) {
                scale /= ZOOMRATE;
                offsetX *= ZOOMRATE;
                offsetY *= ZOOMRATE;
                iterations *= ITERATIONSINCREASERATE;
                generateImage();
            } else if (pressed == KeyCode.MINUS) {
                scale *= ZOOMRATE;
                offsetX /= ZOOMRATE;
                offsetY /= ZOOMRATE;
                iterations /= ITERATIONSINCREASERATE;
                generateImage();
            }
        });
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
