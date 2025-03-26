package com.example.touchtyped.controller;

import com.example.touchtyped.app.Application;
import com.example.touchtyped.constants.StyleConstants;
import com.example.touchtyped.interfaces.ComputerVisionInterface;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.interfaces.KeypressListener;
import com.example.touchtyped.model.*;
import com.example.touchtyped.model.Module;
import com.example.touchtyped.service.AppSettingsService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

/**
 * displays the Module that was clicked on in the Learn view.
 */
public class ModuleViewController implements KeypressListener {


    @FXML
    private FlowPane charContainer;

    @FXML
    private Label moduleDisplayName;

    @FXML
    private Label moduleDisplayText;

    @FXML
    private ImageView learnButton;

    @FXML
    private ImageView gamesButton;

    @FXML
    private Label closeButton;

    @FXML
    private Button nextButton;

    @FXML
    private ImageView optionsButton;

    // vars
    private Module module;
    private Level level;
    private List<Label> letterLabels = new ArrayList<>();
    private int currentIndex;
    private KeyboardInterface keyboardInterface = Application.keyboardInterface;
    private String typedString = "";
    private final int MAX_BOXES_PER_ROW = 16;
    private Map<String, Character> keyMap = new HashMap<String, Character>();
    private ComputerVisionInterface computerVisionInterface;

    private AppSettingsService settingsService;

