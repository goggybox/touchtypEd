package com.example.touchtyped.model;

import com.example.touchtyped.controller.ModuleViewController;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Module {

    private String name;
    private String focus;

    private String displayText;
    private int id;
    private List<Level> levels;

    public Module(String name, String focus, String displayText, int id, List<Level> levels) {
        this.name = name;
        this.focus = focus;
        this.displayText = displayText;
        this.id = id;
        this.levels = levels;
    }

    public Module(String name) {
        this.name = name;
    }

    /**
     * needed for JSON deserialiser
     */
    public Module() {

    }

    /**
     * display the Module on the screen as a button
     * @param buttonGrid is the GridPane to add the button to
     * @param row is the row in the GridPane to add the button to
     * @param col is the column in the GridPane to add the button is
     */
    public void display(GridPane buttonGrid, int row, int col) {
        double completion = getCompletion();

        Runnable onClickAction = () -> {
            if (completion != 1.0) {
                System.out.println(name + " button clicked.");
                try {
                    // Go to the Module View, and give it this module to display.
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/touchtyped/module-view.fxml"));
                    Scene scene = new Scene(loader.load(), 1200, 700);
                    Stage stage = (Stage) buttonGrid.getScene().getWindow();
                    ModuleViewController controller = loader.getController();

                    Platform.runLater(() -> controller.setModule(this));
                    stage.setScene(scene);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println(name + " is complete!");
            }
        };

        StackPane button = ModuleButton.createModuleButton(name, completion, onClickAction);
        buttonGrid.add(button, col, row);
    }

    /**
     * determine what percentage of levels are completed
     * @return the fraction of completed levels (like 3/4 - 0.75)
     */
    public double getCompletion() {
        int numCompletedLevels = 0;
        for (Level level : levels) {
            if (level.isCompleted()) { numCompletedLevels++; }
        }

        return (double) numCompletedLevels / (double) levels.size();
    }

    /**
     * gets the next level for the user to complete.
     * @return the next uncompleted level
     */
    public Level getNextUncompletedLevel() {
        for (int i = 0; i < levels.size(); i++) {
            if (!levels.get(i).isCompleted()) { return levels.get(i); }
        }

        // if not returned by this point, the Module is complete.
        return null;
    }


    // getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    public String getFocus() { return focus; }

    public void setFocus(String focus) { this.focus = focus; }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}
