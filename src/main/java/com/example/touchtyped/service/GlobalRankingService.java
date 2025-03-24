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
 * Global ranking service for communicating with the ranking server
 */
public class GlobalRankingService {
    // Please replace with your global ranking server IP address or domain name
    private static final String SERVER_IP = "10.124.113.224";

    private static final int SERVER_PORT = 8080;

    private static final String API_PATH = "/api/rankings";

    private static final String SERVER_URL = "http://" + SERVER_IP + ":" + SERVER_PORT + API_PATH;
    
    private static GlobalRankingService instance;
    private final ObjectMapper objectMapper;
    
    private GlobalRankingService() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }
    
    public static synchronized GlobalRankingService getInstance() {
        if (instance == null) {
            instance = new GlobalRankingService();
        }
        return instance;
    }
    
    /**
     * Test server connection
     * @return Async result, true if connected successfully, false otherwise
     */
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000); // 5 seconds connection timeout
                connection.setReadTimeout(5000);    // 5 seconds read timeout
                
                int statusCode = connection.getResponseCode();
                return statusCode >= 200 && statusCode < 300;
            } catch (Exception e) {
                System.err.println("Error testing server connection: " + e.getMessage());
                return false;
            }
        }, Executors.newCachedThreadPool());
    }
    
    /**
     * Submit ranking to server
     * @param ranking Ranking to submit
     * @return Async result, true if successful, false otherwise
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
                connection.setConnectTimeout(10000); // 10 seconds connection timeout
                connection.setReadTimeout(10000);    // 10 seconds read timeout
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonRanking.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                
                int statusCode = connection.getResponseCode();
                
                if (statusCode >= 200 && statusCode < 300) {
                    System.out.println("Successfully submitted ranking to server");
                    return true;
                } else {
                    System.err.println("Failed to submit ranking to server, status code: " + statusCode);
                    return false;
                }
            } catch (Exception e) {
                System.err.println("Error submitting ranking: " + e.getMessage());
                return false;
            }
        }, Executors.newCachedThreadPool());
    }
    
    /**
     * Get global rankings list
     * @return Rankings list
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
                    System.err.println("Failed to get global rankings, status code: " + statusCode);
                }
            } catch (Exception e) {
                System.err.println("Error getting global rankings: " + e.getMessage());
            }
            
            return new ArrayList<>();
        }, Executors.newCachedThreadPool());
    }
    
    /**
     * Get player's position in global rankings
     * @param playerName Player name
     * @return Player position (starting from 1), returns -1 if not found
     */
    public CompletableFuture<Integer> getPlayerPosition(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SERVER_URL + "/position/" + playerName);
                System.out.println("Attempting to connect to: " + url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                int statusCode = connection.getResponseCode();
                System.out.println("Server response code: " + statusCode);
                
                if (statusCode == 200) {
                    try (Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8")) {
                        String responseBody = scanner.useDelimiter("\\A").next();
                        System.out.println("Server response body: " + responseBody);
                        return Integer.parseInt(responseBody);
                    }
                } else {
                    System.err.println("Failed to get player position, status code: " + statusCode);
                    // Try to read the error response
                    try (Scanner scanner = new Scanner(connection.getErrorStream(), "UTF-8")) {
                        if (scanner.hasNext()) {
                            String errorBody = scanner.useDelimiter("\\A").next();
                            System.err.println("Server error message: " + errorBody);
                        }
                    } catch (Exception e) {
                        System.err.println("Unable to read error message: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error getting player position: " + e.getMessage());
                e.printStackTrace();
            }
            
            return -1;
        }, Executors.newCachedThreadPool());
    }
} 