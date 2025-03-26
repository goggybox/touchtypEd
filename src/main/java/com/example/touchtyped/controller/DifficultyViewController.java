package com.example.touchtyped.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class DifficultyViewController {

    @FXML
    private BorderPane rootPane;

    @FXML
    public void on15sSelect() {
//        Stage stage = (Stage) rootPane.getScene().getWindow();
        openGameWithTime(15);
    }

    @FXML
    public void on30sSelect() {
        openGameWithTime(30);
    }


    @FXML
    public void on60sSelect() {
        openGameWithTime(60);
    }

    @FXML
    public void on120sSelect() {
        openGameWithTime(120);
    }

    private void openGameWithTime(int time) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-view.fxml"));
            Parent gameRoot = loader.load();

            GameViewController gameController = loader.getController();

            switch (time) {
                case 15 -> gameController.select15s();
                case 30 -> gameController.select30s();
                case 60 -> gameController.select60s();
                case 120 -> gameController.select120s();
            }

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(gameRoot, 1200, 700));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
