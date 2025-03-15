package com.example.touchTypEd_backend.controller;

import com.example.touchTypEd_backend.service.MatchManager;
import com.example.touchTypEd_backend.model.MatchRoom;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/match")
public class MatchController {

    private final MatchManager matchManager;

    public MatchController(MatchManager matchManager) {
        this.matchManager = matchManager;
    }

    /**
     * 让一个玩家进入匹配队列。
     * 若已有一个玩家在等，则马上配对并返回 matchId；否则将此玩家放入队列中，等待下一个玩家进来后再配对。
     */
    @PostMapping("/queue")
    public Map<String, String> queuePlayer(@RequestParam String playerId) {
        String matchId = matchManager.queuePlayer(playerId);
        return Map.of("matchId", matchId == null ? "" : matchId);
    }

    /**
     * 仅用于调试：根据 matchId 获取当前房间信息 (双方分数等)。
     */
    @GetMapping("/room/{matchId}")
    public MatchRoom getMatchRoom(@PathVariable String matchId) {
        return matchManager.getRoom(matchId);
    }
}
