package com.example.touchtyped.model;

import com.example.touchtyped.firestore.UserAccount;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class UserAccountTest {

    @Test
    void testUserAccountEmptyConstructor() {

        // act
        UserAccount userAccount = new UserAccount();

        // assert
        assertNull(userAccount.getClassroomID());
        assertNull(userAccount.getUserID());
        assertNull(userAccount.getUsername());
        assertNull(userAccount.getDefaultTypingPlan());
        assertNull(userAccount.getPersonalisedTypingPlan());
        assertNull(userAccount.getKeyLogs());
        assertNull(userAccount.getPassword());
        assertTrue(userAccount.getJoinedDate() > 0);

    }

    @Test
    void testUserAccountConstructor() {
        String classroomID = "C123456";
        String userID = "U123456";
        String username = "username1";
        TypingPlan defaultPlan = new TypingPlan();
        TypingPlan personalisedPlan = new TypingPlan();
        List<KeyLogsStructure> keyLogs = new ArrayList<>();
        String password = "password123";

        // act
        UserAccount userAccount = new UserAccount(classroomID, userID, username, defaultPlan, personalisedPlan, keyLogs, password);

        // assert
        assertEquals(classroomID, userAccount.getClassroomID());
        assertEquals(userID, userAccount.getUserID());
        assertEquals(username, userAccount.getUsername());
        assertEquals(defaultPlan, userAccount.getDefaultTypingPlan());
        assertEquals(personalisedPlan, userAccount.getPersonalisedTypingPlan());
        assertEquals(keyLogs, userAccount.getKeyLogs());
        assertEquals(password, userAccount.getPassword());

    }

    @Test
    void testKeyLogManagement() {
        UserAccount userAccount = new UserAccount();
        List<KeyLogsStructure> keyLogs = new ArrayList<>();
        KeyLogsStructure keyLog1 = new KeyLogsStructure("keyLog1words");
        KeyLogsStructure keyLog2 = new KeyLogsStructure("keyLog2words");
        keyLogs.add(keyLog1);
        keyLogs.add(keyLog2);

        // act
        userAccount.setKeyLogs(keyLogs);

        // assert
        assertEquals(2, userAccount.getKeyLogs().size());
        assertEquals(keyLog1, userAccount.getKeyLogs().get(0));
        assertEquals(keyLog2, userAccount.getKeyLogs().get(1));

    }



}
