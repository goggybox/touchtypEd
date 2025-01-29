package com.example.touchtyped.controller;

import com.example.touchtyped.model.Module;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ModuleViewController {

    @FXML
    private Label moduleName;

    @FXML
    private Label moduleFocus;

    @FXML
    private Label moduleLevels;

    private Stage stage;

    public void setModule(Module module) {
        moduleName.setText(module.getName());
        moduleFocus.setText("Focus: " + module.getFocus());

        StringBuilder levelsText = new StringBuilder("Levels:\n");
        module.getLevels().forEach(level -> levelsText.append("- ").append(level.getTaskString()).append("\n"));
        moduleLevels.setText(levelsText.toString());
    }

    @FXML
    public void onBackButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/learn-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) moduleName.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
