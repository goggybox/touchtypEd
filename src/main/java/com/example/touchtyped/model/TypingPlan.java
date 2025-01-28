package com.example.touchtyped.model;

import java.util.List;

/**
 * this class represents a personalised typing plan and contains multiple Phases, each of which contains multiple Modules.
 */
public class TypingPlan {

    // TODO: add PDF?

    private List<Phase> phases;

    public TypingPlan(List<Phase> phases) {
        this.phases = phases;
    }


    /**
     * Getters and setters
     */

    public void setPhases(List<Phase> phases) {
        this.phases = phases;
    }

    public List<Phase> getPhases() {
        return phases;
    }

}
