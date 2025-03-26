package com.example.touchtyped.service;

import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.TypingPlan;
import com.example.touchtyped.serialisers.TypingPlanWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class RESTClient {

    private static final String REST_ENDPOINT = "https://sdp-data-premium.azurewebsites.net/api/sdp_data";
    private static final String PDF_ENDPOINT = "https://sdp-data-premium.azurewebsites.net/api/sdp_data_pdf";
    private static final String TYPING_PLAN_ENDPOINT = "https://sdp-data-premium.azurewebsites.net/api/sdp_data_json";
    private static final int MAX_RETRIES = 3;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RESTResponseWrapper getTypingPlan(KeyLogsStructure keyLogsStructure, Runnable onRetry) throws IOException {
        TypingPlan typingPlan = fetchTypingPlan(keyLogsStructure, onRetry);
        return new RESTResponseWrapper(null, typingPlan);
    }

    public RESTResponseWrapper getPDF(KeyLogsStructure keyLogsStructure, Runnable onRetry) throws IOException {
        byte[] pdfData = fetchPDF(keyLogsStructure, onRetry);
        return new RESTResponseWrapper(pdfData, null);
    }

    private TypingPlan fetchTypingPlan(KeyLogsStructure keyLogsStructure, Runnable onRetry) throws IOException {
        String payload = objectMapper.writeValueAsString(keyLogsStructure);
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                URL url = new URL(TYPING_PLAN_ENDPOINT);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                System.out.println("Sending TypingPlan request...");
                try (var outputStream = connection.getOutputStream()) {
                    outputStream.write(payload.getBytes());
                }

                int statusCode = connection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    try (var inputStream = connection.getInputStream()) {
                        TypingPlanWrapper wrapper = objectMapper.readValue(inputStream, TypingPlanWrapper.class);
                        System.out.println("TypingPlan received.");
                        return wrapper.getTypingPlan();
                    }
                } else if (statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    retryCount++;
                    handleRetry(retryCount, onRetry, "TypingPlan");
                } else {
                    throw new IOException("Error fetching TypingPlan. Status code: " + statusCode);
                }
            } catch (IOException e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) throw new IOException("Failed after " + MAX_RETRIES + " attempts.", e);
                handleRetry(retryCount, onRetry, "TypingPlan");
            }
        }
        throw new IOException("Maximum retry attempts reached for TypingPlan.");
    }

    private byte[] fetchPDF(KeyLogsStructure keyLogsStructure, Runnable onRetry) throws IOException {
        String payload = objectMapper.writeValueAsString(keyLogsStructure);
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                URL url = new URL(PDF_ENDPOINT);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                System.out.println("Sending PDF request...");
                try (var outputStream = connection.getOutputStream()) {
                    outputStream.write(payload.getBytes());
                }

                int statusCode = connection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    try (var inputStream = connection.getInputStream()) {
                        System.out.println("PDF received.");
                        return inputStream.readAllBytes();
                    }
                } else if (statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    retryCount++;
                    handleRetry(retryCount, onRetry, "PDF");
                } else {
                    throw new IOException("Error fetching PDF. Status code: " + statusCode);
                }
            } catch (IOException e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) throw new IOException("Failed after " + MAX_RETRIES + " attempts.", e);
                handleRetry(retryCount, onRetry, "PDF");
            }
        }
        throw new IOException("Maximum retry attempts reached for PDF.");
    }

    private void handleRetry(int retryCount, Runnable onRetry, String requestType) {
        System.err.println(requestType + " request failed. Retrying... Attempt: " + retryCount);
        if (onRetry != null) {
            Platform.runLater(onRetry); // Update UI on retry
        }
    }


}
