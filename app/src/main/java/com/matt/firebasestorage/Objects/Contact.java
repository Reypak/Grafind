package com.matt.firebasestorage.Objects;

public class Contact {
    String last_message;
    long timestamp;
    String userID;

    public Contact() {
    }

    public Contact(String last_message, long timestamp, String userID) {
        this.last_message = last_message;
        this.timestamp = timestamp;
        this.userID = userID;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
