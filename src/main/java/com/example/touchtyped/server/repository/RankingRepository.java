package com.example.touchtyped.server.repository;

import com.example.touchtyped.server.entity.RankingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 排名数据访问层接口
 */
@Repository
public interface RankingRepository extends JpaRepository<RankingEntity, Long> {
    
    /**
     * 查找玩家的最佳排名
     * @param playerName 玩家名称
     * @return 最佳排名，如果不存在则返回空
     */
    @Query("SELECT r FROM RankingEntity r WHERE r.playerName = :playerName ORDER BY r.wpm DESC, r.accuracy DESC")
    Optional<RankingEntity> findBestByPlayerName(@Param("playerName") String playerName);
    
    /**
     * 查找所有排名，按WPM和准确率排序
     * @return 排名列表
     */
    @Query("SELECT r FROM RankingEntity r ORDER BY r.wpm DESC, r.accuracy DESC")
    List<RankingEntity> findAllOrderByWpmDescAccuracyDesc();
    
    /**
     * 查找玩家在排名中的位置
     * @param playerName 玩家名称
     * @return 玩家位置（从1开始），如果不存在则返回-1
     */
    @Query(value = "SELECT position FROM (" +
            "SELECT player_name, ROW_NUMBER() OVER (ORDER BY wpm DESC, accuracy DESC) as position " +
            "FROM rankings) r " +
            "WHERE player_name = :playerName", nativeQuery = true)
    Integer findPlayerPosition(@Param("playerName") String playerName);
} 