package com.example.touchtyped.firestore;

import com.example.touchtyped.model.KeyLogsStructure;
import com.example.touchtyped.model.TypingPlan;
import com.google.cloud.firestore.*;
import com.google.api.core.ApiFuture;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public final class UserDAO {

    private static final String USER_COLLECTION = "users";

    private UserDAO() {}

    private static Firestore testDb;
    public static void setTestFirestore(Firestore firestore) {
        testDb = firestore;
    }

    /**
     * get the database; if we are running tests, we can set the testDB, and this is the DB that will be returned.
     * otherwise, simply return the database loaded from FirestoreClient.
     */
    private static Firestore getFirestore() throws IOException {
        if (testDb != null) {
            return testDb;
        }
        return FirestoreClient.getFirestore();
    }

    /**
     * allow creation of a user without providing a password (will be the case the majority of the time)
     * @param classroomID is the classroom the user is joining (all users must be associated with one classroom).
     * @param username is the username to give the user.
     * @param defaultTypingPlan is the user's default TypingPlan.
     * @param personalisedTypingPlan is the user's personalised TypingPlan.
     * @return the created user account
     * @throws InterruptedException idk database stuff
     * @throws ExecutionException lol same
     */
    public static UserAccount createUser(String classroomID, String username, TypingPlan defaultTypingPlan, TypingPlan personalisedTypingPlan) throws InterruptedException, ExecutionException {
        return createUser(classroomID, username, defaultTypingPlan, personalisedTypingPlan, null);
    }

    public static UserAccount createUser(String classroomID, String username, TypingPlan defaultTypingPlan, TypingPlan personalisedTypingPlan, String password) throws InterruptedException, ExecutionException {
        return createUser(classroomID, null, username, defaultTypingPlan, personalisedTypingPlan, password);
    }

    /***
     * create a user and add them to the database. will generate a unique random userID.
     * @param classroomID is the classroom the user is joining (all users must be associated with one classroom).
     * @param username is the username to give the user.
     * @param defaultTypingPlan is the user's default TypingPlan.
     * @param personalisedTypingPlan is the user's personalised TypingPlan.
     * @param password is the user's password (only teachers will have passwords, for others this will be null).
     * @return the created user account
     */
    public static UserAccount createUser(String classroomID, String userID, String username, TypingPlan defaultTypingPlan, TypingPlan personalisedTypingPlan, String password) throws InterruptedException, ExecutionException {
        try {
            Firestore db = getFirestore();
            DocumentReference userDoc = db.collection(USER_COLLECTION).document(classroomID+","+username);

            // check if a user with that username already exists in the classroom
            DocumentSnapshot document = userDoc.get().get();
            if (document.exists()) {
                // already exists
                return null;
            }

            // generate random userID
            if (userID == null) {
                userID = generateUserID();
            }

            UserAccount user = new UserAccount(classroomID, userID, username, defaultTypingPlan, personalisedTypingPlan, new ArrayList<>(), password);

            userDoc.set(user).get();
            return user;

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to add user", e);
        }
    }

    public static String addUserAccount(String classroomID, UserAccount user) throws InterruptedException, ExecutionException {
        try {
            Firestore db = getFirestore();
            DocumentReference userDoc = db.collection(USER_COLLECTION).document(classroomID+","+user.getUsername());

            // check if a user with that username already exists in the classroom
            DocumentSnapshot document = userDoc.get().get();
            if (document.exists()) {
                // already exists
                return null;
            }

            userDoc.set(user).get();
            return user.getUserID();

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to add existing user", e);
        }
    }

    /**
     * generate a random user ID, ensuring uniqueness in the database.
     * @return the generated userID, or null if unsuccessful.
     */
    public static String generateUserID() {
        try {
            Firestore db = getFirestore();
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

    // make password optional
    public static UserAccount getAccountByID(String userID) throws InterruptedException, ExecutionException {
        return getAccountByID(userID, null);
    }

    /**
     * same as getAccount but by using userID instead of unique classroomID+username combination.
     * @param userID is the userID to check for
     * @param password (OPTIONAL) is the user's password.
     * @return the user's account.
     */
    public static UserAccount getAccountByID(String userID, String password) throws InterruptedException, ExecutionException {
        try {
            Firestore db = getFirestore();
            Query query = db.collection(USER_COLLECTION).whereEqualTo("userID", userID).limit(1);
            QuerySnapshot snapshot = query.get().get();

            if (snapshot.isEmpty()) {
                System.out.println("DATABASE FAILURE. Failed to get user account: user doesn't exist. (1)");
                return null;
            }

            DocumentSnapshot document = snapshot.getDocuments().get(0);
            UserAccount userAccount = document.toObject(UserAccount.class);

            // check for password
            if (userAccount.getPassword() == null) {
                return userAccount;
            } else if (userAccount.getPassword() != null && userAccount.getPassword().equals(password)) {
                return userAccount;
            } else {
                System.out.println("DATABASE FAILURE. Failed to get user account: password provided did not match stored password.");
                return null;
            }

        } catch (Exception e) {
            System.out.println("DATABASE FAILURE. Failed to get user account: user doesn't exist. (2)");
            e.printStackTrace();
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
            Firestore db = getFirestore();
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
                System.out.println("DATABASE FAILURE. Failed to get user account: user doesn't exist. (1)");
                return null;
            }
        } catch (Exception e) {
            System.out.println("DATABASE FAILURE. Failed to get user account: user doesn't exist. (2)");
            return null;
        }
    }

    public static boolean deleteUser(String classroomID, String username) throws InterruptedException, ExecutionException {
        return deleteUser(classroomID, username, null);
    }

    public static boolean deleteUser(String classroomID, String username, String password) throws InterruptedException, ExecutionException {
        UserAccount user = getAccount(classroomID, username, password);
        if (user == null) {
            return false;
        }

        try {
            Firestore db = getFirestore();
            DocumentReference userDoc = db.collection(USER_COLLECTION).document(classroomID + "," + username);
            userDoc.delete().get();
            return true;
        } catch (Exception e) {
            System.out.println("DATABASE FAILURE. Failed to delete user");
            return false;
        }
    }


    // make providing a password optional.
    public static void addKeyLog(String classroomID, String username, KeyLogsStructure keyLogs) throws InterruptedException, ExecutionException {
        addKeyLog(classroomID, username, keyLogs, null);
    }


    /**
     * adds a typing test result to the user's records.
     */
    public static void addKeyLog(String classroomID, String username, KeyLogsStructure keyLogs, String password) throws InterruptedException, ExecutionException {
        try {
            Firestore db = getFirestore();
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

    public static boolean updateDefaultTypingPlan (String classroomID, String username, TypingPlan newPlan) throws InterruptedException, ExecutionException {
        return updateDefaultTypingPlan(classroomID, username, newPlan, null);
    }

    public static boolean updateDefaultTypingPlan (String classroomID, String username, TypingPlan newPlan, String password) throws InterruptedException, ExecutionException {
        UserAccount user = getAccount(classroomID, username, password);
        if (user == null) {
            System.out.println("DATABASE FAILURE: Credentials incorrect or user doesn't exist.");
            return false;
        }

        try {
            Firestore db = getFirestore();
            DocumentReference userDoc = db.collection(USER_COLLECTION)
                    .document(classroomID + "," + username);

            ApiFuture<WriteResult> result = userDoc.update("defaultTypingPlan", newPlan);
            System.out.println("Updated default typing plan in database.");
            result.get();
            return true;

        } catch (Exception e) {
            System.out.println("DATABASE FAILURE: Failed to update typing plan - " + e.getMessage());
            return false;
        }
    }

    public static boolean updatePersonalisedTypingPlan(String classroomID, String username, TypingPlan newPlan) throws InterruptedException, ExecutionException {
        return updatePersonalisedTypingPlan(classroomID, username, newPlan, null);
    }

    public static boolean updatePersonalisedTypingPlan (String classroomID, String username, TypingPlan newPlan, String password) throws InterruptedException, ExecutionException {
        UserAccount user = getAccount(classroomID, username, password);
        if (user == null) {
            System.out.println("DATABASE FAILURE: Credentials incorrect or user doesn't exist.");
            return false;
        }

        try {
            Firestore db = getFirestore();
            DocumentReference userDoc = db.collection(USER_COLLECTION)
                    .document(classroomID + "," + username);

            ApiFuture<WriteResult> result = userDoc.update("personalisedTypingPlan", newPlan);
            System.out.println("Updated personalised typing plan in database.");
            result.get();
            return true;

        } catch (Exception e) {
            System.out.println("DATABASE FAILURE: Failed to update typing plan - " + e.getMessage());
            return false;
        }
    }

    public static TypingPlan getDefaultTypingPlan(String classroomID, String username) throws InterruptedException, ExecutionException {
        return getDefaultTypingPlan(classroomID, username, null);
    }

    public static TypingPlan getDefaultTypingPlan(String classroomID, String username, String password) throws InterruptedException, ExecutionException {
        UserAccount user = getAccount(classroomID, username, password);
        return user != null ? user.getDefaultTypingPlan() : null;
    }

    public static TypingPlan getPersonalisedTypingPlan(String classroomID, String username) throws InterruptedException, ExecutionException {
        return getPersonalisedTypingPlan(classroomID, username, null);
    }

    public static TypingPlan getPersonalisedTypingPlan(String classroomID, String username, String password) throws InterruptedException, ExecutionException {
        UserAccount user = getAccount(classroomID, username, password);
        return user != null ? user.getPersonalisedTypingPlan() : null;
    }

}
