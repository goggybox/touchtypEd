package com.example.touchtyped.controller;

import com.example.touchtyped.constants.StyleConstants;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.GameKeypressListener;
import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.UserProfile;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.IOException;
import java.net.URI;
import java.util.*;
//import org.json.JSONObject;

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
    @FXML private RadioButton competitionModeRadio;

    @FXML private Button timedModeInfoButton;
    @FXML private Button returnButton;

    // ========== Competition UI ==========
    @FXML private VBox competitionContainer;
    @FXML private Label compTimerLabel;
    @FXML private Label leftScoreLabel;
    @FXML private Label rightScoreLabel;

    @FXML private TextFlow leftTextFlow;
    @FXML private TextFlow rightTextFlow;
    @FXML private Text leftCursor;
    @FXML private Text rightCursor;

    @FXML private Label comboLabel;
    
    // ========== 新手引导相关变量 ==========
    /**
     * Current tutorial step
     */
    private int tutorialStep = 0;
    
    /**
     * Tutorial dialog
     */
    private Dialog<ButtonType> tutorialDialog;
    
    /**
     * Guide text label
     */
    private Label tutorialLabel;
    
    /**
     * Next and finish buttons
     */
    private Button nextButton, finishButton;

    private Timeline timeline;
    private boolean gameStarted = false;
    private long gameStartTime;
    private int selectedTimeOption = 60; // Timed model time option 15/30/60/120
    private int timeLeft = 60;
    private int competitionTime = 10;    // compttition model time

    private int totalKeystrokes = 0;
    private int correctKeystrokes = 0;
    private int wrongKeystrokes = 0;

    private KeyboardInterface keyboardInterface;
    private GameKeypressListener keyPressListener;
    private KeyLogsStructure keyLogsStructure;

    private List<String> sentencePool = new ArrayList<>();
    private List<String> articles = new ArrayList<>();

    // ====== Timed/Article ======
    private StringBuilder currentSentence;
    private int currentCharIndex;
    private final boolean[] charErrorStates = new boolean[5000];
    private boolean hasFirstError = false;
    private boolean hasUnresolvedError = false;
    private boolean currentWordHasMistake = false;

    // ====== Competition ======
    private boolean isSecondRound = false;
    private boolean playerAOnLeft = true;

    private int scoreLeft;
    private int scoreRight;

    private static final int COMP_LETTER_COUNT = 500;
    private static final int COMP_VISIBLE_LEN   = 12;

    private StringBuilder leftLetters;
    private StringBuilder rightLetters;
    private int leftIndex;
    private int rightIndex;
    private boolean[] leftErrorFlags;
    private boolean[] rightErrorFlags;

    private boolean waitingForSpaceToStartRound = false;
    private boolean betweenRounds = false;

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
            Map.entry("EQUALS", "="),
            Map.entry("BACK_QUOTE", "`"),
            Map.entry("EXCLAMATION", "!"),
            Map.entry("AT", "@"),
            Map.entry("NUMBER_SIGN", "#"),
            Map.entry("DOLLAR", "$"),
            Map.entry("PERCENT", "%"),
            Map.entry("CIRCUMFLEX", "^"),
            Map.entry("AMPERSAND", "&"),
            Map.entry("ASTERISK", "*"),
            Map.entry("LEFT_PARENTHESIS", "("),
            Map.entry("RIGHT_PARENTHESIS", ")"),
            Map.entry("UNDERSCORE", "_"),
            Map.entry("PLUS", "+"),
            Map.entry("BRACELEFT", "{"),
            Map.entry("BRACERIGHT", "}"),
            Map.entry("COLON", ":"),
            Map.entry("QUOTEDBL", "\""),
            Map.entry("LESS", "<"),
            Map.entry("GREATER", ">"),
            Map.entry("QUESTION", "?"),
            Map.entry("PIPE", "|")
    );

    private static final char[] LEFT_HAND_CHARS = {
            'q','w','e','r','t','a','s','d','f','g','z','x','c','v','b'
    };
    private static final char[] RIGHT_HAND_CHARS = {
            'y','u','i','o','p','h','j','k','l','n','m'
    };
    
    // ========== Combo/Streak related variables ==========
    /**
     * currentStreak, maxStreak are now counted by "word" units
     */
    private int currentStreak = 0;   // Current word streak
    private int maxStreak = 0;       // Historical maximum streak

    /**
     * Records the starting index of the current word to determine if the entire word is correct
     */
    private int wordStartIndex = 0;
    
    @FXML
    public void initialize(){
        
        keyboardInterface = new KeyboardInterface();
        keyPressListener  = new GameKeypressListener(this, keyboardInterface);

        loadSentencesFromFile();
        loadArticlesFromFile();

        time15Button.setFocusTraversable(false);
        time30Button.setFocusTraversable(false);
        time60Button.setFocusTraversable(false);
        time120Button.setFocusTraversable(false);

        updateTimeButtonStyle(60);

        gameContainer.sceneProperty().addListener((obs, oldScene, newScene)->{
            if(newScene!=null){
                keyboardInterface.attachToScene(newScene);

                // 场景加载完成后检查是否是第一次使用
                Platform.runLater(() -> {
                    if (checkFirstTimeUser()) {
                        showSimpleTutorial();
                    }
                });
            }
        });

        modeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal)->{
            resetGame();
            updateInfoButtonVisibility();
        });

        updateInfoButtonVisibility();

        leftCursor.setTextOrigin(VPos.BASELINE);
        rightCursor.setTextOrigin(VPos.BASELINE);
        cursorLabel.setTextOrigin(VPos.BASELINE);

        resetGame();
    }

    private void loadSentencesFromFile(){
        try{
            var inputStream = getClass().getResourceAsStream("/com/example/touchtyped/sentences.txt");
            if(inputStream==null){
                System.out.println("sentences.txt not found!");
                return;
            }
            try(Scanner sc = new Scanner(inputStream)){
                while(sc.hasNextLine()){
                    String line = sc.nextLine().trim();
                    if(!line.isEmpty()){
                        sentencePool.add(line);
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadArticlesFromFile(){
        try{
            var inputStream = getClass().getResourceAsStream("/com/example/touchtyped/articles.txt");
            if(inputStream==null){
                System.out.println("articles.txt not found!");
                return;
            }
            try(Scanner sc=new Scanner(inputStream)){
                while(sc.hasNextLine()){
                    String line = sc.nextLine().trim();
                    if(!line.isEmpty()){
                        articles.add(line);
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // ========== Competition: Generate random letters for left and right hand practice ==========
    private void generateRandomLettersForCompetition(int lettersCount){
        leftLetters = new StringBuilder();
        rightLetters= new StringBuilder();
        leftErrorFlags = new boolean[lettersCount];
        rightErrorFlags= new boolean[lettersCount];
        Arrays.fill(leftErrorFlags,false);
        Arrays.fill(rightErrorFlags,false);

        Random r=new Random();
        for(int i=0;i<lettersCount;i++){
            leftLetters.append( LEFT_HAND_CHARS[r.nextInt(LEFT_HAND_CHARS.length)] );
            rightLetters.append(RIGHT_HAND_CHARS[r.nextInt(RIGHT_HAND_CHARS.length)]);
        }
        leftIndex=0;
        rightIndex=0;
    }

    private void updateLeftDisplay(){
        leftTextFlow.getChildren().clear();

        int len  = leftLetters.length();
        int start= Math.max(0, leftIndex - COMP_VISIBLE_LEN/2);
        int end  = Math.min(len, start+COMP_VISIBLE_LEN);

        for(int i=start; i<leftIndex && i<end; i++){
            Text t=new Text(String.valueOf(leftLetters.charAt(i)));
            t.setTextOrigin(VPos.BASELINE);
            if(leftErrorFlags[i]){
                t.getStyleClass().add("error-text");
            } else {
                t.getStyleClass().add("typed-text");
            }
            leftTextFlow.getChildren().add(t);
        }

        if(leftIndex<end){
            leftCursor.setText("|");
            leftCursor.setTextOrigin(VPos.BASELINE);
            leftCursor.getStyleClass().add("cursor");
            leftCursor.setVisible(true);
            leftTextFlow.getChildren().add(leftCursor);
        } else {
            leftCursor.setVisible(false);
        }

        for(int i=Math.max(leftIndex, start); i<end; i++){
            Text t=new Text(String.valueOf(leftLetters.charAt(i)));
            t.setTextOrigin(VPos.BASELINE);
            t.getStyleClass().add("remaining-text");
            leftTextFlow.getChildren().add(t);
        }
    }

    private void updateRightDisplay(){
        rightTextFlow.getChildren().clear();

        int len  = rightLetters.length();
        int start= Math.max(0, rightIndex - COMP_VISIBLE_LEN/2);
        int end  = Math.min(len, start+COMP_VISIBLE_LEN);

        for(int i=start; i<rightIndex && i<end; i++){
            Text t=new Text(String.valueOf(rightLetters.charAt(i)));
            t.setTextOrigin(VPos.BASELINE);
            if(rightErrorFlags[i]){
                t.getStyleClass().add("error-text");
            } else {
                t.getStyleClass().add("typed-text");
            }
            rightTextFlow.getChildren().add(t);
        }

        if(rightIndex<end){
            rightCursor.setText("|");
            rightCursor.setTextOrigin(VPos.BASELINE);
            rightCursor.getStyleClass().add("cursor");
            rightCursor.setVisible(true);
            rightTextFlow.getChildren().add(rightCursor);
        } else {
            rightCursor.setVisible(false);
        }

        for(int i=Math.max(rightIndex, start); i<end; i++){
            Text t=new Text(String.valueOf(rightLetters.charAt(i)));
            t.setTextOrigin(VPos.BASELINE);
            t.getStyleClass().add("remaining-text");
            rightTextFlow.getChildren().add(t);
        }
    }

    @FXML
    public void resetGame(){
        keyboardInterface.stopHaptic();
        if(timeline!=null){
            timeline.stop();
        }
        gameStarted=false;

        timeBox.setVisible(false);
        timeBox.setManaged(false);
        wpmLabel.setVisible(false);
        accuracyLabel.setVisible(false);
        taskLabel.setVisible(false);
        cursorLabel.setVisible(false);
        competitionContainer.setVisible(false);
        competitionContainer.setManaged(false);

        timerLabel.setVisible(true);
        timerLabel.setManaged(true);
        timeLeft= selectedTimeOption;
        timerLabel.setText(String.valueOf(timeLeft));

        totalKeystrokes=0;
        correctKeystrokes=0;
        wrongKeystrokes=0;
        wpmLabel.setText("WPM: 0.0");
        accuracyLabel.setText("Accuracy: 0.0%");
        inputField.clear();
        inputField.setDisable(false);
        resultContainer.setVisible(false);

        currentSentence=null;
        currentCharIndex=0;
        Arrays.fill(charErrorStates,false);
        hasFirstError=false;
        hasUnresolvedError=false;
        keyLogsStructure=null;

        // Reset word streak
        currentStreak = 0;
        maxStreak = 0;
        wordStartIndex = 0; // Ensure each new task counts words from index 0

        if(isCompetitionMode()){
            isSecondRound = false;
            playerAOnLeft = true;
            scoreLeft=0;
            scoreRight=0;
            waitingForSpaceToStartRound = true;
            betweenRounds = false;
            leftTextFlow.getChildren().clear();
            rightTextFlow.getChildren().clear();
        }

        if(isArticleMode()){
            timeLeft=0;
            timerLabel.setText("Article Mode");
            wpmLabel.setVisible(true);
            accuracyLabel.setVisible(true);
            taskLabel.setVisible(true);
        }

        if(isCompetitionMode()){
            timeLeft= competitionTime;
            compTimerLabel.setText("Time Left: "+timeLeft);
            timerLabel.setVisible(false);
            timerLabel.setManaged(false);
            generateRandomLettersForCompetition(COMP_LETTER_COUNT);
            leftTextFlow.getChildren().clear();
            rightTextFlow.getChildren().clear();
            updateLeftDisplay();
            updateRightDisplay();

            leftCursor.setVisible(false);
            rightCursor.setVisible(false);
            leftScoreLabel.setText("PlayerA Score: 0");
            rightScoreLabel.setText("PlayerB Score: 0");
            competitionContainer.setVisible(true);
            competitionContainer.setManaged(true);
        }

        if (isTimeMode()){
            timeLeft= selectedTimeOption;
            timerLabel.setText(String.valueOf(timeLeft));
            timerLabel.setVisible(true);
            timerLabel.setManaged(true);
            timeBox.setVisible(true);
            timeBox.setManaged(true);
            wpmLabel.setVisible(true);
            accuracyLabel.setVisible(true);
            taskLabel.setVisible(true);
        }

        generateNewTask();
        updateTaskDisplay();
    }

    private void generateNewTask(){
        if(isArticleMode()){
            Random r=new Random();
            int idx=r.nextInt(articles.size());
            String paragraph=articles.get(idx).replaceAll("\\r?\\n"," ");
            currentSentence=new StringBuilder(paragraph);

        } else if(isTimeMode()){
            Random r=new Random();
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<3;i++){
                String s= sentencePool.get(r.nextInt(sentencePool.size()));
                sb.append(s).append(" ");
            }
            currentSentence=new StringBuilder(sb.toString().trim());
        }
        if(currentSentence==null){
            currentSentence=new StringBuilder("");
        }
        keyLogsStructure= new KeyLogsStructure(currentSentence.toString());
        currentCharIndex=0;
        wordStartIndex=0; // For newly generated sentences, start from 0
    }

    private boolean isArticleMode(){
        return modeToggleGroup.getSelectedToggle() == articleModeRadio;
    }
    private boolean isCompetitionMode(){
        return modeToggleGroup.getSelectedToggle() == competitionModeRadio;
    }
    private boolean isTimeMode(){
        return modeToggleGroup.getSelectedToggle() == timedModeRadio;
    }

    private void startGame(){
        keyboardInterface.stopHaptic();
        if(!gameStarted){
            gameStarted=true;
            gameStartTime=System.currentTimeMillis();
            if(!isCompetitionMode()){
                cursorLabel.setVisible(true);
            }
            if(isCompetitionMode() || !isArticleMode()){
                timeline=new Timeline(new KeyFrame(Duration.seconds(1), e->{
                    timeLeft--;
                    if(isCompetitionMode()){
                        compTimerLabel.setText("Time Left: "+timeLeft);
                    } else {
                        timerLabel.setText(String.valueOf(timeLeft));
                    }
                    if(timeLeft<=0){
                        endGame();
                    }
                }));
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.play();
            }
        }
    }

    private void endGame(){
        keyboardInterface.stopHaptic();
        if(timeline!=null){
            timeline.stop();
        }

        if(isCompetitionMode()){
            if(!isSecondRound){
                // First round ends
                isSecondRound = true;

                // Score exchange (your original logic):
                int oldLeft = scoreLeft;
                int oldRight= scoreRight;
                scoreLeft   = oldRight;
                scoreRight  = oldLeft;

                // Switch positions
                playerAOnLeft = !playerAOnLeft;
                leftIndex=0;
                rightIndex=0;
                leftTextFlow.getChildren().clear();
                rightTextFlow.getChildren().clear();
                generateRandomLettersForCompetition(COMP_LETTER_COUNT);
                updateLeftDisplay();
                updateRightDisplay();

                // Reset time and UI
                timeLeft = competitionTime;
                compTimerLabel.setText("Time Left: " + timeLeft);
                refreshCompetitionScoreUI();

                // Don't start the next round immediately, show a prompt first
                waitingForSpaceToStartRound = true;
                betweenRounds = true;
                gameStarted = false;

                // Use a dialog or other method to remind players
                showCompetitionRoundMessage("First round ends! Please switch positions and press space to start the next round...");

            } else {
                // Second round ends -> Display competition results
                int finalScoreA, finalScoreB;
                if(playerAOnLeft){
                    finalScoreA = scoreLeft;
                    finalScoreB = scoreRight;
                } else {
                    finalScoreA = scoreRight;
                    finalScoreB = scoreLeft;
                }
                Alert alert=new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Competition Result");
                alert.setHeaderText(null);
                StringBuilder sb=new StringBuilder();
                sb.append("Final Scores:\n");
                sb.append("Player A: ").append(finalScoreA).append("\n");
                sb.append("Player B: ").append(finalScoreB).append("\n");
                if(finalScoreA>finalScoreB) sb.append("Player A wins!");
                else if(finalScoreB>finalScoreA) sb.append("Player B wins!");
                else sb.append("It's a tie!");
                alert.setContentText(sb.toString());
                Platform.runLater(() -> {
                    alert.showAndWait();
                    // Record to Ranking and resetGame
                    String playerName = UserProfile.getInstance().getPlayerName();
                    if (playerName != null && !playerName.isEmpty()) {
                        // ...
                    }
                    resetGame();
                });
            }
        } else {
            // Timed or Article
            double finalWpm=(correctKeystrokes/5.0)/(selectedTimeOption/60.0);
            try{
                FXMLLoader loader=new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-result-view.fxml"));
                Scene resultScene=new Scene(loader.load(),1200,700);

                GameResultViewController resultController=loader.getController();
                
                // Get game mode
                String gameMode = isTimeMode() ? "Timed" : (isArticleMode() ? "Article" : "Competition");
                
                // Use UserProfile to get player name
                String playerName = UserProfile.getInstance().getPlayerName();
                
                resultController.setGameData(
                    (int)finalWpm, 
                    correctKeystrokes, 
                    wrongKeystrokes, 
                    totalKeystrokes,
                    gameMode,
                    playerName
                );
                resultController.setKeyLogsStructure(keyLogsStructure);

                Stage stage=(Stage)gameContainer.getScene().getWindow();
                stage.setScene(resultScene);
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private void showCompetitionRoundMessage(String msg) {
        // Use Platform.runLater to ensure execution in the UI thread
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Prepare for Next Round");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.show(); // Use show() instead of showAndWait()
        });
    }


    @FXML
    public void handleKeyPress(String key){
        // Add debugging info
        System.out.println("Received key: " + key);
        
        // Handle special keys with direct mapping
        if(SPECIAL_KEY_TO_CHAR.containsKey(key)){
            System.out.println("Converting special key: " + key + " to: " + SPECIAL_KEY_TO_CHAR.get(key));
            key = SPECIAL_KEY_TO_CHAR.get(key);
        }
        if(inputField.isDisabled()) return;
        
        // 1. If in competition mode
        if(isCompetitionMode()) {
            // Check if waiting for space to start a round (either first round or between rounds)
            if(!gameStarted || waitingForSpaceToStartRound) {
                // In competition mode, when game hasn't started or waiting for next round, only space key can start
                if(key.equals(" ")) {
                    waitingForSpaceToStartRound = false;
                    betweenRounds = false;
                    startGame();
                    if(currentSentence!=null){
                        keyLogsStructure=new KeyLogsStructure(currentSentence.toString());
                    }
                }
                // In competition mode, if game hasn't started or waiting for next round, ignore non-space keys
                return;
            }
            // If already started (gameStarted==true), continue with processing below
        }
        else {
            // 2. Non-competition mode (Timed/Article)
            if(!gameStarted){
                // Check if the first letter is typed correctly
                if(currentSentence != null && currentSentence.length() > 0) {
                    char expectedFirstChar = currentSentence.charAt(0);
                    String expectedFirstKey = String.valueOf(expectedFirstChar);
                    
                    // Only start the game if the first letter is typed correctly
                    if(key.equals(expectedFirstKey)) {
                        startGame();
                        if(currentSentence!=null){
                            keyLogsStructure=new KeyLogsStructure(currentSentence.toString());
                        }
                    } else {
                        // First letter is wrong, don't start the game
                        return;
                    }
                }
            }
        }

        // 3. Common logic for all modes

        // Only process visible characters and backspace
        if(!key.equals("BACK_SPACE") && !key.matches("[a-zA-Z0-9,\\.;:'\"?!@#$%^&*()\\[\\]{}\\-_=+<>/\\\\|°` ]")){
            System.out.println("Ignored key: " + key); // Add logging for debugging
            return;
        }
        if(keyLogsStructure!=null){
            keyLogsStructure.addKeyLog(key,System.currentTimeMillis()-gameStartTime);
        }

        if(isCompetitionMode()){
            handleKeyForCompetition(key);
        } else {
            handleKeyForTimedOrArticle(key);
        }
        updateStatistics();
    }


    // ========== Key handling in Competition mode ==========
    private void handleKeyForCompetition(String key){
        if(key.equals("BACK_SPACE")){
            // Competition mode does not allow backspace
            return;
        }
        char typedChar = key.charAt(0);
        // Check if character belongs to left or right hand, without converting to lowercase
        boolean belongsToLeft = false;
        boolean belongsToRight = false;
        
        // Check if character belongs to left hand characters (considering case)
        for (char c : LEFT_HAND_CHARS) {
            if (c == Character.toLowerCase(typedChar) || c == Character.toUpperCase(typedChar)) {
                belongsToLeft = true;
                break;
            }
        }
        
        // Check if character belongs to right hand characters (considering case)
        if (!belongsToLeft) {
            for (char c : RIGHT_HAND_CHARS) {
                if (c == Character.toLowerCase(typedChar) || c == Character.toUpperCase(typedChar)) {
                    belongsToRight = true;
                    break;
                }
            }
        }

        if(belongsToLeft){
            if(leftIndex<leftLetters.length()){
                char expected = leftLetters.charAt(leftIndex);
                if(typedChar == expected){
                    correctKeystrokes++;
                    scoreLeft++;
                    leftErrorFlags[leftIndex]=false;
                } else {
                    wrongKeystrokes++;
                    leftErrorFlags[leftIndex]=true;
                }
                leftIndex++;
                updateLeftDisplay();
            } else {
                wrongKeystrokes++;
            }
        }
        else if(belongsToRight){
            if(rightIndex<rightLetters.length()){
                char expected = rightLetters.charAt(rightIndex);
                if(typedChar == expected){
                    correctKeystrokes++;
                    scoreRight++;
                    rightErrorFlags[rightIndex]=false;
                } else {
                    wrongKeystrokes++;
                    rightErrorFlags[rightIndex]=true;
                }
                rightIndex++;
                updateRightDisplay();
            } else {
                wrongKeystrokes++;
            }
        }
        // else => ignore

        if(playerAOnLeft){
            leftScoreLabel.setText("PlayerA Score: "+scoreLeft);
            rightScoreLabel.setText("PlayerB Score: "+scoreRight);
        } else {
            leftScoreLabel.setText("PlayerB Score: "+scoreLeft);
            rightScoreLabel.setText("PlayerA Score: "+scoreRight);
        }
    }

    // ========== Key handling in Timed/Article mode: now judging combos by word ==========
    private void handleKeyForTimedOrArticle(String key) {
        if (key.equals("BACK_SPACE")) {
            if (currentCharIndex > 0) {
                currentCharIndex--;
                charErrorStates[currentCharIndex] = false;
            }
            updateAllUI();
            return;
        }

        if (currentCharIndex >= currentSentence.length()) {
            wrongKeystrokes++;
            provideErrorFeedback(key);
            updateAllUI();
            return;
        }

        char expectedChar = currentSentence.charAt(currentCharIndex);
        String expectedKey = String.valueOf(expectedChar);

        if (key.equals(expectedKey)) {
            // correct
            correctKeystrokes++;
        } else {
            // wrong
            wrongKeystrokes++;
            charErrorStates[currentCharIndex] = true;
            currentWordHasMistake = true;
            currentStreak = 0;
            provideErrorFeedback(key);
        }

        currentCharIndex++;

        if (expectedChar == ' ' || currentCharIndex == currentSentence.length()) {
            if (!currentWordHasMistake) {
                currentStreak++;
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak;
                }
                if (currentStreak % 5 == 0) {
                    triggerStreakEffect(currentStreak);
                }
            } else {
                currentStreak = 0;
            }
            currentWordHasMistake = false;
            wordStartIndex = currentCharIndex;
        }

        if (isTimeMode() && currentSentence.length() - currentCharIndex < 30) {
            addNewWord();
        }

        updateAllUI();
    }

    private void triggerStreakEffect(int streak) {
        comboLabel.setText("Combo x" + streak);
        comboLabel.setVisible(true); // Show label

        try {
            String soundPath = getClass().getResource("/com/example/touchtyped/sounds/338905__toxemiccarton__combo-clap.wav").toExternalForm();
            Media sound = new Media(soundPath);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Create a simple scale up + scale down animation
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), comboLabel);
        scaleUp.setFromX(1.0);
        scaleUp.setFromY(1.0);
        scaleUp.setToX(1.8);
        scaleUp.setToY(1.8);
        scaleUp.setAutoReverse(true);
        scaleUp.setCycleCount(2);

        // Hide the label after animation ends (optional)
        scaleUp.setOnFinished(e -> {
            comboLabel.setVisible(false);
            comboLabel.setScaleX(1.0);
            comboLabel.setScaleY(1.0);
        });

        scaleUp.play();
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

    private void updateAllUI(){
        if(!isCompetitionMode()){
            updateTaskDisplay();
        }
        updateStatistics();
        updateRealtimeStats();

    }

    private void updateTaskDisplay() {
        taskLabel.getChildren().clear();
        if (currentSentence == null) return;

        int visibleLen = 50;
        int start = Math.max(0, currentCharIndex - visibleLen / 2);
        int end   = Math.min(currentSentence.length(), start + visibleLen);

        // Already typed part
        for (int i = start; i < currentCharIndex; i++) {
            Text t = new Text(String.valueOf(currentSentence.charAt(i)));
            t.setTextOrigin(VPos.BASELINE);
            t.getStyleClass().add(charErrorStates[i] ? "error-text" : "typed-text");
            taskLabel.getChildren().add(t);
        }

        // Current character
        if (currentCharIndex < currentSentence.length()) {
            Text curr = new Text(String.valueOf(currentSentence.charAt(currentCharIndex)));
            curr.setTextOrigin(VPos.BASELINE);
            if (gameStarted && charErrorStates[currentCharIndex]) {
                curr.getStyleClass().add("error-text");
            } else {
                curr.getStyleClass().add("remaining-text");
            }
            taskLabel.getChildren().add(curr);
        }

        // Remaining part
        if (currentCharIndex + 1 < end) {
            Text remain = new Text(currentSentence.substring(currentCharIndex + 1, end));
            remain.setTextOrigin(VPos.BASELINE);
            remain.getStyleClass().add("remaining-text");
            taskLabel.getChildren().add(remain);
        }

        double baseX = -(visibleLen * StyleConstants.charWidth / 2.0);
        double offset = (currentCharIndex - start) * StyleConstants.charWidth;
        cursorLabel.setTranslateX(baseX + offset);
    }

    private void updateStatistics(){
        if(!gameStarted) return;
        totalKeystrokes= correctKeystrokes+wrongKeystrokes;
    }

    private void updateRealtimeStats(){
        if(!gameStarted||gameStartTime==0) return;
        long now=System.currentTimeMillis();
        double elapsedSec=(now-gameStartTime)/1000.0;
        if(elapsedSec<=0) return;

        double elapsedMin=elapsedSec/60.0;
        double wpm=(correctKeystrokes/5.0)/elapsedMin;
        wpmLabel.setText(String.format("WPM: %.1f", wpm));

        int total= correctKeystrokes+wrongKeystrokes;
        double acc=(total>0)?(correctKeystrokes*100.0/total):0.0;
        accuracyLabel.setText(String.format("Accuracy: %.1f%%",acc));
    }

    private void provideErrorFeedback(String key) {
        keyboardInterface.sendHapticCommand(key, 500, 100);
        keyboardInterface.activateLights(1000);
    }

    private void provideNextCharacterHint() {
        if (currentSentence != null && currentCharIndex < currentSentence.length()) {
            char c = currentSentence.charAt(currentCharIndex);
            String ch = String.valueOf(c);
            if (ch.equals(" ")) {
                ch = "SPACE";
            }
            keyboardInterface.sendHapticCommand(ch.toUpperCase(), 200, 50);
        }
    }

    @FXML public void select15s(){
        updateTimeButtonStyle(15);
        resetGame();
    }
    @FXML public void select30s(){
        updateTimeButtonStyle(30);
        resetGame();
    }
    @FXML public void select60s(){
        updateTimeButtonStyle(60);
        resetGame();
    }
    @FXML public void select120s(){
        updateTimeButtonStyle(120);
        resetGame();
    }

    private void updateTimeButtonStyle(int selectedTime){
        time15Button.getStyleClass().remove("selected");
        time30Button.getStyleClass().remove("selected");
        time60Button.getStyleClass().remove("selected");
        time120Button.getStyleClass().remove("selected");

        switch(selectedTime){
            case 15  -> time15Button.getStyleClass().add("selected");
            case 30  -> time30Button.getStyleClass().add("selected");
            case 60  -> time60Button.getStyleClass().add("selected");
            case 120 -> time120Button.getStyleClass().add("selected");
        }
        this.selectedTimeOption=selectedTime;
        timeLeft=selectedTime;
        timerLabel.setText(String.valueOf(timeLeft));
    }

    public boolean isInputDisabled(){
        return inputField.isDisabled();
    }

    private boolean arrayContains(char[] arr, char c){
        for(char x:arr){
            if(x==c) return true;
        }
        return false;
    }

    private void refreshCompetitionScoreUI() {
        if(playerAOnLeft){
            leftScoreLabel.setText("PlayerA Score: " + scoreLeft);
            rightScoreLabel.setText("PlayerB Score: " + scoreRight);
        } else {
            leftScoreLabel.setText("PlayerB Score: " + scoreLeft);
            rightScoreLabel.setText("PlayerA Score: " + scoreRight);
        }
    }

    /**
     * 更新比赛分数，由WebSocket客户端调用
     * @param scoreA 玩家A的分数
     * @param scoreB 玩家B的分数
     */
    public void updateCompetitionScores(int scoreA, int scoreB) {
        // 确保在JavaFX应用线程中更新UI
        Platform.runLater(() -> {
            if (playerAOnLeft) {
                scoreLeft = scoreA;
                scoreRight = scoreB;
            } else {
                scoreLeft = scoreB;
                scoreRight = scoreA;
            }
            refreshCompetitionScoreUI();
        });
    }

    @FXML
    public void onLearnButtonClick(){
        try{
            FXMLLoader loader=new FXMLLoader(getClass().getResource("/com/example/touchtyped/learn-view.fxml"));
            Scene scene=new Scene(loader.load(),1200,700);
            Stage stage=(Stage)taskLabel.getScene().getWindow();
            stage.setScene(scene);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    private void onCompetitionHelpButtonClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Competition Mode Instructions");
        alert.setHeaderText("How to play Competition Mode?");

        String content = """
            1. Players take turns:
               - First round: Player A on the left, Player B on the right;
               - Second round: Player A on the right, Player B on the left.

            2. Press [Space] to start each round:
               - Each round has a fixed countdown;
               - The round automatically ends when the countdown finishes.

            3. Scoring rules:
               - Earn 1 point for each correctly typed letter;
               - No points for incorrect inputs, but errors are counted.

            4. Final results:
               - After the second round, scores from both rounds are added;
               - The player with the higher total score wins; equal scores result in a tie.

            Tips: 
               - Maintain typing accuracy to avoid losing points;
               - After a round ends, switch positions and press space to start the next round.
            """;

        alert.setContentText(content);
        alert.showAndWait();
        inputField.requestFocus();
    }


    /**
     * Prompt user to enter player name
     */
    private void promptForPlayerName() {
        // Use our newly designed dialog
        String playerName = PlayerNameDialog.showDialog();
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Anonymous";
        }
        UserProfile.getInstance().setPlayerName(playerName);
    }

    /**
     * 显示计时模式的说明弹窗
     */
    @FXML
    private void showTimedModeInfo() {
        // 创建弹窗
        Dialog<ButtonType> infoDialog = new Dialog<>();
        infoDialog.setTitle("Timed Mode Instructions");
        infoDialog.setHeaderText(null);

        // Set dialog style
        DialogPane dialogPane = infoDialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/touchtyped/game-view-style.css").toExternalForm());
        dialogPane.getStyleClass().add("info-dialog");
        dialogPane.setPrefWidth(650);
        dialogPane.setPrefHeight(500);

        // 创建内容区域
        VBox content = new VBox(15);
        content.setPadding(new Insets(10, 20, 10, 20));
        content.setMaxWidth(600);

        // Add title
        Label titleLabel = new Label("HOW TO PLAY TIMED MODE");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2D7EE8;");
        content.getChildren().add(titleLabel);

        // Add separator
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #2D7EE8;");
        content.getChildren().add(separator);

        // Create instruction list with icons
        VBox instructionsBox = new VBox(12);

        addInstructionWithIcon(instructionsBox, "keyboard",
            "The keyboard will highlight the next character to type with lights and vibration.");

        addInstructionWithIcon(instructionsBox, "timer",
            "The timer starts only after you type the first character correctly.");

        addInstructionWithIcon(instructionsBox, "error",
            "If you make a mistake, the character will turn red. You must use backspace to correct it.");

        addInstructionWithIcon(instructionsBox, "warning",
            "Until the error is corrected, the keyboard will remind you with lights and vibration.");

        addInstructionWithIcon(instructionsBox, "stats",
            "When time runs out, your typing speed (WPM) and accuracy will be displayed.");

        content.getChildren().add(instructionsBox);

        // Add tips section
        VBox tipsBox = new VBox(10);
        tipsBox.setStyle("-fx-background-color: rgba(45, 126, 232, 0.05); -fx-padding: 15; -fx-background-radius: 5;");

        Label tipsTitle = new Label("TIPS FOR BEST RESULTS");
        tipsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2D7EE8;");
        tipsBox.getChildren().add(tipsTitle);

        // Tips content
        VBox tipsList = new VBox(8);
        addTip(tipsList, "Maintain a smooth typing rhythm to improve speed.");
        addTip(tipsList, "Focus on accuracy - errors will decrease your overall performance.");
        addTip(tipsList, "Regular practice is the best way to improve typing speed.");

        tipsBox.getChildren().add(tipsList);
        content.getChildren().add(tipsBox);

        // Set content and show
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("info-scroll-pane");

        dialogPane.setContent(scrollPane);

        // Add close button
        ButtonType closeButton = new ButtonType("Got it!", ButtonBar.ButtonData.OK_DONE);
        infoDialog.getDialogPane().getButtonTypes().setAll(closeButton);

        // Focus management
        Platform.runLater(() -> {
            Button okButton = (Button) dialogPane.lookupButton(closeButton);
            okButton.setDefaultButton(true);
            okButton.getStyleClass().add("info-close-button");
        });

        // Show dialog
        infoDialog.showAndWait();
    }

    /**
     * Helper method to add an instruction with an icon
     */
    private void addInstructionWithIcon(VBox container, String iconType, String text) {
        HBox item = new HBox(15);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Create icon based on type
        Region icon = new Region();
        icon.setPrefSize(24, 24);
        icon.setMinSize(24, 24);
        icon.setMaxSize(24, 24);
        icon.getStyleClass().add("info-icon");

        // Apply specific icon style based on type
        switch (iconType) {
            case "keyboard" -> icon.setStyle("-fx-background-color: #2D7EE8;");
            case "timer" -> icon.setStyle("-fx-background-color: #3A5B8C;");
            case "error" -> icon.setStyle("-fx-background-color: #F5102F;");
            case "warning" -> icon.setStyle("-fx-background-color: #E2B714;");
            case "stats" -> icon.setStyle("-fx-background-color: #4CAF50;");
            default -> icon.setStyle("-fx-background-color: #2D7EE8;");
        }

        // Text content
        Text instructionText = new Text(text);
        instructionText.setWrappingWidth(480);
        instructionText.setStyle("-fx-font-size: 16px;");

        item.getChildren().addAll(icon, instructionText);
        container.getChildren().add(item);
    }

    /**
     * Helper method to add a tip item
     */
    private void addTip(VBox container, String tipText) {
        HBox item = new HBox(10);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Bullet point
        Text bullet = new Text("•");
        bullet.setStyle("-fx-font-size: 16px; -fx-fill: #2D7EE8;");

        // Tip text
        Text tip = new Text(tipText);
        tip.setWrappingWidth(500);
        tip.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");

        item.getChildren().addAll(bullet, tip);
        container.getChildren().add(item);
    }

    /**
     * 更新信息按钮的可见性
     */
    private void updateInfoButtonVisibility() {
        // 只有在"计时模式"下才显示信息按钮
        boolean isTimedMode = timedModeRadio.isSelected();
        timedModeInfoButton.setVisible(isTimedMode);
    }

    /**
     * 检查是否是第一次使用
     */
    private boolean checkFirstTimeUser() {
        UserProfile profile = UserProfile.getInstance();

        // 使用UserProfile的isFirstTimeUser方法检查是否是首次使用
        return profile.isFirstTimeUser();
    }

    /**
     * 显示简化版的新手引导对话框
     */
    private void showSimpleTutorial() {
        // 确保没有活跃的教程对话框
        if (tutorialDialog != null) {
            try {
                tutorialDialog.close();
            } catch (Exception e) {
                // 忽略可能的异常
            }
            tutorialDialog = null;
        }

        tutorialStep = 0;

        // 创建对话框
        tutorialDialog = new Dialog<>();
        tutorialDialog.setTitle("TouchTypEd Tutorial");
        tutorialDialog.setHeaderText(null); // 移除标题，我们将使用自定义标题

        // 设置对话框样式
        DialogPane dialogPane = tutorialDialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/touchtyped/game-view-style.css").toExternalForm());
        dialogPane.getStyleClass().add("tutorial-dialog");
        dialogPane.setPrefWidth(600);
        dialogPane.setPrefHeight(400);

        // 添加关闭按钮类型
        tutorialDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        // 隐藏默认的关闭按钮
        Button closeButton = (Button) tutorialDialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setVisible(false);
        closeButton.setManaged(false);

        // 创建内容区域
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        // 关闭提示标签
        HBox closeHintBox = new HBox(10);
        closeHintBox.setAlignment(Pos.CENTER_LEFT);
        closeHintBox.setPadding(new Insets(5, 10, 10, 5));
        closeHintBox.setStyle("-fx-background-color: #F0F8FF; -fx-background-radius: 6; -fx-border-color: #ADD8E6; -fx-border-radius: 6; -fx-border-width: 1;");

        // 信息图标
        Label infoIcon = new Label("ⓘ");
        infoIcon.setStyle("-fx-text-fill: #2D7EE8; -fx-font-weight: bold; -fx-font-size: 16px;");

        // 提示文本
        Label closeHintText = new Label("Already know? Click X to close");
        closeHintText.setStyle("-fx-text-fill: #2D7EE8; -fx-font-size: 14px;");

        closeHintBox.getChildren().addAll(infoIcon, closeHintText);
        content.getChildren().add(closeHintBox);

        // 引导说明区域
        VBox tutorialContent = new VBox(15);
        tutorialContent.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-padding: 20;");

        // 图标和标题区域
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // 步骤指示器
        Label stepIndicator = new Label("1");
        stepIndicator.setStyle("-fx-background-color: #2D7EE8; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 50%;");

        // 引导标题
        Label stepTitle = new Label("Welcome");
        stepTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        headerBox.getChildren().addAll(stepIndicator, stepTitle);
        tutorialContent.getChildren().add(headerBox);

        // 分隔线
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E0E0E0;");
        tutorialContent.getChildren().add(separator);

        // 引导文本
        tutorialLabel = new Label("Welcome to TouchTypEd!");
        tutorialLabel.setWrapText(true);
        tutorialLabel.setStyle("-fx-font-size: 16px; -fx-line-spacing: 1.2;");
        tutorialContent.getChildren().add(tutorialLabel);

        content.getChildren().add(tutorialContent);

        // 按钮区
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        // 下一步按钮
        nextButton = new Button("Next");
        nextButton.getStyleClass().add("tutorial-button");
        nextButton.setStyle("-fx-background-color: #2D7EE8; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 8 20; -fx-background-radius: 4;");
        nextButton.setDisable(true); // 初始禁用，等内容加载完成后启用
        nextButton.setOnAction(e -> showNextTutorialStep());

        // 完成按钮
        finishButton = new Button("Got it!");
        finishButton.getStyleClass().add("tutorial-button");
        finishButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 8 20; -fx-background-radius: 4;");
        finishButton.setOnAction(e -> {
            endTutorial();
        });
        finishButton.setVisible(false);

        buttonBox.getChildren().addAll(nextButton, finishButton);
        content.getChildren().add(buttonBox);

        dialogPane.setContent(content);

        // 设置关闭行为
        tutorialDialog.setOnCloseRequest(event -> {
            // 直接调用endTutorial，不要消费事件
            endTutorial();
        });

        // 显示对话框（不阻塞）
        Platform.runLater(() -> {
            tutorialDialog.show();

            // 对话框显示后，启动动画并加载内容
            animateDialogEntry(closeHintBox, tutorialContent, buttonBox);
        });
    }

    /**
     * 显示下一步引导
     */
    private void showNextTutorialStep() {
        String tutorialText;
        String nextTitle;

        switch (tutorialStep) {
            case 0 -> {
                tutorialText = "Welcome to TouchTypEd, your personal typing assistant!\n\n" +
                        "This brief tutorial will guide you through the main features of the application. " +
                        "Click 'Next' to continue.";
                nextTitle = "Welcome";
            }
            case 1 -> {
                tutorialText = "At the top of the screen, you can select between different typing modes:\n\n" +
                        "• Timed Mode: Practice typing with a time limit to improve your speed\n\n" +
                        "• Article Mode: Type complete paragraphs without time pressure to focus on accuracy\n\n" +
                        "• Competition Mode: Challenge another player in a typing race to test your skills";
                nextTitle = "Mode Selection";
            }
            case 2 -> {
                tutorialText = "In Timed Mode, you can choose how much time you want for your practice session:\n\n" +
                        "• 15 seconds: Quick practice for experienced typists\n\n" +
                        "• 30 seconds: Short session for warm-up\n\n" +
                        "• 60 seconds: Standard session to track your progress\n\n" +
                        "• 120 seconds: Extended practice for endurance";
                nextTitle = "Time Selection";
            }
            case 3 -> {
                tutorialText = "While typing, real-time statistics help you track your performance:\n\n" +
                        "• WPM (Words Per Minute): Measures your typing speed\n\n" +
                        "• Accuracy: Shows the percentage of correct keystrokes\n\n" +
                        "These metrics help you identify areas for improvement.";
                nextTitle = "Statistics";
            }
            case 4 -> {
                tutorialText = "The main area displays the text you need to type:\n\n" +
                        "• Blue text: Characters you've typed correctly\n\n" +
                        "• Red text: Typing errors that need correction\n\n" +
                        "• Grey text: Remaining text to type\n\n" +
                        "Use Backspace to correct errors before continuing.";
                nextTitle = "Typing Area";
            }
            case 5 -> {
                tutorialText = "The circular button with the return arrow at the bottom:\n\n" +
                        "• Resets your current typing session\n\n" +
                        "• Generates a new text for practice\n\n" +
                        "Use this button whenever you want to start fresh.";
                nextTitle = "Reset Button";
            }
            case 6 -> {
                tutorialText = "In Timed Mode, the 'i' button in the bottom right corner provides:\n\n" +
                        "• Detailed instructions about typing rules\n\n" +
                        "• Tips for improving your typing skills\n\n" +
                        "• Keyboard shortcuts and guidance\n\n" +
                        "Click it anytime you need help!";
                nextTitle = "Information Button";

                // 显示完成按钮
                nextButton.setVisible(false);
                finishButton.setVisible(true);
            }
            default -> {
                endTutorial();
                return;
            }
        }

        // 找到引导对话框中的步骤指示器和标题标签
        Label stepIndicator = (Label) ((HBox) ((VBox) tutorialLabel.getParent()).getChildren().get(0)).getChildren().get(0);
        Label stepTitle = (Label) ((HBox) ((VBox) tutorialLabel.getParent()).getChildren().get(0)).getChildren().get(1);

        // 更新步骤指示器和标题
        stepIndicator.setText(String.valueOf(tutorialStep + 1));
        stepTitle.setText(nextTitle);

        tutorialLabel.setText(tutorialText);
        tutorialStep++;
    }

    /**
     * 结束引导
     */
    private void endTutorial() {
        // 标记用户已完成教程
        UserProfile.getInstance().setCompletedTutorial(true);

        // 立即关闭对话框，不使用动画
        if (tutorialDialog != null) {
            tutorialDialog.close();
            tutorialDialog = null;
        }
    }

    /**
     * 为对话框添加入场动画
     */
    private void animateDialogEntry(Node hintNode, VBox tutorialContent, HBox buttonBox) {
        // 标题淡入动画
        FadeTransition hintFade = new FadeTransition(Duration.millis(300), hintNode);
        hintFade.setFromValue(0);
        hintFade.setToValue(1);

        // 文本内容淡入动画
        FadeTransition textFade = new FadeTransition(Duration.millis(500), tutorialLabel);
        textFade.setFromValue(0);
        textFade.setToValue(1);
        textFade.setDelay(Duration.millis(300));

        // 内容向上移动并淡入动画
        tutorialContent.setTranslateY(20);
        FadeTransition contentFade = new FadeTransition(Duration.millis(500), tutorialContent);
        contentFade.setFromValue(0);
        contentFade.setToValue(1);
        contentFade.setDelay(Duration.millis(200));

        TranslateTransition contentSlide = new TranslateTransition(Duration.millis(500), tutorialContent);
        contentSlide.setFromY(20);
        contentSlide.setToY(0);
        contentSlide.setDelay(Duration.millis(200));

        // 按钮淡入动画
        FadeTransition buttonFade = new FadeTransition(Duration.millis(300), buttonBox);
        buttonFade.setFromValue(0);
        buttonFade.setToValue(1);
        buttonFade.setDelay(Duration.millis(400));

        // 把所有动画放在一起
        ParallelTransition parallelTransition = new ParallelTransition(
                hintFade, contentFade, contentSlide, buttonFade, textFade
        );

        // 动画完成后，显示第一步教程
        parallelTransition.setOnFinished(e -> {
            showNextTutorialStep();
            nextButton.setDisable(false);
        });

        parallelTransition.play();
    }
}
