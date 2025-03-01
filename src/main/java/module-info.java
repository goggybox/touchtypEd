module com.example.touchtyped {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fazecast.jSerialComm;

    opens com.example.touchtyped to javafx.fxml;
    exports com.example.touchtyped.app;
    opens com.example.touchtyped.app to javafx.fxml;
    exports com.example.touchtyped.controller;
    opens com.example.touchtyped.controller to javafx.fxml;
    exports com.example.touchtyped.serialisers to com.fasterxml.jackson.databind;
    exports com.example.touchtyped.model to com.fasterxml.jackson.databind;
}