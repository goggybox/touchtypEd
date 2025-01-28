package com.example.touchtyped.controller;

import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.ExampleKeypressListener;
import com.example.touchtyped.model.LessonButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class LearnViewController {
    @FXML
    private Button learnButton;

    @FXML
    private ImageView gamesButton;

    @FXML
    private Button optionsButton;

    @FXML
    private GridPane buttonGrid;

    private KeyboardInterface keyboardInterface = new KeyboardInterface();

    public void initialize() {
        // attach keyboard interface to scene, when scene is available
        Platform.runLater(() -> {
            Scene scene = buttonGrid.getScene();
            if (scene != null) {
                keyboardInterface.attachToScene(scene);
                // Example keypress listener
                new ExampleKeypressListener(keyboardInterface);
            } else {
                System.err.println("Scene is not available yet.");
            }
        });

        // load font
        Font.loadFont(getClass().getResource("/fonts/AntipastoPro.ttf").toExternalForm(), 50);

        int numberOfButtons = 10;
        addButtons(numberOfButtons);
    }

    private void addButtons(int number) {
        int buttonsPerRow = 3;
        for (int i = 0; i < number; i++) {
            double completion = (double) (number - i) / number;

            // define the action to be performed when the button is clicked
            int finalI = i;
            Runnable onClickAction = () -> {
                System.out.println("Lesson " + (finalI +1) + " clicked!");
            };

            StackPane button = LessonButton.createLessonButton("Lesson "+(i+1), completion, onClickAction);

            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;

            buttonGrid.add(button, col, row);
        }
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
    public void onOptionsButtonClick() {
        System.out.println("Options button clicked!");
        // Navigate to the "Options" screen
    }
}