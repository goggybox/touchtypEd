package com.example.touchtyped.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainViewController {
    @FXML
    private Button learnButton;

    @FXML
    private ImageView gamesButton;

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