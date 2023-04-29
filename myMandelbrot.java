package com.example.demo2;

    /*  Mandelbrot Set
    *   Made by: Valntino Ivanovski
    *   Period: First semester of second year in Computer Science
    *   Subject: Programming 3 */

    /* =========================================Imports================================================ */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
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
import javax.imageio.ImageIO;

public class myMandelbrot extends Application {

    /* =========================================Variables================================================ */

     double width = 800;
     double height = 600;
     double maximumIterations = 500;
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
     int THREADS = 6;
     long startCompilation;
     long endCompilation;
     long resultCompilation;

    /* =========================================MainMethod================================================ */

    public static void main(String[] args) {
        launch(args);
    }

    /* =========================================MainStage================================================= */

    @Override
    public void start(Stage stage) {
        int number = 0;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Choose Option");
        dialog.setHeaderText("Choose Sequential or Parallel?");

        ButtonType sequentialButton = new ButtonType("Sequential");
        ButtonType parallelButton = new ButtonType("Parallel");

        dialog.getDialogPane().getButtonTypes().addAll(sequentialButton, parallelButton);

        Optional<ButtonType> resultButton = dialog.showAndWait();
        if (resultButton.isPresent()) {
            if (resultButton.get() == sequentialButton) {
                 number=1;
            } else if (resultButton.get() == parallelButton) {
                number=2;
            }
        }

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

            TextField typeHeight = new TextField();
            TextField typeWidth = new TextField();
            Button button1 = new Button("Save");

            TextField typeIter = new TextField();
            Button button2 = new Button("Refresh");

            mainMenuBar(stage, group, typeHeight, typeWidth, typeIter, button1, button2);

            textFieldsImg(group, typeHeight, typeWidth, button1);

            textFieldIter(group, typeIter, button2);

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
            infoBox.setPadding(new Insets(10, 10, 10, 10));
            infoBox.setMinWidth(250);

            Label compilationTimeLabel = new Label("Compilation time: "+resultCompilation);
            Label imageSizeLabel = new Label("Image size: "+ (int)width +"x"+(int)height);

            HBox imageSizeBox = new HBox(10);
            imageSizeBox.setAlignment(Pos.CENTER_LEFT);
            Button saveImageButton = new Button("Save Image");
            imageSizeBox.getChildren().addAll(imageSizeLabel, saveImageButton);

            Label zoomLevelLabel = new Label("Zoom level:"+zoomInfo);

            Label iterationsLabel = new Label("Iterations: " + (int)maximumIterations);
            TextField iterationsTextField = new TextField();
            iterationsTextField.setPrefWidth(60);
            onlyNumbers(iterationsTextField);

            Button okButton = new Button("Refresh");
            HBox iterationsBox = new HBox(10);
            okButton.setOnAction(event -> {
                maximumIterations=Double.parseDouble(iterationsTextField.getText());
                startCompilation = System.currentTimeMillis();
                MandelbrotSet(THREADS);
                endCompilation = System.currentTimeMillis();
                resultCompilation = (endCompilation-startCompilation);
                System.out.println(resultCompilation/1000.0);
                compilationTimeLabel.setText("Compilation time: " + resultCompilation/1000.0);
            });
            iterationsTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    iterationsTextField.getParent().requestFocus(); // shift focus to the parent node to deselect the text field
                    event.consume();
                }
            });

            iterationsBox.setAlignment(Pos.CENTER_LEFT);
            iterationsBox.getChildren().addAll(iterationsLabel, iterationsTextField, okButton);

            Label positionLabel = new Label("Position:");

            ProgressBar progressBar = new ProgressBar();
            progressBar.setPrefWidth(200);

            infoBox.getChildren().addAll(compilationTimeLabel, imageSizeBox, zoomLevelLabel, iterationsBox, positionLabel, progressBar);

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
                MandelbrotSet(THREADS);
            });

            scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                height = (int)newVal.doubleValue();
                image = new WritableImage((int) width, (int) height);
                imageView.setImage(image);
                MandelbrotSet(THREADS);
            });

            scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case W, UP -> {
                        if (event.isShiftDown()) {
                            upParallel(10);
                            runTime(compilationTimeLabel);
                        } else {
                            upParallel(100);
                            runTime(compilationTimeLabel);
                        }
                    }
                    case A, LEFT -> {
                        if (event.isShiftDown()) {
                            leftParallel(10);
                            runTime(compilationTimeLabel);
                        } else {
                            leftParallel(100);
                            runTime(compilationTimeLabel);
                        }
                    }
                    case S, DOWN -> {
                        if (event.isShiftDown()) {
                            downParallel(10);
                            runTime(compilationTimeLabel);
                        } else {
                            downParallel(100);
                            runTime(compilationTimeLabel);
                        }
                    }
                    case D, RIGHT -> {
                        if (event.isShiftDown()) {
                            rightParallel(10);
                            runTime(compilationTimeLabel);
                        } else {
                            rightParallel(100);
                            runTime(compilationTimeLabel);
                        }
                    }
                    case EQUALS -> {zoomInParallel();runTime(compilationTimeLabel);}
                    case MINUS -> {zoomOutParallel();runTime(compilationTimeLabel);}
                    case BACK_SPACE -> {resetParallel();runTime(compilationTimeLabel);}
                    //case ESCAPE -> Platform.exit();
                    case DIGIT1 -> {colorLightParallel();runTime(compilationTimeLabel);}
                    case DIGIT2 -> {colorDarkParallel();runTime(compilationTimeLabel);}
                    case DIGIT3 -> {colorHueParallel();runTime(compilationTimeLabel);}
                    case DIGIT4 -> {colorWhiteParallel();runTime(compilationTimeLabel);}
                }
            });     //key listener
            scene.setOnMouseClicked(event -> {
                switch (event.getButton()) {
                    case PRIMARY -> {
                        zoom /= 0.7;
                        runTime(compilationTimeLabel);
                    }
                    case SECONDARY -> {
                        zoom *= 0.7;
                        runTime(compilationTimeLabel);
                    }
                }
            });   //mouse listener for easier zoom

            stage.setScene(scene);

            startCompilation = System.currentTimeMillis();
            MandelbrotSet(THREADS);
            endCompilation = System.currentTimeMillis();
            resultCompilation = (endCompilation-startCompilation);
            System.out.println(resultCompilation/1000.0);
            compilationTimeLabel.setText("Compilation time: " + resultCompilation/1000.0+"sec.");

            stage.setTitle("Mandelbrot Set");
            stage.show();
        }
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

    public void MandelbrotSet(int n) {
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
                                pixelWriter.setColor(x, y - start, javafx.scene.paint.Color.hsb(iterationsOfZ % hue, iterationsOfZ / maximumIterations, brightness));
                            }
                            else if(hue == 300){
                                pixelWriter.setColor(x, y - start, javafx.scene.paint.Color.hsb(hue*iterationsOfZ/maximumIterations, saturation, brightness));
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
            System.out.println("Source image dimensions: " + imgWidth + "x" + imgHeight);
            System.out.println("Destination image dimensions: " + image.getWidth() + "x" + image.getHeight());
            System.out.println("setPixels arguments: " + "0, " + currentHeight + ", " + imgWidth + ", " + imgHeight + ", img.getPixelReader(), 0, 0");
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

    /* ============================================MenuBar================================================ */

    public void mainMenuBar(Stage stage, Group group, TextField typeHeight, TextField typeWidth, TextField typeIter, Button button1, Button button2) {
        MenuBar menubar = new MenuBar();
        menu1(menubar, group);
        menu2(menubar);
        menu3(stage, menubar, typeHeight, typeWidth, typeIter, button1, button2);

    }
    public void menu1(MenuBar menubar, Group group){
        Menu menu1 = new Menu("Color");
        menubar.getMenus().add(menu1);
        group.getChildren().add(menubar);
        menubar.setPrefWidth(176);
        CheckMenuItem m1i1 = new CheckMenuItem("Light");
        CheckMenuItem m1i2 = new CheckMenuItem("Dark");
        CheckMenuItem m1i3 = new CheckMenuItem("Colorful");
        CheckMenuItem m1i4 = new CheckMenuItem("Solid White");
        m1i1.setOnAction(e -> {
            if (m1i1.isSelected()) {
                colorLight();
                m1i2.setSelected(false);
                m1i3.setSelected(false);
                m1i4.setSelected(false);
            }
        });
        m1i2.setOnAction(e -> {
            if (m1i2.isSelected()) {
                colorDark();
                m1i1.setSelected(false);
                m1i3.setSelected(false);
                m1i4.setSelected(false);
            }
        });
        m1i3.setOnAction(e -> {
            if (m1i3.isSelected()) {
                colorHue();
                m1i1.setSelected(false);
                m1i2.setSelected(false);
                m1i4.setSelected(false);
            }
        });
        m1i4.setOnAction(e -> {
            if (m1i4.isSelected()) {
                colorWhite();
                m1i1.setSelected(false);
                m1i2.setSelected(false);
                m1i3.setSelected(false);
            }
        });
        m1i1.setAccelerator(KeyCombination.keyCombination("1"));
        m1i2.setAccelerator(KeyCombination.keyCombination("2"));
        m1i3.setAccelerator(KeyCombination.keyCombination("3"));
        m1i4.setAccelerator(KeyCombination.keyCombination("4"));
        menu1.getItems().addAll(m1i1, m1i2, m1i3, m1i4);
    }
    public void menu2(MenuBar menubar){
        Menu menu2 = new Menu("View");
        menubar.getMenus().add(menu2);
        MenuItem m2i1 = new MenuItem("Zoom in");
        MenuItem m2i2 = new MenuItem("Zoom out");
        MenuItem m2i3 = new MenuItem("Reset");
        MenuItem m2i4 = new MenuItem("Move Up");
        MenuItem m2i5 = new MenuItem("Move Down");
        MenuItem m2i6 = new MenuItem("Move Left");
        MenuItem m2i7 = new MenuItem("Move Right");

        m2i1.setAccelerator(KeyCombination.keyCombination("Plus"));
        m2i2.setAccelerator(KeyCombination.keyCombination("Minus"));
        m2i3.setAccelerator(KeyCombination.keyCombination("Space"));
        m2i4.setAccelerator(KeyCombination.keyCombination("UP"));
        m2i5.setAccelerator(KeyCombination.keyCombination("DOWN"));
        m2i6.setAccelerator(KeyCombination.keyCombination("LEFT"));
        m2i7.setAccelerator(KeyCombination.keyCombination("RIGHT"));

        m2i1.setOnAction(t -> zoomIn());
        m2i2.setOnAction(t -> zoomOut());
        m2i3.setOnAction(t -> reset());
        m2i4.setOnAction(t -> up(100));
        m2i5.setOnAction(t -> down(100));      //menubar location
        m2i6.setOnAction(t -> left(100));
        m2i7.setOnAction(t -> right(100));
        menu2.getItems().addAll(m2i1, m2i2, m2i3, new SeparatorMenuItem(), m2i4, m2i5, m2i6, m2i7);
    }
    public void menu3(Stage stage, MenuBar menubar, TextField typeHeight, TextField typeWidth, TextField typeIter, Button button1, Button button2){
        Menu menu3 = new Menu("Image");
        menubar.getMenus().add(menu3);

        MenuItem m3i1 = new MenuItem("Set Iterations");
        MenuItem m3i2 = new MenuItem("Save Image");

        menu3.getItems().addAll(m3i1, m3i2);

        m3i2.setOnAction(e -> {
            typeHeight.setVisible(true);
            typeWidth.setVisible(true);
            button1.setVisible(true);
            button2.setVisible(false);
            typeIter.setVisible(false);
        });
        button1.setOnAction( e-> {       //setting image resolution and saving
            canvas.widthProperty().unbind();
            canvas.heightProperty().unbind();
            canvas.setWidth(Integer.parseInt(typeHeight.getText()));
            canvas.setHeight(Integer.parseInt(typeWidth.getText()));
            MandelbrotSet();

            saveImage(stage, actualImage);
            canvas.widthProperty().bind(stage.widthProperty());
            canvas.heightProperty().bind(stage.heightProperty());
            MandelbrotSet();

            typeHeight.setVisible(false);
            typeWidth.setVisible(false);
            button1.setVisible(false);
        });
        m3i1.setAccelerator(KeyCombination.keyCombination("CTRL + C"));
        m3i1.setOnAction(e -> {
            typeIter.setVisible(true);
            button2.setVisible(true);
            typeHeight.setVisible(false);
            typeWidth.setVisible(false);
            button1.setVisible(false);
        });
        button2.setOnAction( e -> {
            typeIter.setVisible(false);
            button2.setVisible(false);
            maximumIterations=Double.parseDouble(typeIter.getText());
            MandelbrotSet();
        });
        m3i2.setAccelerator(KeyCombination.keyCombination("CTRL + V"));
    }


    /* ========================================TextFields================================================= */

    public void textFieldsImg(Group group, TextField typeHeight, TextField typeWidth, Button button){

        button.setLayoutX(120);
        button.setLayoutY(33);
        button.setPrefWidth(50);

        typeHeight.setLayoutX(7);
        typeHeight.setLayoutY(33);
        typeHeight.setPrefWidth(50);
        typeWidth.setLayoutX(63);
        typeWidth.setLayoutY(33);
        typeWidth.setPrefWidth(50);

        onlyNumbers(typeHeight);
        onlyNumbers(typeWidth);

        group.getChildren().add(typeHeight);
        group.getChildren().add(typeWidth);
        group.getChildren().add(button);
        typeHeight.setVisible(false);
        typeWidth.setVisible(false);
        button.setVisible(false);
    }
    public void textFieldIter(Group group, TextField typeIter, Button button){
        typeIter.setLayoutY(200);
        button.setLayoutX(92);
        button.setLayoutY(33);
        button.setPrefWidth(78);

        typeIter.setLayoutX(7);
        typeIter.setLayoutY(33);
        typeIter.setPrefWidth(78);

        onlyNumbers(typeIter);
        typeIter.setVisible(false);
        button.setVisible(false);

        group.getChildren().add(typeIter);
        group.getChildren().add(button);
    }
    public void onlyNumbers(TextField text) {
        text.textProperty().addListener((observable, oldNum, newNum) -> {
            if (!newNum.matches("\\d*")) text.setText(newNum.replaceAll("[^\\d]", ""));
        });
    }

    /* ==========================================CalculateRunTime============================================ */

    public void runTime(Label compilationTimeLabel){
        startCompilation = System.currentTimeMillis();
        MandelbrotSet(THREADS);
        endCompilation = System.currentTimeMillis();
        resultCompilation = (endCompilation-startCompilation);
        System.out.println(resultCompilation/1000.0);
        compilationTimeLabel.setText("Compilation time: " + resultCompilation/1000.0);
    }
}
