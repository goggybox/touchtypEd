package com.example.touchtyped.controller;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A beautifully styled dialog for player name input
 */
public class PlayerNameDialog {
    
    private static String result = null;
    
    /**
     * Display a dialog to get the player's name
     * @return The player's name or null (if canceled)
     */
    public static String showDialog() {
        result = null;
        
        // Create a new Stage
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("TouchTypEd");
        dialog.getIcons().add(new Image(PlayerNameDialog.class.getResourceAsStream("/com/example/touchtyped/images/logo.png")));
        dialog.setResizable(false);
        
        // Create main container
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setMinWidth(500);
        root.setMinHeight(300);
        
        // Set background color
        root.setBackground(new Background(new BackgroundFill(
                Color.rgb(245, 245, 245), // Light gray background
                CornerRadii.EMPTY,
                Insets.EMPTY)));
        
        // Add title
        Text titleText = new Text("Welcome to TouchTypEd");
        titleText.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleText.setFill(Color.rgb(50, 50, 50)); // Dark gray text
        
        // Add description text
        Text descriptionText = new Text("Please enter your name to start typing:");
        descriptionText.setFont(Font.font("System", 16));
        descriptionText.setFill(Color.rgb(80, 80, 80)); // Medium gray text
        
        // Create name input field
        TextField nameField = new TextField();
        nameField.setPromptText("Your name");
        nameField.setMaxWidth(300);
        nameField.setPrefHeight(40);
        nameField.setStyle("-fx-font-size: 16px; -fx-background-radius: 5px;");
        
        // Create button container
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        // Create confirm button
        Button confirmButton = new Button("Confirm");
        confirmButton.setPrefSize(120, 40);
        confirmButton.setStyle(
                "-fx-background-color: #4CAF50; " + // Green background
                "-fx-text-fill: white; " +          // White text
                "-fx-font-size: 16px; " +           // Font size
                "-fx-background-radius: 5px;");     // Rounded corners
        
        // Create cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefSize(120, 40);
        cancelButton.setStyle(
                "-fx-background-color: #f5f5f5; " + // Light gray background
                "-fx-text-fill: #333333; " +        // Dark gray text
                "-fx-font-size: 16px; " +           // Font size
                "-fx-background-radius: 5px; " +    // Rounded corners
                "-fx-border-color: #cccccc; " +     // Border color
                "-fx-border-radius: 5px;");         // Border rounded corners
        
        // Add buttons to button container
        buttonBox.getChildren().addAll(cancelButton, confirmButton);
        
        // Add all elements to main container
        root.getChildren().addAll(titleText, descriptionText, nameField, buttonBox);
        
        // Set button actions
        confirmButton.setOnAction(e -> {
            result = nameField.getText();
            dialog.close();
        });
        
        cancelButton.setOnAction(e -> {
            result = null;
            dialog.close();
        });
        
        // Set Enter key to confirm
        nameField.setOnAction(e -> {
            result = nameField.getText();
            dialog.close();
        });
        
        // Create scene and display
        Scene scene = new Scene(root);
        dialog.setScene(scene);
        
        // Focus on input field
        Platform.runLater(nameField::requestFocus);
        
        // Show dialog and wait for result
        dialog.showAndWait();
        
        return result;
    }
} 