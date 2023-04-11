package com.example.demo2;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class Mandelbrott extends Application {
    double width = 800;
    double height = 600;
    double maximumIterations = 50;
    double zoom = 250.0;
    double xPos = -470; //add 0 on both of the coordinates for the accurate plane
    double yPos = 0;
    double hue = 264.0;
    double brightness = 0.9;
    double saturation = maximumIterations;
    WritableImage image = new WritableImage((int) width, (int) height);
    PixelWriter pixelWriter = image.getPixelWriter();
    int R = 60;
    int G = 0;
    int B = 60;
    int THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Service<Void> service = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {

                        int portion = (int) height / THREADS;
                        double centerY = width / 2.0;
                        double centerX = height / 2.0;

                        for (int i = 0; i < THREADS; i++) {

                            int start = portion * i;
                            int end = portion * (i + 1);

                            for (int y = start; y < end; y++) {
                                for (int x = 0; x < width; x++) {

                                    double zr = 0;
                                    double zi = 0;
                                    double cr = xPos / width + (x - centerY) / zoom;
                                    double ci = yPos / height + (y - centerX) / zoom;       //getting position of the points on the canvas

                                    int iterationsOfZ = 0;

                                    while (iterationsOfZ < maximumIterations && (zr * zr) + (zi * zi) < 4) {
                                        double oldZr = zr;
                                        zr = (zr * zr) - (zi * zi) + cr;
                                        zi = 2 * (oldZr * zi) + ci;
                                        iterationsOfZ++;
                                    }
                                    if (iterationsOfZ == maximumIterations) {  //inside the set
                                        pixelWriter.setColor(x, y, Color.rgb(R, G, B));
                                    } else if (brightness == 0.9) {  //white background
                                        pixelWriter.setColor(x, y, Color.hsb(hue, iterationsOfZ / maximumIterations, brightness));
                                    } else if (hue == 300) {  //colorful background
                                        pixelWriter.setColor(x, y, Color.hsb(hue * iterationsOfZ / maximumIterations, saturation, brightness));
                                    } else if (hue == 0 && saturation == 0 && brightness == 1) {
                                        pixelWriter.setColor(x, y, Color.hsb(hue, saturation, brightness));
                                    } else {   //black background
                                        pixelWriter.setColor(x, y, Color.hsb(hue, saturation, iterationsOfZ / brightness));
                                    }
                                }
                            }
                        }
                        return null;
                    }
                };
            }
        };
    }
}