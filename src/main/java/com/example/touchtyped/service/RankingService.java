package com.example.touchtyped.service;

import com.example.touchtyped.model.PlayerRanking;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class RankingService {
    private static final String RANKINGS_FILE = "player_rankings.dat";
    private static final int MAX_RANKINGS = 100;
    private static RankingService instance;
    
    private List<PlayerRanking> rankings;
    private Map<String, PlayerRanking> rankingMap; // HashMap for quickly finding player rankings
    
    private RankingService() {
        rankings = loadRankings();
        // Initialize HashMap and fill with existing rankings
        rankingMap = new HashMap<>();
        for (PlayerRanking ranking : rankings) {
            rankingMap.put(ranking.getPlayerName(), ranking);
        }
        
        // Clear all local rankings because we now only use global rankings
        clearRankings();
    }
    
    /**
     * Get singleton instance
     * @return RankingService instance
     */
    public static synchronized RankingService getInstance() {
        if (instance == null) {
            instance = new RankingService();
        }
        return instance;
    }
    
    /**
     * Add new ranking
     * If a ranking with the same name exists, only keep the better one
     * @param ranking Ranking to add
     * @return true if ranking was added to top 100, false otherwise
     */
    public boolean addRanking(PlayerRanking ranking) {
        // Use HashMap to quickly check if a ranking with the same name exists
        PlayerRanking existingRanking = rankingMap.get(ranking.getPlayerName());
        
        if (existingRanking != null) {
            // If a ranking with the same name exists, compare old and new rankings
            if (ranking.compareTo(existingRanking) < 0) {
                // If new ranking is better, delete old ranking
                rankings.remove(existingRanking);
                System.out.println("Deleted old ranking: " + existingRanking.getPlayerName() + 
                                  " (WPM: " + existingRanking.getWpm() + 
                                  ", Accuracy: " + existingRanking.getAccuracy() + "%)");
                
                // Add new ranking
                rankings.add(ranking);
                rankingMap.put(ranking.getPlayerName(), ranking);
                System.out.println("Added better new ranking: " + ranking.getPlayerName() + 
                                  " (WPM: " + ranking.getWpm() + 
                                  ", Accuracy: " + ranking.getAccuracy() + "%)");
            } else {
                // If old ranking is better, don't add new ranking
                System.out.println("Keeping existing better ranking: " + existingRanking.getPlayerName() + 
                                  " (WPM: " + existingRanking.getWpm() + 
                                  ", Accuracy: " + existingRanking.getAccuracy() + "%)");
                return false;
            }
        } else {
            // If no ranking with the same name exists, add new ranking directly
            rankings.add(ranking);
            rankingMap.put(ranking.getPlayerName(), ranking);
            System.out.println("Added new ranking: " + ranking.getPlayerName() + 
                              " (WPM: " + ranking.getWpm() + 
                              ", Accuracy: " + ranking.getAccuracy() + "%)");
        }
        
        // Sort rankings
        Collections.sort(rankings);
        
        // Only keep top MAX_RANKINGS
        if (rankings.size() > MAX_RANKINGS) {
            // Get top MAX_RANKINGS to keep
            List<PlayerRanking> topRankings = new ArrayList<>(rankings.subList(0, MAX_RANKINGS));
            
            // Update rankingMap, remove rankings not in top MAX_RANKINGS
            rankingMap.clear();
            for (PlayerRanking r : topRankings) {
                rankingMap.put(r.getPlayerName(), r);
            }
            
            // Update rankings list
            rankings = topRankings;
        }
        
        saveRankings();
        
        // Return true if ranking is in top 100
        return rankings.contains(ranking);
    }
    
    /**
     * Get all rankings
     * @return Rankings list
     */
    public List<PlayerRanking> getRankings() {
        return new ArrayList<>(rankings);
    }
    
    /**
     * Get player's position in rankings
     * @param playerName Player name
     * @return Position (starting from 1), returns -1 if not in rankings
     */
    public int getPlayerPosition(String playerName) {
        // Use HashMap to quickly find player ranking
        PlayerRanking existingRanking = rankingMap.get(playerName);
        if (existingRanking != null) {
            return rankings.indexOf(existingRanking) + 1;
        }
        return -1;
    }
    
    /**
     * Load rankings from file
     * @return Rankings list
     */
    @SuppressWarnings("unchecked")
    private List<PlayerRanking> loadRankings() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RANKINGS_FILE))) {
            return (List<PlayerRanking>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("Rankings file does not exist, creating new rankings list");
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
    
    /**
     * Get top N rankings
     * @param n Number of rankings to get
     * @return Top N rankings
     */
    public List<PlayerRanking> getTopRankings(int n) {
        int count = Math.min(n, rankings.size());
        return new ArrayList<>(rankings.subList(0, count));
    }
    
    /**
     * Get rankings for specific game mode
     * @param gameMode Game mode
     * @return Rankings list for specified game mode
     */
    public List<PlayerRanking> getRankingsByGameMode(String gameMode) {
        List<PlayerRanking> filteredRankings = new ArrayList<>();
        for (PlayerRanking ranking : rankings) {
            if (ranking.getGameMode().equals(gameMode)) {
                filteredRankings.add(ranking);
            }
        }
        return filteredRankings;
    }
    
    /**
     * Get all rankings for a specific player
     * @param playerName Player name
     * @return Player's rankings list
     */
    public List<PlayerRanking> getPlayerRankings(String playerName) {
        List<PlayerRanking> playerRankings = new ArrayList<>();
        for (PlayerRanking ranking : rankings) {
            if (ranking.getPlayerName().equals(playerName)) {
                playerRankings.add(ranking);
            }
        }
        return playerRankings;
    }
    
    /**
     * Clear all ranking data
     */
    public void clearRankings() {
        rankings.clear();
        rankingMap.clear();
        saveRankings();
    }
} 