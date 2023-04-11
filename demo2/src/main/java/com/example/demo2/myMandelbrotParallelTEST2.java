package com.example.demo2;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class myMandelbrotParallelTEST2 extends Thread {
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
    final WritableImage imagee;
    final int start;
    final int end;
    final PixelWriter pixelWriter;
    //int x;
    //int y;

    public myMandelbrotParallelTEST2(PixelWriter pixelWriter, int start, int end, WritableImage imagee, double width, double height, double maximumIterations, double zoom, double xPos, double yPos, double hue, double brightness, double saturation, int R, int G, int B){
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
        this.imagee=imagee;
        this.start=start;
        this.end=end;
        this.pixelWriter=pixelWriter;
    }

    @Override
    public synchronized void run(){
            double centerY = width / 2.0;
            double centerX = height / 2.0;
            for (int x = start; x < end; x++) {
                for (int y = 0; y < height; y++) {
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
    }