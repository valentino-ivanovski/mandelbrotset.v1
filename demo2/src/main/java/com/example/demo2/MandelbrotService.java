package com.example.demo2;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class MandelbrotService extends Service<WritableImage> {
    private final int width;
    private final int height;
    private final int n;
    private final int MAX_ITERATIONS;

    public MandelbrotService(int width, int height, int n, int MAX_ITERATIONS) {
        this.width = width;
        this.height = height;
        this.n = n;
        this.MAX_ITERATIONS=MAX_ITERATIONS;
    }

    @Override
    protected Task<WritableImage> createTask() {
        return new MandelbrotTask(width, height, n, MAX_ITERATIONS);
    }
}

class MandelbrotTask extends Task<WritableImage> {
    private final int width;
    private final int height;
    private final int n;
    private final int MAX_ITERATIONS;

    public MandelbrotTask(int width, int height, int n, int MAX_ITERATIONS) {
        this.width = width;
        this.height = height;
        this.n = n;
        this.MAX_ITERATIONS=MAX_ITERATIONS;
    }

    @Override
    protected WritableImage call() throws Exception {
        WritableImage image = new WritableImage(width, height);
        int[] colors = new int[width * height];

        // Divide the image into n sections
        int sectionHeight = height / n;
        ForkJoinPool forkJoinPool = new ForkJoinPool(n);
        for (int i = 0; i < n; i++) {
            int start = i * sectionHeight;
            int end = (i + 1) * sectionHeight;
            forkJoinPool.submit(() -> calculateMandelbrot(start, end, colors, MAX_ITERATIONS));
        }
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.HOURS);

        // Copy the colors into the image
        PixelWriter writer = image.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                writer.setArgb(x, y, colors[x + y * width]);
            }
        }

        return image;
    }

    void calculateMandelbrot(int start, int end, int[] colors, int MAX_ITERATIONS) {
        double scale = 3.0 / Math.min(width, height);

        for (int y = start; y < end; y++) {
            for (int x = 0; x < width; x++) {
                double real = (x - (int)(width / 2)) * scale;
                double imag = (y - (int)(height / 2)) * scale;

                int color = getMandelbrotColor(real, imag, MAX_ITERATIONS);
                colors[x + y * width] = color;
            }
        }
    }

    int getMandelbrotColor(double real, double imag, int MAX_ITERATIONS) {
        int iterations = 0;
        double r = real;
        double i = imag;

        while (iterations < MAX_ITERATIONS && r * r + i * i < 4.0) {
            double r2 = r * r - i * i + real;
            double i2 = 2 * r * i + imag;
            r = r2;
            i = i2;
            iterations++;
        }

        return getColor(iterations, MAX_ITERATIONS);
    }

    int getColor(int iterations, int MAX_ITERATIONS) {
        if (iterations == MAX_ITERATIONS) {
            return 0;
        }

        int color = (int) (iterations / (double) MAX_ITERATIONS * 255);
        int red = (color & 0xff) << 16;
        int green = (color & 0xff) << 8;
        int blue = (color & 0xff);
        return red | green | blue;
    }



}