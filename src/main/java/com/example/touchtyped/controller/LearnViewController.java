package com.example.touchtyped.controller;

import com.example.touchtyped.constants.StyleConstants;
import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.DividerLine;
import com.example.touchtyped.model.ExampleKeypressListener;
import com.example.touchtyped.model.LessonButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class LearnViewController {

    /**
     * reference to the learnButton in learn-view.fxml
     */
    @FXML
    private Button learnButton;

    /**
     * reference to the gamesButton in learn-view.fxml
     */
    @FXML
    private ImageView gamesButton;

    /**
     * reference to the optionsButton in learn-view.fxml
     */
    @FXML
    private Button optionsButton;

    /**
     * reference to the VBox in learn-view.fxml
     */
    @FXML
    private VBox vbox;

    private KeyboardInterface keyboardInterface = new KeyboardInterface();

    public void initialize() {
        // Attach keyboard interface to scene, when scene is available
        Platform.runLater(() -> {
            Scene scene = vbox.getScene(); // Use vbox's scene instead of buttonGrid's
            if (scene != null) {
                keyboardInterface.attachToScene(scene);
                // Example keypress listener
                new ExampleKeypressListener(keyboardInterface);
            } else {
                System.err.println("Scene is not available yet.");
            }
        });

        // Load font
        Font.loadFont(getClass().getResource("/fonts/AntipastoPro.ttf").toExternalForm(), 50);

        displayPhase("Phase 1: Foundations", 9);
        displayPhase("Phase 2: Your MotherARARAR", 3);
    }

    /**
     * Sets up the lesson UI, including the divider line and buttons.
     */
    private void displayPhase(String phaseName, int numButtons) {
        // create and display the divider line
        HBox divider = DividerLine.createDividerLineWithText(phaseName);
        vbox.getChildren().add(divider);

        // Create the GridPane dynamically
        GridPane buttonGrid = new GridPane();
        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.setHgap(60);
        buttonGrid.setVgap(60);
        buttonGrid.setMinWidth(5.0);
        buttonGrid.getStyleClass().add("button-grid");

        // Add the dynamically created GridPane to the VBox
        vbox.getChildren().add(buttonGrid);

        // Add buttons
        addButtons(numButtons, buttonGrid);
    }

    /**
     * Adds lesson buttons to the GridPane.
     *
     * @param number The number of buttons to add.
     */
    private void addButtons(int number, GridPane buttonGrid) {
        int buttonsPerRow = 3;
        for (int i = 0; i < number; i++) {
            double completion = (double) (number - i) / number;

            // Define the action to be performed when the button is clicked
            int finalI = i;
            Runnable onClickAction = () -> {
                System.out.println("Lesson " + (finalI + 1) + " clicked!");
            };

            StackPane button = LessonButton.createLessonButton("Lesson " + (i + 1), completion, onClickAction);

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