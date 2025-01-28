package com.example.touchtyped.model;

import java.util.List;

/**
 * this class represents a Phase in the personalised typing plan, and contains a list of Modules.
 */
public class Phase {

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
