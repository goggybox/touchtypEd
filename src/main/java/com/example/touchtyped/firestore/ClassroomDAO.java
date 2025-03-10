package com.example.touchtyped.firestore;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ClassroomDAO {

    private static final String CLASSROOM_COLLECTION = "classrooms";

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

            Classroom classroom = new Classroom(classroomID, ownerID, studentUsernames != null ? studentUsernames : new ArrayList<>());

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
                if (docRef.get().get().exists()) {
                    return id; // it is unique.
                }
            }

            throw new RuntimeException("DATABASE FAILURE. Failed to generate unique classroomID after "+max_attempts+" attempts.");

        } catch (Exception e) {
            System.out.println("DATABASE FAILURE. Failed to generate classroom ID.");
            return null;
        }

    }

}
