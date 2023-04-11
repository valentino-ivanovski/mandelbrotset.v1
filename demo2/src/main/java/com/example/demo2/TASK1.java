package com.example.demo2;

import javafx.concurrent.Task;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class TASK1 extends Task<Void> {
    private final int width;
    private final int height;
    private final double xMin;
    private final double xMax;
    private final double yMin;
    private final double yMax;
    private final WritableImage image;
    private final PixelWriter pixelWriter;

    public TASK1(int width, int height, double xMin, double xMax, double yMin, double yMax, WritableImage image) {
        this.width = width;
        this.height = height;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.image = image;
        this.pixelWriter = image.getPixelWriter();
    }

    @Override
    protected Void call() throws Exception {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double zx = x * (xMax - xMin) / (width - 1) + xMin;
                double zy = y * (yMax - yMin) / (height - 1) + yMin;
                double cX = zx;
                double cY = zy;
                int iteration = 0;
                int maxIteration = 1000;
                while (iteration < maxIteration && (zx * zx + zy * zy) < 4) {
                    double tmp = zx * zx - zy * zy + cX;
                    zy = 2 * zx * zy + cY;
                    zx = tmp;
                    iteration++;
                }
                Color color = iteration < maxIteration ? Color.BLACK : Color.WHITE;
                pixelWriter.setColor(x, y, color);
            }
            updateProgress(y, height);
        }
        return null;
    }
}
