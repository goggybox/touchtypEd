package com.example.touchtyped.serialisers;

import com.example.touchtyped.model.TypingPlan;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class TypingPlanDeserialiser {

    /**
     * gets a TypingPlan from a given JSON file
     * @return a TypingPlan
     */
    public static TypingPlan getTypingPlan() {
        String filePath = "src/main/resources/com/example/touchtyped/testTypingPlan.json";
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
