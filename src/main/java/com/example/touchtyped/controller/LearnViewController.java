package com.example.touchtyped.controller;

import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.*;
import com.example.touchtyped.model.Module;
import com.example.touchtyped.serialisers.TypingPlanDeserialiser;
import com.example.touchtyped.service.AppSettingsService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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

    @FXML
    public void onGamesButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            
            // Apply settings to the scene
            settingsService.applySettingsToScene(scene);
            
            Stage stage = (Stage) gamesButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
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