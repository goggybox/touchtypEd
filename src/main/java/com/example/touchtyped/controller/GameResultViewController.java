package com.example.touchtyped.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

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
     * Set game result data and update the display
     */
    public void setGameData(List<Double> wpmHistory, int correctKeystrokes, int wrongKeystrokes, int totalKeystrokes) {
        // Calculate final statistics
        double wpm = 0;  // Default to 0
        if (!wpmHistory.isEmpty()) {
            double lastWpm = wpmHistory.get(wpmHistory.size() - 1);
            wpm = Double.isInfinite(lastWpm) || Double.isNaN(lastWpm) ? 0 : lastWpm;
        }

        double accuracy = totalKeystrokes > 0 ? (double) correctKeystrokes / totalKeystrokes * 100 : 0;

        // Update labels
        finalWpmLabel.setText(String.format("%.0f", wpm));
        finalAccLabel.setText(String.format("%.0f%%", accuracy));
        finalCharLabel.setText(String.format("%d/%d/%d",
            totalKeystrokes, correctKeystrokes, wrongKeystrokes));
    }
} 