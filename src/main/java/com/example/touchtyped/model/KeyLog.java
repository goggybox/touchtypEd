package com.example.touchtyped.model;

public class KeyLog {

    private String key;
    private String expected;
    private long timestamp;
    private boolean error;

    public KeyLog() {

    }

    /**
     * constructor
     */
    public KeyLog(String key, String expected, long timestamp, boolean error) {
        this.key = key;
        this.expected = expected;
        this.timestamp = timestamp;
        this.error = error;
    }

    @Override
    public String toString() {
        return String.format("key: %s, timestamp: %d, expected: %s, error: %b", key, timestamp, expected, error);
    }


    /**
     * Getters and setters
     */
    public boolean getError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
