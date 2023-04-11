package com.example.demo2;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Loader extends Thread {

    final int INSTANCE;
    final double WIDTH;
    final double HEIGHT;
    final int DIVIDE;
    final WritableImage IMAGE;
    final double SCALE;
    final double ITERATIONS;
    final double CENTERX;
    final double CENTERY;

    public Loader(int instance, int divide, WritableImage image, double width, double height, double scale, double iterations, double centerX, double centerY) {
        this.INSTANCE = instance;
        WIDTH = width;
        HEIGHT = height;
        DIVIDE = divide;
        IMAGE = image;
        SCALE = scale;
        ITERATIONS = iterations;
        CENTERX = centerX;
        CENTERY = centerY;
    }

    @Override
    public void run() {
        for (int x = (int) (INSTANCE * WIDTH / DIVIDE); x < (INSTANCE + 1) * WIDTH / DIVIDE; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Complex c = new Complex(SCALE * (x - CENTERX), SCALE * (CENTERY - y));//Converting pixel indexes to coordinates for a complex number
                Complex z = c.clone();
                for (int i = 1;; i++) {//Skipped the first iteration
                    if (z.magnitude() >= 2) {
                        IMAGE.getPixelWriter().setColor(x, y, Color.hsb(220-i, 1, 1));
                        break;
                    } else if (i > ITERATIONS) {
                        IMAGE.getPixelWriter().setColor(x, y, Color.BLACK);
                        break;
                    }
                    z = Mandelbrot.function(z, c);
                }
            }
        }
    }
}
