package com.example.touchtyped.server.controller;

import com.example.touchtyped.model.PlayerRanking;
import com.example.touchtyped.server.service.RankingServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排名REST控制器，处理HTTP请求
 */
@RestController
@RequestMapping("/api/rankings")
public class RankingController {
    
    private final RankingServerService rankingService;
    
    @Autowired
    public RankingController(RankingServerService rankingService) {
        this.rankingService = rankingService;
    }
    
    /**
     * 获取所有排名
     * @return 排名列表
     */
    @GetMapping
    public ResponseEntity<List<PlayerRanking>> getAllRankings() {
        List<PlayerRanking> rankings = rankingService.getAllRankings();
        return ResponseEntity.ok(rankings);
    }
    
    /**
     * 添加新排名
     * @param ranking 要添加的排名
     * @return 结果消息
     */
    @PostMapping
    public ResponseEntity<String> addRanking(@RequestBody PlayerRanking ranking) {
        boolean added = rankingService.addRanking(ranking);
        if (added) {
            return ResponseEntity.ok("排名添加成功");
        } else {
            return ResponseEntity.ok("保留了已有更好的排名");
        }
    }
    
    /**
     * 获取玩家的排名位置
     * @param playerName 玩家名称
     * @return 玩家位置（从1开始），如果不存在则返回-1
     */
    @GetMapping("/position/{playerName}")
    public ResponseEntity<Integer> getPlayerPosition(@PathVariable String playerName) {
        int position = rankingService.getPlayerPosition(playerName);
        return ResponseEntity.ok(position);
    }
} 