package com.example.touchtyped.model;

import com.example.touchtyped.serialisers.TypingPlanDeserialiser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * this singleton provides a place to store the TypingPlan and access it globally
 */
public class TypingPlanManager {

    private static TypingPlanManager instance;

    private static final String SAVED_TYPING_PLAN_FILE = "saved_typing_plan.json";
    private boolean typingPlanModified = false;

    private boolean personalisedPlanExists = true;
    private boolean displayingPersonalisedPlan = true;
    private TypingPlan personalisedPlan = null;
    private TypingPlan defaultPlan = null;


    /**
     * private constructor to prevent instantiation
     */
    private TypingPlanManager() {
        defaultPlan = TypingPlanDeserialiser.getTypingPlan();

        File saveFile = new File(SAVED_TYPING_PLAN_FILE);
        if (saveFile.exists()) {
            // if the TypingPlan has been saved, retrieve it.
            personalisedPlan = loadTypingPlan();
            if (personalisedPlan == null) {
                // Fallback to default TypingPlan if loading fails
                System.out.println("Failed to load TypingPlan from save file. Falling back to default.");
                personalisedPlanExists = false;
                displayingPersonalisedPlan = false;
            }
        } else {
            // load default Typing Plan (currently JSON file)
            System.out.println("Failed to load TypingPlan from save file. Falling back to default.");
            personalisedPlanExists = false;
            displayingPersonalisedPlan = false;
        }
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

    public boolean personalisedPlanExists() {
        return personalisedPlanExists;
    }

    public boolean isDisplayingPersonalisedPlan() {
        return displayingPersonalisedPlan;
    }

    public void toggleTypingPlan() {
        displayingPersonalisedPlan = !displayingPersonalisedPlan;

    }


    /**
     * getters and setters for the TypingPlan
     */


    public TypingPlan getTypingPlan() {
        if (displayingPersonalisedPlan) {
            return personalisedPlan;
        } else {
            return defaultPlan;
        }
    }

    public void setTypingPlan(TypingPlan typingPlan) {
        this.personalisedPlan = typingPlan;
        typingPlanModified = true;
        displayingPersonalisedPlan = true;
        personalisedPlanExists = true;
    }

    public boolean getModified() {
        return typingPlanModified;
    }

    /**
     * update a module in the TypingPlan with a provided Module
     * @param module is the module to update
     * @return whether successfully updated
     */
    public boolean updateModule(Module module) {
        typingPlanModified = true;
        if (displayingPersonalisedPlan) {
            return personalisedPlan.updateModule(module);
        } else {
            return defaultPlan.updateModule(module);
        }
    }

    public void saveTypingPlan() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(SAVED_TYPING_PLAN_FILE), personalisedPlan);
            System.out.println("TypingPlan has been saved.");
            personalisedPlanExists = true;
        } catch (IOException e) {
            System.out.println("Error while saving TypingPlan: " + e.getMessage());
        }
    }

    private TypingPlan loadTypingPlan() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypingPlan loadedPlan = objectMapper.readValue(new File(SAVED_TYPING_PLAN_FILE), TypingPlan.class);
            System.out.println("TypingPlan loaded from save file.");
            return loadedPlan;
        } catch (IOException e) {
            System.out.println("Error while loading TypingPlan from file: " + e.getMessage());
            return null;
        }
    }

}
