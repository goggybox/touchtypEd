package com.example.touchtyped.model;

public class Level {

    private String taskString;
    private String displayText;

    private boolean completed = false;

    public Level (String taskString, String displayText, boolean completed) {
        this.taskString = taskString;
        this.displayText = displayText;
        this.completed = completed;
    }

    /**
     * needed for JSON deserialiser
     */
    public Level () {
    }


    // GETTERS AND SETTERS

    public String getTaskString() {
        return taskString;
    }

    public void setTaskString(String taskString) {
        this.taskString = taskString;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public boolean isCompleted() { return completed; }

    public void setCompleted(boolean completed) { this.completed = completed; }
}
