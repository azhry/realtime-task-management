package com.example.acer.plnwunderlist;

/**
 * Created by Ryan Fadholi on 25/07/2017.
 */

public class TodoList {
    private String ID;
    private String name;
    private int accessType;

    public TodoList(String newID, String newName, int accessType){
        this.ID = newID;
        this.name = newName;
        this.accessType = accessType;
    }

    public String getID(){
        return this.ID;
    }

    public int getAccessType() {
        return accessType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
