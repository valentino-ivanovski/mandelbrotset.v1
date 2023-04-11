package com.example.demo2;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TASK1CALL extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        int width = 800;
        int height = 600;
        BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int hue = (int) ((x / (double) width) * 360);
                        int saturation = 100;
                        int brightness = 100;
                        int color = Color.HSBtoRGB(hue / 360.0f, saturation / 100f, brightness / 100f);
                        bImage.setRGB(x, y, color);
                    }
                }
                return null;
            }
        };

        ImageView imageView = new ImageView();
        imageView.setImage(SwingFXUtils.toFXImage(bImage, null));

        task.setOnSucceeded(event -> {
            imageView.setImage(SwingFXUtils.toFXImage(bImage, null));
        });

        new Thread(task).start();

        Scene scene = new Scene(new StackPane(imageView));
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();



    }
}
