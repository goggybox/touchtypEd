package com.example.touchtyped.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class GameViewController {
    @FXML
    private VBox gameContainer;
    
    @FXML
    private void onBackButtonClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/learn-view.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 700);
        Stage stage = (Stage) gameContainer.getScene().getWindow();
        stage.setScene(scene);
    }
} 