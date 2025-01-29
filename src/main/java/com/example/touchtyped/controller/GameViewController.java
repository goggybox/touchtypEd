package com.example.touchtyped.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Random;

import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.GameKeypressListener;

/**
 * Controller for the typing game view.
 * Handles game logic, user input, and statistics tracking.
 */
public class GameViewController {

    // UI Components
    @FXML private VBox gameContainer;
    @FXML private TextFlow taskLabel;
    @FXML private TextField inputField;
    @FXML private Label timerLabel;
    @FXML private Text cursorLabel;
    
    // Time selection buttons
    @FXML private Button time15Button;
    @FXML private Button time30Button;
    @FXML private Button time60Button;
    @FXML private Button time120Button;
    
    // Result display components
    @FXML private VBox resultContainer;

    // Game state variables
    private int timeLeft = 60;
    private Timeline timeline;
    private final Random random = new Random();
    private StringBuilder currentSentence;
    private int currentCharIndex;
    private int selectedTimeOption = 60;
    private boolean gameStarted = false;

    // Statistics tracking
    private long gameStartTime;
    private int totalKeystrokes = 0;
    private int correctKeystrokes = 0;
    private int wrongKeystrokes = 0;
    private final StringBuilder typingHistory = new StringBuilder();

    private KeyboardInterface keyboardInterface;
    private GameKeypressListener keyPressListener;

    // Word bank for typing practice
    private final String[] words = {
            // Length 5
            "apple", "music", "dream", "water", "cloud",
            "happy", "train", "fruit", "stone", "heart",
            "night", "sweet", "grace", "voice", "world",
            "space", "smile", "field", "power", "green",
            "light", "river", "peace", "youth", "house",

            // Length 6
            "simple", "travel", "orange", "stream", "global",
            "summer", "bridge", "beacon", "flight", "island",
            "nature", "action", "vision", "larger", "breeze",
            "matter", "garden", "beauty", "bright", "source",
            "family", "friend", "spirit", "memory", "window",

            // Length 7
            "project", "crystal", "thunder", "freedom", "example",
            "journey", "harmony", "weather", "forever", "teacher",
            "gateway", "network", "village", "science", "student",
            "rainbow", "program", "mystery", "magical", "picture",
            "fantasy", "history", "justice", "captain", "library",

            // Length 8
            "notebook", "sunshine", "mountain", "paradigm", "umbrella",
            "rainbow", "evergreen", "explorer", "pleasant", "strength",
            "platform", "activity", "horizon", "emphasis", "survivor",
            "football", "software", "universe", "operator", "lifetime",
            "original", "movement", "champion", "decision", "discover",

            // Length 9
            "adventure", "champion", "education", "fantastic", "motivator",
            "beautiful", "volcanoes", "disaster", "insurance", "highlight",
            "president", "important", "christmas", "stronger", "backpacks",
            "character", "sunflower", "godfather", "wonderful", "solutions",
            "marketing", "collector", "blueprint", "backstage", "backspace",

            // Length 10
            "revolution", "impression", "generation", "technology", "illuminati",
            "motivation", "destination", "exploration", "performance", "journalism",
            "importance", "foundation", "connection", "application", "reflection",
            "discovery", "landscapes", "overcoming", "abandoned", "friendship",
            "assessment", "commander", "incredible", "environment", "membership"
    };

