package com.example.demo2;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class stackOverflowCode extends Application {
    private static int MAX_ITER = 80;
    private static double w = 10*100;
    private static double h = 2*Math.PI*100;

    static double RE_START = -2;
    static double RE_END = 1;
    static double IM_START = -1;
    static double IM_END = 1;

    public static int mandelbrot(Complex c) {
        Complex z = new Complex(0,0);
        int n = 0;
        while (Complex.abs(z).isLessThanOrEqual(4) && n < MAX_ITER) {
            z = Complex.add(Complex.multiply(z,z),c);
            n++;
        }
        return n;
    }

    public void start(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setTitle("Mandelbrot Viewer");
        primaryStage.setScene(new Scene(root, w, h));
        primaryStage.show();

        Canvas canvas = new Canvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setWidth(w);
        canvas.setHeight(h);
        canvas.relocate(0, 0);
        root.getChildren().add(canvas);
        update(gc);
    }

    private static void update(GraphicsContext gc) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Complex c = new Complex(RE_START + (x / w) * (RE_END - RE_START),
                        IM_START + (y / h) * (IM_END - IM_START));
                int m = mandelbrot(c);
                int hue = 360 * m / MAX_ITER;
                int saturation = 255;
                int value = 0;
                if (m < MAX_ITER) {
                    value = 255;
                }
                gc.setFill(Color.hsb(hue, saturation/255d, value/255d));
                gc.fillRect(x, y, 1, 1);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    static class Complex {
        double r;
        double i;

        Complex(double r, double i) {
            this.r = r;
            this.i = i;
        }

        static public Complex abs(Complex c) {
            return new Complex(Math.abs(c.r), Math.abs(c.i));
        }

        public boolean isLessThanOrEqual(double n) {
            if (r <= n || i <= n) {
                return true;
            }

            return false;
        }

        static public Complex add(Complex c1, Complex c2) {
            return new Complex(c1.r + c2.r, c1.i + c2.i);
        }

        static public Complex multiply(Complex c1, Complex c2) {
            return new Complex((c1.r*c2.r)-(c1.i*c2.i), (c1.r*c2.i)+(c1.i*c2.r));
        }

        public String toString() {
            return (r + ", " + i);
        }
    }
}