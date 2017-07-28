package com.example.acer.plnwunderlist;

import java.sql.Date;

/**
 * Created by Azhary Arliansyah on 27/07/2017.
 *
 * TodoItem.java
 * Custom class to display TodoItem in ListView with ArrayAdapter
 */

public class TodoItem {
    private int ID;
    private int listID;
    private String description;
    private Date dueDate;
    private String note;
    private boolean completed;

    /** Create new TodoItem object */
    public TodoItem(int newID, int listID, String description, String note, Date dueDate) {
        this.ID             = newID;
        this.listID         = listID;
        this.description    = description;
        this.note           = note;
        this.dueDate        = dueDate;
        this.completed      = false;
    }


    /** This method is used to get this item id */
    public int getID() { return this.ID; }

    /**
     * This method is used to get list id from this item.
     * This also can be used to get what todo_list is this item belongs to.
     */
    public int getListID() { return this.listID; }

    /** This method is used to get description from this item */
    public String getDescription() { return this.description; }

    /** This method is used to get due date from this item */
    public Date getDueDate() { return this.dueDate; }

    /** This method is used to get note from this item */
    public String getNote() { return this.note; }

    /** This method is used to check whether this item is completed or not */
    public boolean isCompleted() { return this.completed; }


    /** This method is used to set the description of this item */
    public void setDescription(String description) { this.description = description; }

    /** This method is used to set the due date of this item */
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    /** This method is used to set the note of this item */
    public void setNote(String note) { this.note = note; }
}