package com.example.touchtyped.firestore;

import com.example.touchtyped.model.TypingPlan;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * NOTE: these tests must be run with Google's Firestore Emulator running!
 * !!!
 * NOTE: REMEMBER TO RESTART EMULATOR BEFORE EACH TEST TO CLEAR IT!!!!!!!!!!!
 * !!!
 */
public class UserDAOTest {

    private static Firestore db;

    @BeforeAll
    static void setUp() {
        // set up an emulated database to interact with.
        db = FirestoreOptions.newBuilder()
                .setEmulatorHost("localhost:8080")
                .setProjectId("test-project")
                .build()
                .getService();
    }

    @Test
    void testCreateUserNoPassword() throws InterruptedException, ExecutionException {

        // make UserDAO use emulated database instead of real one.
        UserDAO.setTestFirestore(db);

        String classroomID = "C123456";
        String username = "user1";
        UserAccount userAccount = UserDAO.createUser(classroomID, username, null, new TypingPlan());
        assertNotNull(userAccount);
        assertEquals(classroomID, userAccount.getClassroomID());
        assertEquals(username, userAccount.getUsername());

        // verify that the user has been added to database
        UserAccount fetchedAccount = UserDAO.getAccount(classroomID, username);

        assertEquals(userAccount.getUserID(), fetchedAccount.getUserID());
    }

}
