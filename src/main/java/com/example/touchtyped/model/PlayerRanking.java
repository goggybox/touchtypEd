package com.example.touchtyped.model;

import java.io.Serializable;
import java.time.LocalDateTime;


public class PlayerRanking implements Serializable, Comparable<PlayerRanking> {
    private static final long serialVersionUID = 1L;
    private String playerName;
    private int wpm;
    private double accuracy;
    private LocalDateTime timestamp;
    private String gameMode;

    // 默认构造函数，用于JSON反序列化
    public PlayerRanking() {
        this.timestamp = LocalDateTime.now();
    }

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
        // 首先按WPM排序（降序）
        int wpmCompare = Integer.compare(other.wpm, this.wpm);
        if (wpmCompare != 0) {
            return wpmCompare;
        }

        // 如果WPM相同，则按准确率排序（降序）
        int accuracyCompare = Double.compare(other.accuracy, this.accuracy);
        
        if (accuracyCompare != 0) {
            return accuracyCompare;
        }
        
        // 如果WPM和准确率都相同，则按时间戳排序（升序，较早的排在前面）
        return this.timestamp.compareTo(other.timestamp);
    }

    @Override
    public String toString() {
        return String.format("%s - %d WPM - %.1f%% - %s", 
            playerName, wpm, accuracy, gameMode);
    }
} 