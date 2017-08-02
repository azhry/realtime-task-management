package com.example.acer.plnwunderlist;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by Azhary Arliansyah on 27/07/2017.
 * <p>
 * TodoItem.java
 * Custom class to display TodoItem in ListView with ArrayAdapter
 */

public class TodoItem {

    //JSON object keys
    private static final String TODO_ID_TAG = "TODO_ID";
    private static final String LIST_ID_TAG = "LIST_ID";
    private static final String TODO_DESC_TAG = "ITEM_DESC";
    private static final String TODO_NOTE_TAG = "NOTE";
    private static final String TODO_DATE_TAG = "DUE_DATE";


    private int ID;
    private int listID;
    private String description;
    private Date dueDate;
    private String note;
    private boolean completed;

    /**
     * Create new TodoItem object
     */
    public TodoItem(int newID, int listID, String description, String note, Date dueDate) {
        this.ID = newID;
        this.listID = listID;
        this.description = description;
        this.note = note;
        this.dueDate = dueDate;
        this.completed = false;
    }

    public static TodoItem newInstance(JSONObject param) {

        Integer newID = null;
        Integer newListID = null;
        String newDesc = null;
        String newNote = null;
        String rawDate = null;
        Date newDate = null;

        try {
            //Check for its TODO_ID existence
            if (param.has(TODO_ID_TAG)) {
                newID = param.getInt(TODO_ID_TAG);
            } else {
                Log.e("PLN-Comm", "JSON error, no " + TODO_ID_TAG + " found");
                return null;
            }

            //Check for its LIST_ID existence
            if (param.has(LIST_ID_TAG)) {
                newListID = param.getInt(LIST_ID_TAG);
            } else {
                Log.e("PLN-Comm", "JSON error, no " +  LIST_ID_TAG + " found");
                return null;
            }

            //Check for its ITEM_DESC existence
            if(param.has(TODO_DESC_TAG)){
                newDesc = param.getString(TODO_DESC_TAG);
            } else {
                Log.e("PLN-Comm","JSON error, no " + TODO_DESC_TAG + " found");
                return null;
            }

            //Grab all the extras
            newNote = param.getString(TODO_NOTE_TAG);
            rawDate = param.getString(TODO_DATE_TAG);

            SimpleDateFormat SQLDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            if(rawDate != null){
                newDate = SQLDateFormat.parse(rawDate);
            } else {
                newDate = null;
            }
        } catch (JSONException e) {
            Log.e("PLN-Comm", "JSON error " + e);
        } catch (ParseException e){
            Log.e("PLN-Comm","Error when parsing Task date from JSON, " + e);
        }

        return new TodoItem(newID, newListID, newDesc,newNote, newDate);
    }

    /**
     * This method is used to get this item id
     */
    public int getID() {
        return this.ID;
    }

    /**
     * This method is used to get list id from this item.
     * This also can be used to get what todo_list is this item belongs to.
     */
    public int getListID() {
        return this.listID;
    }

    /**
     * This method is used to get description from this item
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * This method is used to set the description of this item
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * This method is used to get due date from this item
     */
    public Date getDueDate() {
        return this.dueDate;
    }

    /**
     * This method is used to set the due date of this item
     */
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * This method is used to get note from this item
     */
    public String getNote() {
        return this.note;
    }

    /**
     * This method is used to set the note of this item
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * This method is used to check whether this item is completed or not
     */
    public boolean isCompleted() {
        return this.completed;
    }
}