    /**
     * Initializes the game controller.
     * Sets up UI components and default game state.
     */
    @FXML
    public void initialize() {
        // Initialize keyboard interface
        keyboardInterface = new KeyboardInterface();
        keyPressListener = new GameKeypressListener(this, keyboardInterface);
        
        // Set up UI components
        time15Button.setFocusTraversable(false);
        time30Button.setFocusTraversable(false);
        time60Button.setFocusTraversable(false);
        time120Button.setFocusTraversable(false);
        
        // Set default time
        updateTimeButtonStyle(60);
        
        // Attach keyboard listener when scene is available
        gameContainer.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                keyboardInterface.attachToScene(newScene);
            }
        });

        // Initialize game state
        resetGame();
    }

    /**
     * Resets the game to its initial state.
     * Clears all statistics, stops timer, and generates new typing task.
     */
    @FXML
    public void resetGame() {
        // Stop existing timer if running
        if (timeline != null) {
            timeline.stop();
        }
        
        // Reset game state
        gameStarted = false;
        timeLeft = selectedTimeOption;
        currentCharIndex = 0;
        totalKeystrokes = 0;
        correctKeystrokes = 0;
        wrongKeystrokes = 0;
        typingHistory.setLength(0);
        currentSentence = null;

        // Reset UI
        timerLabel.setText(String.valueOf(timeLeft));
        inputField.clear();
        inputField.setDisable(false);
        resultContainer.setVisible(false);

        // Generate new typing task
        generateNewTask();
        cursorLabel.setVisible(false);
    }

    /**
     * Starts the typing game.
     * Initializes timer and begins tracking user input.
     */
    private void startGame() {
        if (!gameStarted) {
            gameStarted = true;
            cursorLabel.setVisible(true);
            
            // Start countdown timer
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                timeLeft--;
                timerLabel.setText(String.valueOf(timeLeft));
                if (timeLeft <= 0) {
                    endGame();
                }
            }));
            timeline.setCycleCount(selectedTimeOption);
            timeline.play();
            
            gameStartTime = System.currentTimeMillis();
            updateTaskDisplay();
        }
    }

    /**
     * Ends the current game session.
     * Calculates final statistics and displays results.
     * TODO: Graph is not finished now.
     */
    private void endGame() {
        if (timeline != null) {
            timeline.stop();
        }

        // Generate game statistics
        generateGameData();
        
        // Load result view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-result-view.fxml"));
            Scene resultScene = new Scene(loader.load(), 1200, 700);
            
            // Get result controller and set data
            GameResultViewController resultController = loader.getController();
            
            // Calculate final WPM
            double finalWpm = (correctKeystrokes / 5.0) / (selectedTimeOption / 60.0);
            resultController.setGameData(
                (int)finalWpm,  // Pass calculated WPM
                correctKeystrokes, 
                wrongKeystrokes, 
                totalKeystrokes
            );
            
            // Display result scene
            Stage stage = (Stage) gameContainer.getScene().getWindow();
            stage.setScene(resultScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the display of the typing task.
     * Shows typed and remaining text with appropriate styling.
     */
    private void updateTaskDisplay() {
        taskLabel.getChildren().clear();
        
        // Calculate visible text range
        int visibleTextLength = 50;
        int start = Math.max(0, currentCharIndex - visibleTextLength /2);
        int end = Math.min(currentSentence.length(), start + visibleTextLength);
        
        // Calculate cursor position
        Text sampleText = new Text("W");
        sampleText.setFont(cursorLabel.getFont());
        double charWidth = sampleText.getLayoutBounds().getWidth();
        double baseX = -(visibleTextLength * charWidth) / 2;
        double offset = (currentCharIndex - start) * charWidth;
        cursorLabel.setTranslateX(baseX + offset);
        
        // Display typed text
        if (currentCharIndex > start) {
            Text typedText = new Text(currentSentence.substring(start, currentCharIndex));
            typedText.getStyleClass().add("typed-text");
            taskLabel.getChildren().add(typedText);
        }
        
        // Display remaining text
        if (currentCharIndex < currentSentence.length()) {
            Text remainingText = new Text(currentSentence.substring(currentCharIndex, end));
            remainingText.getStyleClass().add("remaining-text");
            taskLabel.getChildren().add(remainingText);
        }
    }

    /**
     * Generates a new typing task.
     * Creates initial sentence from word bank if none exists.
     */
    private void generateNewTask() {
        // Initialize sentence with initial words
        if (currentSentence == null) {
            currentSentence = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                addNewWord();
            }
            updateTaskDisplay();
            
            // Provide haptic feedback for first character
            if (!currentSentence.isEmpty() && keyboardInterface != null) {  // 添加空检查
                String nextChar = String.valueOf(currentSentence.charAt(0));
                if (nextChar.equals(" ")) {
                    nextChar = "space";
                }
                keyboardInterface.sendHapticCommand(nextChar, 200, 50);
            }
        }
    }

    /**
     * Adds a new word to the current sentence.
     * Includes space separator if not the first word.
     */
    private void addNewWord() {
        // Add space between words
        if (!currentSentence.isEmpty()) {
            currentSentence.append(" ");
        }
        currentSentence.append(words[random.nextInt(words.length)]);
    }

    /**
     * Handles navigation to learn view.
     * Loads and displays the learn-view.fxml scene.
     */
    @FXML
    public void onLearnButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/learn-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) taskLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles keyboard input during the typing game.
     * Called by GameKeypressListener when a key is pressed.
     */
    public void handleKeyPress(String key) {
        if (!inputField.isDisabled()) {
            totalKeystrokes++;
            char expectedChar = currentSentence.charAt(currentCharIndex);
            String expectedKey = expectedChar == ' ' ? "space" : String.valueOf(expectedChar);
            
            if (key.equalsIgnoreCase(expectedKey)) {
                // Start game on first correct input
                if (!gameStarted) {
                    startGame();
                }
                
                correctKeystrokes++;
                currentCharIndex++;
                inputField.setText(currentSentence.substring(0, currentCharIndex));
                
                // Add new words when running low on text
                if (currentSentence.length() - currentCharIndex < 30) {
                    addNewWord();
                }
                
                updateTaskDisplay();
                typingHistory.append(String.format("+%s,", key));
                
                // Provide haptic feedback for next character
                if (currentCharIndex < currentSentence.length()) {
                    String nextChar = String.valueOf(currentSentence.charAt(currentCharIndex));
                    if (nextChar.equals(" ")) {
                        nextChar = "space";
                    }
                    keyboardInterface.sendHapticCommand(nextChar, 200, 50);
                }
            } else {
                // Handle incorrect input
                wrongKeystrokes++;
                typingHistory.append(String.format("-%s,", key));
                keyboardInterface.sendHapticCommand(key, 500, 100);
                keyboardInterface.activateLights(1000);
            }
        }
    }

    /**
     * Generates JSON formatted game data.
     * Includes timestamp, duration, WPM, accuracy, and keystroke statistics.
     */
    private void generateGameData() {
        long gameEndTime = System.currentTimeMillis();
        double accuracy = (double) correctKeystrokes / totalKeystrokes * 100;
        double wpm = (correctKeystrokes / 5.0) / (selectedTimeOption / 60.0);  // Standard WPM calculation

        // Create JSON data
        String gameData = String.format("""
            {
                "timestamp": %d,
                "duration": %d,
                "wpm": %.2f,
                "accuracy": %.2f,
                "keystrokes": {
                    "total": %d,
                    "correct": %d,
                    "wrong": %d
                },
                "history": "%s"
            }""",
            gameEndTime,
            selectedTimeOption,
            wpm,
            accuracy,
            totalKeystrokes,
            correctKeystrokes,
            wrongKeystrokes,
            typingHistory.toString()
        );

        System.out.println(gameData);  // Temporary output, can be saved to file or sent to server
    }

    /**
     * Updates the visual style of time selection buttons.
     * Highlights selected time option and updates game settings.
     *
     * @param selectedTime The selected time duration in seconds
     */
    private void updateTimeButtonStyle(int selectedTime) {
        // Remove selection style from all buttons
        time15Button.getStyleClass().remove("selected");
        time30Button.getStyleClass().remove("selected");
        time60Button.getStyleClass().remove("selected");
        time120Button.getStyleClass().remove("selected");
        
        // Add selection style to chosen button
        switch (selectedTime) {
            case 15 -> time15Button.getStyleClass().add("selected");
            case 30 -> time30Button.getStyleClass().add("selected");
            case 60 -> time60Button.getStyleClass().add("selected");
            case 120 -> time120Button.getStyleClass().add("selected");
        }
        
        // Update time settings
        selectedTimeOption = selectedTime;
        timeLeft = selectedTime;
        timerLabel.setText(String.valueOf(timeLeft));
    }

    // Time selection handlers
    /**
     * Sets game duration to 15 seconds.
     */
    @FXML public void select15s() {
        selectedTimeOption = 15;
        updateTimeButtonStyle(15);
        resetGame();
    }

    /**
     * Sets game duration to 30 seconds.
     */
    @FXML public void select30s() {
        selectedTimeOption = 30;
        updateTimeButtonStyle(30);
        resetGame();
    }

    /**
     * Sets game duration to 60 seconds.
     */
    @FXML public void select60s() {
        selectedTimeOption = 60;
        updateTimeButtonStyle(60);
        resetGame();
    }

    /**
     * Sets game duration to 120 seconds.
     */
    @FXML public void select120s() {
        selectedTimeOption = 120;
        updateTimeButtonStyle(120);
        resetGame();
    }

    public boolean isInputDisabled() {
        return inputField.isDisabled();
    }
}
