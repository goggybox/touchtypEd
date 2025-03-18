package com.example.touchtyped.service;

import com.example.touchtyped.model.PlayerRanking;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * 全球排名服务，用于与排名服务器通信
 */
public class GlobalRankingService {
    // 服务器地址 - 修改此处连接到你的服务器
    private static final String SERVER_IP = "localhost";
    
    // 服务器端口 - 默认为8080
    private static final int SERVER_PORT = 8080;
    
    // 服务器API路径
    private static final String API_PATH = "/api/rankings";
    
    // 完整服务器URL
    private static final String SERVER_URL = "http://" + SERVER_IP + ":" + SERVER_PORT + API_PATH;
    
    private static GlobalRankingService instance;
    private final ObjectMapper objectMapper;
    
    private GlobalRankingService() {
        this.objectMapper = new ObjectMapper();
        // 注册Java 8日期时间模块，用于处理LocalDateTime等类型
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        System.out.println("排名服务器URL: " + SERVER_URL);
    }
    
    public static synchronized GlobalRankingService getInstance() {
        if (instance == null) {
            instance = new GlobalRankingService();
        }
        return instance;
    }
    
    /**
     * 测试服务器连接
     * @return 异步结果，true表示连接成功，false表示连接失败
     */
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000); // 5秒连接超时
                connection.setReadTimeout(5000);    // 5秒读取超时
                
                int statusCode = connection.getResponseCode();
                return statusCode >= 200 && statusCode < 300;
            } catch (Exception e) {
                System.err.println("测试服务器连接时出错: " + e.getMessage());
                return false;
            }
        }, Executors.newCachedThreadPool());
    }
    
    /**
     * 提交排名到服务器
     * @param ranking 要提交的排名
     * @return 异步结果，true表示成功，false表示失败
     */
    public CompletableFuture<Boolean> submitRanking(PlayerRanking ranking) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonRanking = objectMapper.writeValueAsString(ranking);
                
                URL url = new URL(SERVER_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000); // 10秒连接超时
                connection.setReadTimeout(10000);    // 10秒读取超时
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonRanking.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                
                int statusCode = connection.getResponseCode();
                
                if (statusCode >= 200 && statusCode < 300) {
                    System.out.println("成功提交排名到服务器");
                    return true;
                } else {
                    System.err.println("提交排名到服务器失败，状态码: " + statusCode);
                    return false;
                }
            } catch (Exception e) {
                System.err.println("提交排名时出错: " + e.getMessage());
                return false;
            }
        }, Executors.newCachedThreadPool());
    }
    
    /**
     * 获取全球排名列表
     * @return 排名列表
     */
    public CompletableFuture<List<PlayerRanking>> getGlobalRankings() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                int statusCode = connection.getResponseCode();
                
                if (statusCode == 200) {
                    try (Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8")) {
                        String responseBody = scanner.useDelimiter("\\A").next();
                        return objectMapper.readValue(responseBody, new TypeReference<List<PlayerRanking>>() {});
                    }
                } else {
                    System.err.println("获取全球排名失败，状态码: " + statusCode);
                }
            } catch (Exception e) {
                System.err.println("获取全球排名时出错: " + e.getMessage());
            }
            
            return new ArrayList<>();
        }, Executors.newCachedThreadPool());
    }
    
    /**
     * 获取玩家在全球排名中的位置
     * @param playerName 玩家名称
     * @return 玩家位置（从1开始），如果不存在则返回-1
     */
    public CompletableFuture<Integer> getPlayerPosition(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SERVER_URL + "/position/" + playerName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                int statusCode = connection.getResponseCode();
                
                if (statusCode == 200) {
                    try (Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8")) {
                        String responseBody = scanner.useDelimiter("\\A").next();
                        return Integer.parseInt(responseBody);
                    }
                } else {
                    System.err.println("获取玩家位置失败，状态码: " + statusCode);
                }
            } catch (Exception e) {
                System.err.println("获取玩家位置时出错: " + e.getMessage());
            }
            
            return -1;
        }, Executors.newCachedThreadPool());
    }
} 