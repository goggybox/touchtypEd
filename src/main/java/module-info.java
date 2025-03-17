module com.example.touchtyped {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    
    // Jackson JSON
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    
    // Spring Boot
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.web;
    requires spring.data.commons;
    requires spring.tx;

    // JPA
    requires java.persistence;
    
    // HTTP Server
    requires jdk.httpserver;
    requires spring.webmvc;
    requires spring.data.jpa;
    requires jakarta.persistence;

    // 开放包供Spring反射访问
    opens com.example.touchtyped.model to spring.core, spring.beans, com.fasterxml.jackson.databind;
    opens com.example.touchtyped.server to spring.core, spring.beans, spring.context, spring.web;
    opens com.example.touchtyped.service to spring.core, spring.beans;
    
    exports com.example.touchtyped.app;
    exports com.example.touchtyped.model;
    exports com.example.touchtyped.service;
    exports com.example.touchtyped.server;
}