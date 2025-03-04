package com.example.touchtyped.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * this class represents a Phase in the personalised typing plan, and contains a list of Modules.
 */
public class Phase {

    @JsonProperty("phase")
    private String name;
    private String duration;
    private String goal;
    private List<Module> modules;

    public Phase (String name, String duration, String goal, List<Module> modules) {
        this.name = name;
        this.duration = duration;
        this.goal = goal;
        this.modules = modules;
    }

    /**
     * needed for JSON deserialiser
     */
    public Phase() {

    }

    /**
     * replace a module with the provided Module. USES THE MODULE ID
     * @param module is the module to update
     * @return whether successfully replaced
     */
    public boolean updateModule(Module module) {
        // search through the modules list for matching ID
        int id = module.getId();
        for (int i = 0; i < modules.size(); i++) {
            if (modules.get(i).getId() == id) {
                modules.set(i, module);
                return true;
            }
        }

        // the provided Module doesn't exist in this Phase.
        return false;
    }

    /**
     * display the Phase on screen
     * @param vbox is the VBox component to display the phase in
     */
    public void display(VBox vbox) {
        // create and display the divider line
        HBox divider = DividerLine.createDividerLineWithText(name);
        vbox.getChildren().add(divider);

        // create the GridPane for the buttons
        GridPane buttonGrid = new GridPane();
        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.setHgap(0);
        buttonGrid.setVgap(60);
        buttonGrid.setMinWidth(5.0);
        buttonGrid.getStyleClass().add("button-grid");

        // add the GridPane to the VBox
        vbox.getChildren().add(buttonGrid);

        // add buttons for the modules
        int numButtons = modules.size();
        int buttonsPerRow = 3;
        for (int i = 0; i < numButtons; i++) {
            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;

            modules.get(i).display(buttonGrid, row, col);
        }

    }

    /**
     * Setters and getters
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

}
