package com.example.touchtyped.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import javax.swing.text.Document;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class ClassroomDAO {

    private static final String CLASSROOM_COLLECTION = "classrooms";
    private static final String CACHE_FILE = "user_cache.txt";

    private ClassroomDAO() {}

    /**
     * create a classroom, but without supplying a list of students.
     * @param ownerID is the teacher's ID.
     * @return the classroomID.
     */
    public static String createClassroom(String ownerID, String classroomName) throws InterruptedException, ExecutionException {
        return createClassroom(ownerID, classroomName, null);
    }

    /**
     * create a classroom. generateClassroomID() is used to generate a unique ID for the classroom.
     * @param ownerID is the teacher's ID.
     * @param classroomName is the classroom name.
     * @param studentUsernames (OPTIONAL) is the list of students in the classroom.
     * @return the classroomID.
     */
    public static String createClassroom(String ownerID, String classroomName, List<String> studentUsernames) throws InterruptedException, ExecutionException {
        try {
            Firestore db = FirestoreClient.getFirestore();
            String classroomID = generateClassroomID();

            Classroom classroom = new Classroom(ownerID, classroomID, classroomName, studentUsernames != null ? studentUsernames : new ArrayList<>());

            DocumentReference docRef = db.collection(CLASSROOM_COLLECTION).document(classroomID);
            docRef.set(classroom).get();

            return classroomID;

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to create classroom.");
        }
    }

    /**
     * generate a classroomID, ensuring uniqueness.
     * @return the generated classroomID
     */
    public static String generateClassroomID() {
        try {
            Firestore db = FirestoreClient.getFirestore();
            SecureRandom random = new SecureRandom();
            String char_pool = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            int max_attempts = 100;

            for (int i = 0; i < max_attempts; i++) {
                // generate potential ID
                StringBuilder sb = new StringBuilder("C");
                for (int j = 0; j < 6; j++) {
                    int index = random.nextInt(char_pool.length());
                    sb.append(char_pool.charAt(index));
                }
                String id = sb.toString();

                // check uniqueness
                DocumentReference docRef = db.collection(CLASSROOM_COLLECTION).document(id);
                if (!docRef.get().get().exists()) {
                    return id; // it is unique.
                }
            }

            throw new RuntimeException("DATABASE FAILURE. Failed to generate unique classroomID after "+max_attempts+" attempts.");

        } catch (Exception e) {
            System.out.println("DATABASE FAILURE. Failed to generate classroom ID.");
            return null;
        }

    }

    /**
     * fetch a classroom from the database given an ID.
     * @param classroomID is the classroom ID to search for.
     * @return the specified Classroom, or null if it doesn't exist.
     */
    public static Classroom getClassroom(String classroomID) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection(CLASSROOM_COLLECTION).document(classroomID);
            DocumentSnapshot doc = docRef.get().get();
            if (doc.exists()) {
                return doc.toObject(Classroom.class);
            } else {
                System.out.println("Failed to get classroom: classroomID does not exist.");
                return null;
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.out.println("Failed to get classroom.");
            return null;
        }
    }

    public static boolean replaceStudentList(String classroomID, List<String> studentList) throws InterruptedException, ExecutionException {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection(CLASSROOM_COLLECTION).document(classroomID);

            // Update the list of students
            ApiFuture<WriteResult> result = docRef.update("studentUsernames", studentList);
            result.get(); // Ensure the update is successful by waiting for the result.

            return true;
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.out.println("DATABASE FAILURE: Failed to update list of students in classroom "+classroomID);
            return false;
        }
    }

    public static Boolean classroomExists(String classroomID) throws InterruptedException, ExecutionException {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection(CLASSROOM_COLLECTION).document(classroomID);
            return docRef.get().get().exists();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.out.println("DATABASE FAILURE. Failed to check if classroom exists.");
            return null;
        }
    }

    /**
     * add a student's username to a Classroom's list of students in the database. this does not create a new student
     * account, so this assumes a student with this username + classroomID combination already exists in the database.
     * @param classroomID is the classroomID to add the student to.
     * @param username is the student's username.
     * @return whether or not the operation was successful.
     */
    public static boolean addStudentToClassroom(String classroomID, String username) throws InterruptedException, ExecutionException {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection(CLASSROOM_COLLECTION).document(classroomID);
            ApiFuture<WriteResult> result = docRef.update("studentUsernames", FieldValue.arrayUnion(username));
            result.get();
            return true;
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.out.println("DATABASE FAILURE. Failed to add student to classroom.");
            return false;
        }
    }

    /**
     * check whether a student is in a classroom, by checking for their username in the list of students.
     * @param classroomID is the classroom to check.
     * @param username is the student's username.
     * @return whether or not the student exists in the classroom, or null if the operation failed.
     */
    public static Boolean usernameExistsInClassroom(String classroomID, String username) throws InterruptedException, ExecutionException {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection(CLASSROOM_COLLECTION).document(classroomID);
            DocumentSnapshot doc = docRef.get().get();

            if (!doc.exists()) {
                return false;
            }

            Classroom classroom = doc.toObject(Classroom.class);
            List<String> students = classroom.getStudentUsernames();
            return students != null && students.contains(username);

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.out.println("DATABASE ERROR. Failed to check if username exists in classroom.");
            return null;
        }
    }

    /**
     * does the same as the following saveUserCache function, but making the password argument optional.
     */
    public static boolean saveUserCache(String classroomID, String username) {
        return saveUserCache(classroomID, username, null);
    }

    /**
     * save the user's account information to be loaded next time the application starts.
     * @param classroomID is the classroomID to store.
     * @param username is the user's username to store.
     * @param password (OPTIONAL) is the user's password to store.
     * @return whether or not the operation was successful.
     */
    public static boolean saveUserCache(String classroomID, String username, String password) {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("classroomID=" + classroomID);
            lines.add("username=" + username);

            if (password != null && !password.isEmpty()) {
                lines.add("password=" + password);
            }

            Files.write(Paths.get(CACHE_FILE), lines);
            return true;
        } catch (IOException e) {
            System.err.println("CACHE ERROR: Failed to save user cache");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean cacheExists() {
        return Files.exists(Paths.get(CACHE_FILE));
    }

    /**
     * load the user's information from cache.
     * @return a Map of the information (with field "classroomID", "username", and (optionally) "password").
     */
    public static Map<String, String> loadUserCache() {
        Path cachePath = Paths.get(CACHE_FILE);
        if (!Files.exists(cachePath)) {
            return null; // No cache file exists
        }

        try {
            Map<String, String> cacheData = new HashMap<>();
            List<String> lines = Files.readAllLines(cachePath);
            for (String line : lines) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    cacheData.put(parts[0], parts[1]);
                }
            }
            return cacheData;
        } catch (IOException e) {
            System.err.println("CACHE ERROR: Failed to load user cache");
            e.printStackTrace();
            return null;
        }
    }

    public static boolean deleteUserCache() {
        try {
            Path cachePath = Paths.get(CACHE_FILE);
            return Files.deleteIfExists(cachePath);
        } catch (IOException e) {
            System.err.println("CACHE ERROR: Failed to delete user cache");
            e.printStackTrace();
            return false;
        }
    }

}
