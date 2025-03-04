package com.example.touchtyped.service;

import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.serialisers.TypingPlanWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class RESTClient {

    private static final String REST_ENDPOINT = "https://sdp-data.azurewebsites.net/api/sdp_data";

    public RESTResponseWrapper sendKeyLogs(KeyLogsStructure keyLogsStructure) throws IOException {
        // serialise structure to json object
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(keyLogsStructure);

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
        } else {
            throw new IOException("Error while contacting REST Service. Status code: " + statusCode);
        }

    }

}
