package com.example.touchtyped.server;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import com.example.touchtyped.model.PlayerRanking;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * A simple HTTP server for managing player rankings without Spring dependencies.
 */
public class SimpleRankingServer {
    private static final int PORT = 8080;
    private static final List<PlayerRanking> rankings = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, Integer> playerPositions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        // Create HTTP server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Create endpoints
        server.createContext("/api/rankings", new RankingsHandler());
        server.createContext("/api/rankings/position", new PlayerPositionHandler());
        
        // Set executor
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        // Start server
        server.start();
        
        String hostAddress = java.net.InetAddress.getLocalHost().getHostAddress();
        System.out.println("Simple Ranking Server started on http://" + hostAddress + ":" + PORT);
        System.out.println("API endpoints:");
        System.out.println("  GET  /api/rankings - Get all rankings");
        System.out.println("  POST /api/rankings - Submit a new ranking");
        System.out.println("  GET  /api/rankings/position?playerName=<name> - Get player position");
    }

    /**
     * Handler for /api/rankings endpoint
     */
    static class RankingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if (method.equals("GET")) {
                handleGetRankings(exchange);
            } else if (method.equals("POST")) {
                handleAddRanking(exchange);
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }

        private void handleGetRankings(HttpExchange exchange) throws IOException {
            String responseBody = objectMapper.writeValueAsString(rankings);
            sendResponse(exchange, 200, responseBody);
        }

        private void handleAddRanking(HttpExchange exchange) throws IOException {
            try {
                // Read request body
                InputStream requestBody = exchange.getRequestBody();
                PlayerRanking newRanking = objectMapper.readValue(requestBody, PlayerRanking.class);
                
                // Add new ranking
                synchronized (rankings) {
                    rankings.add(newRanking);
                    // Sort rankings by WPM (descending)
                    rankings.sort((r1, r2) -> Integer.compare(r2.getWpm(), r1.getWpm()));
                    
                    // Update player positions cache
                    updatePlayerPositions();
                }
                
                sendResponse(exchange, 201, "{\"message\": \"Ranking added successfully\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 400, "{\"error\": \"Invalid request: " + e.getMessage() + "\"}");
            }
        }
    }

    /**
     * Handler for /api/rankings/position endpoint
     */
    static class PlayerPositionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }

            // Parse query parameters
            String query = exchange.getRequestURI().getQuery();
            if (query == null) {
                sendResponse(exchange, 400, "{\"error\": \"Missing query parameters\"}");
                return;
            }

            Map<String, String> queryMap = parseQuery(query);
            String playerName = queryMap.get("playerName");
            
            if (playerName == null) {
                sendResponse(exchange, 400, "{\"error\": \"Missing playerName parameter\"}");
                return;
            }

            Integer position = playerPositions.get(playerName);
            if (position == null) {
                sendResponse(exchange, 404, "{\"error\": \"Player not found\"}");
                return;
            }

            sendResponse(exchange, 200, "{\"playerName\": \"" + playerName + "\", \"position\": " + position + "}");
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> result = new HashMap<>();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    result.put(keyValue[0], keyValue[1]);
                }
            }
            return result;
        }
    }

    /**
     * Helper method to send HTTP response
     */
    private static void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, body.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        }
    }

    /**
     * Update player positions cache
     */
    private static void updatePlayerPositions() {
        playerPositions.clear();
        for (int i = 0; i < rankings.size(); i++) {
            playerPositions.put(rankings.get(i).getPlayerName(), i + 1);
        }
    }
} 