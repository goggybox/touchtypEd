package com.example.touchtyped.firestore;

import com.example.touchtyped.model.TypingPlan;
import com.google.cloud.firestore.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * NOTE: these tests must be run with Google's Firestore Emulator running!
 * !!!
 * TODO: NOTE: THE DATABASE IS CLEARED BEFORE EACH TEST.
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

        // make UserDAO use emulated database instead of real one.
        UserDAO.setTestFirestore(db);
    }

    @Test
    void testCreateUserNoPassword() throws InterruptedException, ExecutionException {

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

    @Test
    void testCreateUserWithPassword() throws InterruptedException, ExecutionException {
        String classroomID = "C123546";
        String username = "user2";
        String password = "supersecure123abc";
        UserAccount userAccount = UserDAO.createUser(classroomID, username, null, new TypingPlan(), password);
        assertNotNull(userAccount);
        assertEquals(classroomID, userAccount.getClassroomID());
        assertEquals(username, userAccount.getUsername());
        assertEquals(password, userAccount.getPassword());

        // verify that the user has been added to database
        UserAccount fetchedAccount = UserDAO.getAccount(classroomID, username, password);
        assertEquals(userAccount.getUserID(), fetchedAccount.getUserID());
    }


}
