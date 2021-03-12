package com.matt.firebasestorage;

public class uploadinfo {
    public String title;
    public String imageURL;

    public uploadinfo() {
    }

    public uploadinfo(String name, String url) {
        this.title = name;
        this.imageURL = url;
    }

    public String getTitle() {
        return title;
    }

    public String getImageURL() {
        return imageURL;
    }
}
