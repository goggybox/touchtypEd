module com.example.touchtyped {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fazecast.jSerialComm;
    requires javafx.media;
    requires java.desktop;
    requires java.net.http;
    requires com.fasterxml.jackson.datatype.jsr310;

    // For development
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    
    // Import for javafx
    // requires org.junit.jupiter.api;
    // requires org.hamcrest;
    // requires org.mockito;
    
    // Remove old JPA dependencies, use jakarta.persistence
    requires java.sql;
    requires java.sql.rowset;

    // HTTP Server
    requires jdk.httpserver;

    // Spring Boot for REST
    requires spring.web;
    requires spring.core;
    requires spring.beans;
    requires spring.context;

    // Open packages for reflection access
    opens com.example.touchtyped.model to spring.core, spring.beans, spring.context, spring.web;
    // Remove references to non-existent packages
    // opens com.example.touchtyped.server to spring.core, spring.beans, spring.context, spring.web;

    // Open controller package to JavaFX
    opens com.example.touchtyped.controller to javafx.fxml;
    
    // Open serializers package to Jackson
    opens com.example.touchtyped.serializers to com.fasterxml.jackson.databind;
    
    // Export packages
    exports com.example.touchtyped;
    exports com.example.touchtyped.model;
    exports com.example.touchtyped.controller;
    // Remove exports for non-existent packages
    // exports com.example.touchtyped.server;
    exports com.example.touchtyped.app;
    exports com.example.touchtyped.service;
    exports com.example.touchtyped.serializers;
    exports com.example.touchtyped.interfaces;
}