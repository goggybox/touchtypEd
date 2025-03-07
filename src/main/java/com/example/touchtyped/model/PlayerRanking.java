package com.example.touchtyped.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model class for storing player ranking information
 */
public class PlayerRanking implements Serializable, Comparable<PlayerRanking> {
    private String playerName;
    private int wpm;
    private double accuracy;
    private LocalDateTime timestamp;
    private String gameMode;

    public PlayerRanking(String playerName, int wpm, double accuracy, String gameMode) {
        this.playerName = playerName;
        this.wpm = wpm;
        this.accuracy = accuracy;
        this.gameMode = gameMode;
        this.timestamp = LocalDateTime.now();
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getWpm() {
        return wpm;
    }

    public void setWpm(int wpm) {
        this.wpm = wpm;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public int compareTo(PlayerRanking other) {
        // Primary sort by WPM (descending)
        int wpmCompare = Integer.compare(other.wpm, this.wpm);
        if (wpmCompare != 0) {
            return wpmCompare;
        }
        
        // Secondary sort by accuracy (descending)
        return Double.compare(other.accuracy, this.accuracy);
    }

    @Override
    public String toString() {
        return String.format("%s - %d WPM - %.1f%% - %s", 
            playerName, wpm, accuracy, gameMode);
    }
} 