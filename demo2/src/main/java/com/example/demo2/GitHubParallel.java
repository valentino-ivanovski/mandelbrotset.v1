package com.example.demo2;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.IntStream;

/**
 * Multi-threaded JavaFX Mandelbrot. Adapted from
 * https://introcs.cs.princeton.edu/java/32class/Mandelbrot.java.html
 * https://www.hameister.org/projects_fractal.html
 *
 * @since 23 Dec 2022
 */
public class GitHubParallel extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        // Range sliders
        var xcSlider = new Slider(-5, 5, 0);
        var ycSlider = new Slider(-5, 5, -0.7);
        var sizeSlider = new Slider(0, 10, 3);

        // Setup pixel buffer
        var bb = ByteBuffer.allocateDirect(N * N * Integer.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        var buffer = bb.asIntBuffer();
        var pxBuffer = new PixelBuffer<>(N, N, buffer, PixelFormat.getIntArgbPreInstance());

        Runnable updateBuffer = () -> {
            var xc = xcSlider.getValue();
            var yc = ycSlider.getValue();
            var size = sizeSlider.getValue();

            // Update individual pixels in the buffer
            // (can run on any thread)
            IntStream.range(0, N).parallel().forEach(x -> {
                for (int y = 0; y < N; y++) {
                    double x0 = xc - size / 2 + size * x / N;
                    double y0 = yc - size / 2 + size * y / N;
                    var count = computeIterations(x0, y0);
                    buffer.put((x * N) + y, chooseColor(count));
                }
            });

            // Let JavaFX know that the underlying buffer has changed
            // (needs to run on the FX thread)
            pxBuffer.updateBuffer(obj -> null);
        };

        xcSlider.valueProperty().addListener((obs, prev, value) -> updateBuffer.run());
        ycSlider.valueProperty().addListener((obs, prev, value) -> updateBuffer.run());
        sizeSlider.valueProperty().addListener((obs, prev, value) -> updateBuffer.run());
        updateBuffer.run();

        // Layout
        var imgView = new ImageView(new WritableImage(pxBuffer));
        var imgPane = new Pane(imgView);
        var fitSize = Bindings.min(imgPane.widthProperty(),imgPane.heightProperty());
        imgView.fitWidthProperty().bind(fitSize);
        imgView.fitHeightProperty().bind(fitSize);

        xcSlider.setOrientation(Orientation.VERTICAL);
        ycSlider.setOrientation(Orientation.HORIZONTAL);
        sizeSlider.setOrientation(Orientation.VERTICAL);

        var borderPane = new BorderPane();
        borderPane.setLeft(xcSlider);
        borderPane.setBottom(ycSlider);
        borderPane.setCenter(imgPane);
        borderPane.setRight(sizeSlider);
        stage.setScene(new Scene(borderPane));
        stage.show();
    }

    public int computeIterations(double ci, double c) {
        double zi = 0;
        double z = 0;
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double ziT = 2 * (z * zi);
            double zT = z * z - (zi * zi);
            z = zT + c;
            zi = ziT + ci;

            if (z * z + zi * zi >= 4.0) {
                return i;
            }
        }
        return MAX_ITERATIONS;
    }

    private int chooseColor(int iterations) {
        int a = 0xFF;
        int r = (iterations % 170);
        int g = (iterations % 85);
        int b = iterations;
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    private static final int N = 512;
    private static final int MAX_ITERATIONS = 1000;

}