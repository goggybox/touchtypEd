package com.example.touchtyped.service;

import com.example.touchtyped.model.PlayerRanking;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Service for managing player rankings
 */
public class RankingService {
    private static final String RANKINGS_FILE = "player_rankings.dat";
    private static final int MAX_RANKINGS = 100;
    private static RankingService instance;
    
    private List<PlayerRanking> rankings;
    private Map<String, PlayerRanking> rankingMap; // HashMap for quickly finding player rankings
    
    private RankingService() {
        rankings = loadRankings();
        // Initialize the HashMap and populate with existing rankings
        rankingMap = new HashMap<>();
        for (PlayerRanking ranking : rankings) {
            rankingMap.put(ranking.getPlayerName(), ranking);
        }
    }
    
    public static synchronized RankingService getInstance() {
        if (instance == null) {
            instance = new RankingService();
        }
        return instance;
    }
    
    /**
     * Add a new player ranking
     * If a ranking with the same name already exists, only keep the better one
     * @param ranking The player ranking to add
     * @return true if the ranking was added to the top 100, false otherwise
     */
    public boolean addRanking(PlayerRanking ranking) {
        // Use HashMap to quickly find if a ranking with the same name already exists
        PlayerRanking existingRanking = rankingMap.get(ranking.getPlayerName());
        
        if (existingRanking != null) {
            // If a ranking with the same name exists, compare the old and new rankings
            if (ranking.compareTo(existingRanking) < 0) {
                // If the new ranking is better, remove the old one
                rankings.remove(existingRanking);
                System.out.println("Removed old ranking: " + existingRanking.getPlayerName() + 
                                  " (WPM: " + existingRanking.getWpm() + 
                                  ", Accuracy: " + existingRanking.getAccuracy() + "%)");
                
                // Add the new ranking
                rankings.add(ranking);
                rankingMap.put(ranking.getPlayerName(), ranking);
                System.out.println("Added better new ranking: " + ranking.getPlayerName() + 
                                  " (WPM: " + ranking.getWpm() + 
                                  ", Accuracy: " + ranking.getAccuracy() + "%)");
            } else {
                // If the old ranking is better, don't add the new one
                System.out.println("Keeping existing better ranking: " + existingRanking.getPlayerName() + 
                                  " (WPM: " + existingRanking.getWpm() + 
                                  ", Accuracy: " + existingRanking.getAccuracy() + "%)");
                return false;
            }
        } else {
            // If no ranking with the same name exists, add the new ranking directly
            rankings.add(ranking);
            rankingMap.put(ranking.getPlayerName(), ranking);
            System.out.println("Added new ranking: " + ranking.getPlayerName() + 
                              " (WPM: " + ranking.getWpm() + 
                              ", Accuracy: " + ranking.getAccuracy() + "%)");
        }
        
        // Sort rankings
        Collections.sort(rankings);
        
        // Keep only the top MAX_RANKINGS
        if (rankings.size() > MAX_RANKINGS) {
            // Get the top MAX_RANKINGS to keep
            List<PlayerRanking> topRankings = new ArrayList<>(rankings.subList(0, MAX_RANKINGS));
            
            // Update rankingMap, remove rankings not in the top MAX_RANKINGS
            rankingMap.clear();
            for (PlayerRanking r : topRankings) {
                rankingMap.put(r.getPlayerName(), r);
            }
            
            // Update rankings list
            rankings = topRankings;
        }
        
        saveRankings();
        
        // Return true if the ranking is in the top 100
        return rankings.contains(ranking);
    }
    
    /**
     * Get all rankings
     * @return List of player rankings
     */
    public List<PlayerRanking> getRankings() {
        return new ArrayList<>(rankings);
    }
    
    /**
     * Get the player's position in the rankings
     * @param ranking The player ranking
     * @return The position (1-based) or -1 if not in the rankings
     */
    public int getPlayerPosition(PlayerRanking ranking) {
        // Use HashMap to quickly find the player's ranking
        PlayerRanking existingRanking = rankingMap.get(ranking.getPlayerName());
        if (existingRanking != null) {
            return rankings.indexOf(existingRanking) + 1;
        }
        return -1;
    }
    
    /**
     * Load rankings from file
     * @return List of player rankings
     */
    @SuppressWarnings("unchecked")
    private List<PlayerRanking> loadRankings() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RANKINGS_FILE))) {
            return (List<PlayerRanking>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("Rankings file not found. Creating new rankings list.");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading rankings: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Save rankings to file
     */
    private void saveRankings() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RANKINGS_FILE))) {
            oos.writeObject(rankings);
        } catch (IOException e) {
            System.err.println("Error saving rankings: " + e.getMessage());
        }
    }
} 