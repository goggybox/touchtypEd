package com.example.touchtyped.controller;

import com.example.touchtyped.constants.StyleConstants;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.interfaces.KeypressListener;
import com.example.touchtyped.model.ExampleKeypressListener;
import com.example.touchtyped.model.Level;
import com.example.touchtyped.model.Module;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * displays the Module that was clicked on in the Learn view.
 */
public class ModuleViewController implements KeypressListener {


    @FXML
    private HBox charContainer;

    @FXML
    private Label moduleDisplayText;

    /**
     * reference to the gamesButton in learn-view.fxml
     */
    @FXML
    private ImageView gamesButton;

    @FXML
    private Region spacer;

    @FXML
    private Label closeButton;

    @FXML
    private Button nextButton;

    // vars
    private Module module;
    private Level level;
    private List<Label> letterLabels = new ArrayList<>();
    private int currentIndex;
    private KeyboardInterface keyboardInterface = new KeyboardInterface();

    public void initialize() {
        // Attach keyboard interface to scene, when scene is available
        Platform.runLater(() -> {
            Scene scene = closeButton.getScene();
            if (scene != null) {
                keyboardInterface.attachToScene(scene);
            } else {
                System.err.println("Scene is not available yet.");
            }
        });

        // register as a keypress listener
        keyboardInterface.addKeypressListener(this);

        closeButton.setStyle(String.format("-fx-font-size: 20px; -fx-cursor: hand; -fx-text-fill: %s;", StyleConstants.GREY_COLOUR));
        StackPane.setMargin(closeButton, new Insets(40, 80, 0, 0));

    }

    /**
     * set the Module to display
     * @param module is the Module to display
     */
    public void setModule(Module module) {
        this.module = module;
        moduleDisplayText.setText(module.getDisplayText());
        loadLevel();
    }

    private void loadLevel() {
        level = module.getNextUncompletedLevel();
        if (level == null) {
            // the module is completed. this shouldn't happen!! RETURN TO MENU
            onBackButtonClick();
            return;
        }

        charContainer.getChildren().clear();
        letterLabels.clear();
        currentIndex = 0;

        for (char c : level.getTaskString().toCharArray()) {
            StackPane letterBox = createLetterBox(c);
            charContainer.getChildren().add(letterBox);
        }

        // initially hide next button
        nextButton.setVisible(false);
    }

    private StackPane createLetterBox(char c) {
        Rectangle box = new Rectangle(50, 50);
        box.setArcWidth(30);
        box.setArcHeight(30);
        box.setStroke(Color.web(StyleConstants.GREY_COLOUR));
        box.setStrokeWidth(4);
        box.setFill(Color.TRANSPARENT);

        Label letter = new Label(String.valueOf(c));
        letter.setStyle(String.format("-fx-font-size: 32px; -fx-font-family: 'Manjari'; -fx-text-fill: %s", StyleConstants.GREY_COLOUR));
        letterLabels.add(letter);

        StackPane container = new StackPane();
        container.getChildren().addAll(box, letter);
        return container;
    }

    @Override
    public void onKeypress(String key) {
        System.out.println(key);
        Label currentLetter = letterLabels.get(currentIndex);
        StackPane currentLetterBox = (StackPane) charContainer.getChildren().get(currentIndex);
        Rectangle currentLetterRectangle = (Rectangle) currentLetterBox.getChildren().get(0);
        char expectedChar = level.getTaskString().charAt(currentIndex);

        if (key.equalsIgnoreCase(String.valueOf(expectedChar))) {
            // the user typed the expected character.
            currentLetter.setStyle(String.format("-fx-font-size: 32px; -fx-font-family: 'Manjari'; -fx-text-fill: %s", StyleConstants.BLUE_COLOUR));
            currentLetterRectangle.setStroke(Color.web(StyleConstants.BLUE_COLOUR));
            currentIndex++;
        } else {
            // the user typed the wrong character.
            currentLetter.setStyle(String.format("-fx-font-size: 32px; -fx-font-family: 'Manjari'; -fx-text-fill: %s", StyleConstants.RED_COLOUR));
            currentLetterRectangle.setStroke(Color.web(StyleConstants.RED_COLOUR));
            currentIndex++;
        }

        if (currentIndex >= letterLabels.size()) {
            level.setCompleted(true);

            // display the NEXT button
            nextButton.setVisible(true);
        }
    }

    @FXML
    public void onNextButtonClick() {
        loadLevel();
    }

    @FXML
    public void onGamesButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/game-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) gamesButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onBackButtonClick() {
        try {

            // update the Module in the module list.

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/learn-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) gamesButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
