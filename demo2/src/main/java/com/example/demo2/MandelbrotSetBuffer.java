package com.example.demo2;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MandelbrotSetBuffer extends Application {
    int width = 800; // Width of the image to be generated
    int height = 600; // Height of the image to be generated
    int max_iter = 1000; // Maximum number of iterations for the Mandelbrot set calculation
    int numThreads = 8; // Number of threads to use for calculation
    int[][] color_map; // Color map for each iteration
    WritableImage image; // Image to display in the JavaFX scene
    PixelWriter pixelWriter; // Used to write the pixels to the `image`
    BufferedImage bufImage; // Buffer for generating the image

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane(); // Root of the scene graph
        Scene scene = new Scene(root, width, height); // Scene with specified width and height
        primaryStage.setScene(scene);
        primaryStage.show();

        image = new WritableImage(width, height); // Create a new WritableImage with specified dimensions
        pixelWriter = image.getPixelWriter(); // Get the PixelWriter for the image
        color_map = new int[max_iter][3]; // Create the color map

        // Generate the color map for each iteration
        for (int i = 0; i < max_iter; i++) {
            int red = (int) (255.0 * i / max_iter);
            int green = (int) (255.0 * i / max_iter);
            int blue = (int) (255.0 * i / max_iter);
            color_map[i] = new int[] {red, green, blue};
        }

        bufImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // Create the buffer image

        int rowsPerThread = height / numThreads; // Rows per thread
        List<Task<Void>> tasks = new ArrayList<>(); // List of tasks for each thread

        // Create a task for each thread
        for (int i = 0; i < numThreads; i++) {
            int startRow = i * rowsPerThread; // Starting row for the task
            int endRow = (i + 1) * rowsPerThread - 1; // Ending row for the task
            // If this is the last task, make sure it covers the remaining rows
            if (i == numThreads - 1) {
                endRow = height - 1;
            }
            tasks.add(new MandelbrotTask(startRow, endRow, width, height, max_iter, color_map, bufImage));
        }

        // Start each task in a new thread
        for (Task<Void> task : tasks) {
            new Thread(task).start();
        }

        // Add the image to the scene graph
        root.getChildren().add(new javafx.scene.image.ImageView(image));
    }

    // Inner class for the task
    class MandelbrotTask extends Task<Void> {
        // Start row number of the part of the image that this task will process
        int startRow;
        // End row number of the part of the image that this task will process
        int endRow;
        // Width of the image
        int width;
        // Height of the image
        int height;
        // Maximum iteration to determine whether a point is in the Mandelbrot set or not
        int max_iter;
        // Color map for the set
        int[][] color_map;
        // The image to be processed
        BufferedImage bufImage;

        // Constructor to initialize instance variables
        public MandelbrotTask(int startRow, int endRow, int width, int height, int max_iter, int[][] color_map, BufferedImage bufImage) {
            this.startRow = startRow;
            this.endRow = endRow;
            this.width = width;
            this.height = height;
            this.max_iter = max_iter;
            this.color_map = color_map;
            this.bufImage = bufImage;
        }

        // Overriding the call method
        @Override
        protected Void call() {
            // Loop through the part of the image that this task will process
            for (int row = startRow; row <= endRow; row++) {
                for (int col = 0; col < width; col++) {
                    // Calculate the real part of c
                    double c_re = (col - width / 2.0) * 4.0 / width;
                    // Calculate the imaginary part of c
                    double c_im = (row - height / 2.0) * 4.0 / width;
                    double x = 0, y = 0;
                    int iteration = 0;
                    // Check if the point is in the Mandelbrot set or not
                    while (x * x + y * y <= 4 && iteration < max_iter) {
                        double x_new = x * x - y * y + c_re;
                        y = 2 * x * y + c_im;
                        x = x_new;
                        iteration++;
                    }
                    // Get the color for the point from the color map
                    int[] color = color_map[iteration % max_iter];
                    // Convert the color to RGB format
                    int rgb = (color[0] << 10) | (color[1] << 8) | color[2];
                    // Set the color of the point in the buffer image
                    bufImage.setRGB(col, row, rgb);
                }
            }

            // Use JavaFX thread to update the image
            javafx.application.Platform.runLater(() -> {
                // Loop through the part of the image that this task will process
                for (int row = startRow; row <= endRow; row++) {
                    for (int col = 0; col < width; col++) {
                        // Get the RGB value of the point from the buffer image
                        int rgb = bufImage.getRGB(col, row);
                        // Convert the RGB value to a Color object
                        Color color = Color.rgb((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff);
                        pixelWriter.setColor(col, row, color);
                    }
                }
            });

            return null;
        }
    }
}



