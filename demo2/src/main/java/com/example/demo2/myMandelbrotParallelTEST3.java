package com.example.demo2;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.concurrent.CyclicBarrier;

public class myMandelbrotParallelTEST3 extends Service<Void> {
    final double width;
    final double height;
    final double maximumIterations;
    final double zoom;
    final double xPos; //add 0 on both of the coordinates for the accurate plane
    final double yPos;
    final double hue;
    final double brightness;
    final double saturation;
    final int R;
    final int G;
    final int B;
    final int start;
    final int end;
    final WritableImage image;
    public myMandelbrotParallelTEST3(WritableImage image, int start, int end, double width, double height, double maximumIterations, double zoom, double xPos, double yPos, double hue, double brightness, double saturation, int R, int G, int B){
        this.width=width;
        this.height=height;
        this.maximumIterations=maximumIterations;
        this.zoom=zoom;
        this.xPos=xPos;
        this.yPos=yPos;
        this.hue=hue;
        this.brightness=brightness;
        this.saturation=saturation;
        this.R=R;
        this.G=G;
        this.B=B;
        this.start=start;
        this.end=end;
        this.image=image;
    }

    /*protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {

                double centerY = width / 2.0;
                double centerX = height / 2.0;

                    for (int y = start; y < end; y++) {
                        for (int x = 0; x < width; x++) {

                            double zr = 0;
                            double zi = 0;
                            double cr = xPos / width + (x - centerY) / zoom;
                            double ci = yPos / height + (y - centerX) / zoom;       //getting position of the points on the image

                            int iterationsOfZ;

                            for (iterationsOfZ = 0; iterationsOfZ < maximumIterations && (zr * zr) + (zi * zi) < 4; iterationsOfZ++){
                                    double oldZr = zr;
                                    zr = (zr * zr) - (zi * zi) + cr;
                                    zi = 2 * (oldZr * zi) + ci;
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
                return null;
            }
        };

    }*/

    /*SYNCHRONIZED ZA DA NE TI DAVAT ERROR BAKI*/
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                double centerY = width / 2.0;
                double centerX = height / 2.0;
                PixelWriter pixelWriter = image.getPixelWriter();

                //synchronized (pixelWriter) {
                    for (int y = start; y < end; y++) {
                        for (int x = 0; x < width; x++) {
                            double zr = 0;
                            double zi = 0;
                            double cr = xPos / width + (x - centerY) / zoom;
                            double ci = yPos / height + (y - centerX) / zoom;

                            int iterationsOfZ;

                            for (iterationsOfZ = 0; iterationsOfZ < maximumIterations && (zr * zr) + (zi * zi) < 4; iterationsOfZ++) {
                                double oldZr = zr;
                                zr = (zr * zr) - (zi * zi) + cr;
                                zi = 2 * (oldZr * zi) + ci;
                            }

                            if (iterationsOfZ == maximumIterations) {
                                pixelWriter.setColor(x, y, Color.rgb(R, G, B));
                            } else if (brightness == 0.9) {
                                pixelWriter.setColor(x, y, Color.hsb(hue, iterationsOfZ / maximumIterations, brightness));
                            } else if (hue == 300) {
                                pixelWriter.setColor(x, y, Color.hsb(hue * iterationsOfZ / maximumIterations, saturation, brightness));
                            } else if (hue == 0 && saturation == 0 && brightness == 1) {
                                pixelWriter.setColor(x, y, Color.hsb(hue, saturation, brightness));
                            } else {
                                pixelWriter.setColor(x, y, Color.hsb(hue, saturation, iterationsOfZ / brightness));
                            }
                        }
                    }
                //}
                return null;
            }
        };
    }
}
