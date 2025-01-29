package com.example.touchtyped.controller;

import com.example.touchtyped.constants.StyleConstants;
import com.example.touchtyped.model.Module;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;

public class ModuleViewController {


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

    public void initialize() {
        moduleDisplayText.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 36px; " +
                "-fx-font-family: 'Antipasto'; -fx-padding: 40px 0px 0px 80px;", StyleConstants.GREY_COLOUR));

        closeButton.setStyle(String.format("-fx-font-size: 20px; -fx-cursor: hand; -fx-text-fill: %s;" +
                " -fx-padding: 40px 80px 0px 0px", StyleConstants.GREY_COLOUR));
    }

    public void setModule(Module module) {
        moduleDisplayText.setText(module.getDisplayText());


        charContainer.getChildren().clear();

        if (!module.getLevels().isEmpty()) {
            String firstLevelText = module.getLevels().get(0).getTaskString();

            for (char c : firstLevelText.toCharArray()) {
                StackPane letterBox = createLetterBox(c);
                charContainer.getChildren().add(letterBox);
            }
        }
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

        StackPane container = new StackPane();
        container.getChildren().addAll(box, letter);
        return container;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/learn-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) gamesButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
