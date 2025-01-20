module com.example.touchtyped {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.touchtyped to javafx.fxml;
    exports com.example.touchtyped.app;
    opens com.example.touchtyped.app to javafx.fxml;
    exports com.example.touchtyped.controller;
    opens com.example.touchtyped.controller to javafx.fxml;
}