package com.example.touchtyped.controller;

import com.example.touchtyped.model.UserProfile;
import com.example.touchtyped.service.AppSettingsService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.*;

public class OptionsViewController {

    @FXML
    private RadioButton dayModeRadio;
    
    @FXML
    private RadioButton nightModeRadio;
    
    @FXML
    private RadioButton colorblindModeRadio;
    
    @FXML
    private Slider fontSizeSlider;
    
    @FXML
    private ImageView learnButton;
    
    @FXML
    private ImageView gamesButton;
    
    @FXML
    private ImageView classroomButton;
    
    private ToggleGroup displayMode;
    
    private AppSettingsService settingsService;
    
    @FXML
    public void initialize() {
        // Get settings service
        settingsService = AppSettingsService.getInstance();
        
        // Initialize toggle group
        displayMode = new ToggleGroup();
        dayModeRadio.setToggleGroup(displayMode);
        nightModeRadio.setToggleGroup(displayMode);
        colorblindModeRadio.setToggleGroup(displayMode);
        
        // Add listener for font size slider changes if slider exists
        if (fontSizeSlider != null) {
            fontSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                onFontSizeChanged();
            });
        }
        
        // Load saved settings
        loadSavedSettings();
    }
    
    private void loadSavedSettings() {
        // Load display mode setting
        String savedDisplayMode = settingsService.getDisplayMode();
        switch (savedDisplayMode) {
            case AppSettingsService.NIGHT_MODE:
                nightModeRadio.setSelected(true);
                applyNightMode();
                break;
            case AppSettingsService.COLORBLIND_MODE:
                colorblindModeRadio.setSelected(true);
                applyColorblindMode();
                break;
            default:
                dayModeRadio.setSelected(true);
                applyDayMode();
                break;
        }
        
        // Load font size setting only if slider exists
        if (fontSizeSlider != null) {
            double savedFontSize = settingsService.getFontSize();
            fontSizeSlider.setValue(savedFontSize);
        }
    }
    
    @FXML
    public void onDisplayModeChanged() {
        if (dayModeRadio.isSelected()) {
            applyDayMode();
            settingsService.setDisplayMode(AppSettingsService.DAY_MODE);
        } else if (nightModeRadio.isSelected()) {
            applyNightMode();
            settingsService.setDisplayMode(AppSettingsService.NIGHT_MODE);
        } else if (colorblindModeRadio.isSelected()) {
            applyColorblindMode();
            settingsService.setDisplayMode(AppSettingsService.COLORBLIND_MODE);
        }
        
        // Save settings immediately
        settingsService.saveSettings();
    }
    
    private void applyDayMode() {
        Scene scene = dayModeRadio.getScene();
        if (scene != null) {
            BorderPane root = (BorderPane) scene.getRoot();
            root.getStyleClass().remove("dark-mode");
            root.getStyleClass().remove("colorblind-mode");
        }
    }
    
    private void applyNightMode() {
        Scene scene = nightModeRadio.getScene();
        if (scene != null) {
            BorderPane root = (BorderPane) scene.getRoot();
            root.getStyleClass().add("dark-mode");
            root.getStyleClass().remove("colorblind-mode");
        }
    }
    
    private void applyColorblindMode() {
        Scene scene = colorblindModeRadio.getScene();
        if (scene != null) {
            BorderPane root = (BorderPane) scene.getRoot();
            root.getStyleClass().remove("dark-mode");
            root.getStyleClass().add("colorblind-mode");
        }
    }
    
    @FXML
    public void onFontSizeChanged() {
        // Only proceed if slider exists
        if (fontSizeSlider == null) return;
        
        double fontSize = fontSizeSlider.getValue();
        // Apply font size changes to the UI
        Scene scene = fontSizeSlider.getScene();
        if (scene != null) {
            scene.getRoot().setStyle("-fx-font-size: " + fontSize + "px;");
        }
        
        // Update settings
        settingsService.setFontSize(fontSize);
        settingsService.saveSettings();
    }
    
    @FXML
    public void onSaveButtonClick() {
        try {
            // Show confirmation alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Settings Saved");
            alert.setHeaderText(null);
            alert.setContentText("Your settings have been saved successfully.");
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error during save operation: " + e.getMessage());
            
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to save settings: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    public void onLearnButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/learn-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            
            // Apply saved settings to new scene
            settingsService.applySettingsToScene(scene);
            
            Stage stage = (Stage) learnButton.getScene().getWindow();
            boolean wasFullScreen = stage.isFullScreen();
            
            if(wasFullScreen) {
                stage.setOpacity(0);
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.setOpacity(1);
            } else {
                stage.setScene(scene);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void onGamesButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            
            // Apply saved settings to new scene
            settingsService.applySettingsToScene(scene);
            
            Stage stage = (Stage) gamesButton.getScene().getWindow();
            boolean wasFullScreen = stage.isFullScreen();
            
            if(wasFullScreen) {
                stage.setOpacity(0);
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.setOpacity(1);
            } else {
                stage.setScene(scene);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void onClassroomButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/classroom-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            
            // Apply saved settings to new scene
            settingsService.applySettingsToScene(scene);
            
            Stage stage = (Stage) classroomButton.getScene().getWindow();
            boolean wasFullScreen = stage.isFullScreen();
            
            if(wasFullScreen) {
                stage.setOpacity(0);
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.setOpacity(1);
            } else {
                stage.setScene(scene);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 