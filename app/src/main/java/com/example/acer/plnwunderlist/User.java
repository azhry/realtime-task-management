package com.example.acer.plnwunderlist;

/**
 * Created by Ryan Fadholi on 31/07/2017.
 */

public class User {
    private int userID;
    private String email;
    private String name;

    public User(int userID, String email, String name) {
        this.userID = userID;
        this.email = email;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserID() {
        return userID;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
