package com.example.touchtyped.server.service;

import com.example.touchtyped.model.PlayerRanking;
import com.example.touchtyped.server.entity.RankingEntity;
import com.example.touchtyped.server.repository.RankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 排名服务层，处理业务逻辑
 */
@Service
public class RankingServerService {
    
    private final RankingRepository rankingRepository;
    
    @Autowired
    public RankingServerService(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
    }
    
    /**
     * 添加一个新排名
     * 如果已存在相同名称的排名，只保留更好的一个
     * @param ranking 要添加的排名
     * @return true如果排名被添加，false如果保留了已有更好的排名
     */
    @Transactional
    public boolean addRanking(PlayerRanking ranking) {
        // 检查是否已存在相同名称的排名
        Optional<RankingEntity> existingRanking = rankingRepository.findBestByPlayerName(ranking.getPlayerName());
        
        if (existingRanking.isPresent()) {
            RankingEntity existing = existingRanking.get();
            
            // 比较排名（更高的WPM更好，如果相同，更高的准确率更好）
            if (ranking.getWpm() > existing.getWpm() || 
                    (ranking.getWpm() == existing.getWpm() && ranking.getAccuracy() > existing.getAccuracy())) {
                // 更新现有排名
                existing.setWpm(ranking.getWpm());
                existing.setAccuracy(ranking.getAccuracy());
                existing.setGameMode(ranking.getGameMode());
                existing.setTimestamp(ranking.getTimestamp());
                rankingRepository.save(existing);
                return true;
            } else {
                // 保留现有排名
                return false;
            }
        } else {
            // 添加新排名
            RankingEntity entity = new RankingEntity(
                    ranking.getPlayerName(),
                    ranking.getWpm(),
                    ranking.getAccuracy(),
                    ranking.getGameMode()
            );
            rankingRepository.save(entity);
            return true;
        }
    }
    
    /**
     * 获取所有排名
     * @return 排名列表
     */
    @Transactional(readOnly = true)
    public List<PlayerRanking> getAllRankings() {
        return rankingRepository.findAllOrderByWpmDescAccuracyDesc()
                .stream()
                .map(this::convertToPlayerRanking)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取玩家在排名中的位置
     * @param playerName 玩家名称
     * @return 玩家位置（从1开始），如果不存在则返回-1
     */
    @Transactional(readOnly = true)
    public int getPlayerPosition(String playerName) {
        Integer position = rankingRepository.findPlayerPosition(playerName);
        return position != null ? position : -1;
    }
    
    /**
     * 将RankingEntity转换为PlayerRanking
     * @param entity 要转换的实体
     * @return 转换后的PlayerRanking
     */
    private PlayerRanking convertToPlayerRanking(RankingEntity entity) {
        PlayerRanking ranking = new PlayerRanking(
                entity.getPlayerName(),
                entity.getWpm(),
                entity.getAccuracy(),
                entity.getGameMode()
        );
        ranking.setTimestamp(entity.getTimestamp());
        return ranking;
    }
} 