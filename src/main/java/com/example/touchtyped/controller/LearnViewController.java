package com.example.touchtyped.controller;

import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.model.*;
import com.example.touchtyped.model.Module;
import com.example.touchtyped.serialisers.TypingPlanDeserialiser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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

        // displayPhase("Phase 1: Foundations", 9);
        TypingPlan typingPlan = TypingPlanDeserialiser.getTypingPlan();
        displayTypingPlan(typingPlan);
        HBox divider = DividerLine.createDividerLineWithText("");
        vbox.getChildren().add(divider);
    }

    /**
     * sets up the typing plan UI
     * @param typingPlan is the TypingPlan to display
     */
    private void displayTypingPlan(TypingPlan typingPlan) {
        for (Phase phase : typingPlan.getPhases()) {
            displayPhase(phase);
        }
    }

    private void displayPhase(Phase phase) {
        String phaseName = phase.getName();
        List<Module> modules = phase.getModules();

        // create and display the divider line
        HBox divider = DividerLine.createDividerLineWithText(phaseName);
        vbox.getChildren().add(divider);

        // create the GridPane for the buttons
        GridPane buttonGrid = new GridPane();
        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.setHgap(60);
        buttonGrid.setVgap(60);
        buttonGrid.setMinWidth(5.0);
        buttonGrid.getStyleClass().add("button-grid");

        // add the GridPane to the VBox
        vbox.getChildren().add(buttonGrid);

        // add buttons for the modules
        addModuleButtons(modules, buttonGrid);

    }

    private void addModuleButtons(List<Module> modules, GridPane buttonGrid) {

        int numButtons = modules.size();
        int buttonsPerRow = 3;
        for (int i = 0; i < numButtons; i++) {
            Module module = modules.get(i);
            double completion = module.getCompletion();

            // TODO: implement clickable action better
            Runnable onClickAction = () -> {
                System.out.println(module.getName() + " button clicked.");
            };

            StackPane button = ModuleButton.createModuleButton(module.getName(), completion, onClickAction);

            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;

            buttonGrid.add(button, col, row);

        }

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

        // TEST TYPINGPLAN PARSER
        TypingPlan typingPlan = TypingPlanDeserialiser.getTypingPlan();
        if (typingPlan != null) {
            System.out.println("Successfully parsed TypingPlan.");
        } else {
            System.out.println("Failed to parse TypingPlan.");
        }

        System.out.println(typingPlan.toString());
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

            StackPane button = ModuleButton.createModuleButton("Lesson " + (i + 1), completion, onClickAction);

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