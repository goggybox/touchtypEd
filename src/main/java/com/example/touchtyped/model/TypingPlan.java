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
     * needed for JSON deserialiser
     */
    public TypingPlan() {

    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < phases.size(); i++) {
            Phase phase = phases.get(i);
            output.append("Phase ").append(i).append(": \"").append(phase.getName()).append("\"\n   Modules:\n");
            for (int j = 0; j < phase.getModules().size(); j++) {
                Module module = phase.getModules().get(j);
                output.append("      ").append(j).append(". \"").append(module.getName()).append("\"\n");
            }
            output.append("\n");
        }

        return output.toString();
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
