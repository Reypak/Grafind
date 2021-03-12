package com.matt.firebasestorage.Objects;

public class nMessage {
    String userID;
    String message;
    long timestamp;
    String status;

    public nMessage(String userID, String message, long timestamp, String status) {
        this.userID = userID;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }

    public nMessage() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
