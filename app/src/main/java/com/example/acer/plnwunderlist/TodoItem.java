package com.example.acer.plnwunderlist;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public class TodoItem implements Parcelable {

    //JSON object keys
    private static final String TODO_ID_TAG = "TODO_ID";
    private static final String LIST_ID_TAG = "LIST_ID";
    private static final String TODO_DESC_TAG = "ITEM_DESC";
    private static final String TODO_NOTE_TAG = "NOTE";
    private static final String TODO_DATE_TAG = "DUE_DATE";
    private static final String TODO_ASSIGNED_USER_TAG = "ASSIGNEE_ID";

    private int ID;
    private int listID;
    private String description;
    private Date dueDate;
    private String note;
    private String assignedUserID;
    private boolean completed;
    private boolean hasFiles;

    /**
     * Create new TodoItem object
     */
//    public TodoItem(int newID, int listID, String description, String note, Date dueDate) {
//        this.ID = newID;
//        this.listID = listID;
//        this.description = description;
//        this.note = note;
//        this.dueDate = dueDate;
//        this.completed = false;
//        this.hasFiles = false;
//    }

    public TodoItem(int newID, int listID, String description,
                    String note, Date dueDate, String assignedUser) {
        this.ID = newID;
        this.listID = listID;
        this.description = description;
        this.note = note;
        this.dueDate = dueDate;
        this.completed = false;
        this.hasFiles = false;
        this.assignedUserID = assignedUser;
    }

    TodoItem(Parcel in){
        this.ID =  in.readInt();
        this.listID = in.readInt();
        this.description = in.readString();
        this.note = in.readString();
        //completed == true if readByte != 0, conforming to the parcelling (?) method.
        this.completed = in.readByte() != 0;
        //Because the date can be null (which was indicated as -1 in writeToParcel),
        //first read the long in a temporary value.
        long tempDate = in.readLong();

        if(tempDate != -1){
            this.dueDate = new Date(tempDate);
        } else {
            this.dueDate = null;
        }

        this.assignedUserID = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.ID);
        parcel.writeInt(this.listID);
        parcel.writeString(this.description);
        parcel.writeString(this.note);
        //Because boolean is not natively supported, parcel completed as byte instead.
        parcel.writeByte((byte) (completed ? 1 : 0));
        //Because java.util.Date is not natively supported, parcel its Long value instead.
        parcel.writeLong(dueDate != null ? dueDate.getTime() : -1);
        parcel.writeString(this.assignedUserID);
    }

    public static TodoItem newInstance(JSONObject param) {

        Integer newID = null;
        Integer newListID = null;
        String newDesc = null;
        String newNote = null;
        Date newDate = null;
        String newAssigneeID = null;

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

            //Retrieve the object temporarily at first, and set newNote as null.
            if(!param.isNull(TODO_NOTE_TAG)){
                newNote = param.getString(TODO_NOTE_TAG);
            }

            SimpleDateFormat SQLDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            if(!param.isNull(TODO_DATE_TAG)){
                newDate = SQLDateFormat.parse(param.getString(TODO_DATE_TAG));
            } else {
                newDate = null;
            }


            //Check for its ASSIGNED_USER existence
            if(param.has(TODO_ASSIGNED_USER_TAG)){
                newAssigneeID = param.getString(TODO_ASSIGNED_USER_TAG);
            } else {
                Log.e("PLN-Comm","JSON error, no " + TODO_ASSIGNED_USER_TAG + " found");
                newAssigneeID = null;
//                return null;
            }

        } catch (JSONException e) {
            Log.e("PLN-Comm", "JSON error " + e);
        } catch (ParseException e){
            Log.e("PLN-Comm","Error when parsing Task date from JSON, " + e);
        }

        TodoItem newTodoItem = new TodoItem(newID, newListID, newDesc,newNote, newDate, newAssigneeID);
        try {
            newTodoItem.setHasFiles(param.getBoolean("HAS_FILES"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newTodoItem;
    }

    public static final Creator<TodoItem> CREATOR = new Creator<TodoItem>() {
        @Override
        public TodoItem[] newArray(int size) {
            return new TodoItem[size];
        }

        @Override
        public TodoItem createFromParcel(Parcel source) {
            return new TodoItem(source);
        }
    };

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

    public boolean hasFiles() {
        return this.hasFiles;
    }

    public void setHasFiles(boolean hasFiles) {
        this.hasFiles = hasFiles;
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

    public String getAssignedUserID() {
        return assignedUserID;
    }

    public void setAssignedUserID(String assignedUserID) {
        this.assignedUserID = assignedUserID;
    }
}