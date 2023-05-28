package com.example.mandelbrot;

/*  Mandelbrot Set
 *   Made by: Valntino Ivanovski
 *   Period: First semester of second year in Computer Science
 *   Subject: Programming 3 */

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import javafx.embed.swing.SwingFXUtils;
import javafx.util.Duration;
import javax.imageio.ImageIO;

public class myMandelbrot extends Application {

    int widthWindow = 800, heightWindow = 600;
    double totalIter = 100; //double because of the color calculation
    double xStart = -400, yStart = 0;
    double zoomScale = 200.0; //by dividing by 200 we achieve the width and height of the complex plane to be x: -2 to 2 and y: -1.5 to 1.5 since 400/200 is 2 and 300/200 is 1.5
    double resolutionMultiplier = 1;
    int setMode;
    BorderPane layout;
    Canvas canvas = new Canvas(widthWindow-250, heightWindow);
    WritableImage finalImage;
    WritableImage savedFinalImage;
    double hueMultiplier = 0.8;
    double saturationMultiplier = 1;
    double brightnessMultiplier = 1;
    Color colorOfSet=Color.web("#003333");
    int setColorOfSet;
    long timeToExecute;
    Label timeToExecuteLabel;
    Label timeToExecuteSaveImageLabel;
    int THREADS = Runtime.getRuntime().availableProcessors();
    ProgressBar imageLoad_progressBar = new ProgressBar();
    PauseTransition resizePause = new PauseTransition(Duration.millis(200)); //as long as we are resizing the window, the MandelbrotSet() method is waiting till the resizing stops, and then it waits the final 200ms before its being called.

