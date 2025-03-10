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

    /**
     * allow creation of a user without providing a password (will be the case the majority of the time)
     * @param classroomID is the classroom the user is joining (all users must be associated with one classroom).
     * @param username is the username to give the user.
     * @param typingPlan is the user's personalised TypingPlan.
     * @return whether the creation was successful.
     * @throws InterruptedException idk database stuff
     * @throws ExecutionException lol same
     */
    public static boolean createUser(String classroomID, String username, TypingPlan typingPlan) throws InterruptedException, ExecutionException {
        return createUser(classroomID, username, typingPlan, null);
    }

    /***
     * create a user and add them to the database. will generate a unique random userID.
     * @param classroomID is the classroom the user is joining (all users must be associated with one classroom).
     * @param username is the username to give the user.
     * @param typingPlan is the user's personalised TypingPlan.
     * @param password is the user's password (only teachers will have passwords, for others this will be null).
     * @return whether the creation was successful.
     * @throws InterruptedException idk database stuff
     * @throws ExecutionException lol same
     */
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

            // generate random userID
            String userID = generateUserID();

            UserAccount user = new UserAccount(classroomID, userID, username, typingPlan, new ArrayList<>(), password);
            userDoc.set(user).get();
            return true;

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to add user", e);
        }
    }

    /**
     * generate a random user ID, ensuring uniqueness in the database.
     * @return the generated userID, or null if unsuccessful.
     */
    public static String generateUserID() {
        try {
            Firestore db = FirestoreClient.getFirestore();
            SecureRandom random = new SecureRandom();
            String char_pool = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            int max_attempts = 100;

            for (int i = 0; i < max_attempts; i++) {
                // generate potential ID
                StringBuilder sb = new StringBuilder("U");
                for (int j = 0; j < 6; j++) {
                    int index = random.nextInt(char_pool.length());
                    sb.append(char_pool.charAt(index));
                }
                String id = sb.toString();

                // check uniqueness
                Query query = db.collection(USER_COLLECTION).whereEqualTo("userID", id).limit(1);
                QuerySnapshot snapshot = query.get().get();

                if (snapshot.isEmpty()) {
                    return id; // it is unique.
                }
            }

            throw new RuntimeException("DATABASE FAILURE. Failed to generate unique userID after "+max_attempts+" attempts.");

        } catch (Exception e) {
            System.out.println("DATABASE FAILURE. Failed to generate user ID.");
            return null;
        }

    }

    // get a user account without needing a password.
    public static UserAccount getAccount(String classroomID, String username) throws InterruptedException, ExecutionException {
        return getAccount(classroomID, username, null);
    }

    /**
     * returns the UserAccount for a provided classroomID+username combination. If the account has a stored password,
     * the correct password MUST be provided to the function.
     * @param classroomID is the classroom the user is a part of.
     * @param username is the username of the user.
     * @param password (OPTIONAL) is the password of the user's acocunt.
     * @return the UserAccount, or null if failed.
     */
    public static UserAccount getAccount(String classroomID, String username, String password) throws InterruptedException, ExecutionException {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference userDoc = db.collection(USER_COLLECTION).document(classroomID + "," + username);
            DocumentSnapshot document = userDoc.get().get();

            if (document.exists()) {
                UserAccount userAccount = document.toObject(UserAccount.class);

                // check if the account has a password. if so, ensure that the correct password was supplied to this function.
                if (userAccount.getPassword() == null) {
                    return userAccount;
                } else if (userAccount.getPassword() != null && userAccount.getPassword().equals(password)) {
                    return userAccount;
                } else {
                    System.out.println("DATABASE FAILURE. Failed to get user account: password provided did not match stored password.");
                    return null;
                }
            } else {
                System.out.println("DATABASE FAILURE. Failed to get user account: user doesn't exist.");
                return null;
            }
        } catch (Exception e) {
            System.out.println("DATABASE FAILURE. Failed to get user account: user doesn't exist.");
            return null;
        }
    }


    // make providing a password optional.
    public static void addKeyLog(String classroomID, String username, KeyLogsStructure keyLogs) throws InterruptedException, ExecutionException {
        addKeyLog(classroomID, username, keyLogs, null);
    }


    public static void addKeyLog(String classroomID, String username, KeyLogsStructure keyLogs, String password) throws InterruptedException, ExecutionException {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference userDoc = db.collection(USER_COLLECTION).document(classroomID + "," + username);
            DocumentSnapshot document = userDoc.get().get();
            UserAccount userAccount = document.toObject(UserAccount.class);

            if ((userAccount.getPassword() == null) || (userAccount.getPassword() != null && userAccount.getPassword().equals(password)))
            {
                // append to keyLogs List
                ApiFuture<WriteResult> future = userDoc.update("keyLogs", FieldValue.arrayUnion(keyLogs));
                future.get();
            } else {
                // denied access to account (invalid password provided)
                System.out.println("DATABASE FAILURE. Failed to add key logs to user account: password provided did not match stored password.");
            }

        } catch (Exception e) {
            System.out.println("DATABASE FAILURE. Failed to add key logs to user account: user doesn't exist.");
        }
    }

}
