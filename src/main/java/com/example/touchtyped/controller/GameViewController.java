package com.example.touchtyped.controller;

import com.example.touchtyped.app.Application;
import com.example.touchtyped.constants.StyleConstants;
import com.example.touchtyped.firestore.ClassroomDAO;
import com.example.touchtyped.firestore.UserAccount;
import com.example.touchtyped.firestore.UserDAO;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.GameKeypressListener;
import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.UserProfile;
import com.example.touchtyped.service.AppSettingsService;
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
import javafx.scene.layout.BorderPane;
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
import javafx.scene.layout.Priority;
import javafx.scene.input.KeyCode;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

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
    @FXML private Button viewRankingButton;

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

    @FXML private ImageView classroomButton;

    @FXML private Label comboLabel;

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
     * Tutorial content
     */
    private Label tutorialContent;

    /**
     * Tutorial buttons
     */
    private Button tutorialNextButton, tutorialFinishButton;

    private Timeline timeline;
    private boolean gameStarted = false;
    private long gameStartTime;
    private int selectedTimeOption = 60; // Timed model time option 15/30/60/120
    private int timeLeft = 60;
    private int competitionTime = 30;    // compttition model time

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

    //physical ui
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

    /**
     * Sets the keyboard interface for this controller.
     * @param keyboardInterface The keyboard interface to use
     */
    public void setKeyboardInterface(KeyboardInterface keyboardInterface) {
        this.keyboardInterface = keyboardInterface;
    }

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

    private AppSettingsService settingsService;

    @FXML
    public void initialize(){
        // 初始化用户配置文件
        userProfile = UserProfile.getInstance();

        // Get settings service
        settingsService = AppSettingsService.getInstance();

        // Only create a new KeyboardInterface if one hasn't been set
        if (keyboardInterface == null) {
            keyboardInterface = Application.keyboardInterface;
        }
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
                // Apply settings to the scene
                settingsService.applySettingsToScene(newScene);

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

        // already typed
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
        // cursor
        if(leftIndex<end){
            leftCursor.setText("|");
            leftCursor.setTextOrigin(VPos.BASELINE);
            leftCursor.getStyleClass().add("cursor");
            leftCursor.setVisible(true);
            leftTextFlow.getChildren().add(leftCursor);
        } else {
            leftCursor.setVisible(false);
        }
        // not typed
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

        // already typed
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
        // cursor
        if(rightIndex<end){
            rightCursor.setText("|");
            rightCursor.setTextOrigin(VPos.BASELINE);
            rightCursor.getStyleClass().add("cursor");
            rightCursor.setVisible(true);
            rightTextFlow.getChildren().add(rightCursor);
        } else {
            rightCursor.setVisible(false);
        }
        // not typed
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
        viewRankingButton.setVisible(false); // 初始化时隐藏排名按钮

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

        // update ui
        if(isArticleMode()){
            timeLeft=0;
            timerLabel.setText("Article Mode");
            wpmLabel.setVisible(true);
            accuracyLabel.setVisible(true);
            taskLabel.setVisible(true);
            viewRankingButton.setVisible(true); // 文章模式显示排名按钮
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
            viewRankingButton.setVisible(false); // 竞赛模式隐藏排名按钮
        }

        if (isTimeMode()){
            // Timed
            timeLeft= selectedTimeOption;
            timerLabel.setText(String.valueOf(timeLeft));
            timerLabel.setVisible(true);
            timerLabel.setManaged(true);
            timeBox.setVisible(true);
            timeBox.setManaged(true);
            wpmLabel.setVisible(true);
            accuracyLabel.setVisible(true);
            taskLabel.setVisible(true);
            viewRankingButton.setVisible(true); // 计时模式显示排名按钮
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
                // only show player's cursor
                cursorLabel.setVisible(true);
            }
            // other model
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

                // generate next round letter
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

                // update score
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

                // game is over, display results and save keyLogsStructure.

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

                // add simple statistics to structure
                keyLogsStructure.setWpm((int) finalWpm);
                keyLogsStructure.setCorrectKeystrokes(correctKeystrokes);
                keyLogsStructure.setIncorrectKeystrokes(wrongKeystrokes);

                // save keyLogsStructure to database
                Map<String, String> userDetails = ClassroomDAO.loadUserCache();
                if (userDetails != null) {
                    try {
                        String classroomID = userDetails.get("classroomID");
                        String username = userDetails.get("username");
                        String password = userDetails.getOrDefault("password", null);

                        UserAccount userAccount = UserDAO.getAccount(classroomID, username, password);

                        if (userAccount == null) {
                            // either the user doesn't exist or password is wrong.
                        } else {
                            UserDAO.addKeyLog(classroomID, username, keyLogsStructure, password);
                            System.out.println("Added key logs structure to database.");
                        }

                    } catch (Exception e) {
                        System.out.println("DATABASE FAILURE. Failed to save keyLogsStructure to user account.");
                    }

                } else {
                    // do nothing - user is not logged in
                }

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
        if(!key.equals("BACK_SPACE") && !key.matches("[a-zA-Z0-9,\\.;:'\"?!@#$%^&*()\\[\\]{}\\-_=+<>/\\\\|°`~ ]")){
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

        // 如果key长度大于1，说明是特殊键，忽略它
        if (key.length() != 1) {
            return;
        }

        char typedChar = key.charAt(0);
        // Check if character belongs to left or right hand, without converting to lowercase
        boolean belongsToLeft = false;
        boolean belongsToRight = false;

        // 使用toLowerCase进行比较，这样我们只需要定义小写版本的LEFT_HAND_CHARS和RIGHT_HAND_CHARS
        char lowerTypedChar = Character.toLowerCase(typedChar);

        // 检查字符是否属于左手
        for (char c : LEFT_HAND_CHARS) {
            if (c == lowerTypedChar) {
                belongsToLeft = true;
                break;
            }
        }

        // 检查字符是否属于右手
        if (!belongsToLeft) {
            for (char c : RIGHT_HAND_CHARS) {
                if (c == lowerTypedChar) {
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
        // right => scoreRight++
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

        // 如果是文章模式且已经完成了所有文本，结束游戏
        if (isArticleMode() && currentCharIndex >= currentSentence.length()) {
            // 短暂延迟后结束游戏，让用户看到最后一个字符
            Platform.runLater(() -> {
                // 更新一次UI确保显示最后一个字符
                updateAllUI();
                // 0.5秒延迟后结束游戏
                Timeline delayedEnd = new Timeline(new KeyFrame(Duration.millis(500), e -> endGame()));
                delayedEnd.play();
            });
        } else if (isTimeMode() && currentSentence.length() - currentCharIndex < 30) {
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

        int start;
        if (isArticleMode() && currentSentence.length() <= visibleLen) {
            start = 0;
        } else if (isArticleMode() && currentSentence.length() - currentCharIndex < visibleLen / 2) {
            start = Math.max(0, currentSentence.length() - visibleLen);
        } else {
            start = Math.max(0, currentCharIndex - visibleLen / 2);
        }

        int end = Math.min(currentSentence.length(), start + visibleLen);

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

        // 更新光标位置，确保即使在文章结束时也能正确显示
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

        // 确保至少经过1秒钟才开始计算WPM，避免刚开始时数值异常大
        if(elapsedSec < 1.0) {
            wpmLabel.setText("WPM: 0.0");
            int total = correctKeystrokes+wrongKeystrokes;
            double acc = (total>0) ? (correctKeystrokes*100.0/total) : 0.0;
            accuracyLabel.setText(String.format("Accuracy: %.1f%%", acc));
            return;
        }

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
            keyboardInterface.sendHapticCommand(ch.toUpperCase(), 500, 50);
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
            LearnViewController lVController = loader.getController();
            lVController.setKeyboardInterface(keyboardInterface);

            // Apply settings to the scene
            settingsService.applySettingsToScene(scene);

            Stage stage=(Stage)taskLabel.getScene().getWindow();

            // 保存当前全屏状态和当前位置大小
            boolean wasFullScreen = stage.isFullScreen();

            if(wasFullScreen) {
                // 如果是全屏状态，设置新场景时保持全屏
                // 先设置场景不可见
                stage.setOpacity(0);

                // 设置新场景
                stage.setScene(scene);

                // 确保全屏设置正确
                stage.setFullScreen(true);

                // 恢复可见性
                stage.setOpacity(1);
            } else {
                // 非全屏状态正常切换
                stage.setScene(scene);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void onOptionsButtonClick(){
        try{
            FXMLLoader loader=new FXMLLoader(getClass().getResource("/com/example/touchtyped/options-view.fxml"));
            Scene scene=new Scene(loader.load(),1200,700);

            // Apply settings to the scene
            settingsService.applySettingsToScene(scene);

            Stage stage=(Stage)taskLabel.getScene().getWindow();

            // 保存当前全屏状态
            boolean wasFullScreen = stage.isFullScreen();

            // 设置新场景并保持全屏状态
            if(wasFullScreen) {
                stage.setOpacity(0);
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.setOpacity(1);
            } else {
                stage.setScene(scene);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void onClassroomButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/classroom-view.fxml"));

            // 添加加载异常处理
            loader.setControllerFactory(controllerClass -> {
                try {
                    Object controller = controllerClass.getDeclaredConstructor().newInstance();
                    return controller;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            });

            Scene scene = null;
            try {
                scene = new Scene(loader.load(), 1200, 700);

                // 应用主题设置到场景
                settingsService.applySettingsToScene(scene);

                Stage stage = (Stage) classroomButton.getScene().getWindow();

                // 保存当前全屏状态
                boolean wasFullScreen = stage.isFullScreen();

                // 设置新场景并保持全屏状态
                if(wasFullScreen) {
                    stage.setOpacity(0);
                    stage.setScene(scene);
                    stage.setFullScreen(true);
                    stage.setOpacity(1);
                } else {
                    stage.setScene(scene);
                }
            } catch (Exception e) {
                // 处理FXML加载错误
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("无法加载教室页面");
                alert.setContentText("加载教室页面时发生错误。请稍后再试。\n\n错误详情: " + e.getMessage());
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("无法加载教室页面");
            alert.setContentText("加载教室页面时发生错误。请稍后再试。\n\n错误详情: " + e.getMessage());
            alert.showAndWait();
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

        // 应用当前的主题设置
        if (settingsService.isDarkMode()) {
            dialogPane.getStyleClass().add("dark-mode");
        } else if (settingsService.isColorblindMode()) {
            dialogPane.getStyleClass().add("colorblind-mode");
        }

        dialogPane.setPrefWidth(650);
        dialogPane.setPrefHeight(500);

        VBox content = new VBox(20);
        content.setPadding(new Insets(10, 20, 10, 20));

        // 创建标题
        Label titleLabel = new Label("How to Play Timed Mode");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 如果是暗黑模式，手动设置标题颜色
        if (settingsService.isDarkMode()) {
            titleLabel.setTextFill(javafx.scene.paint.Color.web("#E0E0E0"));
        } else if (settingsService.isColorblindMode()) {
            titleLabel.setTextFill(javafx.scene.paint.Color.BLACK);
        }

        content.getChildren().add(titleLabel);

        // 添加说明文字
        // 使用滚动面板显示所有内容
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("info-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(350);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // 内容容器
        VBox instructionsContainer = new VBox(15);
        instructionsContainer.setPadding(new Insets(10, 10, 10, 10));

        // 添加说明和提示
        addInstructionWithIcon(instructionsContainer, "key", "Type the text shown on the screen. Use the keyboard to input characters.");
        addTip(instructionsContainer, "Focus on accuracy first, speed will come naturally as you practice.");

        addInstructionWithIcon(instructionsContainer, "time", "Choose a time limit (15, 30, 60, or 120 seconds) for your typing session.");
        addTip(instructionsContainer, "Start with shorter sessions and gradually increase as you improve.");

        addInstructionWithIcon(instructionsContainer, "wpm", "The WPM (Words Per Minute) displays your current typing speed.");
        addTip(instructionsContainer, "A word is counted as 5 characters, including spaces and punctuation.");

        addInstructionWithIcon(instructionsContainer, "accuracy", "Accuracy shows the percentage of correct keystrokes.");
        addTip(instructionsContainer, "Use Backspace to correct mistakes before continuing to the next character.");

        scrollPane.setContent(instructionsContainer);
        content.getChildren().add(scrollPane);

        // 添加关闭按钮
        Button closeButton = new Button("Got it!");
        closeButton.getStyleClass().add("info-close-button");
        closeButton.setOnAction(event -> infoDialog.close());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeButton);

        content.getChildren().add(buttonBox);

        dialogPane.setContent(content);

        // 添加但隐藏关闭按钮
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Button closeBtn = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        closeBtn.setVisible(false);

        // 显示弹窗
        infoDialog.showAndWait();
    }

    /**
     * Helper method to add an instruction with an icon
     */
    private void addInstructionWithIcon(VBox container, String iconType, String text) {
        try {
            HBox instructionBox = new HBox(10);
            instructionBox.setAlignment(Pos.CENTER_LEFT);

            // 创建图标
            ImageView iconView = new ImageView();
            iconView.setFitWidth(24);
            iconView.setFitHeight(24);

            // 根据类型设置不同图标
            String iconPath;
            switch (iconType) {
                case "key":
                    iconPath = "/com/example/touchtyped/images/info-content/key.png";
                    break;
                case "time":
                    iconPath = "/com/example/touchtyped/images/info-content/time.png";
                    break;
                case "accuracy":
                    iconPath = "/com/example/touchtyped/images/info-content/accuracy.png";
                    break;
                case "wpm":
                    iconPath = "/com/example/touchtyped/images/info-content/speed.png";
                    break;
                default:
                    iconPath = "/com/example/touchtyped/images/info-content/info.png";
                    break;
            }

            Image icon = new Image(getClass().getResource(iconPath).toExternalForm());
            iconView.setImage(icon);
            iconView.getStyleClass().add("info-icon");

            // 创建文本标签
            Label instructionText = new Label(text);
            instructionText.setWrapText(true);
            instructionText.getStyleClass().add("info-instruction");

            // 如果是暗黑模式，手动设置文本颜色
            if (settingsService.isDarkMode()) {
                instructionText.setTextFill(javafx.scene.paint.Color.web("#E0E0E0"));
            } else if (settingsService.isColorblindMode()) {
                instructionText.setTextFill(javafx.scene.paint.Color.BLACK);
            }

            instructionBox.getChildren().addAll(iconView, instructionText);
            container.getChildren().add(instructionBox);

        } catch (Exception e) {
            System.err.println("Error adding instruction: " + e.getMessage());
        }
    }

    private void addTip(VBox container, String tipText) {
        try {
            HBox tipBox = new HBox(10);
            tipBox.setAlignment(Pos.CENTER_LEFT);
            tipBox.setPadding(new Insets(0, 0, 0, 34));

            Label tipTextLabel = new Label("Tip: " + tipText);
            tipTextLabel.setWrapText(true);
            tipTextLabel.getStyleClass().add("info-tip");

            // 如果是暗黑模式，手动设置文本颜色
            if (settingsService.isDarkMode()) {
                tipTextLabel.setTextFill(javafx.scene.paint.Color.web("#B0B0B0"));
            } else if (settingsService.isColorblindMode()) {
                tipTextLabel.setTextFill(javafx.scene.paint.Color.web("#555555"));
            }

            tipBox.getChildren().add(tipTextLabel);
            container.getChildren().add(tipBox);

        } catch (Exception e) {
            System.err.println("Error adding tip: " + e.getMessage());
        }
    }


    private void updateInfoButtonVisibility() {
        boolean isTimedMode = timedModeRadio.isSelected();
        timedModeInfoButton.setVisible(isTimedMode);
        viewRankingButton.setVisible(isTimeMode() || isArticleMode());
    }

    /**
     * check if it is first time use
     */
    private boolean checkFirstTimeUser() {
        UserProfile profile = UserProfile.getInstance();

        // 使用UserProfile的isFirstTimeUser方法检查是否是首次使用
        return profile.isFirstTimeUser();
    }


    private void showSimpleTutorial() {
        tutorialDialog = new Dialog<>();
        tutorialDialog.setTitle("TouchTypEd Tutorial");
        tutorialDialog.setHeaderText(null);

        // 设置对话框样式
        DialogPane dialogPane = tutorialDialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/touchtyped/game-view-style.css").toExternalForm());
        dialogPane.getStyleClass().add("tutorial-dialog");

        // 应用当前的主题设置
        if (settingsService.isDarkMode()) {
            dialogPane.getStyleClass().add("dark-mode");
        } else if (settingsService.isColorblindMode()) {
            dialogPane.getStyleClass().add("colorblind-mode");
        }

        dialogPane.setPrefWidth(750);  // 增大宽度
        dialogPane.setPrefHeight(550); // 增大高度
        dialogPane.setMinWidth(750);   // 增大最小宽度
        dialogPane.setMinHeight(550);  // 增大最小高度

        tutorialStep = 0;

        BorderPane dialogContent = new BorderPane();
        VBox contentArea = new VBox(20);
        contentArea.getStyleClass().add("tutorial-content-area");

        // 创建对话框标题
        Label titleLabel = new Label("Welcome to TouchTypEd!");
        titleLabel.getStyleClass().add("tutorial-dialog-title");

        // 创建内容区域
        tutorialContent = new Label();
        tutorialContent.setWrapText(true);
        tutorialContent.setMaxWidth(650);  // 增大最大宽度

        // 添加关闭提示
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        Label closeHint = new Label("Press ESC to close at any time");
        closeHint.getStyleClass().add("tutorial-close-hint");
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("tutorial-close-button");
        closeButton.setOnAction(e -> endTutorial());
        topBar.getChildren().addAll(closeHint, closeButton);
        topBar.setSpacing(10);

        contentArea.getChildren().addAll(titleLabel, tutorialContent);

        // 创建按钮区域
        HBox buttonArea = new HBox(10);
        buttonArea.getStyleClass().add("tutorial-button-area");
        buttonArea.setAlignment(Pos.CENTER_RIGHT);

        Button nextButton = new Button("Next");
        nextButton.getStyleClass().addAll("tutorial-button", "tutorial-next-button");
        nextButton.setDefaultButton(true);
        nextButton.setOnAction(e -> showNextTutorialStep());

        Button finishButton = new Button("Got it!");
        finishButton.getStyleClass().addAll("tutorial-button", "tutorial-next-button");
        finishButton.setOnAction(e -> endTutorial());
        finishButton.setVisible(false);

        tutorialNextButton = nextButton;
        tutorialFinishButton = finishButton;

        buttonArea.getChildren().addAll(finishButton, nextButton);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonArea.getChildren().add(0, spacer);

        // 组装对话框内容
        dialogContent.setTop(topBar);
        dialogContent.setCenter(contentArea);
        dialogContent.setBottom(buttonArea);

        dialogPane.setContent(dialogContent);

        // 添加但隐藏关闭按钮
        tutorialDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Button closeBtn = (Button) tutorialDialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeBtn.setManaged(false);
        closeBtn.setVisible(false);

        dialogPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                endTutorial();
            }
        });

        // 显示第一步教程
        showNextTutorialStep();

        // 非阻塞式显示对话框
        Platform.runLater(() -> {
            tutorialDialog.show();
            // 确保对话框始终在最前面
            Stage stage = (Stage) tutorialDialog.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
        });
    }

    /**
     * Next step instruction
     */
    private void showNextTutorialStep() {
        tutorialStep++;
        String content = "";
        String title = "Welcome to TouchTypEd!";

        switch (tutorialStep) {
            case 1:
                title = "Welcome to TouchTypEd!";
                content = "This tutorial will guide you through the basics of TouchTypEd, a typing practice application designed to help you improve your typing speed and accuracy.\n\nYou'll learn how to use the various features and get the most out of your practice sessions.";
                break;
            case 2:
                title = "Typing Practice";
                content = "In the main screen, you'll see text to type. The current character you need to type will be highlighted, and the cursor shows your current position.\n\nJust start typing to begin! Correct characters will appear in blue, while errors will be marked in red. Use the Backspace key to correct any mistakes.";
                break;
            case 3:
                title = "Timer and Statistics";
                content = "You can select different time limits for your practice session (15, 30, 60, or 120 seconds). The timer starts automatically when you begin typing.\n\nYour WPM (Words Per Minute), accuracy, and score are tracked in real-time so you can see your progress as you type.";
                break;
            case 4:
                title = "Game Modes";
                content = "TouchTypEd offers multiple game modes:\n\n• Timed Mode: Practice with a time limit to improve your speed\n• Article Mode: Type complete articles without time pressure to focus on accuracy\n• Competition Mode: Challenge another player to test your skills";
                break;
            case 5:
                title = "Ready to Start!";
                content = "Now you're ready to begin practicing! Remember these tips for success:\n\n• Regular practice is key to improving typing speed and accuracy\n• Focus on accuracy first, then speed will follow\n• Try to maintain a steady rhythm while typing\n\nGood luck, and enjoy your typing journey!";
                tutorialNextButton.setVisible(false);
                tutorialFinishButton.setVisible(true);
                break;
            default:
                endTutorial();
                return;
        }

        VBox contentArea = (VBox) ((BorderPane) tutorialDialog.getDialogPane().getContent()).getCenter();
        Label titleLabel = (Label) contentArea.getChildren().get(0);
        titleLabel.setText(title);
        tutorialContent.setText(content);
        tutorialNextButton.setDisable(false);
    }


    private void endTutorial() {
        if (tutorialDialog != null) {
            tutorialDialog.close();
            tutorialDialog = null;
        }

        if (userProfile != null) {
            userProfile.setCompletedTutorial(true);
            userProfile.saveProfile();
        }
    }


    private void animateDialogEntry(Node hintNode, VBox tutorialContent, HBox buttonBox) {
        FadeTransition hintFade = new FadeTransition(Duration.millis(300), hintNode);
        hintFade.setFromValue(0);
        hintFade.setToValue(1);

        FadeTransition textFade = new FadeTransition(Duration.millis(500), tutorialLabel);
        textFade.setFromValue(0);
        textFade.setToValue(1);
        textFade.setDelay(Duration.millis(300));

        tutorialContent.setTranslateY(20);
        FadeTransition contentFade = new FadeTransition(Duration.millis(500), tutorialContent);
        contentFade.setFromValue(0);
        contentFade.setToValue(1);
        contentFade.setDelay(Duration.millis(200));

        TranslateTransition contentSlide = new TranslateTransition(Duration.millis(500), tutorialContent);
        contentSlide.setFromY(20);
        contentSlide.setToY(0);
        contentSlide.setDelay(Duration.millis(200));

        FadeTransition buttonFade = new FadeTransition(Duration.millis(300), buttonBox);
        buttonFade.setFromValue(0);
        buttonFade.setToValue(1);
        buttonFade.setDelay(Duration.millis(400));

        ParallelTransition parallelTransition = new ParallelTransition(
                hintFade, contentFade, contentSlide, buttonFade, textFade
        );

        parallelTransition.setOnFinished(e -> {
            showNextTutorialStep();
            tutorialNextButton.setDisable(false);
        });

        parallelTransition.play();
    }

    @FXML
    private StackPane mainContainer;
    private UserProfile userProfile;


    @FXML
    public void onViewRankingButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/ranking-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);

            // Apply settings to the scene
            settingsService.applySettingsToScene(scene);
            Stage stage = (Stage) viewRankingButton.getScene().getWindow();
            boolean wasFullScreen = stage.isFullScreen();
            stage.setScene(scene);
            if(wasFullScreen) {
                stage.setFullScreen(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