    public void initialize() {
        // Get settings service
        settingsService = AppSettingsService.getInstance();

        // Attach keyboard interface to scene, when scene is available
        Platform.runLater(() -> {
            Scene scene = closeButton.getScene();
            if (scene != null) {
                keyboardInterface.attachToScene(scene);

                // Apply settings to the scene
                settingsService.applySettingsToScene(scene);
                System.out.println("Applied settings to module view scene");
            } else {
                System.err.println("Scene is not available yet.");
            }
        });

        computerVisionInterface = new ComputerVisionInterface();
        computerVisionInterface.runCVProgramWithPopups();

        // register as a keypress listener
        keyboardInterface.addKeypressListener(this);

        closeButton.setStyle(String.format("-fx-font-size: 20px; -fx-cursor: hand; -fx-text-fill: %s; -fx-padding: 10px 0px 0px 0px;", StyleConstants.GREY_COLOUR));
        StackPane.setMargin(closeButton, new Insets(40, 80, 0, 0));

        moduleDisplayName.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 36px; " +
                "-fx-font-family: 'Antipasto'; -fx-padding: 40px 0px 0px 80px;", StyleConstants.GREY_COLOUR));

        moduleDisplayText.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 26px; " +
                "-fx-font-family: 'Antipasto'; -fx-padding: 10px 80px 0px 80px; -fx-wrap-text: true;", StyleConstants.GREY_COLOUR));

        // style next button
        nextButton.setStyle(String.format(
                "-fx-background-color: %s; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 34px; " +
                "-fx-font-family: 'Antipasto';" +
                "-fx-background-radius: 30px;" +
                "-fx-cursor: hand;",
                StyleConstants.BLUE_COLOUR
        ));

        // populate special character key map
        keyMap.put("COMMA", ',');
        keyMap.put("PERIOD", '.');
        keyMap.put("SLASH", '/');
        keyMap.put("SEMICOLON", ';');
        keyMap.put("QUOTE", '\'');
        keyMap.put("HASHTAG", '#');
        keyMap.put("OPEN_BRACKET", '[');
        keyMap.put("CLOSE_BRACKET", ']');
        keyMap.put("MINUS", '-');
        keyMap.put("EQUALS", '=');

    }

    public void setKeyboardInterface(KeyboardInterface keyboardInterface){
        this.keyboardInterface = keyboardInterface;
    }

    /**
     * set the Module to display
     * @param module is the Module to display
     */
    public void setModule(Module module) {
        this.module = module;
        moduleDisplayName.setText(module.getName());
        // TODO: properly move from using displayText to focus instead.
        moduleDisplayText.setText(module.getFocus());
        loadLevel();
    }

    private void loadLevel() {
        level = module.getNextUncompletedLevel();
        if (level == null) {
            // the module is completed. this shouldn't happen!! RETURN TO MENU
            onBackButtonClick();
            return;
        }

        // reset vars for next level
        charContainer.getChildren().clear();
        letterLabels.clear();
        typedString = "";
        currentIndex = 0;

        for (char c : level.getTaskString().toCharArray()) {
            StackPane letterBox = createLetterBox(c);
            charContainer.getChildren().add(letterBox);
        }

        // initially hide next button
        nextButton.setVisible(false);

        // vibrate first key
        char key = level.getTaskString().toCharArray()[0];
        keyboardInterface.sendHapticCommand(String.valueOf(key), 200, 50);

    }

    private HBox createNewRow() {
        HBox newRow = new HBox();
        newRow.setAlignment(javafx.geometry.Pos.CENTER);
        newRow.setSpacing(10);
        return newRow;
    }

    private StackPane createLetterBox(char c) {
        Rectangle box = new Rectangle(50, 50);
        box.setArcWidth(30);
        box.setArcHeight(30);
        box.setStroke(Color.web(StyleConstants.GREY_COLOUR));
        box.setStrokeWidth(4);
        box.setFill(Color.TRANSPARENT);

        // Add style class to allow CSS styling
        box.getStyleClass().add("letter-box");

        Label letter = new Label(String.valueOf(c));
        letter.setStyle(String.format("-fx-font-size: 32px; -fx-font-family: 'Manjari';"));
        letter.getStyleClass().add("letter-label");
        letterLabels.add(letter);

        StackPane container = new StackPane();
        container.getStyleClass().add("letter-container");
        container.getChildren().addAll(box, letter);
        return container;
    }

    @Override
    public void onKeypress(String key) {

        // convert special characters to the character equivalent (so "OPEN_BRACKET" to '[')
        key = (keyMap.containsKey(key)) ? convertKeyToChar(key) : key;

        if (!(currentIndex == 0 && key.equals("BACK_SPACE"))) {
            // ignore any key press except for alphanumeric or BACK_SPACE
            if (!key.matches("[a-zA-Z0-9,./;'#\\[\\]\\-=`]") && !key.equals("BACK_SPACE") && !key.equals(" ")) {
                return;
            }

            // check if BACK_SPACE , and that the level hasn't already been completed
            if (key.equals("BACK_SPACE") && currentIndex > 0 && !level.isCompleted()) {
                // move back a character, and turn the previous character grey
                currentIndex--;
                setLetterColour(currentIndex, StyleConstants.GREY_COLOUR);
                typedString = typedString.substring(0, typedString.length() - 1);

                // vibrate the next character to be pressed
                char keyToVibrate = level.getTaskString().charAt(currentIndex);
                keyboardInterface.sendHapticCommand(String.valueOf(keyToVibrate), 200, 50);

                return;
            }

            // check if all characters have already been typed
            if (currentIndex < letterLabels.size()) {
                char expectedChar = level.getTaskString().charAt(currentIndex);

                // keep track of typed string
                typedString += key;

                if (key.equalsIgnoreCase(String.valueOf(expectedChar))) {
                    // the user typed the expected character.
                    setLetterColour(currentIndex, StyleConstants.BLUE_COLOUR);
                    currentIndex++;

                    if (currentIndex < letterLabels.size()) {
                        // vibrate the next key to be pressed
                        char keyToVibrate = level.getTaskString().charAt(currentIndex);
                        keyboardInterface.sendHapticCommand(String.valueOf(keyToVibrate), 200, 50);
                    }
                } else {
                    // the user typed the wrong character.
                    setLetterColour(currentIndex, StyleConstants.RED_COLOUR);
                    currentIndex++;

                    // turn on LEDs
                    keyboardInterface.activateLights(1000);

                    // vibrate the BACK_SPACE key
                    keyboardInterface.sendHapticCommand("BACK_SPACE", 200, 50);
                }

                if (currentIndex >= letterLabels.size()) {
                    if (typedString.equalsIgnoreCase(level.getTaskString())) {
                        // level has been completed
                        level.setCompleted(true);

                        // display the NEXT button
                        nextButton.setVisible(true);
                    } else {
                        // user typed the whole string, but made a mistake.
                    }
                }
            }
        }
    }

    private String convertKeyToChar(String key) {
        return String.valueOf(keyMap.getOrDefault(key, null));
    }

    private void setLetterColour(int index, String colour) {
        Label currentLetter = letterLabels.get(index);
        StackPane currentLetterBox = (StackPane) charContainer.getChildren().get(index);
        Rectangle currentLetterRectangle = (Rectangle) currentLetterBox.getChildren().get(0);

        // Remove any existing color classes
        currentLetter.getStyleClass().removeAll("letter-label-grey", "letter-label-blue", "letter-label-red");
        currentLetterRectangle.getStyleClass().removeAll("letter-box-grey", "letter-box-blue", "letter-box-red");

        // Add appropriate color class based on the color parameter
        if (colour.equals(StyleConstants.GREY_COLOUR)) {
            currentLetter.getStyleClass().add("letter-label-grey");
            currentLetterRectangle.getStyleClass().add("letter-box-grey");
        } else if (colour.equals(StyleConstants.BLUE_COLOUR)) {
            currentLetter.getStyleClass().add("letter-label-blue");
            currentLetterRectangle.getStyleClass().add("letter-box-blue");
        } else if (colour.equals(StyleConstants.RED_COLOUR)) {
            currentLetter.getStyleClass().add("letter-label-red");
            currentLetterRectangle.getStyleClass().add("letter-box-red");
        }

        // Fallback for backward compatibility
        currentLetter.setStyle(String.format("-fx-font-size: 32px; -fx-font-family: 'Manjari'; -fx-text-fill: %s", colour));
        currentLetterRectangle.setStroke(Color.web(colour));
    }

    @FXML
    public void onNextButtonClick() {
        loadLevel();
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

            LearnViewController lvController = loader.getController();
            lvController.setKeyboardInterface(keyboardInterface);
            computerVisionInterface.closeCVProgram();

            // Apply settings to the scene
            settingsService.applySettingsToScene(scene);

            Stage stage = (Stage) gamesButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
        keyboardInterface.stopHaptic();
    }

    @FXML
    public void onGamesButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            GameViewController gVController = loader.getController();
            gVController.setKeyboardInterface(keyboardInterface);
            computerVisionInterface.closeCVProgram();

            GameViewController gameViewController = loader.getController();
            gameViewController.setKeyboardInterface(keyboardInterface);

            // Apply settings to the scene
            settingsService.applySettingsToScene(scene);

            Stage stage = (Stage) gamesButton.getScene().getWindow();
            keyboardInterface.stopHaptic();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onBackButtonClick() {
        try {
            // update the module in the TypingPlan
            TypingPlanManager manager = TypingPlanManager.getInstance();
            manager.updateModule(module);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/learn-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);

            // Apply settings to the scene
            settingsService.applySettingsToScene(scene);

            LearnViewController learnViewController = loader.getController();
            learnViewController.setKeyboardInterface(keyboardInterface);
            Stage stage = (Stage) gamesButton.getScene().getWindow();
            computerVisionInterface.closeCVProgram();
            keyboardInterface.stopHaptic();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onOptionsButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/options-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);

            // Apply settings to the scene
            settingsService.applySettingsToScene(scene);

            Stage stage = (Stage) optionsButton.getScene().getWindow();
            keyboardInterface.stopHaptic();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
