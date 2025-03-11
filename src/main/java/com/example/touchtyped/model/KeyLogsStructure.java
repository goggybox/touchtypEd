package com.example.touchtyped.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KeyLogsStructure {

    /**
     * represents the following JSON structure:
     * {
     *   "keyLogs": [
     *     {"key": "H", "timestamp": 100, "expected": "H", "error": false},
     *     {"key": "E", "timestamp": 200, "expected": "E", "error": false},
     *     {"key": "L", "timestamp": 300, "expected": "L", "error": false},
     *     {"key": "L", "timestamp": 400, "expected": "L", "error": false},
     *     {"key": "O", "timestamp": 500, "expected": "O", "error": false},
     *     {"key": "Space", "timestamp": 600, "expected": " ", "error": false},
     *     {"key": "W", "timestamp": 700, "expected": "W", "error": false},
     *     {"key": "R", "timestamp": 800, "expected": "O", "error": true},
     *     {"key": "O", "timestamp": 900, "expected": "R", "error": true},
     *     {"key": "Backspace", "timestamp": 950, "error": true},
     *     {"key": "Backspace", "timestamp": 1000, "error": true},
     *     {"key": "O", "timestamp": 1100, "expected": "O", "error": false},
     *     {"key": "R", "timestamp": 1200, "expected": "R", "error": false},
     *     {"key": "L", "timestamp": 1300, "expected": "L", "error": false},
     *     {"key": "D", "timestamp": 1400, "expected": "D", "error": false}
     *   ],
     *   "sessionDuration": 5000,
     *   "wordsGiven": "Hello World"
     * }
     */

    private List<KeyLog> keyLogs = new ArrayList<>();
    private long sessionDuration = 0; // will update every time a KeyLog is added
    private String wordsGiven = "";
    private int charPosition = 0;
    private int errors = 0;

    /**
     * constructor. This class must be given a wordsGiven.
     */
    public KeyLogsStructure(String wordsGiven) {
        this.wordsGiven = wordsGiven; // force the words to uppercase
    }

    /**
     * add a new KeyLog to the list, based on an inputted key and the timestamp at which the key press occurred.
     * based on the expected character from the wordsGiven, create a KeyLog instance, giving it an expected and an error.
     * @param key is the key which was pressed
     * @param timestamp is the timestamp at which the key was pressed
     */
    public void addKeyLog(String key, long timestamp) {
        if (charPosition < 0) {
            charPosition = 0;
        } else if (charPosition > wordsGiven.length()) {
            charPosition = wordsGiven.length();
        }

        // determine the expected keypress based on the expected character in wordsGiven at charPosition.
        // if the last char was mistyped, errors == 1, and the user SHOULD be pressing BACK_SPACE to fix the error.
        String expected = null;
        if (errors > 0) {
            expected = "BACK_SPACE";
        } else {
            if (charPosition < wordsGiven.length()) {
                expected = String.valueOf(wordsGiven.charAt(charPosition));
            }
        }

        // if the user HAS pressed BACK_SPACE, they are trying to fix the error.
        if (expected != null && expected.equals("BACK_SPACE") && key.equals("BACK_SPACE")) {
            errors = Math.max(errors - 1, 0);
        }

        // determine whether this keypress was an error based on the expected character
        boolean error = false;
        if (expected == null || !Objects.equals(key, expected)) {
            error = true;
            if (!key.equals("BACK_SPACE")) { errors++; }
        }

        // build and add a new KeyLog
        KeyLog keyLog = new KeyLog(key, expected, timestamp, error);
        keyLogs.add(keyLog);

        // calculate session duration to be the time between the first and most recent KeyLog's timestamps
        // if there is only 0 or 1 logs in the structure, keep sessionDuration at 0.
        if (keyLogs.size() > 1) {
            sessionDuration = timestamp - keyLogs.get(0).getTimestamp();
        }

        // if the key is a BACK_SPACE, decrement charPosition, otherwise increment it.
        if (Objects.equals(key, "BACK_SPACE")) {
            // avoid -1 index
            if (charPosition > 0) {
                charPosition--;
            }
        } else {
            charPosition++;
        }

        // clamp again
        if (charPosition < 0) {
            charPosition = 0;
        } else if (charPosition > wordsGiven.length()) {
            charPosition = wordsGiven.length();
        }

    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (KeyLog keyLog : keyLogs) {
            output.append(keyLog.toString()).append("\n");
        }

        output.append("Session Duration: " + sessionDuration + "\nWords Given: " + wordsGiven);
        return output.toString();
    }



    /**
     * Getters and setters
     */
    public List<KeyLog> getKeyLogs() {
        return keyLogs;
    }

    public void setKeyLogs(List<KeyLog> keyLogs) {
        this.keyLogs = keyLogs;
    }

    public long getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(long sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public String getWordsGiven() {
        return wordsGiven;
    }

    public void setWordsGiven(String wordsGiven) {
        this.wordsGiven = wordsGiven.toUpperCase();
    }

}
