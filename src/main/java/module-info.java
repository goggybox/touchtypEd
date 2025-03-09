module com.example.touchtyped {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fazecast.jSerialComm;
    requires java.desktop;
    requires google.cloud.core;
    requires google.cloud.firestore;
    requires com.google.auth;
    requires com.google.auth.oauth2;
    requires com.google.api.apicommon;

    opens com.example.touchtyped to javafx.fxml;
    exports com.example.touchtyped.app;
    opens com.example.touchtyped.app to javafx.fxml;
    exports com.example.touchtyped.controller;
    opens com.example.touchtyped.controller to javafx.fxml;
    exports com.example.touchtyped.serialisers to com.fasterxml.jackson.databind;
    exports com.example.touchtyped.model to com.fasterxml.jackson.databind, google.cloud.firestore;
    opens com.example.touchtyped.model to google.cloud.firestore;
    exports com.example.touchtyped.firestore to google.cloud.firestore;
    opens com.example.touchtyped.firestore to google.cloud.firestore;
}