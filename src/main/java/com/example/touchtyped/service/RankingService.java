package com.example.touchtyped.service;

import com.example.touchtyped.model.PlayerRanking;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排名服务，管理本地排名
 */
public class RankingService {
    private static final String RANKINGS_FILE = "player_rankings.dat";
    private static final int MAX_RANKINGS = 100;
    private static RankingService instance;
    
    private List<PlayerRanking> rankings;
    private Map<String, PlayerRanking> rankingMap; // 用于快速查找玩家排名的HashMap
    
    private RankingService() {
        rankings = loadRankings();
        // 初始化HashMap并填充现有排名
        rankingMap = new HashMap<>();
        for (PlayerRanking ranking : rankings) {
            rankingMap.put(ranking.getPlayerName(), ranking);
        }
    }
    
    /**
     * 获取单例实例
     * @return RankingService实例
     */
    public static synchronized RankingService getInstance() {
        if (instance == null) {
            instance = new RankingService();
        }
        return instance;
    }
    
    /**
     * 添加新排名
     * 如果已存在相同名称的排名，只保留更好的那个
     * @param ranking 要添加的排名
     * @return true如果排名被添加到前100，false否则
     */
    public boolean addRanking(PlayerRanking ranking) {
        // 使用HashMap快速查找是否已存在相同名称的排名
        PlayerRanking existingRanking = rankingMap.get(ranking.getPlayerName());
        
        if (existingRanking != null) {
            // 如果存在相同名称的排名，比较旧排名和新排名
            if (ranking.compareTo(existingRanking) < 0) {
                // 如果新排名更好，删除旧排名
                rankings.remove(existingRanking);
                System.out.println("删除旧排名: " + existingRanking.getPlayerName() + 
                                  " (WPM: " + existingRanking.getWpm() + 
                                  ", 准确率: " + existingRanking.getAccuracy() + "%)");
                
                // 添加新排名
                rankings.add(ranking);
                rankingMap.put(ranking.getPlayerName(), ranking);
                System.out.println("添加更好的新排名: " + ranking.getPlayerName() + 
                                  " (WPM: " + ranking.getWpm() + 
                                  ", 准确率: " + ranking.getAccuracy() + "%)");
            } else {
                // 如果旧排名更好，不添加新排名
                System.out.println("保留现有更好的排名: " + existingRanking.getPlayerName() + 
                                  " (WPM: " + existingRanking.getWpm() + 
                                  ", 准确率: " + existingRanking.getAccuracy() + "%)");
                return false;
            }
        } else {
            // 如果不存在相同名称的排名，直接添加新排名
            rankings.add(ranking);
            rankingMap.put(ranking.getPlayerName(), ranking);
            System.out.println("添加新排名: " + ranking.getPlayerName() + 
                              " (WPM: " + ranking.getWpm() + 
                              ", 准确率: " + ranking.getAccuracy() + "%)");
        }
        
        // 排序排名
        Collections.sort(rankings);
        
        // 只保留前MAX_RANKINGS名
        if (rankings.size() > MAX_RANKINGS) {
            // 获取前MAX_RANKINGS名保留
            List<PlayerRanking> topRankings = new ArrayList<>(rankings.subList(0, MAX_RANKINGS));
            
            // 更新rankingMap，删除不在前MAX_RANKINGS名的排名
            rankingMap.clear();
            for (PlayerRanking r : topRankings) {
                rankingMap.put(r.getPlayerName(), r);
            }
            
            // 更新排名列表
            rankings = topRankings;
        }
        
        saveRankings();
        
        // 如果排名在前100名，返回true
        return rankings.contains(ranking);
    }
    
    /**
     * 获取所有排名
     * @return 排名列表
     */
    public List<PlayerRanking> getRankings() {
        return new ArrayList<>(rankings);
    }
    
    /**
     * 获取玩家在排名中的位置
     * @param playerName 玩家名称
     * @return 位置（从1开始），如果不在排名中则返回-1
     */
    public int getPlayerPosition(String playerName) {
        // 使用HashMap快速查找玩家排名
        PlayerRanking existingRanking = rankingMap.get(playerName);
        if (existingRanking != null) {
            return rankings.indexOf(existingRanking) + 1;
        }
        return -1;
    }
    
    /**
     * 从文件加载排名
     * @return 排名列表
     */
    @SuppressWarnings("unchecked")
    private List<PlayerRanking> loadRankings() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RANKINGS_FILE))) {
            return (List<PlayerRanking>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("排名文件不存在，创建新排名列表");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载排名时出错: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 保存排名到文件
     */
    private void saveRankings() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RANKINGS_FILE))) {
            oos.writeObject(rankings);
        } catch (IOException e) {
            System.err.println("保存排名时出错: " + e.getMessage());
        }
    }
} 