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

    // 这个 VBox 对应 difficulty-view.fxml 里的根节点（fx:id="root"）
    // 如果你需要用它获取当前 Stage，就可以通过 root.getScene().getWindow() 来拿到 Stage
    @FXML
    private BorderPane rootPane; // 对应 fx:id="rootPane"

    // 当点击 15s 按钮
    @FXML
    public void on15sSelect() {
//        Stage stage = (Stage) rootPane.getScene().getWindow();
        openGameWithTime(15);
    }

    // 当点击 30s 按钮
    @FXML
    public void on30sSelect() {
        openGameWithTime(30);
    }

    // 当点击 60s 按钮
    @FXML
    public void on60sSelect() {
        openGameWithTime(60);
    }

    // 当点击 120s 按钮
    @FXML
    public void on120sSelect() {
        openGameWithTime(120);
    }

    /**
     * 通用方法：按下难度后，加载 GameView 并把时间参数带过去
     * @param time 要设置的秒数 (15/30/60/120)
     */
    private void openGameWithTime(int time) {
        try {
            // 1. 加载 GameView 的 FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-view.fxml"));
            Parent gameRoot = loader.load();

            // 2. 获取对应的 GameViewController
            GameViewController gameController = loader.getController();
            // 这里可以直接调用它已有的 4 个按钮的逻辑
            // 或者你也可以写个 setTimeOption(...) 方法
            // 例如：
            switch (time) {
                case 15 -> gameController.select15s();
                case 30 -> gameController.select30s();
                case 60 -> gameController.select60s();
                case 120 -> gameController.select120s();
            }

            // 3. 切换场景到 GameView
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(gameRoot, 1200, 700));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
