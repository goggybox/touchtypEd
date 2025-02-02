package com.example.touchtyped.controller;

import com.example.touchtyped.constants.StyleConstants;
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
import java.util.List;

import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.GameKeypressListener;
import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.KeyLog;

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

    private KeyboardInterface keyboardInterface;
    private GameKeypressListener keyPressListener;

    private KeyLogsStructure keyLogsStructure;

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
     * Array to track error states for each character in the typing sequence.
     * True indicates an error at that position.
     */
    private final boolean[] charErrorStates = new boolean[1000];  // Assume max length of 1000

    /** Flag to track if the first character input was incorrect */
    private boolean hasFirstError = false;

    /** Flag to track if there's an unresolved typing error */
    private boolean hasUnresolvedError = false;

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
        currentSentence = null;

        // Reset UI
        timerLabel.setText(String.valueOf(timeLeft));
        inputField.clear();
        inputField.setDisable(false);
        resultContainer.setVisible(false);

        // Generate new typing task
        generateNewTask();
        keyLogsStructure = new KeyLogsStructure(currentSentence.toString());
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

        System.out.println(keyLogsStructure.toString());
        
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
        
        // Display typed text (completed text)
        if (currentCharIndex > start) {
            for (int i = start; i < currentCharIndex; i++) {
                Text charText = new Text(String.valueOf(currentSentence.charAt(i)));
                charText.getStyleClass().add(charErrorStates[i] ? "error-text" : "typed-text");
                taskLabel.getChildren().add(charText);
            }
        }
        
        // Display current character
        if (currentCharIndex < currentSentence.length()) {
            Text currentChar = new Text(String.valueOf(currentSentence.charAt(currentCharIndex)));
            // Only show red color when the first character is typed incorrectly
            if (!gameStarted && hasFirstError) {
                currentChar.getStyleClass().add("error-text");
            } else if (gameStarted && charErrorStates[currentCharIndex]) {
                currentChar.getStyleClass().add("error-text");
            } else {
                currentChar.getStyleClass().add("remaining-text");
            }
            taskLabel.getChildren().add(currentChar);
        }
        
        // Display remaining text
        if (currentCharIndex + 1 < end) {
            Text remainingText = new Text(currentSentence.substring(currentCharIndex + 1, end));
            remainingText.getStyleClass().add("remaining-text");
            taskLabel.getChildren().add(remainingText);
        }
        
        // Update cursor position
