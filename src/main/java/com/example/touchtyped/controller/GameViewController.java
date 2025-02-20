package com.example.touchtyped.controller;

import com.example.touchtyped.constants.StyleConstants;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.GameKeypressListener;
import com.example.touchtyped.model.KeyLog;
import com.example.touchtyped.model.KeyLogsStructure;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;

public class GameViewController {

    @FXML private VBox gameContainer;
    @FXML private TextFlow taskLabel;
    @FXML private TextField inputField;
    @FXML private Label timerLabel;
    @FXML private Text cursorLabel;

    @FXML private Button time15Button;
    @FXML private Button time30Button;
    @FXML private Button time60Button;
    @FXML private Button time120Button;
    @FXML private HBox timeBox;


    @FXML private VBox resultContainer;

    @FXML private Label wpmLabel;
    @FXML private Label accuracyLabel;

    @FXML private ToggleGroup modeToggleGroup;
    @FXML private RadioButton timedModeRadio;
    @FXML private RadioButton articleModeRadio;


    // ----------------- Core Game Variables -------------------
    private int timeLeft = 60;
    private Timeline timeline;
    private boolean gameStarted = false;
    private long gameStartTime;

    private int totalKeystrokes = 0;
    private int correctKeystrokes = 0;
    private int wrongKeystrokes = 0;

    private int selectedTimeOption = 60;

    private KeyboardInterface keyboardInterface;
    private GameKeypressListener keyPressListener;
    private KeyLogsStructure keyLogsStructure;

    // ================== Multiple-line multiple-word structure ==================
    // outside resources
    private List<String> sentencePool = new ArrayList<>();
    private List<String> articles = new ArrayList<>();

    // Maximum number of words per line
    private static final int WORDS_PER_LINE = 8;
    // lines[lineIndex] => The words in that line
    private List<List<String>> lines = new ArrayList<>();

    // typedWords[lineIndex][wordIndex] = The user's actual input
    private List<List<StringBuilder>> typedWords = new ArrayList<>();
    private List<List<List<Boolean>>> errorFlags = new ArrayList<>();

    // The current line and word indices
    private int currentLineIndex = 0;
    private int currentWordIndex = 0;

    // A mapping table for special keys to characters
    private static final Map<String, String> SPECIAL_KEY_TO_CHAR = Map.ofEntries(
            Map.entry("SEMICOLON", ";"),
            Map.entry("QUOTE", "'"),
            Map.entry("COMMA", ","),
            Map.entry("PERIOD", "."),
            Map.entry("SLASH", "/"),
            Map.entry("BACK_SLASH", "\\"),
            Map.entry("OPEN_BRACKET", "["),
            Map.entry("CLOSE_BRACKET", "]"),
            Map.entry("MINUS", "-"),
            Map.entry("EQUALS", "=")
    );

