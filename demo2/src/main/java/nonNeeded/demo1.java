package nonNeeded;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Control;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class demo1 extends Application {

    private static final int width = 800;
    private static final int height = 600;
    private Scene scene;
    private final Canvas canvas = new Canvas(width, height);
    static final double DEFAULT_ZOOM = 2;
    static final double DEFAULT_TOP_LEFT_X = 400;
    static final double DEFAULT_TOP_LEFT_Y = -150.2;
    double zoomFactor = DEFAULT_ZOOM;
    double topLeftX = DEFAULT_TOP_LEFT_X;
    double topLeftY = DEFAULT_TOP_LEFT_Y;
    static final int MAX_ITER=50;
    static double scale = .003;

    @Override
    public void start(Stage stage) {
        scene = new Scene(new VBox(canvas));
        stage.setScene(scene);
        //render();
        //drawTransparentBg(canvas, 0, 0, 800, 600);
        drawMandelbrot();
        scene.setOnKeyPressed(event -> {
            KeyCode pressed = event.getCode();
            if (pressed == KeyCode.LEFT || pressed == KeyCode.A) {
                topLeftX += 0.1;
                drawMandelbrot();
            } else if (pressed == KeyCode.RIGHT || pressed == KeyCode.D) {
                topLeftX -= 0.1;
                drawMandelbrot();
            } else if (pressed == KeyCode.UP || pressed == KeyCode.W) {
                topLeftY -= 0.1;
                drawMandelbrot();
            } else if (pressed == KeyCode.DOWN || pressed == KeyCode.S) {
                topLeftY += 0.1;
                drawMandelbrot();
            } else if (pressed == KeyCode.EQUALS) {
                zoomFactor/=0.7;
                drawMandelbrot();
            } else if (pressed == KeyCode.MINUS) {
                zoomFactor*=0.7;
                drawMandelbrot();
            }
        });
        stage.show();
        exitOnEsc();
    }

    private void render() {
        drawTransparentBg(canvas, 0, 0, 800, 600);
        Color color = Color.BLACK;

        WritableImage image = new WritableImage(200, 200);
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                image.getPixelWriter().setColor(x, y, color);
            }
        }
        canvas.getGraphicsContext2D().drawImage(image, 50, 50);

    }

    public void drawTransparentBg(Canvas canvas, int xPos, int yPos, int width, int height) {
        int gridSize = 8;
        boolean darkX = true;
        String darkCol = "#111111";
        String lightCol = "#222266";

        for (int x = xPos; x < canvas.getWidth(); x += gridSize) {
            boolean dark = darkX;
            darkX = !darkX;
            if (x > width) {
                break;
            }

            for (int y = yPos; y < canvas.getHeight(); y += gridSize) {
                if (y > height) {
                    break;
                }

                dark = !dark;
                String color;
                if (dark) {
                    color = darkCol;
                } else {
                    color = lightCol;
                }
                canvas.getGraphicsContext2D().setFill(Paint.valueOf(color));
                canvas.getGraphicsContext2D().fillRect(x, y, gridSize, gridSize);
            }
        }
    }


    private double getXPos(double x){ //transforming th x coordinate into a point on the complex plane
        return x/zoomFactor - topLeftX;
    }
    private double getYPos(double y){ //transforming th x coordinate into a point on the complex plane
        return y/zoomFactor + topLeftY;
    }



    public void drawMandelbrot(){
        WritableImage image = new WritableImage(800, 600);
        for(int x = 0; x<canvas.getWidth(); x++){
            for(int y = 0; y<canvas.getHeight(); y++){
                double centerX = width / 2 - getXPos(x);
                double centerY = height / 2 - getYPos(y);
                double cr = (scale * (x - centerX));//getXPos(x);
                double ci = (scale * (centerY - y));//getYPos(y);
                int iterCount = computeIterations(cr, ci);
                int iterCount2 = testing(cr, ci);
                if(iterCount == MAX_ITER){
                    image.getPixelWriter().setColor(x, y, Color.BLACK);
                }
                else{
                    image.getPixelWriter().setColor(x, y, Color.RED);
                }
            }
        }
        canvas.getGraphicsContext2D().drawImage(image, 0, 0); // x and y coordinates of the picture
    }

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
            double saveZr = zr; //we use this in zi
            zr = (zr*zr) - (zi*zi) + cr; //applying the formula z' = zr^2 + zi^2 + cr + ci
            zi = 2 * saveZr * zi + ci;           //but we can split it in zr and zi parts thanks to the algebra
            //if point is inside of the mandelbrot set
            if(iterCount == MAX_ITER){ //since we are going until MAX_ITER, which are the maximum iterations that the set allows, the iterCount should stop there.
                return MAX_ITER;
            }
            iterCount++;
        }//if while loop breaks then the complex point c is outside of the mandelbrot set
        return iterCount;
    }
    public int testing(double cr, double ci){
        double zr = 0.0;
        double zi = 0.0;
        int iterCount = 0;/*
        while((zr*zr) + (zi*zi) > 4.0){
            double saveZr = zr; //we use this in zi
            zr = (zr*zr) - (zi*zi) + cr; //applying the formula z' = zr^2 + zi^2 + cr + ci
            zi = 2 * saveZr * zi + ci;           //but we can split it in zr and zi parts thanks to the algebra
            //if point is inside of the mandelbrot set
            if(iterCount == MAX_ITER*2){ //since we are going until MAX_ITER, which are the maximum iterations that the set allows, the iterCount should stop there.
                return MAX_ITER*2;
            }
            iterCount++;*/
        while (iterCount <= MAX_ITER){
            if((zr*zr) + (zi*zi) > 4.0){
                break;
            }
            iterCount++;

        }//if while loop breaks then the complex point c is outside of the mandelbrot set
        return iterCount;
    }


    /*
    while (iter < maxIter){
                    double temp = zr;
                    zr = (zr*zr) - (zi*zi) + cr;
                    zi = 2*temp*zi + ci;
                    //check |z|^2 > 4 then stop
                    if(zr*zr+zi*zi > 4){
                        break;
                    }
                    iter++;
                }
     */


    private void exitOnEsc() {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                Platform.exit();
            }
        });
    }
}