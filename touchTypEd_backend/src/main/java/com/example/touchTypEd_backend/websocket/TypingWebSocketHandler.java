package com.example.touchTypEd_backend.websocket;

import com.example.touchTypEd_backend.service.MatchManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.json.JSONObject;

@Component
public class TypingWebSocketHandler extends TextWebSocketHandler {

    private final MatchManager matchManager;

    // 可以使用构造注入
    public TypingWebSocketHandler(MatchManager matchManager) {
        this.matchManager = matchManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket connected: " + session.getId());
        // 可给客户端一个欢迎消息
        session.sendMessage(new TextMessage("{\"type\":\"WELCOME\",\"msg\":\"Connected to server.\"}"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Recv from client: " + payload);

        // 简单用 JSON 解析(依赖 org.json)
        JSONObject json = new JSONObject(payload);
        String type = json.optString("type");

        switch(type) {
            case "JOIN_MATCH":
                // 客户端告诉服务器: "我要进入某个 matchId"
                // 这里可把 session 存到 Map<sessionId -> matchId> 里, 方便以后广播
                // 省略...
                break;

            case "INPUT":
                // { "type":"INPUT", "matchId":"xxx", "playerId":"Alice", "char":"a" }
                String matchId = json.optString("matchId");
                String playerId = json.optString("playerId");
                char typedChar = json.optString("char").charAt(0);

                matchManager.handlePlayerInput(matchId, playerId, typedChar);

                // (可选) 再次获取当前分数, 广播给房间内玩家
                broadcastScores(matchId, session);
                break;

            default:
                System.out.println("Unknown msg type: " + type);
        }
    }

    private void broadcastScores(String matchId, WebSocketSession currentSession) {
        var room = matchManager.getRoom(matchId);
        if(room == null) return;

        // 假设要给所有连接到此 matchId 的客户端广播:
        // 这里演示只给当前 session 回发, 你可以保留 sessionList 以通知双方
        try {
            JSONObject resp = new JSONObject();
            resp.put("type","SCORE_UPDATE");
            resp.put("scoreA", room.getScoreA());
            resp.put("scoreB", room.getScoreB());
            resp.put("letters", room.getLetters());

            currentSession.sendMessage(new TextMessage(resp.toString()));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
