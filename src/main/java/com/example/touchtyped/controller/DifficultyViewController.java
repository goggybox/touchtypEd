package com.example.touchtyped.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the difficulty selection view
 */
public class DifficultyViewController {
    // This VBox corresponds to the root node in difficulty-view.fxml (fx:id="root")
    // If you need to get the current Stage, you can do so with root.getScene().getWindow()
    @FXML
    private BorderPane rootPane; // Corresponds to fx:id="rootPane"
    
    // When 15s button is clicked
    @FXML
    private void on15sButtonClick() {
        loadGameViewWithTime(15);
    }
    
    // When 30s button is clicked
    @FXML
    private void on30sButtonClick() {
        loadGameViewWithTime(30);
    }
    
    // When 60s button is clicked
    @FXML
    private void on60sButtonClick() {
        loadGameViewWithTime(60);
    }
    
    // When 120s button is clicked
    @FXML
    private void on120sButtonClick() {
        loadGameViewWithTime(120);
    }
    
    /**
     * Generic method: After selecting difficulty, load GameView with time parameter
     * @param time Seconds to set (15/30/60/120)
     */
    private void loadGameViewWithTime(int time) {
        try {
            // 1. Load GameView FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-view.fxml"));
            Scene gameScene = new Scene(loader.load(), 1200, 700);
            
            // 2. Get the corresponding GameViewController
            GameViewController controller = loader.getController();
            // You can directly call the logic for the 4 buttons here
            controller.initTimedGame(time);
            
            // 3. Set the scene to the current stage
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(gameScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
