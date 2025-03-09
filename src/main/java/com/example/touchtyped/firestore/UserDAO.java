package com.example.touchtyped.firestore;

import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.TypingPlan;
import com.google.cloud.firestore.*;
import com.google.api.core.ApiFuture;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public final class UserDAO {

    private static final String USER_COLLECTION = "users";

    private UserDAO() {}

    public static boolean createUser(String classroomID, String username, TypingPlan typingPlan, String password) throws InterruptedException, ExecutionException {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference userDoc = db.collection(USER_COLLECTION).document(classroomID+","+username);

            // check if a user with that username already exists in the classroom
            DocumentSnapshot document = userDoc.get().get();
            if (document.exists()) {
                // already exists
                return false;
            }

            UserAccount user = new UserAccount(classroomID, username, typingPlan, new ArrayList<>(), password);
            userDoc.set(user).get();
            return true;

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to add user", e);
        }
    }

    public static UserAccount getAccount(String classroomID, String username) throws InterruptedException, ExecutionException {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference userDoc = db.collection(USER_COLLECTION).document(classroomID + "," + username);
            DocumentSnapshot document = userDoc.get().get();

            if (document.exists()) {
                return document.toObject(UserAccount.class);
            } else {
                System.out.println("DATABASE FAILURE. Failed to get user account: user doesn't exist.");
                return null;
            }
        } catch (Exception e) {
            System.out.println("DATABASE FAILURE. Failed to get user account: user doesn't exist.");
            return null;
        }
    }

    public static void addKeyLog(String classroomID, String username, KeyLogsStructure keyLogs) throws InterruptedException, ExecutionException {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference userDoc = db.collection(USER_COLLECTION).document(classroomID + "," + username);

            // append to keyLogs List
            ApiFuture<WriteResult> future = userDoc.update("keyLogs", FieldValue.arrayUnion(keyLogs));
            future.get();

        } catch (Exception e) {
            System.out.println("DATABASE FAILURE. Failed to add key logs to user account: user doesn't exist.");
        }
    }

}