    @FXML
    public void initialize() {
        keyboardInterface = new KeyboardInterface();
        keyPressListener = new GameKeypressListener(this, keyboardInterface);

        loadSentencesFromFile();
        loadArticlesFromFile();

        time15Button.setFocusTraversable(false);
        time30Button.setFocusTraversable(false);
        time60Button.setFocusTraversable(false);
        time120Button.setFocusTraversable(false);

        // Default selection: 60 seconds
        updateTimeButtonStyle(60);

        // After the scene is ready, attach the keyboard listener
        gameContainer.sceneProperty().addListener((ob, oldScene, newScene) -> {
            if (newScene != null) {
                keyboardInterface.attachToScene(newScene);
            }
        });

        modeToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            resetGame();
        });

        resetGame();
    }

    /**
     * Resets the game to its initial state
     */
    @FXML
    public void resetGame() {
        if (timeline != null) {
            timeline.stop();
        }
        gameStarted = false;

        // Article Mode
        if (isArticleMode()) {
            timeBox.setVisible(false);
            timeBox.setManaged(false);
            timeLeft = 0;
            timerLabel.setText("Article Mode");
        } else {
            // Timed Mode
            timeBox.setVisible(true);
            timeBox.setManaged(true);
            timeLeft = selectedTimeOption;
            timerLabel.setText(String.valueOf(timeLeft));
        }

        inputField.clear();
        inputField.setDisable(false);
        resultContainer.setVisible(false);

        totalKeystrokes = 0;
        correctKeystrokes = 0;
        wrongKeystrokes = 0;

        currentLineIndex = 0;
        currentWordIndex = 0;

        wpmLabel.setText("WPM: 0.0");
        accuracyLabel.setText("Accuracy: 0.0%");

        generateNewTask();

        keyLogsStructure = new KeyLogsStructure("MultilineWithCharErrors");
        cursorLabel.setVisible(false);
    }

    /**
     * Loads external file sentences.txt for generating random sentences
     */
    private void loadSentencesFromFile() {
        try {
            var inputStream = getClass().getResourceAsStream("/com/example/touchtyped/sentences.txt");
            if (inputStream == null) {
                System.out.println("sentences.txt not found!");
                return;
            }
            try (var scanner = new Scanner(inputStream)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (!line.isEmpty()) {
                        sentencePool.add(line);
                    }
                }
            }
            System.out.println("Loaded " + sentencePool.size() + " sentences from file.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads external file article.txt for generating paragraph-based text
     */
    private void loadArticlesFromFile() {
        try {
            var inputStream = getClass().getResourceAsStream("/com/example/touchtyped/articles.txt");
            if (inputStream == null) {
                System.out.println("article.txt not found!");
                return;
            }
            try (var scanner = new Scanner(inputStream)) {
                StringBuilder paragraphBuilder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.trim().isEmpty()) {
                        if (paragraphBuilder.length() > 0) {
                            articles.add(paragraphBuilder.toString().trim());
                            paragraphBuilder.setLength(0);
                        }
                    } else {
                        paragraphBuilder.append(line).append(" ");
                    }
                }
                if (paragraphBuilder.length() > 0) {
                    articles.add(paragraphBuilder.toString().trim());
                }
            }
            System.out.println("Loaded " + articles.size() + " paragraphs from article.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Randomly generate multi-line text tasks
     */
    private void generateNewTask() {
        lines.clear();
        typedWords.clear();
        errorFlags.clear();

        String finalText;

        if (isArticleMode()) {
            // Article Mode
            if (articles.isEmpty()) {
                System.out.println("No articles loaded. Fallback to default sentences mode.");
                finalText = getRandomSentences();
            } else {
                Random random = new Random();
                String chosenParagraph = articles.get(random.nextInt(articles.size()));
                finalText = chosenParagraph.trim();
                System.out.println("Selected article paragraph: " + finalText);
            }
        } else {
            // Timed Mode
            if (sentencePool.isEmpty()) {
                System.out.println("Sentence pool is empty, fallback to default words array.");
                return;
            }
            finalText = getRandomSentences();
        }

        // split, build lines
        if (finalText == null || finalText.isEmpty()) {
            System.out.println("Final text is empty, skip.");
            return;
        }

        String[] splitted = finalText.split("\\s+");
        List<String> allWords = new ArrayList<>(List.of(splitted));

        for (int i = 0; i < allWords.size(); i += WORDS_PER_LINE) {
            int end = Math.min(i + WORDS_PER_LINE, allWords.size());
            List<String> lineWords = new ArrayList<>(allWords.subList(i, end));
            lines.add(lineWords);

            List<StringBuilder> typedLine = new ArrayList<>();
            List<List<Boolean>> errorLine = new ArrayList<>();

            for (String word : lineWords) {
                typedLine.add(new StringBuilder());
                errorLine.add(new ArrayList<>());
            }
            typedWords.add(typedLine);
            errorFlags.add(errorLine);
        }

        updateTaskDisplay();
    }

    private String getRandomSentences() {
        Random random = new Random();
        StringBuilder combined = new StringBuilder();
        int sentenceCount = 3;
        for (int i = 0; i < sentenceCount; i++) {
            String s = sentencePool.get(random.nextInt(sentencePool.size()));
            combined.append(s).append(" ");
        }
        return combined.toString().trim();
    }


    /**
     * Starts the game: begins the timer, records the start time
     */
    private void startGame() {
        if (!gameStarted) {
            gameStarted = true;
            gameStartTime = System.currentTimeMillis();

            // 只有在 Timed Mode 下才启动倒计时
            if (!isArticleMode()) {
                timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                    timeLeft--;
                    timerLabel.setText(String.valueOf(timeLeft));
                    if (timeLeft <= 0) {
                        endGame();
                    }
                }));
                timeline.setCycleCount(selectedTimeOption);
                timeline.play();
            }
        }
    }


    /**
     * Ends the game and navigates to the results screen
     */
    private void endGame() {
        if (timeline != null) {
            timeline.stop();
        }
        double finalWpm = (correctKeystrokes / 5.0) / (selectedTimeOption / 60.0);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-result-view.fxml"));
            Scene resultScene = new Scene(loader.load(), 1200, 700);

            GameResultViewController resultController = loader.getController();
            resultController.setGameData((int) finalWpm, correctKeystrokes, wrongKeystrokes, totalKeystrokes);

            Stage stage = (Stage) gameContainer.getScene().getWindow();
            stage.setScene(resultScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Renders 3 lines of text, highlighting incorrect characters in red, and displays the current cursor
     */
    private void updateTaskDisplay() {
        taskLabel.getChildren().clear();

        for (int offset = 0; offset < 3; offset++) {
            int lineIdx = currentLineIndex + offset;
            if (lineIdx < 0 || lineIdx >= lines.size()) break;

            List<String> lineWords = lines.get(lineIdx);
            List<StringBuilder> typedLine = typedWords.get(lineIdx);
            List<List<Boolean>> errorLine = errorFlags.get(lineIdx);

            for (int w = 0; w < lineWords.size(); w++) {
                if (w >= typedLine.size() || w >= errorLine.size()) break;

                String targetWord = lineWords.get(w);
                StringBuilder userInput = typedLine.get(w);
                List<Boolean> errorList = errorLine.get(w);

                int typedLen = userInput.length();

                // Render the user-typed characters
                for (int i = 0; i < typedLen; i++) {
                    Text charNode;
                    char thisChar = userInput.charAt(i);

                    // If the i-th entry in errorList is true => highlight red
                    boolean isError = (i < errorList.size()) && errorList.get(i);
                    if (isError) {
                        charNode = new Text(String.valueOf(thisChar));
                        charNode.getStyleClass().add("error-text");
                    } else {
                        charNode = new Text(String.valueOf(thisChar));
                        charNode.getStyleClass().add("typed-text");
                    }

                    taskLabel.getChildren().add(charNode);
                }

                // Insert the cursor into the currently typed word
                boolean isCurrentWord = (lineIdx == currentLineIndex && w == currentWordIndex);
                if (isCurrentWord) {
                    Text cursorNode = new Text("|");
                    cursorNode.getStyleClass().add("cursor");
                    taskLabel.getChildren().add(cursorNode);
                }

                // Show the untyped portion
                if (typedLen < targetWord.length()) {
                    String remainPart = targetWord.substring(typedLen);
                    Text remainText = new Text(remainPart);
                    remainText.getStyleClass().add("remaining-text");
                    taskLabel.getChildren().add(remainText);
                }
                // Space between words
                taskLabel.getChildren().add(new Text(" "));
            }
            taskLabel.getChildren().add(new Text("\n"));
        }
    }

    /**
     * Handles the logic for key input
     */
    public void handleKeyPress(String key) {
        // If the key is a special key like "SEMICOLON," replace it with the actual character
        if (SPECIAL_KEY_TO_CHAR.containsKey(key)) {
            key = SPECIAL_KEY_TO_CHAR.get(key);
        }

        if (inputField.isDisabled()) return;

        // If the game hasn't started, only allow input [a-zA-Z0-9] to start
        if (!gameStarted) {
            if (!key.matches("[a-zA-Z0-9]")) return;
            startGame();
        }

        // If it's neither backspace nor matches [a-zA-Z0-9,\\.;:'\"?! ], ignore
        if (!key.equals("BACK_SPACE") && !key.matches("[a-zA-Z0-9,\\.;:'\"?! ]")) {
            System.out.println("Ignored input: " + key);
            return;
        }

        if (currentLineIndex < 0 || currentLineIndex >= lines.size()) {
            return;
        }
        List<String> curLine = lines.get(currentLineIndex);
        if (currentWordIndex < 0 || currentWordIndex >= curLine.size()) {
            moveToNextLine();
            updateAllUI();
            return;
        }

        long now = System.currentTimeMillis();
        if (keyLogsStructure != null) {
            keyLogsStructure.addKeyLog(key, now - gameStartTime);
        }

        if (key.equals("BACK_SPACE")) {
            handleBackspace();
            updateAllUI();
            return;
        }

        // Target word
        String targetWord = curLine.get(currentWordIndex);
        StringBuilder userInput = typedWords.get(currentLineIndex).get(currentWordIndex);
        List<Boolean> charErrFlags = errorFlags.get(currentLineIndex).get(currentWordIndex);

        // If the word is fully typed
        if (userInput.length() >= targetWord.length()) {
            if (key.equals(" ")) {
                moveToNextWord();
            } else {
                // If it exceeds the length => count as an error
                wrongKeystrokes++;
                provideErrorFeedback(key);
            }
            updateAllUI();
            return;
        }

        // If space is typed but the word is not yet fully typed => error
        if (key.equals(" ")) {
            targetWord = curLine.get(currentWordIndex);
            userInput = typedWords.get(currentLineIndex).get(currentWordIndex);
            charErrFlags = errorFlags.get(currentLineIndex).get(currentWordIndex);

            if (userInput.length() == 0) {
                return;
            }

            int typedLen = userInput.length();
            int wordLen = targetWord.length();
            if (typedLen < wordLen) {
                for (int i = typedLen; i < wordLen; i++) {
                    char leftoverChar = targetWord.charAt(i);
                    userInput.append(leftoverChar);
                    charErrFlags.add(true);
                    wrongKeystrokes++;
                }
            }


            moveToNextWord();
            updateAllUI();
            return;
        }

        // Compare characters
        int idx = userInput.length();
        char typedChar = key.charAt(0);
        char expectedChar = targetWord.charAt(idx);
        userInput.append(expectedChar);

        if (typedChar == expectedChar) {
            // dynamically add false
            charErrFlags.add(false);
            correctKeystrokes++;
        } else {
            // dynamically add true
            charErrFlags.add(true);
            wrongKeystrokes++;
            provideErrorFeedback(key);
        }

        updateAllUI();
    }

    /**aaaa
     * Moves to the next word
     */
    private void moveToNextWord() {
        currentWordIndex++;
        if (currentWordIndex >= lines.get(currentLineIndex).size()) {
            currentWordIndex = 0;
            currentLineIndex++;
        }
        if (currentLineIndex >= lines.size()) {
            endGame();
        }
    }

    /**
     * Moves to the next line
     */
    private void moveToNextLine() {
        currentLineIndex++;
        currentWordIndex = 0;
        if (currentLineIndex >= lines.size()) {
            endGame();
        }
    }

    /**
     * Handles backspace logic
     */
    private void handleBackspace() {
        if (currentLineIndex >= lines.size()) return;
        List<String> curLine = lines.get(currentLineIndex);
        if (currentWordIndex >= curLine.size()) {
            return;
        }

        StringBuilder userInput = typedWords.get(currentLineIndex).get(currentWordIndex);
        List<Boolean> charErrFlags = errorFlags.get(currentLineIndex).get(currentWordIndex);

        if (userInput.length() > 0) {
            userInput.deleteCharAt(userInput.length() - 1);
            // Plan A: correspondingly remove the last error flag
            charErrFlags.remove(charErrFlags.size() - 1);
        } else {
            // If already at the start of the current word, move back to the previous word
            if (currentWordIndex > 0) {
                currentWordIndex--;
            } else if (currentLineIndex > 0) {
                currentLineIndex--;
                currentWordIndex = lines.get(currentLineIndex).size() - 1;
            }
        }
    }

    // ----------------- Statistics & UI -----------------
    private void updateAllUI() {
        updateTaskDisplay();
        updateStatistics();
        updateRealtimeStats();
    }

    /**
     * Update statistical information (total keystrokes, errors, etc.)
     */
    private void updateStatistics() {
        if (!gameStarted) return;
        totalKeystrokes = correctKeystrokes + wrongKeystrokes;
    }

    /**
     * Calculate WPM and accuracy in real time
     */
    private void updateRealtimeStats() {
        if (!gameStarted || gameStartTime == 0) return;

        long now = System.currentTimeMillis();
        double elapsedSec = (now - gameStartTime) / 1000.0;
        if (elapsedSec <= 0) return;

        double elapsedMin = elapsedSec / 60.0;
        double wpm = (correctKeystrokes / 5.0) / elapsedMin;
        wpmLabel.setText(String.format("WPM: %.1f", wpm));

        int total = correctKeystrokes + wrongKeystrokes;
        double acc = (total > 0)
                ? (correctKeystrokes * 100.0 / total)
                : 0.0;
        accuracyLabel.setText(String.format("Accuracy: %.1f%%", acc));
    }

    /**
     * Suggests the next character (optional)
     */
    private void provideNextCharacterHint() {
        // Optional feature
    }

    /**
     * Provides feedback for incorrect input (vibration, lighting, etc.)
     */
    private void provideErrorFeedback(String key) {
        keyboardInterface.sendHapticCommand(key, 500, 100);
        keyboardInterface.activateLights(1000);
    }

    // ----------------- Time buttons & navigation -----------------
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
     * Update the selection state of the time buttons
     */
    private void updateTimeButtonStyle(int selectedTime) {
        time15Button.getStyleClass().remove("selected");
        time30Button.getStyleClass().remove("selected");
        time60Button.getStyleClass().remove("selected");
        time120Button.getStyleClass().remove("selected");

        switch (selectedTime) {
            case 15 -> time15Button.getStyleClass().add("selected");
            case 30 -> time30Button.getStyleClass().add("selected");
            case 60 -> time60Button.getStyleClass().add("selected");
            case 120 -> time120Button.getStyleClass().add("selected");
        }
        this.selectedTimeOption = selectedTime;
        timeLeft = selectedTime;
        timerLabel.setText(String.valueOf(timeLeft));
    }

    @FXML public void select15s() {
        updateTimeButtonStyle(15);
        resetGame();
    }
    @FXML public void select30s() {
        updateTimeButtonStyle(30);
        resetGame();
    }
    @FXML public void select60s() {
        updateTimeButtonStyle(60);
        resetGame();
    }
    @FXML public void select120s() {
        updateTimeButtonStyle(120);
        resetGame();
    }

    public boolean isInputDisabled() {
        return inputField.isDisabled();
    }

    private boolean isArticleMode() {
        return modeToggleGroup.getSelectedToggle() == articleModeRadio;
    }

}
