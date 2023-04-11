package com.example.demo2;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class OVAOVA extends Application {

    int width = 800;
    int height = 600;
    int n = 8;
    int MAX_ITERATIONS=100;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ImageView imageView = new ImageView();
        StackPane stackPane = new StackPane(imageView);
        MandelbrotService service = new MandelbrotService(width, height, n, MAX_ITERATIONS);
        imageView.imageProperty().bind(service.valueProperty());
        service.start();

        Scene scene = new Scene(stackPane, width, height);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
