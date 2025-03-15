package com.example.touchtyped.websocket;

import com.example.touchtyped.controller.GameViewController;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

public class ScoreWebSocketClient extends WebSocketClient {

    private final GameViewController controller;
    private final String matchId;
    private final String playerId;

    public ScoreWebSocketClient(URI serverUri, GameViewController controller,
                                String matchId, String playerId) {
        super(serverUri);
        this.controller = controller;
        this.matchId = matchId;
        this.playerId = playerId;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("WebSocket connected!");
        // 连接成功后，发送JOIN_MATCH
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", "JOIN_MATCH");
            msg.put("matchId", matchId);
            msg.put("playerId", playerId);
            send(msg.toString());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Recv from server: " + message);
        try {
            JSONObject json = new JSONObject(message);
            String type = json.optString("type");
            switch(type) {
                case "WELCOME":
                    // 服务器欢迎消息
                    break;
                case "SCORE_UPDATE":
                    int scoreA = json.optInt("scoreA");
                    int scoreB = json.optInt("scoreB");
                    // letters 也可解析
                    // 交给Controller来更新UI
                    controller.updateCompetitionScores(scoreA, scoreB);
                    break;
                default:
                    System.out.println("Unknown msg type: " + type);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
