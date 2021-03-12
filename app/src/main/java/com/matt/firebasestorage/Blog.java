package com.matt.firebasestorage;

public class Blog {
    String title;
    String description;
    String imageURL;
    String userID;
    String timestamp;

    public Blog() {
    }

    public Blog(String title, String description, String imageURL, String userID, String timestamp) {
        this.title = title;
        this.description = description;
        this.imageURL = imageURL;
        this.userID = userID;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
