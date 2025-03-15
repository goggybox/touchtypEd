package com.example.touchTypEd_backend.service;

import com.example.touchTypEd_backend.model.MatchRoom;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchManager {

    // 等待匹配的队列 (简单示例: 同时只等待一个玩家)
    private final Queue<String> waitingPlayers = new LinkedList<>();

    // matchId -> MatchRoom
    private final Map<String, MatchRoom> rooms = new HashMap<>();

    /**
     * 当有玩家进入队列:
     *   1. 如果队列是空的，就把这个玩家放入队列
     *   2. 如果队列里已经有一位等待玩家，则配对他们并返回 matchId
     */
    public synchronized String queuePlayer(String playerId) {
        if (waitingPlayers.isEmpty()) {
            waitingPlayers.add(playerId);
            return null; // 暂时没有 matchId
        } else {
            String otherPlayer = waitingPlayers.poll();
            String matchId = UUID.randomUUID().toString();
            MatchRoom room = new MatchRoom(matchId, otherPlayer, playerId);
            rooms.put(matchId, room);

            // 你可以在这里或后面启动倒计时线程, 并通过 WebSocket 广播"Start"
            System.out.println("匹配成功! roomId=" + matchId + ", players=" + otherPlayer + " & " + playerId);
            return matchId;
        }
    }

    public MatchRoom getRoom(String matchId) {
        return rooms.get(matchId);
    }

    /**
     * 处理玩家输入字符, 并更新房间分数/进度
     */
    public synchronized void handlePlayerInput(String matchId, String playerId, char typedChar) {
        MatchRoom room = rooms.get(matchId);
        if (room != null) {
            room.handleInput(playerId, typedChar);
        }
    }
}
