package com.matt.firebasestorage.Objects;

public class User {
    String email;
    String location;
    String phone;
    String username;

    public User() {
    }

    public User(String email, String location, String phone, String username) {
        this.email = email;
        this.location = location;
        this.phone = phone;
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
