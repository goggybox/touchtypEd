package com.example.touchtyped.service;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.io.*;
import java.util.Properties;

/**
 * Service class for managing application settings across different views
 */
public class AppSettingsService {
    
    private static AppSettingsService instance;
    
    private Properties appSettings;
    private File settingsFile;
    
    public static final String DISPLAY_MODE_KEY = "displayMode";
    public static final String FONT_SIZE_KEY = "fontSize";
    
    public static final String DAY_MODE = "day";
    public static final String NIGHT_MODE = "night";
    public static final String COLORBLIND_MODE = "colorblind";
    
    private AppSettingsService() {
        appSettings = new Properties();
        settingsFile = new File("app_settings.properties");
        loadSettings();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized AppSettingsService getInstance() {
        if (instance == null) {
            instance = new AppSettingsService();
        }
        return instance;
    }
    
    /**
     * Load settings from file
     */
    public void loadSettings() {
        try {
            // Load settings if file exists
            if (settingsFile.exists()) {
                try (FileInputStream in = new FileInputStream(settingsFile)) {
                    appSettings.load(in);
                    System.out.println("Settings loaded successfully");
                }
            } else {
                System.out.println("Settings file does not exist, using defaults");
            }
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
        }
    }
    
    /**
     * Save settings to file
     */
    public void saveSettings() {
        try {
            try (FileOutputStream out = new FileOutputStream(settingsFile)) {
                appSettings.store(out, "TouchTypEd Application Settings");
                System.out.println("Settings saved successfully");
            }
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }
    
    /**
     * Get the current display mode
     */
    public String getDisplayMode() {
        return appSettings.getProperty(DISPLAY_MODE_KEY, DAY_MODE);
    }
    
    /**
     * Set the display mode
     */
    public void setDisplayMode(String mode) {
        appSettings.setProperty(DISPLAY_MODE_KEY, mode);
    }
    
    /**
     * Get the font size
     */
    public double getFontSize() {
        String fontSizeStr = appSettings.getProperty(FONT_SIZE_KEY, "18.0");
        try {
            return Double.parseDouble(fontSizeStr);
        } catch (NumberFormatException e) {
            return 18.0;
        }
    }
    
    /**
     * Set the font size
     */
    public void setFontSize(double fontSize) {
        appSettings.setProperty(FONT_SIZE_KEY, String.valueOf(fontSize));
    }
    
    /**
     * 检查是否是暗黑模式
     */
    public boolean isDarkMode() {
        return NIGHT_MODE.equals(getDisplayMode());
    }
    
    /**
     * 检查是否是色盲模式
     */
    public boolean isColorblindMode() {
        return COLORBLIND_MODE.equals(getDisplayMode());
    }
    
    /**
     * Apply settings to a scene
     */
    public void applySettingsToScene(Scene scene) {
        if (scene == null) return;
        
        String displayMode = getDisplayMode();
        BorderPane root = (BorderPane) scene.getRoot();
        
        // Clear existing styles from both scene and root
        scene.getRoot().getStyleClass().remove("dark-mode");
        scene.getRoot().getStyleClass().remove("colorblind-mode");
        scene.getStylesheets().remove("dark-mode");
        scene.getStylesheets().remove("colorblind-mode");
        
        // Apply selected mode to root element
        switch (displayMode) {
            case NIGHT_MODE:
                root.getStyleClass().add("dark-mode");
                System.out.println("Applied dark mode to scene");
                break;
            case COLORBLIND_MODE:
                root.getStyleClass().add("colorblind-mode");
                System.out.println("Applied colorblind mode to scene");
                break;
            default:
                System.out.println("Applied day mode to scene");
                break;
        }
        
        // Apply font size
        double fontSize = getFontSize();
        root.setStyle("-fx-font-size: " + fontSize + "px;");
    }
} 