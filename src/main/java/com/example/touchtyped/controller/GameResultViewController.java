package com.example.touchtyped.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class GameResultViewController {
    @FXML private Label finalWpmLabel;
    @FXML private Label finalAccLabel;
    @FXML private Label finalCharLabel;

    /**
     * Navigate to learn view
     */
    @FXML
    public void onLearnButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/learn-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) finalWpmLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return to game view
     */
    @FXML
    public void onReturnButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-view.fxml"));
            Scene gameScene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) finalWpmLabel.getScene().getWindow();
            stage.setScene(gameScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set game result data and update the display.
     * @param wpm Words per minute achieved
     * @param correctKeystrokes Number of correctly typed keystrokes
     * @param wrongKeystrokes Number of incorrectly typed keystrokes
     * @param totalKeystrokes Total number of keystrokes
     */
    public void setGameData(int wpm, int correctKeystrokes, int wrongKeystrokes, int totalKeystrokes) {
        // Calculate accuracy percentage
        double accuracy = totalKeystrokes > 0 ? (double) correctKeystrokes / totalKeystrokes * 100 : 0;

        // Update result labels
        finalWpmLabel.setText(String.format("%d", wpm));
        finalAccLabel.setText(String.format("%.0f%%", accuracy));
        finalCharLabel.setText(String.format("%d/%d/%d",
            totalKeystrokes, correctKeystrokes, wrongKeystrokes));
    }
} 