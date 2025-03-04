package com.example.touchtyped.service;

import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.serialisers.TypingPlanWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class RESTClient {

    private static final String REST_ENDPOINT = "https://sdp-data.azurewebsites.net/api/sdp_data";
    private static final int MAX_RETRIES = 3;

    public RESTResponseWrapper sendKeyLogs(KeyLogsStructure keyLogsStructure, Runnable onRetry) throws IOException {
        // serialise structure to json object
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(keyLogsStructure);

        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                // create http request
                URL url = new URL(REST_ENDPOINT);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                System.out.println("Sending request to REST service...");
                try (var outputStream = connection.getOutputStream()) {
                    outputStream.write(payload.getBytes());
                }
                System.out.println("Request sent. Waiting for response (this may take around 30 seconds)...");

                // handle http response
                int statusCode = connection.getResponseCode();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    try (var inputStream = connection.getInputStream()) {
                        String responseBody = new String(inputStream.readAllBytes());

                        // parse response as TypingPlanWrapper
                        TypingPlanWrapper wrapper = objectMapper.readValue(responseBody, TypingPlanWrapper.class);

                        // decode pdf
                        byte[] pdfData = null;
                        if (wrapper.getPdf() != null && !wrapper.getPdf().isEmpty()) {
                            pdfData = Base64.getDecoder().decode(wrapper.getPdf());
                        }

                        // return pdf and typing plan in ResponseWrapper
                        System.out.println("Response received from REST service.");
                        return new RESTResponseWrapper(pdfData, wrapper.getTypingPlan());
                    }
                } else if (statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    // retry on status code 500
                    retryCount++;
                    System.err.println("Server returned 500. Retrying... Attempt: " + retryCount);
                    if (onRetry != null) {
                        Platform.runLater(onRetry); // Update UI on retry
                    }
                    continue;
                } else {
                    throw new IOException("Error while contacting REST Service. Status code: " + statusCode);
                }
            } catch (IOException e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    throw new IOException("Failed after " + MAX_RETRIES + " attempts.", e);
                }
                System.err.println("Request failed. Retrying... Attempt: " + retryCount);
                if (onRetry != null) {
                    Platform.runLater(onRetry); // Update UI on retry
                }
            }
        }
        throw new IOException("Maximum retry attempts reached (" + MAX_RETRIES + "). Giving up.");
    }
}
