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

    private TypingPlan typingPlan;

    private static final String SAVED_TYPING_PLAN_FILE = "saved_typing_plan.json";
    private boolean typingPlanModified = false;


    /**
     * private constructor to prevent instantiation
     */
    private TypingPlanManager() {
        this.typingPlan = TypingPlanDeserialiser.getTypingPlan();

        File saveFile = new File(SAVED_TYPING_PLAN_FILE);
        if (saveFile.exists()) {
            // if the TypingPlan has been saved, retrieve it.
            typingPlan = loadTypingPlan();
            if (typingPlan == null) {
                // Fallback to default TypingPlan if loading fails
                System.out.println("Failed to load TypingPlan from save file. Falling back to default.");
                typingPlan = TypingPlanDeserialiser.getTypingPlan();
            }
        } else {
            // load TypingPlan from service (currently JSON file)
            // TODO: implement getting TypingPlan from REST service.
            System.out.println("TypingPlan loaded from REST service.");
            typingPlan = TypingPlanDeserialiser.getTypingPlan();
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


    /**
     * getters and setters for the TypingPlan
     */


    public TypingPlan getTypingPlan() {
        return typingPlan;
    }

    public void setTypingPlan(TypingPlan typingPlan) {
        this.typingPlan = typingPlan;
        typingPlanModified = true;
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
        return typingPlan.updateModule(module);
    }

    public void saveTypingPlan() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(SAVED_TYPING_PLAN_FILE), typingPlan);
            System.out.println("TypingPlan has been saved.");
        } catch (IOException e) {
            System.out.println("Error while saving TypingPlan: " + e.getMessage());
        }
    }

    private TypingPlan loadTypingPlan() {
        File saveFile = new File(SAVED_TYPING_PLAN_FILE);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypingPlan loadedPlan = objectMapper.readValue(saveFile, TypingPlan.class);
            System.out.println("TypingPlan loaded from save file.");
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

}
