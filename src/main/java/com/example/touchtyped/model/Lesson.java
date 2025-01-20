package com.example.touchtyped.model;

import java.util.List;

public class Lesson {

    private String name;
    private int id;
    private List<Level> levels;

    public Lesson(String name, List<Level> levels) {
        this.name = name;
        this.levels = levels;
    }

    public Lesson(String name) {
        this.name = name;
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
}
