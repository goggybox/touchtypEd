package com.example.touchtyped.firestore;

import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.TypingPlan;

import java.util.List;

public class UserAccount {
    private String classroomID;
    private String username;
    private String userID;
    private TypingPlan typingPlan;
    private List<KeyLogsStructure> keyLogs;
    private String password;

    public UserAccount() {}

    public UserAccount(String classroomID, String userID, String username, TypingPlan typingPlan, List<KeyLogsStructure> keyLogs, String password) {
        this.classroomID = classroomID;
        this.username = username;
        this.userID = userID;
        this.typingPlan = typingPlan;
        this.keyLogs = keyLogs;
        this.password = password;
    }

    // GETTERS AND SETTERS
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

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
