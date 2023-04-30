package com.example.demo2;

    /*  Mandelbrot Set
    *   Made by: Valntino Ivanovski
    *   Period: First semester of second year in Computer Science
    *   Subject: Programming 3 */

    /* =========================================Imports================================================ */

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;

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
     Canvas canvas = new Canvas(width, height);
     WritableImage actualImage;
     double zoom = 250.0;
     double zoomInfo = 0;
     double xPos = -470; //add 0 on both of the coordinates for the accurate plane
     double yPos = 0;
     double hue = 264.0;
     WritableImage image;
     ImageView imageView;
     double saturation = maximumIterations;
     double brightness = 0.9;
     int R = 60;
     int G = 0;
     int B = 60;
     int THREADS = Runtime.getRuntime().availableProcessors();
     long startCompilation;
     long endCompilation;
     long resultCompilation;
     private int number;
     ProgressBar progressBar = new ProgressBar();
     Label compilationTimeLabel;
     Label imageCompilationLabel;

    /* =========================================MainMethod================================================ */

    public static void main(String[] args) {
        launch(args);
    }

    /* =========================================MainStage================================================= */

    @Override
    public void start(Stage stage) {
        // create a new dialog
        Dialog<Integer> dialog = new Dialog<>();

        // set the title and header text
        dialog.setTitle("Choose Mode");
        dialog.setHeaderText(null);

        // importing image and adding it to an imageView
        Image imageLogo = new Image(getClass().getResource("/Image.png").toString());
        ImageView imageViewDialog = new ImageView(imageLogo);

        // create a label
        Label label = new Label("Choose Mode:");
        Font font = new Font("Arial Bold", 15);
        label.setPadding(new Insets(0, 0, 7, 0));
        label.setTextFill(Color.web("#633B5D"));

        // set the label's font to the new font
        label.setFont(font);

        // create three buttons
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

        // create a VBox to hold the content
        VBox content = new VBox(imageViewDialog, label, sequentialButton, parallelButton, distributedButton);
        content.setSpacing(10);
        content.setAlignment(Pos.CENTER);

        // set the dialog content
        dialog.getDialogPane().setContent(content);

        // make the dialog close when pressing x
        dialog.getDialogPane().getScene().getWindow().setOnCloseRequest(event -> {
            dialog.close();
        });

        // show the dialog and wait for a result
        imageViewDialog.requestFocus();
        dialog.showAndWait();

        // use the selected number
        System.out.println("Selected number: " + number);

        if (number == 1){
            Group group = new Group(canvas);

            Scene scene = new Scene(group, width, height);
            scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case W, UP -> {
                        if(event.isShiftDown()){
                            up(10);
                        }
                        else{up(100);}
                    }
                    case A, LEFT -> {
                        if(event.isShiftDown()){
                            left(10);
                        }
                        else{left(100);}
                    }
                    case S, DOWN -> {
                        if(event.isShiftDown()){
                            down(10);
                        }
                        else{down(100);}
                    }
                    case D, RIGHT -> {
                        if(event.isShiftDown()){
                            right(10);
                        }
                        else{right(100);}
                    }
                    case EQUALS -> zoomIn();
                    case MINUS -> zoomOut();
                    case SPACE -> reset();
                    case ESCAPE -> Platform.exit();
                }
            });     //key listener
            scene.setOnMouseClicked(event -> {
                switch (event.getButton()) {
                    case PRIMARY -> {

                        zoom /= 0.7;

                        startCompilation = System.currentTimeMillis();
                        MandelbrotSet();
                        endCompilation = System.currentTimeMillis();
                        resultCompilation = (endCompilation-startCompilation);
                        System.out.println(resultCompilation/1000.0);
                    }
                    case SECONDARY -> {
                        zoom *= 0.7;
                        startCompilation = System.currentTimeMillis();
                        MandelbrotSet();
                        endCompilation = System.currentTimeMillis();
                        resultCompilation = (endCompilation-startCompilation);
                        System.out.println(resultCompilation/1000.0);
                    }
                }
            });   //mouse listener for easier zoom


            stage.widthProperty().addListener((obs, oldVal, newVal) -> {
                canvas.setWidth(newVal.doubleValue());
                //canvas.widthProperty().bind(stage.widthProperty());
                MandelbrotSet();
            });

            stage.heightProperty().addListener((obs, oldVal, newVal) -> {
                canvas.setHeight(newVal.doubleValue());
                //canvas.heightProperty().bind(stage.heightProperty());
                MandelbrotSet();
            });

            stage.setScene(scene);

            long start = System.currentTimeMillis();
            MandelbrotSet();
            long end = System.currentTimeMillis();
            long result = (end-start);
            //runTime(group, stage, result);
            System.out.println(result/1000.0);

            stage.setTitle("Mandelbrot Set");
            stage.show();
        }
        else if(number==2){
            image = new WritableImage((int) width, (int) height);
            imageView = new ImageView(image);
            /*SIDEBAR*/
            VBox infoBox = new VBox(10);
            infoBox.setPadding(new Insets(10, 0, 10, 16));
            infoBox.setMinWidth(250);

            compilationTimeLabel = new Label();
            Label imageSizeLabel = new Label("Image size:");
            imageCompilationLabel = new Label("Execution time of saved image: null");

            TextField widthTextField = new TextField(String.valueOf((int)width));
            widthTextField.setPrefWidth(50);
            Label xLabel = new Label("x");
            TextField heightTextField = new TextField(String.valueOf((int)height));
            heightTextField.setPrefWidth(50);

            heightTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    heightTextField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            }); //Press esc to deselect the text field

            widthTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    widthTextField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            }); //Press esc to deselect the text field

            HBox imageSizeBox = new HBox(10);
            imageSizeBox.setAlignment(Pos.CENTER_LEFT);
            Button saveImageButton = new Button("Save Image");

            //Save Image button that saves the image of any size
            saveImageButton.setOnAction(event ->{
                double oldWidth = width;
                double oldHeight = height;
                int newWidth = Integer.parseInt(widthTextField.getText());
                int newHeight = Integer.parseInt(heightTextField.getText());

                width = newWidth;
                height = newHeight;

                runTimeImage();

                saveImageParallel(stage);

                width = oldWidth;
                height = oldHeight;

                imageView.requestFocus();

            });

            //adding the image to an imageview
            ImageView imageViewLogo = new ImageView(imageLogo);

            Label activeThreads = new Label("Number of active threads: "+Thread.activeCount());

            infoBox.getChildren().add(imageViewLogo);

            imageSizeBox.getChildren().addAll(widthTextField, xLabel, heightTextField, saveImageButton);

            Label iterationsLabel = new Label("Iterations: " + (int)maximumIterations);
            TextField iterationsTextField = new TextField();
            iterationsTextField.setPrefWidth(60);
            onlyNumbers(iterationsTextField);

            Button refreshButton = new Button("Refresh");
            HBox iterationsBox = new HBox(10);
            refreshButton.setOnAction(event -> {
                maximumIterations=Double.parseDouble(iterationsTextField.getText());
                runTime();
                iterationsLabel.setText("Iterations: "+(int)maximumIterations);
                iterationsTextField.setText("");
                imageView.requestFocus();
            });
            iterationsTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    iterationsTextField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            iterationsBox.setAlignment(Pos.CENTER_LEFT);
            iterationsBox.getChildren().addAll(iterationsLabel, iterationsTextField, refreshButton);

            progressBar.setPrefWidth(200);

            infoBox.getChildren().addAll(compilationTimeLabel, progressBar, iterationsBox, imageSizeBox, imageCompilationLabel, activeThreads);

            AnchorPane mainLayout = new AnchorPane(imageView, infoBox);
            AnchorPane.setTopAnchor(imageView, 0.0);
            AnchorPane.setBottomAnchor(imageView, 0.0);
            AnchorPane.setLeftAnchor(imageView, 0.0);

            AnchorPane.setTopAnchor(infoBox, 0.0);
            AnchorPane.setRightAnchor(infoBox, 0.0);
            AnchorPane.setBottomAnchor(infoBox, 0.0);
            /*SIDEBAR*/

            Scene scene = new Scene(mainLayout, width+250, height);

            scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                width = (int)newVal.doubleValue()-250;
                image = new WritableImage((int) width, (int) height);
                imageView.setImage(image);
                imageSizeLabel.setText("Image size: "+ (int)width +"x"+(int)height);
                widthTextField.setText(String.valueOf((int)width));
                runTime();
            });

            scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                height = (int)newVal.doubleValue();
                image = new WritableImage((int) width, (int) height);
                heightTextField.setText(String.valueOf((int)height));
                imageView.setImage(image);
                runTime();
            });

            imageView.setPreserveRatio(true);

            imageView.fitWidthProperty().bind(mainLayout.widthProperty());
            imageView.fitHeightProperty().bind(mainLayout.heightProperty());

            scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case W, UP -> {
                        if (event.isShiftDown()) {
                            upParallel(10);
                            runTime();
                        } else {
                            upParallel(100);
                            runTime();
                        }
                    }
                    case A, LEFT -> {
                        if (event.isShiftDown()) {
                            leftParallel(10);
                            runTime();
                        } else {
                            leftParallel(100);
                            runTime();
                        }
                    }
                    case S, DOWN -> {
                        if (event.isShiftDown()) {
                            downParallel(10);
                            runTime();
                        } else {
                            downParallel(100);
                            runTime();
                        }
                    }
                    case D, RIGHT -> {
                        if (event.isShiftDown()) {
                            rightParallel(10);
                            runTime();
                        } else {
                            rightParallel(100);
                            runTime();
                        }
                    }
                    case EQUALS -> {zoomInParallel();runTime();}
                    case MINUS -> {zoomOutParallel();runTime();}
                    case BACK_SPACE -> {resetParallel();runTime();}
                    //case ESCAPE -> Platform.exit();
                    case DIGIT1 -> {colorLightParallel();runTime();}
                    case DIGIT2 -> {colorDarkParallel();runTime();}
                    case DIGIT3 -> {colorHueParallel();runTime();}
                    case DIGIT4 -> {colorWhiteParallel();runTime();}
                }
            });     //key listener
            scene.setOnMouseClicked(event -> {
                switch (event.getButton()) {
                    case PRIMARY -> {
                        zoom /= 0.7;
                        runTime();
                        //calcProgress(progressBar);
                    }
                    case SECONDARY -> {
                        zoom *= 0.7;
                        runTime();
                        //calcProgress(progressBar);
                    }
                }
            });   //mouse listener for easier zoom

            stage.setScene(scene);

            runTime();

            imageView.requestFocus();
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
        WritableImage image = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        actualImage = image;
        double centerY = canvas.getWidth() / 2.0;
        double centerX = canvas.getHeight() / 2.0;
        for (int x = 0; x < canvas.getWidth(); x++) {
            for (int y = 0; y < canvas.getHeight(); y++) {
                double cr = xPos / width + (x - centerY) / zoom;
                double ci = yPos / height + (y - centerX) / zoom;       //getting position of the points on the canvas
                double zr = 0;
                double zi = 0;

                int iterationsOfZ;

                for (iterationsOfZ = 0; iterationsOfZ < maximumIterations && (zr * zr) + (zi * zi) < 4; iterationsOfZ++) {
                    double oldZr = zr;
                    zr = (zr * zr) - (zi * zi) + cr;
                    zi = 2 * (oldZr * zi) + ci;
                }

                if (iterationsOfZ == maximumIterations) {  //inside the set
                    image.getPixelWriter().setColor(x, y, Color.rgb(R, G, B));
                } else if (brightness == 0.9) {  //white background
                    image.getPixelWriter().setColor(x, y, Color.hsb(hue, iterationsOfZ / maximumIterations, brightness));
                } else if (hue == 300) {  //colorful background
                    image.getPixelWriter().setColor(x, y, Color.hsb(hue * iterationsOfZ / maximumIterations, saturation, brightness));
                } else if (hue == 0 && saturation == 0 && brightness == 1) {
                    image.getPixelWriter().setColor(x, y, Color.hsb(hue, saturation, brightness));
                } else {   //black background
                    image.getPixelWriter().setColor(x, y, Color.hsb(hue, saturation, iterationsOfZ / brightness));
                }
            }
        }
        canvas.getGraphicsContext2D().drawImage(image, 0, 0); //x and y coordinates of the image.
    }

    /* ===========================================Parallel================================================== */

    public void MandelbrotSet(int n, ProgressBar progressBar, Label compilationTimeLabel) {
        long startTime = System.currentTimeMillis(); // Record the start time
        int portion = (int) height / n;
        List<myMandelbrot.MandelbrotService> services = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(n);

        for (int i = 0; i < n; i++) {
            int start = i * portion;
            int end = start + portion;

            myMandelbrot.MandelbrotService service = new myMandelbrot.MandelbrotService(start, end);
            service.setOnSucceeded(event -> {
                latch.countDown();
                service.cancel();
            });
            service.start();
            services.add(service);
        }

        Thread waitForCompletion = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                List<WritableImage> images = services.stream().map(Service::getValue).collect(Collectors.toList());
                mergeImages(images);
                imageView.setImage(image);
                long endTime = System.currentTimeMillis(); // Record the end time
                long elapsedTime = (endTime - startTime); // Calculate the elapsed time in seconds
                resultCompilation = elapsedTime;
                compilationTimeLabel.setText("Execution time: " + resultCompilation / 1000.0);
                calcProgress(progressBar);
            });
        });

        waitForCompletion.start();
    }




    /* ========================================Tasks============================================== */

    private class MandelbrotService extends Service<WritableImage> {
        private final int start;
        private final int end;

        public MandelbrotService(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Task<WritableImage> createTask() {
            return new Task<WritableImage>() {
                @Override
                protected WritableImage call() {
                    WritableImage localImage = new WritableImage((int) width, (int)end - start);
                    double centerY = width / 2.0;
                    double centerX = height / 2.0;
                    PixelWriter pixelWriter = localImage.getPixelWriter(); // use localImage instead of image
                    for (int y = start; y < end; y++) {
                        for (int x = 0; x < width; x++) {
                            double zr = 0;
                            double zi = 0;
                            double cr = xPos / width + (x - centerY) / zoom;
                            double ci = yPos / height + (y - centerX) / zoom;

                            int iterationsOfZ;

                            for (iterationsOfZ = 0; iterationsOfZ < maximumIterations && (zr * zr) + (zi * zi) < 4; iterationsOfZ++) {
                                double oldZr = zr;
                                zr = (zr * zr) - (zi * zi) + cr;
                                zi = 2 * (oldZr * zi) + ci;
                            }

                            if(iterationsOfZ == maximumIterations){
                                pixelWriter.setColor(x, y - start, javafx.scene.paint.Color.rgb(R, G, B));
                            }
                            else if(brightness == 0.9){
                                pixelWriter.setColor(x, y - start, javafx.scene.paint.Color.hsb( hue/maximumIterations, iterationsOfZ / maximumIterations, brightness));
                            }
                            else if(hue == 300){
                                pixelWriter.setColor(x, y - start, javafx.scene.paint.Color.hsb(hue*iterationsOfZ/maximumIterations, iterationsOfZ / maximumIterations, brightness));
                            }
                            else if(hue == 0 && saturation == 0 && brightness == 1){
                                pixelWriter.setColor(x, y - start, javafx.scene.paint.Color.hsb(hue, saturation, brightness));
                            }
                            else{
                                pixelWriter.setColor(x, y - start, javafx.scene.paint.Color.hsb(hue, saturation, iterationsOfZ/brightness));
                            }
                        }
                    }
                    return localImage;
                }
            };
        }
    }
    private void mergeImages(List<WritableImage> images) {
        int totalHeight = images.stream().mapToInt(img -> (int) img.getHeight()).sum();
        if (width != (int) image.getWidth() || totalHeight != (int) image.getHeight()) {
            image = new WritableImage((int)width, totalHeight);
            imageView.setImage(image);
        }
        PixelWriter pixelWriter = image.getPixelWriter();
        int currentHeight = 0;
        for (WritableImage img : images) {
            int imgHeight = (int) img.getHeight();
            int imgWidth = (int) img.getWidth();
            pixelWriter.setPixels(0, currentHeight, imgWidth, imgHeight, img.getPixelReader(), 0, 0);
            currentHeight += imgHeight;
        }
    }


    /* ===========================================Colors================================================== */

    public void colorLight() {
        hue = 246.0;
        saturation = maximumIterations;
        brightness = 0.9;
        R = 60;
        G = 0;
        B = 60;
        MandelbrotSet();
    }
    public void colorDark() {
        hue = 0;
        saturation = 0;
        brightness = maximumIterations;
        R = 15;
        G = 15;
        B = 15;
        MandelbrotSet();
    }
    public void colorHue() {
        hue = 300.0;
        saturation = 1.0;
        brightness = 1.0;
        R = 35;
        G = 0;
        B = 35;
        MandelbrotSet();
    }
    public void colorWhite() {
        hue = 0.0;
        saturation = 0.0;
        brightness = 1.0;
        R = 0;
        G = 0;
        B = 0;
        MandelbrotSet();
    }
    public void colorLightParallel() {
        hue = 246.0;
        saturation = maximumIterations;
        brightness = 0.9;
        R = 60;
        G = 0;
        B = 60;
    }
    public void colorDarkParallel() {
        hue = 0;
        saturation = 0;
        brightness = maximumIterations;
        R = 15;
        G = 15;
        B = 15;
    }
    public void colorHueParallel() {
        hue = 300.0;
        saturation = 1.0;
        brightness = 1.0;
        R = 35;
        G = 0;
        B = 35;
    }
    public void colorWhiteParallel() {
        hue = 0.0;
        saturation = 0.0;
        brightness = 1.0;
        R = 0;
        G = 0;
        B = 0;
    }

    /* ==========================================Position================================================= */

    public void up(int number) {
        yPos -= (height / zoom) * number;
        MandelbrotSet();
    }
    public void down(int number) {
        yPos += (height / zoom) * number;
        MandelbrotSet();
    }
    public void left(int number) {
        xPos -= (width / zoom) * number;
        MandelbrotSet();
    }
    public void right(int number) {
        xPos += (width / zoom) * number;
        MandelbrotSet();
    }
    public void zoomIn() {
        zoom /= 0.7;
        MandelbrotSet();
    }
    public void zoomOut() {
        zoom *= 0.7;
        MandelbrotSet();
    }
    public void reset() {
        zoom = 250.0;
        xPos = -470;
        yPos = 30;
        MandelbrotSet();
    }
    public void upParallel(int number) {
        yPos -= (height / zoom) * number;
    }

    public void downParallel(int number) {
        yPos += (height / zoom) * number;
    }

    public void leftParallel(int number) {
        xPos -= (width / zoom) * number;
    }

    public void rightParallel(int number) {
        xPos += (width / zoom) * number;
    }

    public void zoomInParallel() {
        zoom /= 0.7;
    }

    public void zoomOutParallel() {
        zoom *= 0.7;
    }

    public void resetParallel() {
        zoom = 250.0;
        xPos = -470;
        yPos = 30;
    }

    /* ==========================================SaveImage================================================ */

    public void saveImage(Stage stage, WritableImage image){
        FileChooser fc = new FileChooser();
        fc.setTitle("Save File");
        FileChooser.ExtensionFilter extensions = new FileChooser.ExtensionFilter("Images *.jpg, *.png", "*.jpg", "*.png");

        fc.getExtensionFilters().add(extensions);

        File file = fc.showSaveDialog(stage);
        if (file != null) {
            try {
                canvas.snapshot(null, image);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void saveImageParallel(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save File");
        FileChooser.ExtensionFilter extensions = new FileChooser.ExtensionFilter("Images *.jpg, *.png", "*.jpg", "*.png");

        fc.getExtensionFilters().add(extensions);

        File file = fc.showSaveDialog(stage);
        if (file != null) {
            try {
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }



    /* ============================================MenuBar================================================ */

    public void onlyNumbers(TextField text) {
        text.textProperty().addListener((observable, oldNum, newNum) -> {
            if (!newNum.matches("\\d*")) text.setText(newNum.replaceAll("[^\\d]", ""));
        });
    }

    /* ==========================================CalculateRunTime============================================ */

    public void runTime(){
        MandelbrotSet(THREADS, progressBar, compilationTimeLabel);
        compilationTimeLabel.setText("Execution time: " + resultCompilation/1000.0);
    }
    public void runTimeImage(){
        MandelbrotSet(THREADS, progressBar, compilationTimeLabel);
        imageCompilationLabel.setText("Execution time of saved image: " + resultCompilation/1000.0);
    }
}
