package com.example.mandelbrot;

/*  Mandelbrot Set
 *   Made by: Valntino Ivanovski
 *   Period: First semester of second year in Computer Science
 *   Subject: Programming 3 */

/* =========================================Imports================================================ */

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

    /* =========================================Variables================================================ */

    double width = 800;
    double height = 600;
    double maximumIterations = 100;
    double zoom = 250.0;
    double xPos = -470; //add 0 on both of the coordinates for the accurate plane
    double yPos = 0;
    double superSamplingFactor = 1;
    WritableImage image;
    double hueFactor = 0.8; // Change this factor to adjust the rate of color change
    double saturationFactor = 1; // Set the saturation to a high value for vibrant colors
    double brightnessFactor = 1;
    int determineColor;
    int THREADS = Runtime.getRuntime().availableProcessors();
    long resultCompilation;
    private int number;
    ProgressBar progressBar = new ProgressBar();
    Label compilationTimeLabel;
    Label imageCompilationLabel;
    BorderPane mainLayout;
    Canvas canvas = new Canvas((int)width, (int)height);
    WritableImage actualImage;
    Color colorOfSet=Color.web("#43003E");
    private static final Duration RESIZE_DELAY = Duration.millis(200);
    private final PauseTransition pauseTransition = new PauseTransition(RESIZE_DELAY);

    /* =========================================MainMethod================================================ */

    public static void main(String[] args) {
        launch(args);
    }

    /* =========================================DialogStage================================================= */

    @Override
    public void start(Stage stage) {
        //creating a dialog from where you choose the execution mode
        stage.setMinHeight(575);
        stage.setMinWidth(385+250);
        stage.setResizable(true);
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Choose Mode");
        dialog.setHeaderText(null);

        Image imageLogo = new Image(Objects.requireNonNull(getClass().getResource("/Image.png")).toString());
        ImageView imageViewDialog = new ImageView(imageLogo);

        Label label = new Label("Choose Mode:");
        Font font = new Font("Arial Bold", 15);
        label.setPadding(new Insets(0, 0, 7, 0));
        label.setTextFill(Color.web("#633B5D"));
        label.setFont(font);
        label.setMinWidth(300);
        label.setAlignment(Pos.CENTER);

        Button sequentialButton = new Button("Sequential");
        sequentialButton.setMinWidth(76);
        sequentialButton.setOnAction(event -> {
            number = 1;
            dialog.setResult(number);
            dialog.close();
        });

        Button parallelButton = new Button("Parallel");
        parallelButton.setMinWidth(76);
        parallelButton.setOnAction(event -> {
            number = 2;
            dialog.setResult(number);
            dialog.close();
        });

        Button distributedButton = new Button("Distributed");
        distributedButton.setMinWidth(76);
        distributedButton.setOnAction(event -> {
            number = 3;
            dialog.setResult(number);
            dialog.close();
        });

        VBox content = new VBox(imageViewDialog, label, sequentialButton, parallelButton, distributedButton);
        content.setSpacing(10);
        content.setAlignment(Pos.CENTER);

        dialog.getDialogPane().setContent(content); //adding the vbox to the dialog

        // make the dialog close when pressing x
        dialog.getDialogPane().getScene().getWindow().setOnCloseRequest(event -> dialog.close());

        imageViewDialog.requestFocus();
        dialog.showAndWait();

        /* ============================================SequentialStage================================================ */

        if (number == 1){
            /*SIDEBAR*/
            VBox infoBox = new VBox(10);
            infoBox.setPadding(new Insets(10, 0, 10, 16));
            infoBox.setMinWidth(250);

            compilationTimeLabel = new Label();
            imageCompilationLabel = new Label("Execution time of saved image: null");

            TextField widthTextField = new TextField(String.valueOf((int)width));
            widthTextField.setPrefWidth(50);
            onlyNumbers(widthTextField);
            Label x = new Label("x");
            TextField heightTextField = new TextField(String.valueOf((int)height));
            onlyNumbers(heightTextField);
            heightTextField.setPrefWidth(50);

            heightTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    heightTextField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            widthTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    widthTextField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            HBox imageSizeBox = new HBox(10);
            imageSizeBox.setAlignment(Pos.CENTER_LEFT);
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
                FileChooser.ExtensionFilter extensions = new FileChooser.ExtensionFilter("Images *.jpg, *.png", "*.jpg", "*.png");  //allowing the image to be saved as png or jpg

                fileChooser.getExtensionFilters().add(extensions);

                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    try {
                        canvas.snapshot(null, actualImage);
                        RenderedImage renderedImage = SwingFXUtils.fromFXImage(actualImage, null);
                        ImageIO.write(renderedImage, "png", file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                canvas.widthProperty().bind(stage.widthProperty().subtract(250));
                canvas.heightProperty().bind(stage.heightProperty());

                imageCompilationLabel.setText("Execution time of saved image: " + resultCompilation/1000.0);
                MandelbrotSet();

                canvas.requestFocus();

            });

            //adding the image to an imageview
            ImageView imageViewLogo = new ImageView(imageLogo);
            imageViewLogo.setFitHeight(190);

            Label samplingLabel = new Label("SSAA (Caution!): ");

            TextField samplingText = new TextField();
            onlyNumbers(samplingText);
            samplingText.setMaxWidth(38);
            samplingText.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    samplingText.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            Button refreshSampling = new Button("Refresh");

            refreshSampling.setOnAction(event -> {
                double temp = Double.parseDouble(samplingText.getText());
                if (temp > 0 && temp <= 10){
                    superSamplingFactor = temp;
                }
                else {
                    superSamplingFactor = 1;
                }
                MandelbrotSet();
                canvas.requestFocus();
            });

            HBox samplingBox = new HBox(samplingLabel, samplingText, refreshSampling);
            samplingBox.setSpacing(12);
            samplingBox.setAlignment(Pos.CENTER_LEFT);

            infoBox.getChildren().add(imageViewLogo);

            imageSizeBox.getChildren().addAll(widthTextField, x, heightTextField, saveImageButton);

            Label iterationsLabel = new Label("Iterations: " + (int)maximumIterations);
            TextField iterationsTextField = new TextField();
            iterationsTextField.setPrefWidth(60);
            onlyNumbers(iterationsTextField);

            Button refreshButton = new Button("Refresh");
            HBox iterationsBox = new HBox(10);
            refreshButton.setOnAction(event -> {
                double temp=Double.parseDouble(iterationsTextField.getText());
                maximumIterations = (int)temp;
                MandelbrotSet();
                iterationsLabel.setText("Iterations: "+(int)maximumIterations);
                iterationsTextField.setText("");
                canvas.requestFocus();
            });
            iterationsTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    iterationsTextField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            iterationsBox.setAlignment(Pos.CENTER_LEFT);
            iterationsBox.getChildren().addAll(iterationsLabel, iterationsTextField, refreshButton);

            progressBar.setPrefWidth(220);

            Label labelHSB = new Label("HSB");

            Label selectedColor = new Label("Change the color inside the set:");

            Label enterValues = new Label("Enter HSB values between 0.0 and 1.0:");
            labelHSB.setMinWidth(20);
            labelHSB.setPadding(new Insets(5, 0,0,0));
            Separator separator = new Separator();
            separator.setOrientation(Orientation.HORIZONTAL);
            separator.setPadding(new Insets(0, 16, 0, 0));

            TextField hueField = new TextField();
            onlyNumbers(hueField);
            hueField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    hueField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            TextField saturationField = new TextField();
            onlyNumbers(saturationField);
            saturationField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    saturationField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            TextField brightnessField = new TextField();
            onlyNumbers(brightnessField);
            brightnessField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    brightnessField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            hueField.setMinWidth(40);
            saturationField.setMinWidth(40);
            brightnessField.setMinWidth(40);

            HBox HSB = new HBox();
            HSB.setMaxWidth(200);
            HSB.setSpacing(12);

            Button refreshButtonHSB = new Button("Refresh");
            refreshButtonHSB.setMinWidth(61);

            HSB.getChildren().addAll(hueField, saturationField, brightnessField, refreshButtonHSB);

            refreshButtonHSB.setOnAction(event -> {
                try {
                    hueFactor = Double.parseDouble(hueField.getText());
                    saturationFactor = Double.parseDouble(saturationField.getText());
                    brightnessFactor = Double.parseDouble(brightnessField.getText());

                    System.out.println("Hue: " + hueFactor);
                    System.out.println("Saturation: " + saturationFactor);
                    System.out.println("Brightness: " + brightnessFactor);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter valid numbers.");
                }
                MandelbrotSet();
            });

            ColorPicker colorPicker = new ColorPicker();

            // Set the initial color
            colorPicker.setValue(Color.WHITE);

            // Add an action listener to store the selected color in a variable
            colorPicker.setOnAction(event -> {
                colorOfSet = colorPicker.getValue();
                System.out.println("Selected Color: " + colorOfSet.toString());
                MandelbrotSet();
            });

            Button resetInside = new Button("Reset");
            resetInside.setOnAction(actionEvent -> {
                if (determineColor == 2 || determineColor == 1)
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

            infoBox.getChildren().addAll(compilationTimeLabel, progressBar, iterationsBox, imageSizeBox, imageCompilationLabel, samplingBox, separator, enterValues, HSB, selectedColor, insideSet, madeByContainer);

            mainLayout = new BorderPane();
            mainLayout.setCenter(canvas);
            mainLayout.setRight(infoBox);

            /*SIDEBAR*/

            Scene scene = new Scene(mainLayout, width+250, height);

            canvas.widthProperty().bind(scene.widthProperty().subtract(250));
            canvas.heightProperty().bind(scene.heightProperty());

            canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
                widthTextField.setText(String.valueOf((int) newVal.doubleValue()));
                pauseTransition.setOnFinished(e -> MandelbrotSet());
                pauseTransition.playFromStart();
            });

            canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
                heightTextField.setText(String.valueOf((int) newVal.doubleValue()));
                pauseTransition.setOnFinished(e -> MandelbrotSet());
                pauseTransition.playFromStart();
            });


            scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case W, UP -> {
                        if (event.isShiftDown()) {
                            up(50);
                            MandelbrotSet();
                        } else {
                            up(100);
                            MandelbrotSet();
                        }
                        event.consume();
                    }
                    case A, LEFT -> {
                        if (event.isShiftDown()) {
                            left(50);
                            MandelbrotSet();
                        } else {
                            left(100);
                            MandelbrotSet();
                        }
                    }
                    case S, DOWN -> {
                        if (event.isShiftDown()) {
                            down(50);
                            MandelbrotSet();
                        } else {
                            down(100);
                            MandelbrotSet();
                        }
                    }
                    case D, RIGHT -> {
                        if (event.isShiftDown()) {
                            right(50);
                            MandelbrotSet();
                        } else {
                            right(100);
                            MandelbrotSet();
                        }
                        event.consume();
                    }
                    case EQUALS -> {zoomIn();MandelbrotSet();}
                    case MINUS -> {zoomOut();MandelbrotSet();}
                    case BACK_SPACE -> {reset();MandelbrotSet();}
                    case DIGIT1 -> {colorHue();MandelbrotSet();}
                    case DIGIT2 -> {colorHue2();MandelbrotSet();}
                    case DIGIT3 -> {colorLight();MandelbrotSet();}
                    case DIGIT4 -> {colorDark();MandelbrotSet();}
                }
            });     //key listener
            scene.setOnMouseClicked(event -> {
                switch (event.getButton()) {
                    case PRIMARY -> {
                        zoom /= 0.7;
                        MandelbrotSet();
                        //calcProgress(progressBar);
                    }
                    case SECONDARY -> {
                        zoom *= 0.7;
                        MandelbrotSet();
                        //calcProgress(progressBar);
                    }
                }
            });   //mouse listener for easier zoom

            stage.setScene(scene);

            colorHue();
            MandelbrotSet();
            System.out.println("hue"+hueFactor);
            System.out.println("sat"+saturationFactor);
            System.out.println("bright"+brightnessFactor);

            canvas.requestFocus();
            stage.setTitle("Mandelbrot Set");
            stage.show();
        }
        else if(number==2){

            /* ============================================ParallelStage================================================ */

            /*SIDEBAR*/
            VBox infoBox = new VBox(10);
            infoBox.setPadding(new Insets(10, 0, 10, 16));
            infoBox.setMinWidth(250);

            compilationTimeLabel = new Label();
            imageCompilationLabel = new Label("Execution time of saved image: null");

            TextField widthTextField = new TextField(String.valueOf((int)width));
            widthTextField.setPrefWidth(50);
            onlyNumbers(widthTextField);
            Label x = new Label("x");
            TextField heightTextField = new TextField(String.valueOf((int)height));
            heightTextField.setPrefWidth(50);
            onlyNumbers(heightTextField);

            heightTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    heightTextField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            widthTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    widthTextField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            HBox imageSizeBox = new HBox(10);
            imageSizeBox.setAlignment(Pos.CENTER_LEFT);
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
                FileChooser.ExtensionFilter extensions = new FileChooser.ExtensionFilter("Images *.jpg, *.png", "*.jpg", "*.png");  //allowing the image to be saved as png or jpg

                fileChooser.getExtensionFilters().add(extensions);

                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    try {
                        canvas.snapshot(null, actualImage);
                        RenderedImage renderedImage = SwingFXUtils.fromFXImage(actualImage, null);
                        ImageIO.write(renderedImage, "png", file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                canvas.widthProperty().bind(stage.widthProperty().subtract(250));
                canvas.heightProperty().bind(stage.heightProperty());

                imageCompilationLabel.setText("Execution time of saved image: " + resultCompilation/1000.0);
                MandelbrotSet(THREADS);

                canvas.requestFocus();

            });

            //adding the image to an imageview
            ImageView imageViewLogo = new ImageView(imageLogo);

            imageViewLogo.setFitHeight(190);
            Label samplingLabel = new Label("SSAA (Caution!): ");
            TextField samplingText = new TextField();
            onlyNumbers(samplingText);
            samplingText.setMaxWidth(38);
            samplingText.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    samplingText.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });
            Button refreshSampling = new Button("Refresh");
            refreshSampling.setOnAction(event -> {
                double temp = Double.parseDouble(samplingText.getText());
                if (temp > 0 && temp <= 10){
                    superSamplingFactor = temp;
                }
                else {
                    superSamplingFactor = 1;
                }
                MandelbrotSet(THREADS);
                canvas.requestFocus();
            });
            HBox samplingBox = new HBox(samplingLabel, samplingText, refreshSampling);
            samplingBox.setSpacing(12);
            samplingBox.setAlignment(Pos.CENTER_LEFT);

            infoBox.getChildren().add(imageViewLogo);

            imageSizeBox.getChildren().addAll(widthTextField, x, heightTextField, saveImageButton);

            Label iterationsLabel = new Label("Iterations: " + (int)maximumIterations);
            TextField iterationsTextField = new TextField();
            iterationsTextField.setPrefWidth(60);
            onlyNumbers(iterationsTextField);

            Button refreshButton = new Button("Refresh");
            HBox iterationsBox = new HBox(10);
            refreshButton.setOnAction(event -> {
                double temp=Double.parseDouble(iterationsTextField.getText());
                maximumIterations = (int)temp;
                MandelbrotSet(THREADS);
                iterationsLabel.setText("Iterations: "+(int)maximumIterations);
                iterationsTextField.setText("");
                canvas.requestFocus();
            });
            iterationsTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    iterationsTextField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            iterationsBox.setAlignment(Pos.CENTER_LEFT);
            iterationsBox.getChildren().addAll(iterationsLabel, iterationsTextField, refreshButton);

            progressBar.setPrefWidth(220);

            Label labelHSB = new Label("HSB");

            Label selectedColor = new Label("Change the color inside the set:");

            Label enterValues = new Label("Enter HSB values between 0.0 and 1.0:");
            labelHSB.setMinWidth(20);
            labelHSB.setPadding(new Insets(5, 0,0,0));
            Separator separator = new Separator();
            separator.setOrientation(Orientation.HORIZONTAL);
            separator.setPadding(new Insets(0, 16, 0, 0));

            TextField hueField = new TextField();
            onlyNumbers(hueField);
            hueField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    hueField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            TextField saturationField = new TextField();
            onlyNumbers(saturationField);
            saturationField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    saturationField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            TextField brightnessField = new TextField();
            onlyNumbers(brightnessField);
            brightnessField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    brightnessField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            hueField.setMinWidth(40);
            saturationField.setMinWidth(40);
            brightnessField.setMinWidth(40);
            hueField.setMinWidth(40);
            saturationField.setMinWidth(40);
            brightnessField.setMinWidth(40);

            HBox HSB = new HBox();
            HSB.setMaxWidth(200);
            HSB.setSpacing(12);

            Button refreshButtonHSB = new Button("Refresh");
            refreshButtonHSB.setMinWidth(61);

            HSB.getChildren().addAll(hueField, saturationField, brightnessField, refreshButtonHSB);

            refreshButtonHSB.setOnAction(event -> {
                try {
                    hueFactor = Double.parseDouble(hueField.getText());
                    saturationFactor = Double.parseDouble(saturationField.getText());
                    brightnessFactor = Double.parseDouble(brightnessField.getText());

                    System.out.println("Hue: " + hueFactor);
                    System.out.println("Saturation: " + saturationFactor);
                    System.out.println("Brightness: " + brightnessFactor);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter valid numbers.");
                }
                MandelbrotSet(THREADS);
            });

            ColorPicker colorPicker = new ColorPicker();

            // Set the initial color
            colorPicker.setValue(Color.WHITE);

            // Add an action listener to store the selected color in a variable
            colorPicker.setOnAction(event -> {
                colorOfSet = colorPicker.getValue();
                System.out.println("Selected Color: " + colorOfSet.toString());
                MandelbrotSet(THREADS);
            });

            Button resetInside = new Button("Reset");
            resetInside.setOnAction(actionEvent -> {
                if (determineColor == 2 || determineColor == 1)
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

            infoBox.getChildren().addAll(compilationTimeLabel, progressBar, iterationsBox, imageSizeBox, imageCompilationLabel, samplingBox, separator, enterValues, HSB, selectedColor, insideSet, madeByContainer);

            mainLayout = new BorderPane();
            mainLayout.setCenter(canvas);
            mainLayout.setRight(infoBox);
            /*SIDEBAR*/

            Scene scene = new Scene(mainLayout, width+250, height);

            canvas.widthProperty().bind(scene.widthProperty().subtract(250));
            canvas.heightProperty().bind(scene.heightProperty());

            canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
                widthTextField.setText(String.valueOf((int) newVal.doubleValue()));
                pauseTransition.setOnFinished(e -> MandelbrotSet(THREADS));
                pauseTransition.playFromStart();
            });

            canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
                heightTextField.setText(String.valueOf((int) newVal.doubleValue()));
                pauseTransition.setOnFinished(e -> MandelbrotSet(THREADS));
                pauseTransition.playFromStart();
            });


            scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case W, UP -> {
                        if (event.isShiftDown()) {
                            up(10);
                            MandelbrotSet(THREADS);
                        } else {
                            up(100);
                            MandelbrotSet(THREADS);
                        }
                        event.consume();
                    }
                    case A, LEFT -> {
                        if (event.isShiftDown()) {
                            left(10);
                            MandelbrotSet(THREADS);
                        } else {
                            left(100);
                            MandelbrotSet(THREADS);
                        }
                    }
                    case S, DOWN -> {
                        if (event.isShiftDown()) {
                            down(10);
                            MandelbrotSet(THREADS);
                        } else {
                            down(100);
                            MandelbrotSet(THREADS);
                        }
                    }
                    case D, RIGHT -> {
                        if (event.isShiftDown()) {
                            right(10);
                            MandelbrotSet(THREADS);
                        } else {
                            right(100);
                            MandelbrotSet(THREADS);
                        }
                        event.consume();
                    }
                    case EQUALS -> {zoomIn();MandelbrotSet(THREADS);}
                    case MINUS -> {zoomOut();MandelbrotSet(THREADS);}
                    case BACK_SPACE -> {reset();MandelbrotSet(THREADS);}
                    case DIGIT1 -> {colorHue();MandelbrotSet(THREADS);}
                    case DIGIT2 -> {colorHue2();MandelbrotSet(THREADS);}
                    case DIGIT3 -> {colorLight();MandelbrotSet(THREADS);}
                    case DIGIT4 -> {colorDark();MandelbrotSet(THREADS);}
                }
            });     //key listener
            scene.setOnMouseClicked(event -> {
                switch (event.getButton()) {
                    case PRIMARY -> {
                        zoom /= 0.7;
                        MandelbrotSet(THREADS);
                        //calcProgress(progressBar);
                    }
                    case SECONDARY -> {
                        zoom *= 0.7;
                        MandelbrotSet(THREADS);
                        //calcProgress(progressBar);
                    }
                }
            });   //mouse listener for easier zoom

            stage.setScene(scene);

            colorHue();
            MandelbrotSet(THREADS);
            System.out.println("hue"+hueFactor);
            System.out.println("sat"+saturationFactor);
            System.out.println("bright"+brightnessFactor);

            canvas.requestFocus();
            stage.setTitle("Mandelbrot Set");
            stage.show();
        }
    }

    /* ========================================ProgressBar============================================== */

    public void calcProgress(ProgressBar progressBar){
        Duration duration = Duration.seconds(resultCompilation/1000.0);

        // Create a timeline to animate the progress bar.
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(duration, new KeyValue(progressBar.progressProperty(), 1))
        );
        timeline.play();
    }

    /* ========================================MandelbrotSet============================================== */

    public void MandelbrotSet() {
        long startTime = System.currentTimeMillis(); // Record the start time
        int width = (int) (canvas.getWidth() * superSamplingFactor);
        int height = (int) (canvas.getHeight() * superSamplingFactor);
        WritableImage image = new WritableImage(width, height);
        actualImage = image;
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double cr = xPos / this.width + (x - centerX) / (zoom * superSamplingFactor);
                double ci = yPos / this.height + (y - centerY) / (zoom * superSamplingFactor);
                double zr = 0;
                double zi = 0;

                int iterationsOfZ;

                for (iterationsOfZ = 0; iterationsOfZ < maximumIterations && (zr * zr) + (zi * zi) < 4; iterationsOfZ++) {
                    double oldZr = zr;
                    zr = (zr * zr) - (zi * zi) + cr;
                    zi = 2 * (oldZr * zi) + ci;
                }

                if (iterationsOfZ == maximumIterations) {  //inside the set
                    image.getPixelWriter().setColor(x, y, colorOfSet);
                } else if (determineColor == 1) {
                    image.getPixelWriter().setColor(x, y, Color.hsb(hueFactor * 360 * (iterationsOfZ) % 360, saturationFactor , brightnessFactor));
                } else if (determineColor == 3) {
                    image.getPixelWriter().setColor(x, y, Color.hsb(hueFactor * 360 % 360, iterationsOfZ/maximumIterations, brightnessFactor));
                } else if (determineColor == 4){   //black background
                    image.getPixelWriter().setColor(x, y, Color.hsb(hueFactor * 360 % 360, saturationFactor, iterationsOfZ / maximumIterations));
                } else if (determineColor == 2){
                    image.getPixelWriter().setColor(x, y, Color.hsb(hueFactor*iterationsOfZ*7%360, saturationFactor , brightnessFactor));
                }
            }
        }

        WritableImage downscaledImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.getGraphicsContext2D().drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
        actualImage = downscaledImage;
        long endTime = System.currentTimeMillis(); // Record the end time
        resultCompilation = (endTime - startTime);
        compilationTimeLabel.setText("Execution time: " + resultCompilation / 1000.0 + "  (Threads: 1)");
        calcProgress(progressBar);
    }

    /* ====================================MandelbrotSetParallel================================================== */

    public void MandelbrotSet(int n) {
        long startTime = System.currentTimeMillis(); // Record the start time
        int scaledHeight = (int) (canvas.getHeight() * superSamplingFactor);
        int portion = scaledHeight / n;
        List<MandelbrotService> services = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(n);

        for (int i = 0; i < n; i++) {
            int start = i * portion;
            int end = start + portion;

            MandelbrotService service = new MandelbrotService(start, end);
            service.setOnSucceeded(event -> {
                latch.countDown();
                service.cancel();
            });
            service.start();
            services.add(service);
        }

        DoubleBinding totalProgressBinding = Bindings.createDoubleBinding(() ->
                        services.stream().mapToDouble(Service::getProgress).sum() / n,
                services.stream().map(Service::progressProperty).toArray(DoubleExpression[]::new));

        // Bind the progress bar to the totalProgressBinding
        progressBar.progressProperty().bind(totalProgressBinding);

        Thread waitForCompletion = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                image = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                List<WritableImage> images = services.stream().map(Service::getValue).collect(Collectors.toList());
                mergeImages(images);
                canvas.getGraphicsContext2D().drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
                actualImage=image;
                long endTime = System.currentTimeMillis(); // Record the end time
                resultCompilation = (endTime - startTime);
                compilationTimeLabel.setText("Execution time: " + resultCompilation / 1000.0 + "  (Threads: "+Runtime.getRuntime().availableProcessors() + ")");
                progressBar.progressProperty().unbind();
            });
        });

        waitForCompletion.start();
    }

    /* ====================================ServiceCreationClass============================================== */

    private class MandelbrotService extends Service<WritableImage> {
        private final int start;
        private final int end;

        public MandelbrotService(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Task<WritableImage> createTask() {
            return new Task<>() {
                @Override
                protected WritableImage call() {
                    int scaledLocalWidth = (int) (canvas.getWidth() * superSamplingFactor);
                    int scaledHeight = (int) (canvas.getHeight() * superSamplingFactor);
                    int localHeight = end - start;
                    WritableImage localImage = new WritableImage(scaledLocalWidth, localHeight);
                    PixelWriter pixelWriter = localImage.getPixelWriter();
                    double centerX = scaledLocalWidth / 2.0;
                    double centerY = scaledHeight / 2.0;
                    for (int y = start; y < end; y++) {
                        for (int x = 0; x < scaledLocalWidth; x++) {
                            double cr = xPos / width + (x - centerX) / (zoom * superSamplingFactor);
                            double ci = yPos / height + (y - centerY) / (zoom * superSamplingFactor);
                            double zr = 0;
                            double zi = 0;

                            int iterationsOfZ;

                            for (iterationsOfZ = 0; iterationsOfZ < maximumIterations && (zr * zr) + (zi * zi) < 4; iterationsOfZ++) {
                                double oldZr = zr;
                                zr = (zr * zr) - (zi * zi) + cr;
                                zi = 2 * (oldZr * zi) + ci;
                            }

                            if (iterationsOfZ == maximumIterations) {  //inside the set
                                pixelWriter.setColor(x, y - start, colorOfSet);
                            } else if (determineColor == 1) {
                                pixelWriter.setColor(x, y - start, Color.hsb(hueFactor * 360 * (iterationsOfZ) % 360+240, saturationFactor , brightnessFactor));
                            } else if (determineColor == 3) {
                                pixelWriter.setColor(x, y - start, Color.hsb(hueFactor * 360 % 360, iterationsOfZ/maximumIterations, brightnessFactor));
                            } else if (determineColor == 4){   //black background
                                pixelWriter.setColor(x, y - start, Color.hsb(hueFactor * 360 % 360, saturationFactor, iterationsOfZ / maximumIterations));
                            } else if (determineColor == 2){
                                pixelWriter.setColor(x, y - start, Color.hsb(hueFactor*iterationsOfZ * 7 % 360, saturationFactor , brightnessFactor));
                            }
                        }
                        updateProgress(y - start + 1, end - start);
                    }
                    return localImage;
                }
            };
        }
    }
    private void mergeImages(List<WritableImage> images) {
        int totalHeight = images.stream().mapToInt(img -> (int) img.getHeight()).sum();
        WritableImage combinedImage = new WritableImage((int) (canvas.getWidth() * superSamplingFactor), totalHeight);
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
        actualImage = downscaledImage;
    }


    /* ===========================================Colors================================================== */

    public void colorLight() {
        hueFactor = 0.6;
        saturationFactor = 1;
        brightnessFactor = 0.9;
        colorOfSet = Color.web("#43003E");
        determineColor = 3;
    }
    public void colorDark() {
        hueFactor = 0;
        saturationFactor = 0;
        brightnessFactor = maximumIterations;
        colorOfSet = Color.web("#000000");
        determineColor = 4;
    }
    public void colorHue() {
        hueFactor = 0.7;
        saturationFactor = 0.7;
        brightnessFactor = 1.0;
        colorOfSet = Color.web("#43003E");
        determineColor = 1;
    }
    public void colorHue2(){
        hueFactor = 0.7;
        saturationFactor = 0.7;
        brightnessFactor = 1.0;
        colorOfSet = Color.web("#43003E");
        determineColor = 2;
    }

    /* ==========================================Position================================================= */

    public void up(int number) {
        yPos -= (height / zoom) * number;
    }

    public void down(int number) {
        yPos += (height / zoom) * number;
    }

    public void left(int number) {
        xPos -= (width / zoom) * number;
    }

    public void right(int number) {
        xPos += (width / zoom) * number;
    }

    public void zoomIn() {
        zoom /= 0.7;
    }

    public void zoomOut() {
        zoom *= 0.7;
    }

    public void reset() {
        zoom = 250.0;
        xPos = -470;
        yPos = 30;
    }

    /* ============================================MenuBar================================================ */

    public void onlyNumbers(TextField text) {
        text.textProperty().addListener((observable, oldNum, newNum) -> {
            if (!newNum.matches("[0-9]*[.]?[0-9]*")) {
                text.setText(newNum.replaceAll("[^0-9.]", ""));
            }
        });
    }

}
