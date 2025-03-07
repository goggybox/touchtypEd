package com.example.touchtyped.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Singleton class for managing user profile
 */
public class UserProfile {
    private static final String PROFILE_FILE = "user_profile.dat";
    private static UserProfile instance;
    
    private String playerName;
    
    private UserProfile() {
        loadProfile();
    }
    
    public static synchronized UserProfile getInstance() {
        if (instance == null) {
            instance = new UserProfile();
        }
        return instance;
    }
    
    /**
     * Get the player's name
     * @return The player's name, or null if not set
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Set the player's name
     * @param playerName The player's name
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        saveProfile();
    }
    
    /**
     * Check if the player name has been set
     * @return true if the player name has been set, false otherwise
     */
    public boolean hasPlayerName() {
        return playerName != null && !playerName.trim().isEmpty();
    }
    
    /**
     * Load user profile from file
     */
    private void loadProfile() {
        try {
            if (Files.exists(Paths.get(PROFILE_FILE))) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PROFILE_FILE))) {
                    playerName = (String) ois.readObject();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading user profile: " + e.getMessage());
            playerName = null;
        }
    }
    
    /**
     * Save user profile to file
     */
    private void saveProfile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PROFILE_FILE))) {
            oos.writeObject(playerName);
        } catch (IOException e) {
            System.err.println("Error saving user profile: " + e.getMessage());
        }
    }
} 