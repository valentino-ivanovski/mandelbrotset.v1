package com.example.demo2;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class myMandelbrotParallel extends Thread {
    final int instance;
    final double width;
    final double height;
    final double maximumIterations;
    final Canvas canvas;
    final double zoom;
    final double xPos; //add 0 on both of the coordinates for the accurate plane
    final double yPos;
    final double hue;
    final double brightness;
    final double saturation;
    final int R;
    final int G;
    final int B;
    final int threads;
    final WritableImage imagee;

    public myMandelbrotParallel(WritableImage imagee, int instance, int threads, double width, double height, double maximumIterations, Canvas canvas, double zoom, double xPos, double yPos, double hue, double brightness, double saturation, int R, int G, int B){
        this.instance = instance;
        this.threads=threads;
        this.width=width;
        this.height=height;
        this.maximumIterations=maximumIterations;
        this.canvas=canvas;
        this.zoom=zoom;
        this.xPos=xPos;
        this.yPos=yPos;
        this.hue=hue;
        this.brightness=brightness;
        this.saturation=saturation;
        this.R=R;
        this.G=G;
        this.B=B;
        this.imagee=imagee;
    }

    @Override
    public void run(){
        double centerY = canvas.getWidth() / 2.0;
        double centerX = canvas.getHeight() / 2.0;
        for (int x = (int) (instance*canvas.getWidth()/threads); x < (instance+1)*canvas.getWidth()/threads; x++) {
            for (int y = 0; y < canvas.getHeight(); y++) {
                double cr = xPos / width + (x - centerY) / zoom;
                double ci = yPos / height + (y - centerX) / zoom;       //getting position of the points on the canvas

                int iterationsOfZ = 0;
                double zr = 0.0;
                double zi = 0.0;

                while (iterationsOfZ < maximumIterations && (zr * zr) + (zi * zi) < 4) {
                    double oldZr = zr;
                    zr = (zr * zr) - (zi * zi) + cr;
                    zi = 2 * (oldZr * zi) + ci;
                    iterationsOfZ++;
                }

                int iterations = iterationsOfZ;

                if (iterations == maximumIterations) {  //inside the set
                    imagee.getPixelWriter().setColor(x, y, Color.rgb(R, G, B));
                } else if (brightness == 0.9) {  //white background
                    imagee.getPixelWriter().setColor(x, y, Color.hsb(hue, iterations / maximumIterations, brightness));
                } else if (hue == 300) {  //colorful background
                    imagee.getPixelWriter().setColor(x, y, Color.hsb(hue * iterations / maximumIterations, saturation, brightness));
                } else if (hue == 0 && saturation == 0 && brightness == 1) {
                    imagee.getPixelWriter().setColor(x, y, Color.hsb(hue, saturation, brightness));
                } else {   //black background
                    imagee.getPixelWriter().setColor(x, y, Color.hsb(hue, saturation, iterations / brightness));
                }
            }
        }
    }
}
