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

    private static final String CACHE_DIR = ".touchtyped"; // stored at "~/.touchtyped/user_cache.properties
    private static final String CACHE_FILE = "user_cache.properties";

    private ClassroomDAO() {}

    /**
     * create a classroom, but without supplying a list of students.
     * @param ownerID is the teacher's ID.
     * @return the classroomID.
     */
    public static String createClassroom(String ownerID) throws InterruptedException, ExecutionException {
        return createClassroom(ownerID, null);
    }

    /**
     * create a classroom. generateClassroomID() is used to generate a unique ID for the classroom.
     * @param ownerID is the teacher's ID.
     * @param studentUsernames (OPTIONAL) is the list of students in the classroom.
     * @return the classroomID.
     */
    public static String createClassroom(String ownerID, List<String> studentUsernames) throws InterruptedException, ExecutionException {
        try {
            Firestore db = FirestoreClient.getFirestore();
            String classroomID = generateClassroomID();

            Classroom classroom = new Classroom(ownerID, classroomID, studentUsernames != null ? studentUsernames : new ArrayList<>());

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

    public static boolean saveUserCache(String classroomID, String username) {
        return saveUserCache(classroomID, username, null);
    }

    public static boolean saveUserCache(String classroomID, String username, String password) {
        Properties props = new Properties();
        Path cacheFile = Paths.get(System.getProperty("user.home"), CACHE_DIR, CACHE_FILE);

        if (Files.exists(cacheFile)) {
            try (InputStream input = Files.newInputStream(cacheFile)) {
                props.load(input);
            } catch (IOException e) {
                System.err.println("Failed to load existing cache");
            }
        }

        props.setProperty("classroomID", classroomID);
        props.setProperty("username", username);

        if (password != null) {
            props.setProperty("password", password);
        } else {
            props.remove("password"); // explicitly remove if null
        }

        try {
            Files.createDirectories(cacheFile.getParent());
            try (OutputStream output = Files.newOutputStream(cacheFile)) {
                props.store(output, "User cache - DO NOT MODIFY");
            }
            return true;
        } catch (IOException e) {
            System.err.println("CACHE ERROR: Failed to save user cache");
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String, String> loadUserCache() {
        Path cacheFile = Paths.get(System.getProperty("user.home"), CACHE_DIR, CACHE_FILE);
        if (!Files.exists(cacheFile)) return null;

        Properties props = new Properties();
        Map<String, String> result = new HashMap<>();

        try (InputStream input = Files.newInputStream(cacheFile)) {
            props.load(input);
            result.put("classroomID", props.getProperty("classroomID"));
            result.put("username", props.getProperty("username"));

            String password = props.getProperty("password");
            if (password != null) {
                result.put("password", password);
            }

            return result;
        } catch (IOException e) {
            System.err.println("CACHE ERROR: Failed to load user cache");
            return null;
        }
    }

}
