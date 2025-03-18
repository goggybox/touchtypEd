module com.example.touchtyped {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    
    // Java AWT/Swing
    requires java.desktop;
    
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
    // 移除旧版JPA依赖，使用jakarta.persistence
    // requires java.persistence;
    
    // HTTP Server
    requires jdk.httpserver;
    requires spring.webmvc;
    requires spring.data.jpa;
    requires jakarta.persistence;
    requires com.fazecast.jSerialComm;

    // 开放包供Spring反射访问
    opens com.example.touchtyped.model to spring.core, spring.beans, com.fasterxml.jackson.databind;
    opens com.example.touchtyped.server to spring.core, spring.beans, spring.context, spring.web;
    opens com.example.touchtyped.service to spring.core, spring.beans;
    
    // 开放控制器包供JavaFX FXML加载器访问
    opens com.example.touchtyped.controller to javafx.fxml;
    
    // 开放序列化器包供Jackson访问
    opens com.example.touchtyped.serialisers to com.fasterxml.jackson.databind;
    
    exports com.example.touchtyped.app;
    exports com.example.touchtyped.model;
    exports com.example.touchtyped.service;
    exports com.example.touchtyped.server;
    exports com.example.touchtyped.controller;
    exports com.example.touchtyped.serialisers;
}