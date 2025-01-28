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
     * 导航到学习视图
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
     * 返回游戏视图
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
     * 设置游戏结果数据
     */
    public void setGameData(List<Double> wpmHistory, int correctKeystrokes, int wrongKeystrokes, int totalKeystrokes) {
        // 计算最终统计数据
        double wpm = wpmHistory.isEmpty() ? 0 : wpmHistory.get(wpmHistory.size() - 1);
        double accuracy = totalKeystrokes > 0 ? (double) correctKeystrokes / totalKeystrokes * 100 : 0;

        // 更新标签
        finalWpmLabel.setText(String.format("%.0f", wpm));
        finalAccLabel.setText(String.format("%.0f%%", accuracy));
        finalCharLabel.setText(String.format("%d/%d/%d/%d", 
            totalKeystrokes, correctKeystrokes, wrongKeystrokes, 
            totalKeystrokes - correctKeystrokes - wrongKeystrokes));
    }
} 