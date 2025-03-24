package com.example.touchtyped.controller;

import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.*;
import com.example.touchtyped.model.Module;
import com.example.touchtyped.serialisers.TypingPlanDeserialiser;
import com.example.touchtyped.service.AppSettingsService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class LearnViewController {

    /**
     * reference to the learnButton in learn-view.fxml
     */
    @FXML
    private Button learnButton;

    /**
     * reference to the gamesButton in learn-view.fxml
     */
    @FXML
    private ImageView gamesButton;

    /**
     * reference to the optionsButton in learn-view.fxml
     */
    @FXML
    private ImageView optionsButton;

    /**
     * reference to the VBox in learn-view.fxml
     */
    @FXML
    private VBox vbox;


    private KeyboardInterface keyboardInterface = new KeyboardInterface();
    private AppSettingsService settingsService;

    public void initialize() {
        // Get settings service
        settingsService = AppSettingsService.getInstance();
        
        // Attach keyboard interface to scene, when scene is available
        Platform.runLater(() -> {
            Scene scene = vbox.getScene(); // Use vbox's scene instead of buttonGrid's
            if (scene != null) {
                // Apply settings
                settingsService.applySettingsToScene(scene);
                
                keyboardInterface.attachToScene(scene);
                keyboardInterface.stopHaptic();
                // Example keypress listener
                new ExampleKeypressListener(keyboardInterface);
            } else {
                System.err.println("Scene is not available yet.");
            }
        });

        // Load font
        Font antipastoFont = Font.loadFont(getClass().getResource("/fonts/AntipastoPro.ttf").toExternalForm(), 26);

        // load TypingPlan from JSON and display.
        TypingPlan typingPlan = TypingPlanManager.getInstance().getTypingPlan();
        typingPlan.display(vbox);
        HBox divider = DividerLine.createDividerLineWithText("");
        vbox.getChildren().add(divider);
    }

    public void setKeyboardInterface(KeyboardInterface keyboardInterface){
        this.keyboardInterface = keyboardInterface;
    }

    @FXML
    public void onGamesButtonClick() {
        System.out.println("onGamesButtonClick方法被调用");
        try {
            // 先尝试一个非常简单的测试，只显示一个警告对话框
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("测试");
            alert.setHeaderText("测试游戏按钮点击");
            alert.setContentText("如果你看到这个对话框，说明游戏按钮的点击事件正常工作！");
            alert.showAndWait();
            
            System.out.println("尝试加载game-view.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-view.fxml"));
            System.out.println("开始调用loader.load()");
            Scene scene = new Scene(loader.load(), 1200, 700);
            System.out.println("loader.load()调用完成");
            
            System.out.println("获取GameViewController");
            GameViewController gameViewController = loader.getController();
            System.out.println("设置keyboardInterface");
            gameViewController.setKeyboardInterface(keyboardInterface);
            
            System.out.println("应用场景设置");
            settingsService.applySettingsToScene(scene);
            
            System.out.println("获取当前窗口并切换场景");
            Stage stage = (Stage) gamesButton.getScene().getWindow();
            stage.setScene(scene);
            System.out.println("场景切换完成");
        } catch (IOException e) {
            System.out.println("发生IO异常: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("发生其他异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onOptionsButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/options-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            
            // Apply settings to the scene
            settingsService.applySettingsToScene(scene);
            
            Stage stage = (Stage) optionsButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}