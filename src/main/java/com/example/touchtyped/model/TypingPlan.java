package com.example.touchtyped.model;

import javafx.scene.layout.VBox;

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
     * replace a module within the TypingPlan with the provided Module. USES THE MODULE ID
     * @param module is the module to update
     * @return whether successfully replaced
     */
    public boolean updateModule(Module module) {
        // try each Phase.
        for (Phase phase : phases) {
            if (phase.updateModule(module)) {
                System.out.println("UPDATED THE MODULE in "+phase.getName());
                return true;
            }
        }

        // the provided Module doesn't exist in the TypingPlan.
        return false;
    }

    /**
     * display the TypingPlan on screen
     * @param vbox is the VBox component to display the TypingPlan in
     */
    public void display(VBox vbox) {
        for (Phase phase : phases) {
            phase.display(vbox);
        }
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
