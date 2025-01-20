package com.example.touchtyped.model;

public class Level {

    private String taskString;
    private String displayText;

    public Level (String taskString, String displayText) {
        this.taskString = taskString;
        this.displayText = displayText;
    }

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
}
