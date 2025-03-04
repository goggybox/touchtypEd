package com.example.touchtyped.service;

import com.example.touchtyped.model.TypingPlan;

public class RESTResponseWrapper {

    private final byte[] pdfData;
    private final TypingPlan typingPlan;

    public RESTResponseWrapper(byte[] pdfData, TypingPlan typingPlan) {
        this.pdfData = pdfData;
        this.typingPlan = typingPlan;
    }

    public byte[] getPdfData() {
        return pdfData;
    }

    public TypingPlan getTypingPlan() {
        return typingPlan;
    }

}
