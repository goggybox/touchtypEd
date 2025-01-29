package com.example.touchtyped.model;

import java.util.List;

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
