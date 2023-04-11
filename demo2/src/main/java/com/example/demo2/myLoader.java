package com.example.demo2;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class myLoader extends Thread{

     int INSTANCE;
     double width;
     double height;
     int threads;
     WritableImage actualImage;
     WritableImage image;
     double zoom;
     double maximumIterations;
     Canvas canvas;
     double xPos;
     double yPos;
     double brightness;
     double saturation;
     double hue;
     int R;
     int G;
     int B;

    public myLoader(int instance, int divide, WritableImage image, WritableImage actualImage, double width, double height, double zoom, double maximumIterations, double yPos, double xPos, Canvas canvas, double brightness, double saturation, double hue, int R, int G, int B) {
        this.INSTANCE = instance;
        this.width = width;
        this.height = height;
        this.threads = divide;
        this.actualImage = actualImage;
        this.image = image;
        this.zoom = zoom;
        this.maximumIterations = maximumIterations;
        this.canvas = canvas;
        this.xPos = xPos;
        this.yPos = yPos;
        this.brightness = brightness;
        this.saturation = saturation;
        this.hue = hue;
        this.R = R;
        this.G = G;
        this.B = B;

    }

    public int iterationChecker(double cr, double ci) {
        int iterationsOfZ = 0;
        double zr = 0.0;
        double zi = 0.0;

        while (iterationsOfZ < maximumIterations && (zr * zr) + (zi * zi) < 4) {
            double oldZr = zr;
            zr = (zr * zr) - (zi * zi) + cr;
            zi = 2 * (oldZr * zi) + ci;
            iterationsOfZ++;
        }
        return iterationsOfZ;
    }

    public void MandelbrotSet() {
        for (int i = 0; i < 8; i++) {
            WritableImage image = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            actualImage = image;
            double centerY = canvas.getWidth() / 2.0;
            double centerX = canvas.getHeight() / 2.0;
            for (int x = (int)(i * canvas.getWidth() / 8); x < (i+1)*canvas.getWidth()/8; x++) {
                for (int y = 0; y < canvas.getHeight(); y++) {
                    double cr = xPos / width + (x - centerY) / zoom;
                    double ci = yPos / height + (y - centerX) / zoom;       //getting position of the points on the canvas

                    int iterations = iterationChecker(cr, ci);

                    if (iterations == maximumIterations) {  //inside the set
                        image.getPixelWriter().setColor(x, y, Color.rgb(R, G, B));
                    } else if (brightness == 0.9) {  //white background
                        image.getPixelWriter().setColor(x, y, Color.hsb(hue, iterations / maximumIterations, brightness));
                    } else if (hue == 300) {  //colorful background
                        image.getPixelWriter().setColor(x, y, Color.hsb(hue * iterations / maximumIterations, saturation, brightness));
                    } else if (hue == 0 && saturation == 0 && brightness == 1) {
                        image.getPixelWriter().setColor(x, y, Color.hsb(hue, saturation, brightness));
                    } else {   //black background
                        image.getPixelWriter().setColor(x, y, Color.hsb(hue, saturation, iterations / brightness));
                    }
                }
                canvas.getGraphicsContext2D().drawImage(image, 0, 0); //x and y coordinates of the image.
            }
        }
    }

}
