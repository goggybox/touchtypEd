package com.example.touchtyped.controller;

import com.example.touchtyped.app.Application;
import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.PlayerRanking;
import com.example.touchtyped.model.TypingPlan;
import com.example.touchtyped.model.TypingPlanManager;
import com.example.touchtyped.model.UserProfile;
import com.example.touchtyped.service.GlobalRankingService;
import com.example.touchtyped.service.RESTClient;
import com.example.touchtyped.service.RESTResponseWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GameResultViewController {
    @FXML private Label finalWpmLabel;
    @FXML private Label finalAccLabel;
    @FXML private Label finalCharLabel;
    @FXML private Label globalRankLabel;

    @FXML private Button generateAdvancedStatsButton; // option to contact REST service for more advanced stats
    @FXML private Label descriptionLabel;
    @FXML private Button viewRankingsButton;

    private KeyLogsStructure keyLogsStructure; // receive the structure from GameView
    private int wpm;
    private double accuracy;
    private String gameMode = "Standard Mode"; // Default game mode
    private PlayerRanking currentRanking;

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
     * Navigate to rankings view
     */
    @FXML
    public void onViewRankingsButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/ranking-view.fxml"));
            Scene rankingScene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) finalWpmLabel.getScene().getWindow();
            stage.setScene(rankingScene);
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
     * @param gameMode Game mode (Timed, Article, Competition)
     * @param playerName Player's name
     */
    public void setGameData(int wpm, int correctKeystrokes, int wrongKeystrokes, int totalKeystrokes, String gameMode, String playerName) {
        // Calculate accuracy percentage
        double accuracy = totalKeystrokes > 0 ? (double) correctKeystrokes / totalKeystrokes * 100 : 0;

        // Update result labels
        finalWpmLabel.setText(String.format("%d", wpm));
        finalAccLabel.setText(String.format("%.0f%%", accuracy));
        finalCharLabel.setText(String.format("%d/%d/%d",
                totalKeystrokes, correctKeystrokes, wrongKeystrokes));

        // Save data
        this.wpm = wpm;
        this.accuracy = accuracy;
        this.gameMode = gameMode;
        
        // Create ranking and submit to global server
        submitRanking(playerName);
    }

    /**
     * Submit ranking to global server
     * @param playerName Player name
     */
    private void submitRanking(String playerName) {
        try {
            // Create ranking object
            currentRanking = new PlayerRanking(playerName, wpm, accuracy, gameMode);

            // Update global ranking label
            if (globalRankLabel != null) {
                globalRankLabel.setText("Submitting to global ranking server...");
            }

            // Test server connection
            GlobalRankingService.getInstance().testConnection()
                .thenAccept(connected -> {
                    if (connected) {
                        System.out.println("Successfully connected to global ranking server");
                        
                        // Submit ranking to global server
                        Application.submitGameRanking(currentRanking);
                        
                        // Get player ranking
                        GlobalRankingService.getInstance().getPlayerPosition(playerName)
                            .thenAccept(position -> {
                                if (position > 0) {
                                    javafx.application.Platform.runLater(() -> {
                                        if (globalRankLabel != null) {
                                            globalRankLabel.setText("Global Rank: #" + position);
                                        }
                                    });
                                } else {
                                    javafx.application.Platform.runLater(() -> {
                                        if (globalRankLabel != null) {
                                            globalRankLabel.setText("Unable to get global ranking position");
                                        }
                                    });
                                }
                            });
                    } else {
                        System.out.println("Unable to connect to global ranking server");
                        javafx.application.Platform.runLater(() -> {
                            if (globalRankLabel != null) {
                                globalRankLabel.setText("Unable to connect to global ranking server");
                            }
                        });
                    }
                });

        } catch (Exception e) {
            System.err.println("Error submitting ranking: " + e.getMessage());
            e.printStackTrace();

            if (globalRankLabel != null) {
                globalRankLabel.setText("Failed to submit ranking");
            }
        }
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
        descriptionLabel.setText("Generating advanced statistics... (Can take up to 30 seconds!)");

        // Create a Task to handle the REST service call
        Task<RESTResponseWrapper> restTask = new Task<>() {
            @Override
            protected RESTResponseWrapper call() throws Exception {
                RESTClient restService = new RESTClient();
                return restService.sendKeyLogs(keyLogsStructure, () ->
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

                    // save this typing plan.
                    TypingPlanManager manager = TypingPlanManager.getInstance();
                    manager.setTypingPlan(typingPlan);
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