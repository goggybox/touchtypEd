package com.example.touchTypEd_backend.model;

import java.io.Serializable;
import java.util.Random;

public class MatchRoom implements Serializable {
    private String matchId;
    private String playerA;
    private String playerB;

    private int scoreA;
    private int scoreB;
    private String letters; // 随机生成的字母序列或句子

    public MatchRoom(String matchId, String pA, String pB) {
        this.matchId = matchId;
        this.playerA = pA;
        this.playerB = pB;
        this.scoreA = 0;
        this.scoreB = 0;
        this.letters = generateLetters(30); // 简单示例: 30个随机字符
    }

    public void handleInput(String playerId, char typedChar) {
        // 判断playerId是A还是B
        if (playerId.equals(playerA)) {
            // 简化: 不做严格判定, 直接score++
            scoreA++;
        } else if (playerId.equals(playerB)) {
            scoreB++;
        }
        // 你也可以把 typedChar 与 letters[x] 比较, 再决定是否加分
    }

    private String generateLetters(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for(int i=0; i<length; i++){
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // Getters / Setters ...
    public String getMatchId() { return matchId; }
    public String getPlayerA() { return playerA; }
    public String getPlayerB() { return playerB; }
    public int getScoreA() { return scoreA; }
    public int getScoreB() { return scoreB; }
    public String getLetters() { return letters; }
}
