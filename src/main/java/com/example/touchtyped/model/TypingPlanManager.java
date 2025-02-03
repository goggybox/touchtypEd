package com.example.touchtyped.model;

import com.example.touchtyped.serialisers.TypingPlanDeserialiser;

/**
 * this singleton provides a place to store the TypingPlan and access it globally
 */
public class TypingPlanManager {

    private static TypingPlanManager instance;

    private TypingPlan typingPlan;

    /**
     * private constructor to prevent instantiation
     */
    private TypingPlanManager() {
        this.typingPlan = TypingPlanDeserialiser.getTypingPlan();
    }

    /**
     * this method gets the singleton instances
     * @return the singleton instance
     */
    public static TypingPlanManager getInstance() {
        if (instance == null) {
            instance = new TypingPlanManager();
        }
        return instance;
    }


    /**
     * getters and setters for the TypingPlan
     */


    public TypingPlan getTypingPlan() {
        return typingPlan;
    }

    public void setTypingPlan(TypingPlan typingPlan) {
        this.typingPlan = typingPlan;
    }

    /**
     * update a module in the TypingPlan with a provided Module
     * @param module is the module to update
     * @return whether successfully updated
     */
    public boolean updateModule(Module module) {
        return typingPlan.updateModule(module);
    }

}