//        Text sampleText = new Text("W");
//        sampleText.setFont(cursorLabel.getFont());
//        double charWidth = sampleText.getLayoutBounds().getWidth();
//        System.out.println("charWidth: " + charWidth);
//        System.out.println("Current font: " + sampleText.getFont().getName());
//        System.out.println("Current font family: " + sampleText.getFont().getFamily());
//
//        double baseX = -(visibleTextLength * charWidth) / 2;
//        double offset = (currentCharIndex - start) * charWidth;
//        double finalX = baseX + offset;
//        System.out.println("finalX: " + finalX);

         double baseX = -(visibleTextLength * StyleConstants.charWidth / 2.0);
         double offset = (currentCharIndex - start) * StyleConstants.charWidth;
         double finalX = baseX + offset;

        cursorLabel.setTranslateX(finalX);
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
            if (!currentSentence.isEmpty() && keyboardInterface != null) {
                String nextChar = String.valueOf(currentSentence.charAt(0)).toUpperCase();
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
    private String addNewWord() {
        // Add space between words
        if (!currentSentence.isEmpty()) {
            currentSentence.append(" ");
        }
        String newWord = words[random.nextInt(words.length)];
        currentSentence.append(newWord);
        return newWord;
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
     * Records each keystroke in the KeyLogsStructure and provides feedback.
     *
     * @param key The string representation of the pressed key
     */
    public void handleKeyPress(String key) {
        if (inputField.isDisabled()) {
            return;
        }

        // Before game starts, only process letters and numbers
        if (!gameStarted) {
            if (!key.matches("[a-zA-Z0-9]")) {
                return;  // Ignore special characters and space before game starts
            }
        } else {
            // After game starts, process letters, numbers, space and backspace
            if (!key.matches("[a-zA-Z0-9 ]")) {
                return;  // Ignore special characters
            }
        }

        // Get the expected character
        char expectedChar = currentSentence.charAt(currentCharIndex);
        String expectedKey = expectedChar == ' ' ? " " : String.valueOf(expectedChar);

        // Record the keystroke with current timestamp
        long currentTime = System.currentTimeMillis();
        if (!gameStarted) {
            gameStartTime = currentTime;
            startGame();
        }

        keyLogsStructure.addKeyLog(key, currentTime - gameStartTime);


//         Handle correct input
//        if (key.equalsIgnoreCase(expectedKey)) {
//            if (!gameStarted) {
//                startGame();
//                hasFirstError = false;
//            }
//
//            currentCharIndex++;
//
//            if (hasUnresolvedError) {
//                provideErrorFeedback(key);
//            } else {
//                provideNextCharacterHint();
//            }
//        }
//        // Handle incorrect input
//        else {
//            if (!gameStarted) {
//                hasFirstError = true;
//            } else {
//                charErrorStates[currentCharIndex] = true;
//                hasUnresolvedError = true;
//                currentCharIndex++;
//            }
//            provideErrorFeedback(key);
//        }


        if (key.equalsIgnoreCase(expectedKey)) {
            // correct inout
            hasUnresolvedError = false;
            charErrorStates[currentCharIndex] = false;
            currentCharIndex++;
            provideNextCharacterHint();
        } else {
            // incorrect input
            charErrorStates[currentCharIndex] = true;
            hasUnresolvedError = true;
            currentCharIndex++;
            provideErrorFeedback(key);
        }

        // Add new words when running low on text
        if (currentSentence.length() - currentCharIndex < 30) {
            String newWord = addNewWord();
            keyLogsStructure.setWordsGiven(keyLogsStructure.getWordsGiven() + " " + newWord);
        }

        updateTaskDisplay();
        updateStatistics();

        System.out.println(
                "currentIndex=" + currentCharIndex +
                        ", expected=" + currentSentence.charAt(currentCharIndex < currentSentence.length()
                        ? currentCharIndex : currentSentence.length()-1) +
                        ", hasUnresolvedError=" + hasUnresolvedError
        );

    }

    /**
     * Provides error feedback through haptic vibration and LED lights.
     * Used when an incorrect key is pressed or when typing with unresolved errors.
     *
     * @param key The key that triggered the error feedback
     */
    private void provideErrorFeedback(String key) {
        keyboardInterface.sendHapticCommand(key, 500, 100);
        keyboardInterface.activateLights(1000);
    }

    /**
     * Provides haptic hint for the next character to be typed.
     * This helps users locate the next key they need to press.
     * For space character, sends "SPACE" as the hint.
     */
    private void provideNextCharacterHint() {
        if (currentCharIndex < currentSentence.length()) {
            String nextChar = String.valueOf(currentSentence.charAt(currentCharIndex));
            if (nextChar.equals(" ")) {
                nextChar = "SPACE";
            }
            keyboardInterface.sendHapticCommand(nextChar.toUpperCase(), 200, 50);
        }
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

    /**
     * Updates the statistics display based on KeyLogsStructure data
     */
    private void updateStatistics() {
        if (!gameStarted) return;

        List<KeyLog> logs = keyLogsStructure.getKeyLogs();
        int total = 0;
        int correct = 0;

        for (KeyLog log : logs) {
            if (!log.getKey().equals("BACK_SPACE")) {
                total++;
                if (!log.isError()) {
                    correct++;
                }
            }
        }

        totalKeystrokes = total;
        correctKeystrokes = correct;
        wrongKeystrokes = total - correct;
    }
}
