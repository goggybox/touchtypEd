package com.example.touchtyped.controller;

import com.example.touchtyped.constants.StyleConstants;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.GameKeypressListener;
import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.KeyLog;
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

/**
 * 修改版：不管首字符对错都开始游戏；在每次正确/错误时累加 correctKeystrokes / wrongKeystrokes
 */
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

    // 计时
    private Timeline timeline;
    private boolean gameStarted = false;
    private long gameStartTime;
    private int selectedTimeOption = 60;
    private int timeLeft = 60;

    // 统计
    private int totalKeystrokes = 0;
    private int correctKeystrokes = 0;
    private int wrongKeystrokes = 0;

    // 其它
    private KeyboardInterface keyboardInterface;
    private GameKeypressListener keyPressListener;
    private KeyLogsStructure keyLogsStructure;

    private List<String> sentencePool = new ArrayList<>();
    private List<String> articles = new ArrayList<>();

    // 单行逻辑
    private StringBuilder currentSentence;
    private int currentCharIndex;
    private final boolean[] charErrorStates = new boolean[5000];
    private boolean hasFirstError = false;
    private boolean hasUnresolvedError = false;

    // 特殊字符映射
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

        updateTimeButtonStyle(60);

        // 当 scene 就绪后，注册键盘监听
        gameContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                keyboardInterface.attachToScene(newScene);
            }
        });

        // 监听模式切换
        modeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            resetGame();
        });

        resetGame();
    }

    private void loadSentencesFromFile() {
        try {
            var inputStream = getClass().getResourceAsStream("/com/example/touchtyped/sentences.txt");
            if (inputStream == null) {
                System.out.println("sentences.txt not found!");
                return;
            }
            try (Scanner scanner = new Scanner(inputStream)) {
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

    private void loadArticlesFromFile() {
        try {
            var inputStream = getClass().getResourceAsStream("/com/example/touchtyped/articles.txt");
            if (inputStream == null) {
                System.out.println("article.txt not found!");
                return;
            }
            try (Scanner scanner = new Scanner(inputStream)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (!line.isEmpty()) {
                        articles.add(line);
                    }
                }
            }
            System.out.println("Loaded " + articles.size() + " paragraphs from article.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    @FXML
    public void resetGame() {
        if (timeline != null) {
            timeline.stop();
        }
        gameStarted = false;

        if (isArticleMode()) {
            timeBox.setVisible(false);
            timeBox.setManaged(false);
            timeLeft = 0;
            timerLabel.setText("Article Mode");
        } else {
            timeBox.setVisible(true);
            timeBox.setManaged(true);
            timeLeft = selectedTimeOption;
            timerLabel.setText(String.valueOf(timeLeft));
        }

        totalKeystrokes = 0;
        correctKeystrokes = 0;
        wrongKeystrokes = 0;
        wpmLabel.setText("WPM: 0.0");
        accuracyLabel.setText("Accuracy: 0.0%");

        inputField.clear();
        inputField.setDisable(false);
        resultContainer.setVisible(false);

        currentSentence = null;
        currentCharIndex = 0;
        Arrays.fill(charErrorStates, false);
        hasFirstError = false;
        hasUnresolvedError = false;

        keyLogsStructure = null;
        cursorLabel.setVisible(false);

        generateNewTask();
        updateTaskDisplay();
    }

    private void generateNewTask() {
        if (isArticleMode()) {
            if (articles.isEmpty()) {
                currentSentence = new StringBuilder("No article found.");
            } else {
                Random r = new Random();
                int idx = r.nextInt(articles.size());
                String paragraph = articles.get(idx).replaceAll("\\r?\\n", " ");
                currentSentence = new StringBuilder(paragraph);
            }
        } else {
            if (sentencePool.isEmpty()) {
                currentSentence = new StringBuilder("Hello world!");
            } else {
                StringBuilder combined = new StringBuilder();
                Random r = new Random();
                for (int i = 0; i < 3; i++) {
                    String s = sentencePool.get(r.nextInt(sentencePool.size()));
                    combined.append(s).append(" ");
                }
                currentSentence = new StringBuilder(combined.toString().trim());
            }
        }
        keyLogsStructure = new KeyLogsStructure(currentSentence.toString());
        currentCharIndex = 0;
        System.out.println("Current text: " + currentSentence);
    }

    private boolean isArticleMode() {
        return modeToggleGroup.getSelectedToggle() == articleModeRadio;
    }

    private void startGame() {
        if (!gameStarted) {
            gameStarted = true;
            gameStartTime = System.currentTimeMillis();
            cursorLabel.setVisible(true);

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
     * 核心：不论第一键对错，都先 startGame()，再判断对错并累加统计
     */
    public void handleKeyPress(String key) {
        // 如果有特殊映射
        if (SPECIAL_KEY_TO_CHAR.containsKey(key)) {
            key = SPECIAL_KEY_TO_CHAR.get(key);
        }
        if (inputField.isDisabled()) return;

        // ================ 不管对错，都先 startGame() ================
        if (!gameStarted) {
            startGame();
            gameStartTime = System.currentTimeMillis();
            keyLogsStructure = new KeyLogsStructure(currentSentence.toString());
        }

        // 若要限制键盘输入，可写：
        if (!key.equals("BACK_SPACE")
                && !key.matches("[a-zA-Z0-9,\\.;:'\"?! ]")) {
            return;
        }

        long now = System.currentTimeMillis();
        keyLogsStructure.addKeyLog(key, now - gameStartTime);

        // ============ 处理回退 ============
        if (key.equals("BACK_SPACE")) {
            if (currentCharIndex > 0) {
                currentCharIndex--;
                charErrorStates[currentCharIndex] = false;
                hasUnresolvedError = false;
                for (int i = 0; i < currentCharIndex; i++) {
                    if (charErrorStates[i]) {
                        hasUnresolvedError = true;
                        break;
                    }
                }
                hasFirstError = false;

                updateAllUI();
                provideNextCharacterHint();
            }
            return;
        }

        // ============ 普通字符：比对 expectedChar ============
        if (currentCharIndex >= currentSentence.length()) {
            // 如果已经超出文本长度，就判错
            wrongKeystrokes++;
            provideErrorFeedback(key);
            // 也可以选择 endGame() 或 ignore
            updateAllUI();
            updateStatistics();
            return;
        }

        char expectedChar = currentSentence.charAt(currentCharIndex);
        String expectedKey = String.valueOf(expectedChar);

        if (key.equals(expectedKey)) {
            // 正确
            correctKeystrokes++;
            currentCharIndex++;
            if (hasUnresolvedError) {
                provideErrorFeedback(key);
            } else {
                provideNextCharacterHint();
            }
        } else {
            // 错误
            charErrorStates[currentCharIndex] = true;
            hasUnresolvedError = true;
            wrongKeystrokes++;
            currentCharIndex++;
            provideErrorFeedback(key);
        }

        // 如果是 Timed Mode，文本剩余不足30再补单词
        if (!isArticleMode() && (currentSentence.length() - currentCharIndex < 30)) {
            addNewWord();
        }

        updateAllUI();
        updateStatistics();
    }

    private String addNewWord() {
        if (currentSentence.length() == 0) {
            String w = getRandomWord();
            currentSentence.append(w);
            return w;
        } else {
            currentSentence.append(" ");
            String w = getRandomWord();
            currentSentence.append(w);
            if (keyLogsStructure != null) {
                keyLogsStructure.setWordsGiven(keyLogsStructure.getWordsGiven() + " " + w);
            }
            return w;
        }
    }

    private String getRandomWord() {
        if (sentencePool.isEmpty()) return "word";
        Random r = new Random();
        return sentencePool.get(r.nextInt(sentencePool.size()));
    }

    private void updateAllUI() {
        updateTaskDisplay();
        updateStatistics();
        updateRealtimeStats();
    }

    private void updateTaskDisplay() {
        taskLabel.getChildren().clear();
        if (currentSentence == null) return;

        int visibleLen = 50;
        int start = Math.max(0, currentCharIndex - visibleLen / 2);
        int end = Math.min(currentSentence.length(), start + visibleLen);

        // 已打
        for (int i = start; i < currentCharIndex; i++) {
            Text t = new Text(String.valueOf(currentSentence.charAt(i)));
            t.getStyleClass().add(charErrorStates[i] ? "error-text" : "typed-text");
            taskLabel.getChildren().add(t);
        }
        // 当前字符
        if (currentCharIndex < currentSentence.length()) {
            Text curr = new Text(String.valueOf(currentSentence.charAt(currentCharIndex)));
            if (gameStarted && charErrorStates[currentCharIndex]) {
                curr.getStyleClass().add("error-text");
            } else {
                curr.getStyleClass().add("remaining-text");
            }
            taskLabel.getChildren().add(curr);
        }
        // 剩余
        if (currentCharIndex + 1 < end) {
            Text remain = new Text(currentSentence.substring(currentCharIndex + 1, end));
            remain.getStyleClass().add("remaining-text");
            taskLabel.getChildren().add(remain);
        }

        double baseX = -(visibleLen * StyleConstants.charWidth / 2.0);
        double offset = (currentCharIndex - start) * StyleConstants.charWidth;
        cursorLabel.setTranslateX(baseX + offset);
    }

    // =========== 统计 & 提示相关 ===========

    /**
     * 每次按键都更新 totalKeystrokes
     */
    private void updateStatistics() {
        if (!gameStarted) return;
        totalKeystrokes = correctKeystrokes + wrongKeystrokes;
    }

    /**
     * 实时更新 WPM / Accuracy
     */
    private void updateRealtimeStats() {
        if (!gameStarted || gameStartTime == 0) return;

        long now = System.currentTimeMillis();
        double elapsedSec = (now - gameStartTime) / 1000.0;
        if (elapsedSec <= 0) return;

        double elapsedMin = elapsedSec / 60.0;
        // correctKeystrokes / 5 => typed words
        double wpm = (correctKeystrokes / 5.0) / elapsedMin;
        wpmLabel.setText(String.format("WPM: %.1f", wpm));

        int total = correctKeystrokes + wrongKeystrokes;
        double acc = (total > 0)
                ? (correctKeystrokes * 100.0 / total)
                : 0.0;
        accuracyLabel.setText(String.format("Accuracy: %.1f%%", acc));
    }

    private void provideErrorFeedback(String key) {
        keyboardInterface.sendHapticCommand(key, 500, 100);
        keyboardInterface.activateLights(1000);
    }

    private void provideNextCharacterHint() {
        if (currentCharIndex < currentSentence.length()) {
            String ch = String.valueOf(currentSentence.charAt(currentCharIndex));
            if (ch.equals(" ")) {
                ch = "SPACE";
            }
            keyboardInterface.sendHapticCommand(ch.toUpperCase(), 200, 50);
        }
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

    public boolean isInputDisabled() {
        return inputField.isDisabled();
    }
}
