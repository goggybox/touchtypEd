package com.example.touchtyped.serialisers;

import com.example.touchtyped.model.TypingPlan;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class TypingPlanDeserialiser {

    /**
     * gets a TypingPlan from a given JSON file
     * @param filePath is the JSON file to read from
     * @return a TypingPlan
     */
    public static TypingPlan parseTypingPlan(String filePath) {
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            TypingPlanWrapper wrapper = objectMapper.readValue(new File(filePath), TypingPlanWrapper.class);
            return wrapper.getTypingPlan();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
