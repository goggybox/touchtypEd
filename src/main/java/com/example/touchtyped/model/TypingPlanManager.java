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
    private static final String SAVED_DEFAULT_PLAN = "saved_default_plan.json";
    private boolean personalisedPlanModified = false;
    private boolean defaultPlanModified = false;

    private boolean personalisedPlanExists = true;
    private boolean displayingPersonalisedPlan = true;
    private TypingPlan personalisedPlan = null;
    private TypingPlan defaultPlan = null;


    /**
     * private constructor to prevent instantiation
     */
    private TypingPlanManager() {
        File defaultFile = new File(SAVED_DEFAULT_PLAN);
        if (defaultFile.exists()) {
            // if the typingplan has been saved, retrieve it.
            defaultPlan = loadDefaultPlan();
            if (defaultPlan == null) {
                System.out.println("Failed to load saved default plan. Falling back to clean default.");
                defaultPlan = TypingPlanDeserialiser.getTypingPlan();
            }
        } else {
            // load clean default typing plan
            defaultPlan = TypingPlanDeserialiser.getTypingPlan();
            System.out.println("Loaded clean default plan.");
        }

        File personalisedFile = new File(SAVED_TYPING_PLAN_FILE);
        if (personalisedFile.exists()) {
            // if the TypingPlan has been saved, retrieve it.
            personalisedPlan = loadPersonalisedPlan();
            if (personalisedPlan == null) {
                // Fallback to default TypingPlan if loading fails
                System.out.println("Failed to load TypingPlan from save file. Falling back to default.");
                personalisedPlanExists = false;
                displayingPersonalisedPlan = false;
            }
        } else {
            // load default Typing Plan
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
        personalisedPlanModified = true;
        displayingPersonalisedPlan = true;
        personalisedPlanExists = true;
    }

    public boolean getModified() {
        if (displayingPersonalisedPlan) {
            return personalisedPlanModified;
        } else {
            return defaultPlanModified;
        }
    }

    /**
     * update a module in the TypingPlan with a provided Module
     * @param module is the module to update
     * @return whether successfully updated
     */
    public boolean updateModule(Module module) {
        if (displayingPersonalisedPlan) {
            personalisedPlanModified = true;
            return personalisedPlan.updateModule(module);
        } else {
            defaultPlanModified = true;
            return defaultPlan.updateModule(module);
        }
    }

    public void saveTypingPlan() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(SAVED_TYPING_PLAN_FILE), personalisedPlan);
            System.out.println("Personalised typing plan has been saved.");
            personalisedPlanExists = true;

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(SAVED_DEFAULT_PLAN), defaultPlan);
            System.out.println("Default typing plan has been saved.");

        } catch (IOException e) {
            System.out.println("Error while saving TypingPlan: " + e.getMessage());
        }
    }

    private TypingPlan loadPersonalisedPlan() {
        File saveFile = new File(SAVED_TYPING_PLAN_FILE);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypingPlan loadedPlan = objectMapper.readValue(new File(SAVED_TYPING_PLAN_FILE), TypingPlan.class);
            System.out.println("Personalised typing plan loaded from save file.");
            return loadedPlan;
        } catch (IOException e) {
            System.out.println("Error while loading TypingPlan from file: " + e.getMessage());
            // Delete damaged file
            if (saveFile.exists()) {
                boolean deleted = saveFile.delete();
                if (deleted) {
                    System.out.println("Deleted corrupted typing plan file.");
                } else {
                    System.out.println("Failed to delete corrupted typing plan file.");
                }
            }
            return null;
        }
    }

    private TypingPlan loadDefaultPlan() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypingPlan loadedPlan = objectMapper.readValue(new File(SAVED_DEFAULT_PLAN), TypingPlan.class);
            System.out.println("Default typing plan loaded from save file.");
            return loadedPlan;
        } catch (IOException e) {
            System.out.println("ERROR while loading saved default typing plan.");
            return null;
        }
    }

}
