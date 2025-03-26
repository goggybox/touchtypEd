package com.example.touchtyped.model;

import com.example.touchtyped.firestore.ClassroomDAO;
import com.example.touchtyped.firestore.UserAccount;
import com.example.touchtyped.firestore.UserDAO;
import com.example.touchtyped.serialisers.TypingPlanDeserialiser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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

        defaultPlan = loadDefaultPlan();
        personalisedPlan = loadPersonalisedPlan();

        if (personalisedPlan != null) {
            personalisedPlanExists = true;
            displayingPersonalisedPlan = true;
        } else {
            personalisedPlanExists = false;
            displayingPersonalisedPlan = false;
        }

    }

    public void updatePlans() {
        System.out.println("Updating plans...");
        defaultPlan = loadDefaultPlan();
        personalisedPlan = loadPersonalisedPlan();
    }

    public TypingPlan loadDefaultPlan() {
        Map<String, String> userCache = ClassroomDAO.loadUserCache();
        if (userCache != null) {
            // the user is logged in, try to load the default plan from the database.
            String classroomID = userCache.get("classroomID");
            String username = userCache.get("username");
            String password = userCache.getOrDefault("password", null);

            try {
                TypingPlan loadedDefault = UserDAO.getDefaultTypingPlan(classroomID, username, password);
                System.out.println("LOADED DEFAULT: "+loadedDefault);

                if (loadedDefault == null) {
                    // there is no default plan in the database, load from local file instead.
                    return loadDefaultPlanFromFile();
                } else {
                    // successfully loaded the default plan.
                    System.out.println("Successfully loaded default plan from database.");
                    return loadedDefault;
                }

            } catch (Exception e) {
                e.printStackTrace();
                // something went wrong while loading from database, load locally instead.
                return loadDefaultPlanFromFile();
            }

        } else {
            // the user is not logged in, load default plan from local file, or create from scratch.
            return loadDefaultPlanFromFile();
        }
    }

    public TypingPlan loadPersonalisedPlan() {
        Map<String, String> userCache = ClassroomDAO.loadUserCache();
        if (userCache != null) {
            // the user is logged in, try to load the personalised plan from the database.
            String classroomID = userCache.get("classroomID");
            String username = userCache.get("username");
            String password = userCache.getOrDefault("password", null);

            try {
                TypingPlan loadedPersonalised = UserDAO.getPersonalisedTypingPlan(classroomID, username, password);

                if (loadedPersonalised == null) {
                    // there is no personalised plan in the database, load from local file instead.
                    return loadPersonalisedPlanFromFile();
                } else {
                    // successfully loaded the personalised plan from database.
                    System.out.println("Successfully loaded personalised plan from database.");
                    personalisedPlanExists = true;
                    displayingPersonalisedPlan = true;
                    return loadedPersonalised;
                }
            } catch (Exception e) {
                e.printStackTrace();
                // something went wrong while loading from the database, load locally instead.
                return loadPersonalisedPlanFromFile();
            }
        } else {
            // the user is not logged in, load personalised plan from local file, otherwise return null.
            return loadPersonalisedPlanFromFile();
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

    public TypingPlan getPersonalisedPlan() {
        System.out.println("Gotten personalised plan from manager");
        return personalisedPlan;
    }

    public TypingPlan getDefaultPlan() {
        System.out.println("Gotten default plan from manager");
        return defaultPlan;
    }

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

    public void clearTypingPlans() {
        personalisedPlan = null;
        defaultPlan = TypingPlanDeserialiser.getCleanDefaultTypingPlan();
        personalisedPlanExists = false;
        displayingPersonalisedPlan = false;
        personalisedPlanModified = false;
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
            // save plans to database, if the user is logged in
            Map<String, String> userCache = ClassroomDAO.loadUserCache();
            if (userCache != null) {
                String classroomID = userCache.get("classroomID");
                String username = userCache.get("username");
                String password = userCache.getOrDefault("password", null);
                try {
                    UserDAO.updatePersonalisedTypingPlan(classroomID, username, personalisedPlan, password);
                    UserDAO.updateDefaultTypingPlan(classroomID, username, defaultPlan, password);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("User not logged in; typing plans not saved to database.");
                // save plans to local files.
                ObjectMapper objectMapper = new ObjectMapper();
                if (personalisedPlan != null) {
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(SAVED_TYPING_PLAN_FILE), personalisedPlan);
                    System.out.println("Personalised typing plan has been saved.");
                    personalisedPlanExists = true;
                }

                if (defaultPlan != null) {
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(SAVED_DEFAULT_PLAN), defaultPlan);
                    System.out.println("Default typing plan has been saved.");
                }
            }

        } catch (IOException e) {
            System.out.println("Error while saving TypingPlan: " + e.getMessage());
        }
    }

    private TypingPlan loadPersonalisedPlanFromFile() {
        File saveFile = new File(SAVED_TYPING_PLAN_FILE);
        if (saveFile.exists()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                TypingPlan loadedPlan = objectMapper.readValue(new File(SAVED_TYPING_PLAN_FILE), TypingPlan.class);
                System.out.println("Successfully loaded personalised typing plan from local file.");
                personalisedPlanExists = true;
                displayingPersonalisedPlan = true;
                return loadedPlan;
            } catch (IOException e) {
                System.out.println("ERROR while loading saved personalised typing plan from local file.");
                // Delete damaged file
                if (saveFile.exists()) {
                    boolean deleted = saveFile.delete();
                    if (deleted) {
                        System.out.println("Deleted corrupted typing plan file.");
                    } else {
                        System.out.println("Failed to delete corrupted typing plan file.");
                    }
                }
                // failed to load personalised typing plan, return null instead.
                return null;
            }
        } else {
            // no saved personalised typing plan exists. return null.
            return null;
        }
    }

    private TypingPlan loadDefaultPlanFromFile() {
        File defaultFile = new File(SAVED_DEFAULT_PLAN);
        if (defaultFile.exists()) {
            // a default typing plan exists in a local file. load that and return.
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                TypingPlan loadedPlan = objectMapper.readValue(new File(SAVED_DEFAULT_PLAN), TypingPlan.class);
                System.out.println("Successfully loaded default typing plan from local file.");
                return loadedPlan;
            } catch (IOException e) {
                System.out.println("ERROR while loading saved default typing plan from local file.");
                // delete corrupted file
                if (defaultFile.exists()) {
                    boolean deleted = defaultFile.delete();
                    if (deleted) {
                        System.out.println("Deleted corrupted default typing plan file.");
                    } else {
                        System.out.println("Failed to delete corrupted default typing plan file.");
                    }
                }
                System.out.println("Failed to load default typing plan from local file. Creating a new one...");
                return TypingPlanDeserialiser.getCleanDefaultTypingPlan();
            }
        } else {
            // no default plan exists. create and return a new one.
            System.out.println("No saved default plan exists. Creating a new one...");
            TypingPlan clean = TypingPlanDeserialiser.getCleanDefaultTypingPlan();
            System.out.println(clean);
            return clean;
        }
    }

    public void deleteLocalFiles() {
        File defaultFile = new File(SAVED_DEFAULT_PLAN);
        File personalisedFile = new File(SAVED_TYPING_PLAN_FILE);

        if (defaultFile.exists()) {
            boolean deleted = defaultFile.delete();
            if (deleted) {
                System.out.println("Deleted default typing plan file.");
            }
        }

        if (personalisedFile.exists()) {
            boolean deleted = personalisedFile.delete();
            if (deleted) {
                System.out.println("Deleted personalised typing plan file.");
            }
        }
    }

}
