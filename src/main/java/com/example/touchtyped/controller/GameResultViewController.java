package com.example.touchtyped.controller;

import com.example.touchtyped.firestore.ClassroomDAO;
import com.example.touchtyped.firestore.UserDAO;
import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.TypingPlan;
import com.example.touchtyped.model.TypingPlanManager;
import com.example.touchtyped.service.RESTClient;
import com.example.touchtyped.service.RESTResponseWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameResultViewController {
    @FXML private Label finalWpmLabel;
    @FXML private Label finalAccLabel;
    @FXML private Label finalCharLabel;

    @FXML private Button generateAdvancedStatsButton; // option to contact REST service for more advanced stats
    @FXML private Label descriptionLabel;
    @FXML private ImageView classroomButton;

    private KeyLogsStructure keyLogsStructure; // receive the structure from GameView

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
     * Navigate to options view
     */
    @FXML
    public void onOptionButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/option-view.fxml"));
            Scene optionScene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) finalWpmLabel.getScene().getWindow();
            stage.setScene(optionScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClassroomButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/classroom-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) classroomButton.getScene().getWindow();
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

    /**
     * receive keyLogsStructure from GameView page
     * @param keyLogsStructure is the structure to receive
     */
    public void setKeyLogsStructure(KeyLogsStructure keyLogsStructure) {
        this.keyLogsStructure = keyLogsStructure;
    }

    @FXML
    public void onGenerateAdvancedStatsButtonClick() {
        if (keyLogsStructure == null) {
            System.err.println("KeyLogsStructure is null");
            return;
        }

        // Disable the button and show a loading message
        generateAdvancedStatsButton.setDisable(true);
        descriptionLabel.setText("Generating typing plan... (Can take up to 30 seconds!)");

        // Create a Task to handle the REST service call - should return a TypingPlan
        Task<RESTResponseWrapper> restTask = new Task<>() {
            @Override
            protected RESTResponseWrapper call() throws Exception {
                RESTClient restService = new RESTClient();
                return restService.getTypingPlan(keyLogsStructure, () ->
                        descriptionLabel.setText("Request failed. Trying again... (Please be patient!)"));
            }
        };

        // Handle the result of the Task on the JavaFX Application Thread
        restTask.setOnSucceeded(event -> {
            try {
                RESTResponseWrapper response = restTask.getValue();

                // handle pdf
                if (response.getPdfData() != null) {
                    System.out.println("Decoded PDF size: " + response.getPdfData().length + " bytes");
                    saveAndDisplayPDF(response.getPdfData());
                }

                // handle TypingPlan
                TypingPlan typingPlan = response.getTypingPlan();
                if (typingPlan != null) {
                    System.out.println("Received Typing Plan: " + typingPlan);
                    descriptionLabel.setText("Successfully updated typing plan.");
                    generateAdvancedStatsButton.setVisible(false);

                    // save this typing plan.
                    TypingPlanManager manager = TypingPlanManager.getInstance();
                    manager.setTypingPlan(typingPlan);
                    TypingPlanManager.getInstance().saveTypingPlan();

                    // save to database - using cached credentials
                    Map<String, String> credentials = ClassroomDAO.loadUserCache();
                    if (credentials == null) {
                        System.out.println("Not saved to database; not logged in.");
                    } else {
                        String classroomID = credentials.get("classroomID");
                        String username = credentials.get("username");
                        String password = credentials.getOrDefault("password", null);
                        UserDAO.updateTypingPlan(classroomID, username, typingPlan, password);
                        System.out.println("Saved typing plan to database.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        restTask.setOnFailed(event -> {
            Throwable exception = restTask.getException();
            System.err.println("An error occurred while communicating with the REST service.");
            exception.printStackTrace();

            // notify the user.
            descriptionLabel.setText("REST Service failed to generate advanced statistics after multiple attempts. Please try again later.");
        });

        // Start the Task in a new thread
        new Thread(restTask).start();
    }

    private void saveAndDisplayPDF(byte[] pdf) {
        try {
            File tempFile = File.createTempFile("typing-stats-", ".pdf");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdf);
            }

            // open the pdf file (if supported, otherwise just give them the filepath).
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();

                new Thread(() -> {
                    try {
                        desktop.open(tempFile);
                    } catch (IOException e) {
                        System.err.println("Failed to open the PDF file.");
                        e.printStackTrace();
                    }
                }).start();
            } else {
                System.out.println("PDF saved to: " + tempFile.getAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("An error occurred while displaying PDF.");
        }
    }
} 