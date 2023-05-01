module com.example.mandelbrot {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;
    requires javafx.swing;
    requires javafx.graphics;


    opens com.example.mandelbrot to javafx.fxml;
    exports com.example.mandelbrot;
}