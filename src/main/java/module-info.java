module com.example.touchtyped {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media;

    // Java AWT/Swing
    requires java.desktop;

    // Jackson JSON
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires javafx.swing;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;

    // Spring Boot
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.web;
    requires spring.data.commons;
    requires spring.tx;


    // HTTP Server
    requires jdk.httpserver;
    requires spring.webmvc;
    requires spring.data.jpa;
    requires jakarta.persistence;
    requires com.fazecast.jSerialComm;
    requires google.cloud.core;
    requires google.cloud.firestore;
    requires com.google.auth;
    requires com.google.auth.oauth2;
    requires com.google.api.apicommon;
    requires java.sql;
    requires org.apache.pdfbox;

    opens com.example.touchtyped.model to spring.core, spring.beans, com.fasterxml.jackson.databind, google.cloud.firestore;
    opens com.example.touchtyped.service to spring.core, spring.beans;

    opens com.example.touchtyped.controller to javafx.fxml;

    opens com.example.touchtyped.serialisers to com.fasterxml.jackson.databind;

    exports com.example.touchtyped.app;
    exports com.example.touchtyped.model;
    exports com.example.touchtyped.service;
    exports com.example.touchtyped.controller;
    exports com.example.touchtyped.serialisers to com.fasterxml.jackson.databind;
    exports com.example.touchtyped.firestore to google.cloud.firestore;
    opens com.example.touchtyped.firestore to google.cloud.firestore;
}