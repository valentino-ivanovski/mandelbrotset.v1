module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;
    requires javafx.swing;
    requires javafx.graphics;

    opens com.example.demo2 to javafx.fxml;
    exports com.example.demo2;
    exports nonNeeded;
    opens nonNeeded to javafx.fxml;
}