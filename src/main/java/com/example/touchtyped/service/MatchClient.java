package com.example.touchtyped.service;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MatchClient {

    /**
     * 调用后端接口: POST /match/queue?playerId=xxx
     * 返回后端 JSON => { "matchId": "xxxx" } or { "matchId": "" }
     */
    public static String queuePlayer(String baseUrl, String playerId) throws IOException {
        // 例如 baseUrl = "http://localhost:8080"
        // POST /match/queue?playerId=Alice
        URL url = new URL(baseUrl + "/match/queue?playerId=" + playerId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        int code = conn.getResponseCode();
        if (code == 200) {
            try (InputStream is = conn.getInputStream()) {
                String resp = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                // resp is like {"matchId":"xxxx"} or {"matchId":""}
                JSONObject obj = new JSONObject(resp);
                return obj.optString("matchId", "");
            }
        } else {
            throw new IOException("HTTP error code: " + code);
        }
    }
}
