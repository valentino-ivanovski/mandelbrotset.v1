package nonNeeded;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class AI extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    // Create a new Canvas object
    Canvas canvas = new Canvas(800, 600);

    // Get the GraphicsContext from the Canvas
    GraphicsContext gc = canvas.getGraphicsContext2D();
    Scene scene = new Scene(new Group(canvas), 800, 600);
    @Override
    public void start(Stage stage) {
        stage.setScene(scene);
        drawMandelbrotSet();
        stage.setHeight(627);
        stage.setWidth(800);
        stage.setTitle("Mandelbrot Set");
        stage.show();
    }
    static double maxIter = 50;
    // Create a new Canvas object
    //Canvas canvas = new Canvas(800, 600);

    // Get the GraphicsContext from the Canvas
    //GraphicsContext gc = canvas.getGraphicsContext2D();

    // Set the initial scale of the x- and y-axes
    double scaleX = 200;
    double scaleY = 200;

    // Create a function that draws the Mandelbrot Set on the Canvas
    public void drawMandelbrotSet() {
        // Iterate over each pixel on the Canvas
        for (int x = 0; x < 800; x++) {
            for (int y = 0; y < 600; y++) {
                // Convert the pixel coordinates to complex numbers
                double c_re = (x - 400) / scaleX;
                double c_im = (y - 300) / scaleY;

                // Use the Mandelbrot Set algorithm to determine the color of the pixel
                int iter = mandelbrot(c_re, c_im);

                // Set the color of the pixel on the Canvas
                gc.setFill(getColor(iter));
                gc.fillRect(x, y, 1, 1);
            }
        }
    }

    // Create a function that calculates the number of iterations of the Mandelbrot Set algorithm
// for a given complex number
    public int mandelbrot(double c_re, double c_im) {
        // Set the initial values of z_re and z_im to 0
        double z_re = 0;
        double z_im = 0;

        // Set the maximum number of iterations
        int maxIter = 50;

        // Set the initial number of iterations to 0
        int n = 0;

        // Iterate the Mandelbrot Set algorithm until the maximum number of iterations is reached
        // or the magnitude of z exceeds 2
        while (n < maxIter && z_re * z_re + z_im * z_im < 4) {
            double z_re_new = z_re * z_re - z_im * z_im + c_re;
            double z_im_new = 2 * z_re * z_im + c_im;
            z_re = z_re_new;
            z_im = z_im_new;
            n++;
        }

        // Return the number of iterations
        return n;
    }

    // Create a function that maps the number of iterations to a color
    public Color getColor(int iter) {
        // If the number of iterations is less than the maximum, return a color based on the number of iterations
        if (iter < maxIter) {
            return Color.hsb(iter / 100.0 * 360, 1, 1);
        }
        // Otherwise, return black
        else {
            return Color.BLACK;
        }
    }

}
