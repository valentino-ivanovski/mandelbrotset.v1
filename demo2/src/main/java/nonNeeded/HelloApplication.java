package nonNeeded;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

//import java.awt.*;

public class HelloApplication extends Application {
    static final int WIDTH = 800;                   //VARIABLI ZA SIZE NA WINDOW
    static final int HEIGHT = 600;
    static final int MAX_ITER=200; //iterations of mandelbrot set
    static final double DEFAULT_ZOOM = 250.0;
    static final double DEFAULT_TOP_LEFT_X = 2.15;
    static final double DEFAULT_TOP_LEFT_Y = -1.15;
    final WritableImage IMAGE = new WritableImage((int) WIDTH, (int) HEIGHT);
    final ImageView IMAGEVIEW = new ImageView(IMAGE);
    final Group ROOT = new Group(IMAGEVIEW);

    double zoomFactor = DEFAULT_ZOOM;
    double topLeftX = DEFAULT_TOP_LEFT_X;
    double topLeftY = DEFAULT_TOP_LEFT_Y;

    private double getXPos(double x){ //transforming th x coordinate into a point on the complex plane
        return x/zoomFactor - topLeftX;
    }
    private double getYPos(double y){ //transforming th x coordinate into a point on the complex plane
        return y/zoomFactor + topLeftY;
    }

    private Color makeColor(int iterCount){
        if(iterCount == MAX_ITER){
            return Color.BLACK;
        }
        return Color.BLUE;
    }

    public void updateFractal(){
        for(int x = 0; x<WIDTH; x++){
            for(int y = 0; y<HEIGHT; y++){

                double cr = getXPos(x); //this is because the real numbers are on the x axis
                double ci = getYPos(y); //and the imaginary numbers are one the y axis

                int iterCount = computeIterations(cr, ci);
                Color pixelColor = makeColor(iterCount);
                IMAGE.getPixelWriter().setColor(x, y, pixelColor);
            }
        }
    }
    /*public void updateFractal(){
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                IMAGE.getPixelWriter().setColor(x, y, Color.BLACK);
            }
        }
    }*/

    private int computeIterations(double cr, double ci){
          /*
          Let c = cr + ci
          Let z = zr + zi

          z' = z^2 + c
          z' = (zr + zi)(zr + zi) + cr + ci
          z' = zr^2 + 2(zr * zi) + zi^2 + cr + ci
          z' = zr^2 + 2*zr*zi - zi^2 + cr + ci
                                  //we write - here because zi is just the value of the imag number, but squared we get - because if it had i it would be equal to -1
          zr' = zr^2 - zi^2 + cr
          zi' = 2*zr*zi + ci
           */

        //initial values of z for z0

        double zr = 0.0;
        double zi = 0.0;

        //this method returns an integer, the number of iterations

        int iterCount = 0;
        //to calculate the distance from the origin, we use the formula  sqrt((a^2 + b^2) <= 2.0
        //or alternatively, to avoid square roots we can use a^2 + b^2 <= 4.0

        while((zr*zr) + (zi*zi) <= 4.0){
            double temp = zr; //we use this in zi
            zr = (zr*zr) - (zi*zi) + cr; //applying the formula z' = zr^2 + zi^2 + cr + ci
            zi = 2*temp*zi + ci;           //but we can split it in zr and zi parts thanks to the algebra
            //if point is inside of the mandelbrot set
            if(iterCount >= MAX_ITER){
                return MAX_ITER;
            }
            iterCount++;
        }//if while loop breaks then the complex point c is outside of the mandelbrot set
        return iterCount;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        //updateFractal();
        Scene scene = new Scene(ROOT, 800, 600);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
