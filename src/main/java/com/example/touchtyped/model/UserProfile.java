package com.example.touchtyped.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * User profile, stores user information
 */
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String PROFILE_FILE = "user_profile.dat";

    private static UserProfile instance;
    
    private String playerName;
    private Boolean completedTutorial = false;

    // Private constructor to prevent external instantiation
    private UserProfile() {
        playerName = "";
    }
    
    /**
     * Get singleton instance
     * @return UserProfile instance
     */
    public static synchronized UserProfile getInstance() {
        if (instance == null) {
            instance = loadProfile();
        }
        return instance;
    }
    
    /**
     * Load user configuration
     * @return Loaded UserProfile instance, if loading fails return new instance
     */
    private static UserProfile loadProfile() {
        File profileFile = new File(PROFILE_FILE);

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(profileFile))) {
            Object obj = ois.readObject();
            if (obj instanceof UserProfile) {
                return (UserProfile) obj;
            } else {
                System.err.println("File format is incorrect, contains non-UserProfile object: " + obj.getClass().getName());
                // Delete damaged file
                if (profileFile.exists()) {
                    profileFile.delete();
                    System.out.println("Damaged configuration file deleted");
                }
                return new UserProfile();
            }
        } catch (FileNotFoundException e) {
            System.out.println("User configuration file does not exist, creating new configuration");
            return new UserProfile();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.err.println("Error loading user configuration: " + e.getMessage());
            // Delete damaged file
            if (profileFile.exists()) {
                profileFile.delete();
                System.out.println("Damaged configuration file deleted");
            }
            return new UserProfile();
        }
    }

    /**
     * Save user configuration
     */
    public void saveProfile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PROFILE_FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("Error saving user configuration: " + e.getMessage());
        }
    }

    /**
     * Get player name
     * @return Player name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Set player name
     * @param playerName Player name
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        saveProfile();
    }
    
    /**
     * Check if there is already a player name
     * @return true if there is already a player name, false otherwise
     */
    public boolean hasPlayerName() {
        return playerName != null && !playerName.trim().isEmpty();
    }

    /**
     * Get tutorial completion status
     * @return Whether the tutorial is completed
     */
    public Boolean getCompletedTutorial() {
        return completedTutorial;
    }

    /**
     * Set tutorial completion status
     * @param completedTutorial Tutorial completion status
     */
    public void setCompletedTutorial(Boolean completedTutorial) {
        this.completedTutorial = completedTutorial;
        saveProfile();
    }

    /**
     * Check if it's the first time using (has a username but hasn't completed the tutorial)
     * @return true if it's the first time using, false otherwise
     */
    public boolean isFirstTimeUser() {
        return playerName != null && !playerName.isEmpty() && !Boolean.TRUE.equals(completedTutorial);
    }
} 