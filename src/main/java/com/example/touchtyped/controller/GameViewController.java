package com.example.touchtyped.controller;

import com.example.touchtyped.constants.StyleConstants;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.GameKeypressListener;
import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.PlayerRanking;
import com.example.touchtyped.model.TypingPlan;
import com.example.touchtyped.model.UserProfile;
import com.example.touchtyped.service.RESTClient;
import com.example.touchtyped.service.RESTResponseWrapper;
import com.example.touchtyped.service.RankingService;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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
    @FXML private RadioButton competitionModeRadio;

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

    // ========== 核心字段 ==========
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
    // 表示“当前单词”是否已经犯过错（即使后来回退修正，依然算错）
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

    private static final char[] LEFT_HAND_CHARS = {
            'q','w','e','r','t','a','s','d','f','g','z','x','c','v','b'
    };
    private static final char[] RIGHT_HAND_CHARS = {
            'y','u','i','o','p','h','j','k','l','n','m'
    };
    
    // ========== 连击相关变量（修改） ==========
    /**
     * currentStreak, maxStreak 现在以“单词”为单位来统计
     */
    private int currentStreak = 0;   // 当前连击（单词）
    private int maxStreak = 0;       // 历史最高连击
    /**
     * 记录当前单词起始下标，以便判断整词是否正确
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
            }
        });
        modeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal)->{
            resetGame();
        });

        leftCursor.setTextOrigin(VPos.BASELINE);
        rightCursor.setTextOrigin(VPos.BASELINE);
        cursorLabel.setTextOrigin(VPos.BASELINE);

        resetGame();
    }

    /**
     * 加载题库：sentences.txt
     */
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

    /**
     * 加载长文：articles.txt
     */
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

    // ========== Competition: 生成随机字母给左右手练习 ==========
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

        // 重置单词连击
        currentStreak = 0;
        maxStreak = 0;
        wordStartIndex = 0; // 保证每次开始新任务都从0下标起算单词

        if(isCompetitionMode()){
            isSecondRound = false;
            playerAOnLeft = true;
            scoreLeft=0;
            scoreRight=0;
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
        wordStartIndex=0; // 新生成的句子，从0开始
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
                // first round end
                isSecondRound = true;
                int oldLeft = scoreLeft;
                int oldRight= scoreRight;
                scoreLeft   = oldRight;
                scoreRight  = oldLeft;

                // switch player position
                playerAOnLeft = !playerAOnLeft;

                // generate next round letter
                leftIndex=0;
                rightIndex=0;
                leftTextFlow.getChildren().clear();
                rightTextFlow.getChildren().clear();
                generateRandomLettersForCompetition(COMP_LETTER_COUNT);
                updateLeftDisplay();
                updateRightDisplay();

                // reset time
                timeLeft = competitionTime;
                compTimerLabel.setText("Time Left: " + timeLeft);

                // update score
                refreshCompetitionScoreUI();

                // restart time
                gameStarted = false;
                timeline = new Timeline(new KeyFrame(Duration.seconds(1), e->{
                    timeLeft--;
                    compTimerLabel.setText("Time Left: "+timeLeft);
                    if(timeLeft<=0){
                        endGame();
                    }
                }));
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.play();
                gameStarted=true;

            } else {
                // second round end
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
                    
                    // 添加竞争结果到排名
                    String playerName = UserProfile.getInstance().getPlayerName();
                    if (playerName != null && !playerName.isEmpty()) {
                        PlayerRanking rankingA = new PlayerRanking(
                            playerName + " (Player A)", 
                            finalScoreA, 
                            100.0, // 竞争模式没有准确度
                            "Competition Mode"
                        );
                        RankingService.getInstance().addRanking(rankingA);
                        
                        PlayerRanking rankingB = new PlayerRanking(
                            "Player B", 
                            finalScoreB, 
                            100.0, // 竞争模式没有准确度
                            "Competition Mode"
                        );
                        RankingService.getInstance().addRanking(rankingB);
                    }
                });

                resetGame();
            }
        } else {
            // Timed or Article
            double finalWpm=(correctKeystrokes/5.0)/(selectedTimeOption/60.0);
            try{
                FXMLLoader loader=new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-result-view.fxml"));
                Scene resultScene=new Scene(loader.load(),1200,700);

                GameResultViewController resultController=loader.getController();
                
                // 获取游戏模式
                String gameMode = isTimeMode() ? "Timed Mode" : "Article Mode";
                
                // 使用UserProfile获取玩家名称
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

    /**
     * 处理所有按键输入
     */
    @FXML
    public void handleKeyPress(String key){
        if(SPECIAL_KEY_TO_CHAR.containsKey(key)){
            key = SPECIAL_KEY_TO_CHAR.get(key);
        }
        if(inputField.isDisabled())return;

        if(!gameStarted){
            startGame();
            if(currentSentence!=null){
                keyLogsStructure=new KeyLogsStructure(currentSentence.toString());
            }
        }

        // 只处理可见字符和 backspace
        if(!key.equals("BACK_SPACE") && !key.matches("[a-zA-Z0-9,\\.;:'\"?! ]")){
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

    // ========== Competition模式下按键处理 ==========
    private void handleKeyForCompetition(String key){
        if(key.equals("BACK_SPACE")){
            // competition model 不允许回退
            return;
        }
        char typedChar=Character.toLowerCase(key.charAt(0));
        boolean belongsToLeft=arrayContains(LEFT_HAND_CHARS, typedChar);
        boolean belongsToRight=arrayContains(RIGHT_HAND_CHARS, typedChar);

        if(belongsToLeft){
            if(leftIndex<leftLetters.length()){
                char expected= leftLetters.charAt(leftIndex);
                if(typedChar==expected){
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
                char expected= rightLetters.charAt(rightIndex);
                if(typedChar==expected){
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

    // ========== Timed/Article模式下：改成按单词判定连击 ==========
    private void handleKeyForTimedOrArticle(String key){
        if (key.equals("BACK_SPACE")) {
            if (currentCharIndex > 0) {
                currentCharIndex--;
                charErrorStates[currentCharIndex] = false;
                // 重新检查当前单词是否还有错误
                hasUnresolvedError = false;
                for (int i = wordStartIndex; i < currentCharIndex; i++) {
                    if (charErrorStates[i]) {
                        hasUnresolvedError = true;
                        break;
                    }
                }
                hasFirstError = false;
                updateAllUI();
            }
            return;
        }

        if (currentCharIndex >= currentSentence.length()) {
            // 超出句子长度 => 直接判错
            wrongKeystrokes++;
            provideErrorFeedback(key);
            updateAllUI();
            return;
        }

        // 取期望字符
        char expectedChar = currentSentence.charAt(currentCharIndex);
        String expectedKey = String.valueOf(expectedChar);

        // 输入与期望字符是否匹配
        if (key.equalsIgnoreCase(expectedKey)) {
            correctKeystrokes++;
            currentCharIndex++;
            if (hasUnresolvedError) {
                provideErrorFeedback(key);
            } else {
                provideNextCharacterHint();
            }
        } else {
            // 错误输入
            charErrorStates[currentCharIndex] = true;
            hasUnresolvedError = true;
            wrongKeystrokes++;
            currentCharIndex++;
            currentWordHasMistake = true;
            provideErrorFeedback(key);
        }

        if (expectedChar == ' ' || currentCharIndex == currentSentence.length()) {
            if (!currentWordHasMistake) {
                // 这单词完全没犯过错 => 连击+1
                currentStreak++;
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak;
                }
                // 如果正好是5的倍数，就触发动画/音效
                if (currentStreak % 5 == 0) {
                    triggerStreakEffect(currentStreak);
                }
            } else {
                currentStreak = 0;
            }
            // 单词结束，无论对错，都重置这个标记，开始下一个单词
            currentWordHasMistake = false;
            wordStartIndex = currentCharIndex;
        }

        // Timed模式下如果剩余字符不多 => 动态增加
        if (isTimeMode() && (currentSentence.length() - currentCharIndex < 30)) {
            addNewWord();
        }
        updateAllUI();
    }


    private void triggerStreakEffect(int streak) {
        comboLabel.setText("Combo x" + streak + "!");
        comboLabel.setVisible(true); // 显示

        try {
            String soundPath = getClass().getResource("/com/example/touchtyped/sounds/338905__toxemiccarton__combo-clap.wav").toExternalForm();
            Media sound = new Media(soundPath);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. 做一个简单放大+缩回动画
        ScaleTransition st = new ScaleTransition(Duration.millis(600), comboLabel);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.8);
        st.setToY(1.8);
        st.setAutoReverse(true);
        st.setCycleCount(2);

        // 动画结束后自动隐藏 label（可选）
        st.setOnFinished(evt -> {
            comboLabel.setVisible(false);
            comboLabel.setScaleX(1.0);
            comboLabel.setScaleY(1.0);
        });

        st.play();
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

        // 这里可以更新连击UI或在控制台打印
        System.out.println("Current Word Streak: " + currentStreak
                + ", Max Streak: " + maxStreak);
    }

    private void updateTaskDisplay() {
        taskLabel.getChildren().clear();
        if (currentSentence == null) return;

        int visibleLen = 50;
        int start = Math.max(0, currentCharIndex - visibleLen / 2);
        int end   = Math.min(currentSentence.length(), start + visibleLen);

        // 已输入部分
        for (int i = start; i < currentCharIndex; i++) {
            Text t = new Text(String.valueOf(currentSentence.charAt(i)));
            t.setTextOrigin(VPos.BASELINE);
            t.getStyleClass().add(charErrorStates[i] ? "error-text" : "typed-text");
            taskLabel.getChildren().add(t);
        }

        // 当前字符
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

        // 剩余部分
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

    /**
     * 提示用户输入玩家名称
     */
    private void promptForPlayerName() {
        // 使用我们新设计的对话框
        String playerName = PlayerNameDialog.showDialog();
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Anonymous";
        }
        UserProfile.getInstance().setPlayerName(playerName);
    }
}
