package com.example.touchtyped.controller;

import com.example.touchtyped.model.ExampleKeypressListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class MainViewController {
    @FXML
    private Button learnButton;

    @FXML
    private Button gamesButton;

    @FXML
    private Button optionsButton;

    @FXML
    private GridPane buttonGrid;

    public void initialize() {
        int numberOfButtons = 10;
        addButtons(numberOfButtons);
    }

    private void addButtons(int number) {
        int buttonsPerRow = 3;
        for (int i = 0; i < number; i++) {
            Button button = new Button("Button "+(i+1));
            button.setOnAction(event -> System.out.println(button.getText() + " clicked!"));
            button.setPrefSize(130, 150);

            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;

            buttonGrid.add(button, col, row);
        }
    }

    @FXML
    public void onLearnButtonClick() {
        System.out.println("Learn button clicked!");
        // Navigate to the "Learn" screen
    }

    @FXML
    public void onGamesButtonClick() {
        System.out.println("Games button clicked!");
        // Navigate to the "Games" screen
    }

    @FXML
    public void onOptionsButtonClick() {
        System.out.println("Options button clicked!");
        // Navigate to the "Options" screen
    }
}