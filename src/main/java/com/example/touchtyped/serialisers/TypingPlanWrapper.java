package com.example.touchtyped.serialisers;

import com.example.touchtyped.model.TypingPlan;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TypingPlanWrapper {

    @JsonProperty("pdf")
    private String pdf;

    @JsonProperty("personalized_typing_plan")
    private TypingPlan typingPlan;

    public TypingPlanWrapper() {

    }


    /**
     * Getters and Setters
     */

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public TypingPlan getTypingPlan() {
        return typingPlan;
    }

    public void setTypingPlan(TypingPlan typingPlan) {
        this.typingPlan = typingPlan;
    }

}