    @Override
    public void start(Stage stage) {
        stage.setMinHeight(600);
        stage.setMinWidth(385+250);
        stage.setResizable(true);
        Dialog<Integer> startDialog = new Dialog<>(); //return int value when closed, in order to choose mode
        startDialog.setTitle("Mandelbrot Set");

        Image dialogImage = new Image(Objects.requireNonNull(getClass().getResource("/Image.png")).toString());
        ImageView dialogImageView = new ImageView(dialogImage);

        Label chooseMode = new Label("Choose Mode:");
        Font font = new Font("Arial Bold", 15);
        chooseMode.setPadding(new Insets(0, 0, 7, 0));
        chooseMode.setTextFill(Color.web("#633B5D"));
        chooseMode.setFont(font);
        chooseMode.setMinWidth(300);
        chooseMode.setAlignment(Pos.CENTER);

        Button sequentialButton = new Button("Sequential");
        sequentialButton.setMinWidth(76);
        sequentialButton.setOnAction(event -> {
            setMode = 1;
            startDialog.setResult(setMode);
            startDialog.close();
        });

        Button parallelButton = new Button("Parallel");
        parallelButton.setMinWidth(76);
        parallelButton.setOnAction(event -> {
            setMode = 2;
            startDialog.setResult(setMode);
            startDialog.close();
        });

        Button distributedButton = new Button("Distributed");
        distributedButton.setMinWidth(76);
        distributedButton.setOnAction(event -> {
            setMode = 3;
            startDialog.setResult(setMode);
            startDialog.close();
        });

        VBox dialogContents = new VBox(dialogImageView, chooseMode, sequentialButton, parallelButton, distributedButton);
        dialogContents.setSpacing(10);
        dialogContents.setAlignment(Pos.CENTER);

        startDialog.getDialogPane().setContent(dialogContents);

        //make the dialog close when pressing x
        startDialog.getDialogPane().getScene().getWindow().setOnCloseRequest(event -> startDialog.close());

        dialogImageView.requestFocus();
        startDialog.showAndWait();

        if (setMode == 1){
            VBox sidebar = new VBox(10);
            sidebar.setPadding(new Insets(10, 0, 10, 16));
            sidebar.setMinWidth(250);

            timeToExecuteLabel = new Label();
            timeToExecuteSaveImageLabel = new Label("Execution time of saved image: null");

            TextField widthTextField = new TextField(String.valueOf(widthWindow));
            widthTextField.setPrefWidth(50);
            onlyAcceptNumbers(widthTextField);
            Label x = new Label("x");
            TextField heightTextField = new TextField(String.valueOf(heightWindow));
            onlyAcceptNumbers(heightTextField);
            heightTextField.setPrefWidth(50);

            heightTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    heightTextField.getParent().requestFocus();
                    event.consume();
                }
            });

            widthTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    widthTextField.getParent().requestFocus();
                    event.consume();
                }
            });

            HBox imageSizeContent = new HBox(10);
            imageSizeContent.setAlignment(Pos.CENTER_LEFT);
            Button saveImageButton = new Button("Save Image");

            saveImageButton.setOnAction(event ->{
                canvas.widthProperty().unbind();
                canvas.heightProperty().unbind();
                int newWidth = Integer.parseInt(widthTextField.getText());
                int newHeight = Integer.parseInt(heightTextField.getText());
                canvas.setWidth(newWidth);
                canvas.setHeight(newHeight);

                MandelbrotSet();

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                FileChooser.ExtensionFilter extensions = new FileChooser.ExtensionFilter("Images *.jpg, *.png", "*.jpg", "*.png");

                fileChooser.getExtensionFilters().add(extensions);

                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    try {
                        canvas.snapshot(null, savedFinalImage);
                        RenderedImage renderedImage = SwingFXUtils.fromFXImage(savedFinalImage, null);
                        ImageIO.write(renderedImage, "png", file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                canvas.widthProperty().bind(stage.widthProperty().subtract(250));
                canvas.heightProperty().bind(stage.heightProperty());

                timeToExecuteSaveImageLabel.setText("Execution time of saved image: " + timeToExecute/1000.0);
                MandelbrotSet();

                canvas.requestFocus();
            });

            ImageView mainImageView = new ImageView(dialogImage);
            mainImageView.setFitHeight(190);

            Label multiplierLabel = new Label("SSAA (Max 10): ");

            TextField multiplierText = new TextField();
            onlyAcceptNumbers(multiplierText);
            multiplierText.setMaxWidth(38);
            multiplierText.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    multiplierText.getParent().requestFocus();
                    event.consume();
                }
            });

            Button refreshMultiplier = new Button("Refresh");

            refreshMultiplier.setOnAction(event -> {
                double temp = Double.parseDouble(multiplierText.getText());
                if (temp > 0 && temp <= 10){
                    resolutionMultiplier = temp;
                }
                else {
                    resolutionMultiplier = 1;
                }
                MandelbrotSet();
                canvas.requestFocus();
            });

            refreshMultiplier.setMinWidth(68);
            HBox multiplierContent = new HBox(multiplierLabel, multiplierText, refreshMultiplier);
            multiplierContent.setSpacing(12);
            multiplierContent.setAlignment(Pos.CENTER_LEFT);

            sidebar.getChildren().add(mainImageView);

            imageSizeContent.getChildren().addAll(widthTextField, x, heightTextField, saveImageButton);

            Label iterationsLabel = new Label("Iterations:");
            TextField iterationsText = new TextField(String.valueOf((int)totalIter));
            iterationsText.setPrefWidth(60);
            onlyAcceptNumbers(iterationsText);

            Button refreshButton = new Button("Refresh");
            HBox iterationsContent = new HBox(10);
            refreshButton.setOnAction(event -> {
                double temp=Double.parseDouble(iterationsText.getText());
                totalIter = (int)temp;
                MandelbrotSet();
                iterationsText.setText(String.valueOf((int)totalIter));
                canvas.requestFocus();
            });
            iterationsText.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    iterationsText.getParent().requestFocus();
                    event.consume();
                }
            });

            iterationsContent.setAlignment(Pos.CENTER_LEFT);
            refreshButton.setMinWidth(85);
            iterationsContent.getChildren().addAll(iterationsLabel, iterationsText, refreshButton);

            imageLoad_progressBar.setPrefWidth(220);
            imageLoad_progressBar.setMinHeight(20);

            Label labelHSB = new Label("HSB");

            Label selectedColor = new Label("Change the color inside the set:");

            Label enterValues = new Label("Enter HSB values between 0.0 and 1.0:");
            labelHSB.setMinWidth(20);
            labelHSB.setPadding(new Insets(5, 0,0,0));
            Separator separator = new Separator();
            separator.setOrientation(Orientation.HORIZONTAL);
            separator.setPadding(new Insets(0, 16, 0, 0));

            TextField hueText = new TextField();
            onlyAcceptNumbers(hueText);
            hueText.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    hueText.getParent().requestFocus();
                    event.consume();
                }
            });

            TextField saturationText = new TextField();
            onlyAcceptNumbers(saturationText);
            saturationText.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    saturationText.getParent().requestFocus();
                    event.consume();
                }
            });

            TextField brightnessText = new TextField();
            onlyAcceptNumbers(brightnessText);
            brightnessText.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    brightnessText.getParent().requestFocus();
                    event.consume();
                }
            });

            hueText.setMinWidth(40);
            saturationText.setMinWidth(40);
            brightnessText.setMinWidth(40);

            HBox HSB = new HBox();
            HSB.setMaxWidth(200);
            HSB.setSpacing(12);

            Button refreshButtonHSB = new Button("Refresh");
            refreshButtonHSB.setMinWidth(61);

            HSB.getChildren().addAll(hueText, saturationText, brightnessText, refreshButtonHSB);

            refreshButtonHSB.setOnAction(event -> {
                hueMultiplier = Double.parseDouble(hueText.getText());
                saturationMultiplier = Double.parseDouble(saturationText.getText());
                brightnessMultiplier = Double.parseDouble(brightnessText.getText());

                MandelbrotSet();
            });

            ColorPicker colorPicker = new ColorPicker();

            colorPicker.setValue(colorOfSet);

            colorPicker.setOnAction(event -> {
                colorOfSet = colorPicker.getValue();
                MandelbrotSet();
            });

            Button resetInside = new Button("Reset");
            resetInside.setOnAction(actionEvent -> {
                if (setColorOfSet == 2 || setColorOfSet == 1)
                colorOfSet = Color.web("#43003E");
                else colorOfSet=Color.web("#000000");
                MandelbrotSet();
            });

            HBox insideSet = new HBox(colorPicker, resetInside);
            insideSet.setSpacing(12);
            resetInside.setPrefWidth(60);
            colorPicker.setPrefWidth(145);

            Label madeBy = new Label("Made by: Valentino Ivanovski\n           For Programming III\n                   UP FAMNIT");
            madeBy.setTextFill(Paint.valueOf("#A9A9A9"));
            madeBy.setFont(new Font(10));
            VBox madeByContainer = new VBox();
            madeByContainer.setAlignment(Pos.CENTER);
            madeByContainer.setPadding(new Insets(0,20,0,0));
            madeByContainer.getChildren().add(madeBy);

            sidebar.getChildren().addAll(timeToExecuteLabel, imageLoad_progressBar, iterationsContent, imageSizeContent, timeToExecuteSaveImageLabel, multiplierContent, separator, enterValues, HSB, selectedColor, insideSet, madeByContainer);

            layout = new BorderPane();
            layout.setCenter(canvas);
            layout.setRight(sidebar);

            Scene scene = new Scene(layout, widthWindow +250, heightWindow);

            canvas.widthProperty().bind(scene.widthProperty().subtract(250));
            canvas.heightProperty().bind(scene.heightProperty());

            canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
                widthTextField.setText(String.valueOf((int) newVal.doubleValue()));
                resizePause.setOnFinished(e -> MandelbrotSet());
                resizePause.playFromStart();
            });

            canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
                heightTextField.setText(String.valueOf((int) newVal.doubleValue()));
                resizePause.setOnFinished(e -> MandelbrotSet());
                resizePause.playFromStart();
            });


            scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case W, UP -> {
                        if (event.isShiftDown()) {
                            moveUp(50);
                            MandelbrotSet();
                        } else {
                            moveUp(100);
                            MandelbrotSet();
                        }
                        event.consume();
                    }
                    case A, LEFT -> {
                        if (event.isShiftDown()) {
                            moveLeft(50);
                            MandelbrotSet();
                        } else {
                            moveLeft(100);
                            MandelbrotSet();
                        }
                    }
                    case S, DOWN -> {
                        if (event.isShiftDown()) {
                            moveDown(50);
                            MandelbrotSet();
                        } else {
                            moveDown(100);
                            MandelbrotSet();
                        }
                    }
                    case D, RIGHT -> {
                        if (event.isShiftDown()) {
                            moveRight(50);
                            MandelbrotSet();
                        } else {
                            moveRight(100);
                            MandelbrotSet();
                        }
                        event.consume();
                    }
                    case EQUALS -> {zoomIn();MandelbrotSet();}
                    case MINUS -> {zoomOut();MandelbrotSet();}
                    case BACK_SPACE -> {showComplexPlane();MandelbrotSet();}
                    case DIGIT1 -> {colorWhite();colorPicker.setValue(colorOfSet);MandelbrotSet();}
                    case DIGIT2 -> {colorBlack();colorPicker.setValue(colorOfSet);MandelbrotSet();}
                    case DIGIT3 -> {colorHue2();colorPicker.setValue(colorOfSet);MandelbrotSet();}
                    case DIGIT4 -> {colorHue();colorPicker.setValue(colorOfSet);MandelbrotSet();}
                }
            });

            scene.setOnMouseClicked(event -> {
                switch (event.getButton()) {
                    case PRIMARY -> {zoomIn();MandelbrotSet();}
                    case SECONDARY -> {zoomOut();MandelbrotSet();}
                }
            });

            stage.setScene(scene);

            colorWhite();
            MandelbrotSet();

            canvas.requestFocus();
            stage.setTitle("Mandelbrot Set");
            stage.show();
            stage.toFront();
        }
        else if(setMode == 2){

            VBox sidebar = new VBox(10);
            sidebar.setPadding(new Insets(10, 0, 10, 16));
            sidebar.setMinWidth(250);

            timeToExecuteLabel = new Label();
            timeToExecuteSaveImageLabel = new Label("Execution time of saved image: null");

            TextField widthTextField = new TextField(String.valueOf(widthWindow));
            widthTextField.setPrefWidth(50);
            onlyAcceptNumbers(widthTextField);
            Label x = new Label("x");
            TextField heightTextField = new TextField(String.valueOf(heightWindow));
            heightTextField.setPrefWidth(50);
            onlyAcceptNumbers(heightTextField);

            heightTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    heightTextField.getParent().requestFocus();
                    event.consume();
                }
            });

            widthTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    widthTextField.getParent().requestFocus();
                    event.consume();
                }
            });

            HBox imageSizeContent = new HBox(10);
            imageSizeContent.setAlignment(Pos.CENTER_LEFT);
            Button saveImageButton = new Button("Save Image");

            saveImageButton.setOnAction(event ->{
                canvas.widthProperty().unbind();
                canvas.heightProperty().unbind();
                int newWidth = Integer.parseInt(widthTextField.getText());
                int newHeight = Integer.parseInt(heightTextField.getText());
                canvas.setWidth(newWidth);
                canvas.setHeight(newHeight);

                MandelbrotSet(THREADS);

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                FileChooser.ExtensionFilter extensions = new FileChooser.ExtensionFilter("Images *.jpg, *.png", "*.jpg", "*.png");

                fileChooser.getExtensionFilters().add(extensions);

                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    try {
                        canvas.snapshot(null, savedFinalImage);
                        RenderedImage renderedImage = SwingFXUtils.fromFXImage(savedFinalImage, null);
                        ImageIO.write(renderedImage, "png", file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                canvas.widthProperty().bind(stage.widthProperty().subtract(250));
                canvas.heightProperty().bind(stage.heightProperty());

                timeToExecuteSaveImageLabel.setText("Execution time of saved image: " + timeToExecute /1000.0);
                MandelbrotSet(THREADS);

                canvas.requestFocus();

            });

            ImageView mainImageView = new ImageView(dialogImage);
            mainImageView.setFitHeight(190);

            Label multiplierLabel = new Label("SSAA (Max 10): ");

            TextField multiplierText = new TextField();
            onlyAcceptNumbers(multiplierText);
            multiplierText.setMaxWidth(38);
            multiplierText.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    multiplierText.getParent().requestFocus();
                    event.consume();
                }
            });

            Button refreshMultiplier = new Button("Refresh");

            refreshMultiplier.setOnAction(event -> {
                double temp = Double.parseDouble(multiplierText.getText());
                if (temp > 0 && temp <= 10){
                    resolutionMultiplier = temp;
                }
                else {
                    resolutionMultiplier = 1;
                }
                MandelbrotSet(THREADS);
                canvas.requestFocus();
            });

            refreshMultiplier.setMinWidth(68);
            HBox multiplierContent = new HBox(multiplierLabel, multiplierText, refreshMultiplier);
            multiplierContent.setSpacing(12);
            multiplierContent.setAlignment(Pos.CENTER_LEFT);

            sidebar.getChildren().add(mainImageView);

            imageSizeContent.getChildren().addAll(widthTextField, x, heightTextField, saveImageButton);

            Label iterationsLabel = new Label("Iterations:");
            TextField iterationsText = new TextField(String.valueOf((int)totalIter));
            iterationsText.setPrefWidth(60);
            onlyAcceptNumbers(iterationsText);

            Button refreshButton = new Button("Refresh");
            HBox iterationsContent = new HBox(10);
            refreshButton.setOnAction(event -> {
                double temp=Double.parseDouble(iterationsText.getText());
                totalIter = (int)temp;
                MandelbrotSet(THREADS);
                iterationsText.setText(String.valueOf((int)totalIter));
                canvas.requestFocus();
            });
            iterationsText.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    iterationsText.getParent().requestFocus();
                    event.consume();
                }
            });

            iterationsContent.setAlignment(Pos.CENTER_LEFT);
            refreshButton.setMinWidth(85);
            iterationsContent.getChildren().addAll(iterationsLabel, iterationsText, refreshButton);

            imageLoad_progressBar.setPrefWidth(220);
            imageLoad_progressBar.setMinHeight(20);

            Label labelHSB = new Label("HSB");

            Label selectedColor = new Label("Change the color inside the set:");

            Label enterValues = new Label("Enter HSB values between 0.0 and 1.0:");
            labelHSB.setMinWidth(20);
            labelHSB.setPadding(new Insets(5, 0,0,0));
            Separator separator = new Separator();
            separator.setOrientation(Orientation.HORIZONTAL);
            separator.setPadding(new Insets(0, 16, 0, 0));

            TextField hueText = new TextField();
            onlyAcceptNumbers(hueText);
            hueText.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    hueText.getParent().requestFocus();
                    event.consume();
                }
            });

            TextField saturationText = new TextField();
            onlyAcceptNumbers(saturationText);
            saturationText.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    saturationText.getParent().requestFocus();
                    event.consume();
                }
            });

            TextField brightnessText = new TextField();
            onlyAcceptNumbers(brightnessText);
            brightnessText.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    brightnessText.getParent().requestFocus();
                    event.consume();
                }
            });

            hueText.setMinWidth(40);
            saturationText.setMinWidth(40);
            brightnessText.setMinWidth(40);
            hueText.setMinWidth(40);
            saturationText.setMinWidth(40);
            brightnessText.setMinWidth(40);

            HBox HSB = new HBox();
            HSB.setMaxWidth(200);
            HSB.setSpacing(12);

            Button refreshButtonHSB = new Button("Refresh");
            refreshButtonHSB.setMinWidth(61);

            HSB.getChildren().addAll(hueText, saturationText, brightnessText, refreshButtonHSB);

            refreshButtonHSB.setOnAction(event -> {
                hueMultiplier = Double.parseDouble(hueText.getText());
                saturationMultiplier = Double.parseDouble(saturationText.getText());
                brightnessMultiplier = Double.parseDouble(brightnessText.getText());

                MandelbrotSet(THREADS);
            });

            ColorPicker colorPicker = new ColorPicker();

            colorPicker.setValue(colorOfSet);

            colorPicker.setOnAction(event -> {
                colorOfSet = colorPicker.getValue();
                MandelbrotSet(THREADS);
            });

            Button resetInside = new Button("Reset");
            resetInside.setOnAction(actionEvent -> {
                if (setColorOfSet == 2 || setColorOfSet == 1)
                    colorOfSet = Color.web("#43003E");
                else colorOfSet=Color.web("#000000");
                MandelbrotSet(THREADS);
            });

            HBox insideSet = new HBox(colorPicker, resetInside);
            insideSet.setSpacing(12);
            resetInside.setPrefWidth(60);
            colorPicker.setPrefWidth(145);

            Label madeBy = new Label("Made by: Valentino Ivanovski\n           For Programming III\n                   UP FAMNIT");
            madeBy.setTextFill(Paint.valueOf("#A9A9A9"));
            madeBy.setFont(new Font(10));
            VBox madeByContainer = new VBox();
            madeByContainer.setAlignment(Pos.CENTER);
            madeByContainer.setPadding(new Insets(0,20,0,0));
            madeByContainer.getChildren().add(madeBy);

            sidebar.getChildren().addAll(timeToExecuteLabel, imageLoad_progressBar, iterationsContent, imageSizeContent, timeToExecuteSaveImageLabel, multiplierContent, separator, enterValues, HSB, selectedColor, insideSet, madeByContainer);

            layout = new BorderPane();
            layout.setCenter(canvas);
            layout.setRight(sidebar);

            Scene scene = new Scene(layout, widthWindow +250, heightWindow);

            canvas.widthProperty().bind(scene.widthProperty().subtract(250));
            canvas.heightProperty().bind(scene.heightProperty());

            canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
                widthTextField.setText(String.valueOf((int) newVal.doubleValue()));
                resizePause.setOnFinished(e -> MandelbrotSet(THREADS));
                resizePause.playFromStart();
            });

            canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
                heightTextField.setText(String.valueOf((int) newVal.doubleValue()));
                resizePause.setOnFinished(e -> MandelbrotSet(THREADS));
                resizePause.playFromStart();
            });


            scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case W, UP -> {
                        if (event.isShiftDown()) {
                            moveUp(10);
                            MandelbrotSet(THREADS);
                        } else {
                            moveUp(100);
                            MandelbrotSet(THREADS);
                        }
                        event.consume();
                    }
                    case A, LEFT -> {
                        if (event.isShiftDown()) {
                            moveLeft(10);
                            MandelbrotSet(THREADS);
                        } else {
                            moveLeft(100);
                            MandelbrotSet(THREADS);
                        }
                    }
                    case S, DOWN -> {
                        if (event.isShiftDown()) {
                            moveDown(10);
                            MandelbrotSet(THREADS);
                        } else {
                            moveDown(100);
                            MandelbrotSet(THREADS);
                        }
                    }
                    case D, RIGHT -> {
                        if (event.isShiftDown()) {
                            moveRight(10);
                            MandelbrotSet(THREADS);
                        } else {
                            moveRight(100);
                            MandelbrotSet(THREADS);
                        }
                        event.consume();
                    }
                    case EQUALS -> {zoomIn();MandelbrotSet(THREADS);}
                    case MINUS -> {zoomOut();MandelbrotSet(THREADS);}
                    case BACK_SPACE -> {
                        showComplexPlane();MandelbrotSet(THREADS);}
                    case DIGIT1 -> {colorWhite();colorPicker.setValue(colorOfSet);MandelbrotSet(THREADS);}
                    case DIGIT2 -> {colorBlack();colorPicker.setValue(colorOfSet);MandelbrotSet(THREADS);}
                    case DIGIT3 -> {colorHue2();colorPicker.setValue(colorOfSet);MandelbrotSet(THREADS);}
                    case DIGIT4 -> {colorHue();colorPicker.setValue(colorOfSet);MandelbrotSet(THREADS);}
                }
            });

            scene.setOnMouseClicked(event -> {
                switch (event.getButton()) {
                    case PRIMARY -> {zoomIn();MandelbrotSet(THREADS);}
                    case SECONDARY -> {zoomOut();MandelbrotSet(THREADS);}
                }
            });

            stage.setScene(scene);

            colorWhite();
            MandelbrotSet(THREADS);

            canvas.requestFocus();
            stage.setTitle("Mandelbrot Set");
            stage.show();
            stage.toFront();
        }
        else if(setMode == 3){
            try {
                File mpiProgram = new File("/Users/tino/Local Files/Class Notes/Second Year/First Semester/Mandelbrot Set Parallel/untitled/src");
                String home = System.getenv("MPJ_HOME");

                ProcessBuilder pbCompile = new ProcessBuilder("javac", "-cp", ".:" + home + "/lib/mpj.jar", "MandelbrotSetMPI.java");
                pbCompile.directory(mpiProgram);
                pbCompile.redirectErrorStream(true);
                pbCompile.redirectOutput(ProcessBuilder.Redirect.INHERIT);

                Process javacProcess = pbCompile.start();
                javacProcess.waitFor();

                ProcessBuilder pbRun = new ProcessBuilder(home + "/bin/mpjrun.sh", "-np", String.valueOf(THREADS), "MandelbrotSetMPI");
                pbRun.directory(mpiProgram);
                pbRun.redirectErrorStream(true);
                pbRun.redirectOutput(ProcessBuilder.Redirect.INHERIT);

                Process mpjProcess = pbRun.start();
                mpjProcess.waitFor();

            } catch (Exception e) {
                e.printStackTrace();
            }

            stage.setTitle("Mandelbrot Set");

            BorderPane layout = new BorderPane();
            Canvas canvas = new Canvas(800, 600);
            //⬇️ image that is being saved on my disk from the distributive program which is being run above
            Image mandelbrotImage = new Image("file:/Users/tino/Local Files/Class Notes/Second Year/First Semester/Mandelbrot Set Parallel/untitled/mandelbrot.png");
            canvas.getGraphicsContext2D().drawImage(mandelbrotImage,0,0);
            layout.setCenter(canvas);

            Scene scene = new Scene(layout, 800, 600);

            stage.setScene(scene);
            stage.show();
        }
    }

    public void calcProgress(ProgressBar progressBar){ //sequential progress bar
        Duration duration = Duration.seconds(timeToExecute /1000.0);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(duration, new KeyValue(progressBar.progressProperty(), 1))
        );
        timeline.play();
    }

    public void onlyAcceptNumbers(TextField text) {
        text.textProperty().addListener((observable, oldNum, newNum) -> {
            if (!newNum.matches("[0-9]*[.]?[0-9]*")) {
                text.setText(newNum.replaceAll("[^0-9.]", ""));
            }
        });
    }

    public void MandelbrotSet() {
        long startTime = System.currentTimeMillis();
        int scaledWidth = (int) (canvas.getWidth() * resolutionMultiplier);
        int scaledHeight = (int) (canvas.getHeight() * resolutionMultiplier);
        WritableImage image = new WritableImage(scaledWidth, scaledHeight);
        savedFinalImage = image;
        double centerX = scaledWidth / 2.0;
        double centerY = scaledHeight / 2.0;
        for (int x = 0; x < scaledWidth; x++) {
            for (int y = 0; y < scaledHeight; y++) {
                double c_real = xStart / widthWindow + (x - centerX) / (zoomScale * resolutionMultiplier);
                double c_imag = yStart / heightWindow + (y - centerY) / (zoomScale * resolutionMultiplier);
                double z_real = 0;
                double z_imag = 0;

                int currentIterations;

                for (currentIterations = 0; currentIterations < totalIter && (z_real * z_real) + (z_imag * z_imag) < 4; currentIterations++) {
                    double old_z_real = z_real;
                    z_real = (z_real * z_real) - (z_imag * z_imag) + c_real;
                    z_imag = 2 * (old_z_real * z_imag) + c_imag;
                }

                if (currentIterations == totalIter) {
                    image.getPixelWriter().setColor(x, y, colorOfSet);
                } else if (setColorOfSet == 1) {
                    image.getPixelWriter().setColor(x, y, Color.hsb(hueMultiplier * 360 * (currentIterations) % 360, saturationMultiplier, brightnessMultiplier));
                } else if (setColorOfSet == 3) {
                    image.getPixelWriter().setColor(x, y, Color.hsb(hueMultiplier * 360 % 360, (currentIterations / totalIter) * saturationMultiplier, brightnessMultiplier));
                } else if (setColorOfSet == 4){
                    image.getPixelWriter().setColor(x, y, Color.hsb(hueMultiplier * 360 % 360, saturationMultiplier, (currentIterations / totalIter)));
                } else if (setColorOfSet == 2){
                    image.getPixelWriter().setColor(x, y, Color.hsb(hueMultiplier * currentIterations * 7 % 360, saturationMultiplier, brightnessMultiplier));
                }
            }
        }

        WritableImage downscaledImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.getGraphicsContext2D().drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
        savedFinalImage = downscaledImage;
        long finishTime = System.currentTimeMillis();
        timeToExecute = (finishTime - startTime);
        timeToExecuteLabel.setText("Execution time: " + timeToExecute / 1000.0 + "  (Threads: 1)");
        calcProgress(imageLoad_progressBar);
    }

    public void MandelbrotSet(int n) {
        long beginTime = System.currentTimeMillis();
        int scaledHeight = (int) (canvas.getHeight() * resolutionMultiplier);
        int section = scaledHeight / n;
        List<MandelbrotService> services = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(n);

        for (int i = 0; i < n; i++) {
            int begin = i * section; //compute the start point for each section
            int end = begin + section;

            //creating a service for each section of the image
            MandelbrotService service = new MandelbrotService(begin, end);
            service.setOnSucceeded(event -> {
                latch.countDown(); //decrease the latch count when the service finishes
                service.cancel(); //cancel the service
            });
            service.start(); //start the service
            services.add(service);// add the service to the services list
        }

        //binding the progress of the services to the progress bar
        DoubleBinding totalProgressBinding = Bindings.createDoubleBinding(() -> services.stream().mapToDouble(Service::getProgress).sum() / n, services.stream().map(Service::progressProperty).toArray(DoubleExpression[]::new));
        imageLoad_progressBar.progressProperty().bind(totalProgressBinding);

        //thread to wait for all the services to complete their tasks
        Thread waitForCompletion = new Thread(() -> {
            try {
                latch.await(); //wait until the countdown latch counts down to zero
            } catch (InterruptedException e) {
                e.printStackTrace(); //print the stack trace if an InterruptedException occurs
            }

            //this code will run after all the services complete their tasks
            Platform.runLater(() -> {
                finalImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                List<WritableImage> images = services.stream().map(Service::getValue).collect(Collectors.toList());
                mergeImages(images);
                canvas.getGraphicsContext2D().drawImage(finalImage, 0, 0, canvas.getWidth(), canvas.getHeight());
                savedFinalImage = finalImage;
                long finishTime = System.currentTimeMillis();
                timeToExecute = (finishTime - beginTime);
                timeToExecuteLabel.setText("Execution time: " + timeToExecute / 1000.0 + "  (Threads: "+Runtime.getRuntime().availableProcessors() + ")");
                imageLoad_progressBar.progressProperty().unbind(); //unbind the progress bar
            });
        });

        waitForCompletion.start(); //start the waitForCompletion thread
    }

    private class MandelbrotService extends Service<WritableImage> {
        private final int begin;
        private final int end;

        public MandelbrotService(int start, int end) {
            this.begin = start;
            this.end = end;
        }

        @Override
        protected Task<WritableImage> createTask() {
            return new Task<>() {
                @Override
                protected WritableImage call() {
                    int scaledLocalWidth = (int) (canvas.getWidth() * resolutionMultiplier);
                    int scaledHeight = (int) (canvas.getHeight() * resolutionMultiplier);
                    int localHeight = end - begin;
                    WritableImage localImage = new WritableImage(scaledLocalWidth, localHeight);
                    PixelWriter pixelWriter = localImage.getPixelWriter();
                    for (int y = begin; y < end; y++) {
                        for (int x = 0; x < scaledLocalWidth; x++) {
                            double centerX = scaledLocalWidth / 2.0;
                            double centerY = scaledHeight / 2.0;
                            double c_real = xStart / widthWindow + (x - centerX) / (zoomScale * resolutionMultiplier);
                            double c_imag = yStart / heightWindow + (y - centerY) / (zoomScale * resolutionMultiplier);
                            double z_real = 0;
                            double z_imag = 0;

                            int currentIterations;

                            for (currentIterations = 0; currentIterations < totalIter && (z_real * z_real) + (z_imag * z_imag) < 4; currentIterations++) {
                                double old_z_real = z_real;
                                z_real = (z_real * z_real) - (z_imag * z_imag) + c_real;
                                z_imag = 2 * (old_z_real * z_imag) + c_imag;
                            }

                            if (currentIterations == totalIter) {
                                pixelWriter.setColor(x, y - begin, colorOfSet);
                            } else if (setColorOfSet == 1) {
                                pixelWriter.setColor(x, y - begin, Color.hsb(hueMultiplier * 360 * (currentIterations) % 360+240, saturationMultiplier, brightnessMultiplier));
                            } else if (setColorOfSet == 3) {
                                pixelWriter.setColor(x, y - begin, Color.hsb(hueMultiplier * 360 % 360, (currentIterations/ totalIter)* saturationMultiplier, brightnessMultiplier));
                            } else if (setColorOfSet == 4){
                                pixelWriter.setColor(x, y - begin, Color.hsb(hueMultiplier * 360 % 360, saturationMultiplier, currentIterations / totalIter));
                            } else if (setColorOfSet == 2){
                                pixelWriter.setColor(x, y - begin, Color.hsb(hueMultiplier * currentIterations * 7 % 360, saturationMultiplier, brightnessMultiplier));
                            }
                        }
                        updateProgress(y - begin + 1, end - begin); //update the progress of the task, so it can be transferred to the progress bar
                    }
                    return localImage;
                }
            };
        }
    }
    private void mergeImages(List<WritableImage> images) {
        int totalHeight = images.stream().mapToInt(img -> (int) img.getHeight()).sum();
        WritableImage combinedImage = new WritableImage((int) (canvas.getWidth() * resolutionMultiplier), totalHeight);
        PixelWriter pixelWriter = combinedImage.getPixelWriter();
        int currentHeight = 0;
        for (WritableImage img : images) {
            int imgHeight = (int) img.getHeight();
            int imgWidth = (int) img.getWidth();
            pixelWriter.setPixels(0, currentHeight, imgWidth, imgHeight, img.getPixelReader(), 0, 0);
            currentHeight += imgHeight;
        }

        WritableImage downscaledImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.getGraphicsContext2D().drawImage(combinedImage, 0, 0, canvas.getWidth(), canvas.getHeight());
        savedFinalImage = downscaledImage;
    }

    public void moveUp(int number) {
        yStart -= (heightWindow / zoomScale) * number;
    }

    public void moveDown(int number) {
        yStart += (heightWindow / zoomScale) * number;
    }

    public void moveLeft(int number) {
        xStart -= (widthWindow / zoomScale) * number;
    }

    public void moveRight(int number) {
        xStart += (widthWindow / zoomScale) * number;
    }

    public void zoomIn() {
        zoomScale /= 0.5;
    }

    public void zoomOut() {
        zoomScale *= 0.5;
    }

    public void showComplexPlane() {
        colorHue2();
        zoomScale = 140.0;
        xStart = 0;
        yStart = 0;
    }

    public void colorWhite() {
        hueMultiplier = 0.9;
        saturationMultiplier = 1;
        brightnessMultiplier = 1;
        colorOfSet = Color.web("#003333");
        setColorOfSet = 3;
    }
    public void colorBlack() {
        hueMultiplier = 0;
        saturationMultiplier = 0;
        brightnessMultiplier = totalIter;
        colorOfSet = Color.web("#0c0c0c");
        setColorOfSet = 4;
    }
    public void colorHue() {
        hueMultiplier = 0.7;
        saturationMultiplier = 0.7;
        brightnessMultiplier = 1.0;
        colorOfSet = Color.web("#43003E");
        setColorOfSet = 1;
    }
    public void colorHue2(){
        hueMultiplier = 0.7;
        saturationMultiplier = 0.7;
        brightnessMultiplier = 1.0;
        colorOfSet = Color.web("#221b39");
        setColorOfSet = 2;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
