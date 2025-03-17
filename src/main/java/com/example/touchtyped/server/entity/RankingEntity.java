package com.example.touchtyped.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 排名实体类，用于数据库存储
 */
@Entity
@Table(name = "rankings")
public class RankingEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String playerName;
    
    @Column(nullable = false)
    private int wpm;
    
    @Column(nullable = false)
    private double accuracy;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private String gameMode;
    
    // 默认构造函数（JPA需要）
    public RankingEntity() {
    }
    
    public RankingEntity(String playerName, int wpm, double accuracy, String gameMode) {
        this.playerName = playerName;
        this.wpm = wpm;
        this.accuracy = accuracy;
        this.gameMode = gameMode;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
} 