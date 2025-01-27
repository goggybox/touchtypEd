package com.example.touchtyped.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Random;

public class GameViewController {

    @FXML
    private VBox gameContainer;

    @FXML
    private Label taskLabel;

    @FXML
    private TextField inputField;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label timerLabel;

    @FXML
    private Button startButton;

    @FXML
    private Button endButton;

    private int score = 0;
    private int timeLeft = 60;
    private Timeline timeline;
    private final Random random = new Random();
    private final String[] words = {"Java", "Game", "Typing", "Programming", "Challenge"};

    @FXML
    public void initialize() {
        resetGameUI();
        inputField.setOnKeyReleased(event -> handleInput());
    }

    public void startGame() {
        resetGame();
        inputField.setDisable(false);
        startButton.setDisable(true);
        endButton.setDisable(false);

        // Start the timer
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            timerLabel.setText("Time left: " + timeLeft + "s");
            if (timeLeft <= 0) {
                endGame();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void endGame() {
        if (timeline != null) {
            timeline.stop();
        }

        // Disable input and buttons
        inputField.setDisable(true);
        startButton.setDisable(false);
        endButton.setDisable(true);

        // Display game over message
        taskLabel.setText("Game Over! Final Score: " + score);
    }

    private void resetGame() {
        score = 0;
        timeLeft = 60;

        // Reset UI elements
        scoreLabel.setText("Score: 0");
        timerLabel.setText("Time left: 60s");
        taskLabel.setText("Get ready to type!");
        inputField.clear();

        // Enable the input field
        inputField.setDisable(false);

        // Generate the first task
        generateNewTask();
    }

    private void resetGameUI() {
        // Reset the UI for the first load
        inputField.setDisable(true);
        endButton.setDisable(true);
        startButton.setDisable(false);
    }

    private void generateNewTask() {
        String newTask = words[random.nextInt(words.length)];
        taskLabel.setText(newTask);
    }

    @FXML
    private void handleInput() {
        String userInput = inputField.getText().trim();
        if (userInput.equals(taskLabel.getText())) {
            score += 10;
            scoreLabel.setText("Score: " + score);
            generateNewTask();
            inputField.clear();
        }
    }

    @FXML
    public void onLearnButtonClick() {
        System.out.println("Learn button clicked!");
        // Add logic to navigate to the learn view
    }

}
