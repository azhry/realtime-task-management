package com.example.acer.plnwunderlist;

/**
 * Created by Ryan Fadholi on 25/07/2017.
 */

public class TodoList {
    private String ID;
    private String name;
    private int accessType;

    public TodoList(String newID, String newName){
        this.ID = newID;
        this.name = newName;
    }

    public String getID(){
        return this.ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
