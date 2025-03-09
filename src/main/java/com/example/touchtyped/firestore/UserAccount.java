package com.example.touchtyped.firestore;

import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.TypingPlan;

import java.util.List;

public class UserAccount {
    private String classroomID;
    private String username;
    private TypingPlan typingPlan;
    private List<KeyLogsStructure> keyLogs;

    public UserAccount() {}

    public UserAccount(String classroomID, String username, TypingPlan typingPlan, List<KeyLogsStructure> keyLogs) {
        this.classroomID = classroomID;
        this.username = username;
        this.typingPlan = typingPlan;
        this.keyLogs = keyLogs;
    }


    // GETTERS AND SETTERS

    public List<KeyLogsStructure> getKeyLogs() {
        return keyLogs;
    }

    public void setKeyLogs(List<KeyLogsStructure> keyLogs) {
        this.keyLogs = keyLogs;
    }

    public TypingPlan getTypingPlan() {
        return typingPlan;
    }

    public void setTypingPlan(TypingPlan typingPlan) {
        this.typingPlan = typingPlan;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClassroomID() {
        return classroomID;
    }

    public void setClassroomID(String classroomID) {
        this.classroomID = classroomID;
    }
}
