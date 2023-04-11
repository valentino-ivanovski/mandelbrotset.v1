package com.example.demo2;

/*  Mandelbrot Set
 *   Made by: Valntino Ivanovski
 *   Period: First semester of second year in Computer Science
 *   Subject: Programming 3 */

/* =========================================Imports================================================ */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.concurrent.CyclicBarrier;

public class myMandelbrotTEST3 extends Application {

    /* =========================================Variables================================================ */

    double width = 800;
    double height = 600;
    double maximumIterations = 1000; //NAPRAJ AKO E NAD 500 ITERATIONS PRIMER DA KORISTIT SYNCHRONIZED!!!
    double zoom = 250.0;
    double xPos = -470; //add 0 on both of the coordinates for the accurate plane
    double yPos = 0;
    double hue = 264.0;
    double brightness = 0.9;
    double saturation = maximumIterations;
    WritableImage image = new WritableImage((int) width, (int) height);
    ImageView imageView = new ImageView(image);
    //PixelWriter pixelWriter = image.getPixelWriter();
    int R = 60;
    int G = 0;
    int B = 60;
    int THREADS = (Runtime.getRuntime().availableProcessors())-1;

    /* =========================================MainMethod================================================ */

    public static void main(String[] args) {
        launch(args);
    }

    /* =========================================MainStage================================================= */

    @Override
    public void start(Stage stage) {
        Pane group = new Pane(imageView);
        Scene scene = new Scene(group, width, height);
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W, UP -> {
                    if (event.isShiftDown()) {
                        up(10);
                    } else {
                        up(100);
                    }
                }
                case A, LEFT -> {
                    if (event.isShiftDown()) {
                        left(10);
                    } else {
                        left(100);
                    }
                }
                case S, DOWN -> {
                    if (event.isShiftDown()) {
                        down(10);
                    } else {
                        down(100);
                    }
                }
                case D, RIGHT -> {
                    if (event.isShiftDown()) {
                        right(10);
                    } else {
                        right(100);
                    }
                }
                case EQUALS -> zoomIn();
                case MINUS -> zoomOut();
                case SPACE -> reset();
                case ESCAPE -> Platform.exit();
            }
        });     //key listener
        scene.setOnMouseClicked(event -> {
            switch (event.getButton()) {
                case PRIMARY -> {

                    zoom /= 0.7;

                    MandelbrotSet(THREADS);
                }
                case SECONDARY -> {
                    zoom *= 0.7;
                    MandelbrotSet(THREADS);
                }
            }
        });   //mouse listener for easier zoom

        stage.setScene(scene);

        long start = System.currentTimeMillis();
        MandelbrotSet(THREADS);
        long end = System.currentTimeMillis();
        long result = (end-start);
        System.out.println(result/1000.0);

        stage.setTitle("Mandelbrot Set");
        stage.show();
    }

    /* ========================================MandelbrotSet============================================== */

    /*public void MandelbrotSet(int n) {
        int portion = (int)width / n;
        for (int i = 0; i < n; i++) {
            int start = i * portion;
            int end = start + portion;

            myMandelbrotParallelTEST3 service = new myMandelbrotParallelTEST3(pixelWriter, start, end, width, height, maximumIterations, zoom, xPos, yPos, hue, brightness, saturation, R, G, B);
            service.start();
        }
        Platform.runLater(() -> {
            if (imageView != null) {
                imageView.setImage(image);
            }
        });

    }*/

    public void MandelbrotSet(int n) {

        int portion = (int) width / n;

        for (int i = 0; i < n; i++) {
            int start = i * portion;
            int end = start + portion;

            myMandelbrotParallelTEST3 service = new myMandelbrotParallelTEST3(image, start, end, width, height, maximumIterations, zoom, xPos, yPos, hue, brightness, saturation, R, G, B);

            service.setOnSucceeded(event -> {
                service.cancel();
                Platform.runLater(() -> {
                        imageView.setImage(image);
                });
            });
            service.start();
        }
    }

    /*public void MandelbrotSet(int n) {
        int portion = (int) width / n;

        for (int i = 0; i < n; i++) {
            int start = i * portion;
            int end = start + portion;

            myMandelbrotParallelTEST3 service = new myMandelbrotParallelTEST3(pixelWriter, start, end, width, height, maximumIterations, zoom, xPos, yPos, hue, brightness, saturation, R, G, B);

            service.setOnSucceeded(event -> {
                service.cancel();
                Platform.runLater(() -> {
                    try {
                        if (imageView != null) {
                            imageView.setImage(image);
                        }
                    } catch (NullPointerException e) {
                        System.err.println("NullPointerException caught: " + e.getMessage());
                    }
                });
            });

            service.start();
        }
    }*/



    /* ===========================================Colors================================================== */

    /* ==========================================Position================================================= */

    public void up(int number) {
        yPos -= (height / zoom) * number;
        MandelbrotSet(THREADS);
    }

    public void down(int number) {
        yPos += (height / zoom) * number;
        MandelbrotSet(THREADS);
    }

    public void left(int number) {
        xPos -= (width / zoom) * number;
        MandelbrotSet(THREADS);
    }

    public void right(int number) {
        xPos += (width / zoom) * number;
        MandelbrotSet(THREADS);
    }

    public void zoomIn() {
        zoom /= 0.7;
        MandelbrotSet(THREADS);
    }

    public void zoomOut() {
        zoom *= 0.7;
        MandelbrotSet(THREADS);
    }

    public void reset() {
        zoom = 250.0;
        xPos = -470;
        yPos = 30;
        MandelbrotSet(THREADS);
    }

    /* ==========================================SaveImage================================================ */

}
