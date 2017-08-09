package com.example.acer.plnwunderlist;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;
import com.example.acer.plnwunderlist.Singleton.WebSocketClientManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TaskDetailsActivity extends AppCompatActivity {

    public static String EXTRA_USER_ACTION = "TaskDetails.UserAction";
    public static String EXTRA_ITEM_DATA = "TaskDetails.ItemData";
    public static int USER_ACT_DELETE = -1;
    public static int USER_ACT_FINISH = 1;
    private static final int PICK_FILE_REQUEST = 1;

    private FileListPseudoAdapter fileListPseudoAdapter;
    private LinearLayout fileList;

    private String endpoint;
    private String listID;
    private String listName;
    private String selectedFilePath;
    private boolean isUpdate;
    //Updateable views
    private Button addEditDueDateBtn;
    private Button deleteDueDateBtn;
    private Button addFileBtn;
    private EditText taskNameInput, noteInput;
    private TextView dueDateInput;

    //Extra state-changeable views
    private View fileDivider;
    private TextView fileLabel;
    private TextView noFileLabel;
    private LinearLayout fileListLayout;
    private LinearLayout fileBtns;

    private TodoItem item;
    private Date tempDate;

    private DBPLNHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_FILE_REQUEST);
            Log.e("REQUEST_PERMISSION", "TRUE");
        }

        setTitle("Task Details");

        db = new DBPLNHelper(this);

        endpoint = getString(R.string.uri_endpoint);

        //Initialize Button values
        addEditDueDateBtn = (Button) findViewById(R.id.addEditDueDateBtn);
        deleteDueDateBtn = (Button) findViewById(R.id.deleteDueDateBtn);
        addFileBtn = (Button) findViewById(R.id.addFileBtn);

        //Initialize EditText values
        taskNameInput = (EditText) findViewById(R.id.taskNameEdit);
        noteInput = (EditText) findViewById(R.id.editTaskNote);
        dueDateInput = (TextView) findViewById(R.id.taskDueDateEdit);

        //Initialize extra views
        fileDivider = findViewById(R.id.fileDivider);
        fileLabel = (TextView) findViewById(R.id.taskFileLabel);
        noFileLabel = (TextView) findViewById(R.id.noFileLabel);
        fileListLayout = (LinearLayout) findViewById(R.id.fileListView);
        fileBtns = (LinearLayout) findViewById(R.id.uploadBtnParent);

        //Initialize TodoItem and temporary values
        item = null;
        tempDate = null;
        isUpdate = false;

        if (getIntent().hasExtra("TODO_LIST_ID")) {
            listID = getIntent().getStringExtra("TODO_LIST_ID");
        }

        if (getIntent().hasExtra("TODO_OBJECT")) {
            item = getIntent().getParcelableExtra("TODO_OBJECT");
            setupInputFields(item);
            isUpdate = true;
        } else {
            fileDivider.setVisibility(View.GONE);
            fileLabel.setVisibility(View.GONE);
            fileListLayout.setVisibility(View.GONE);
            fileBtns.setVisibility(View.GONE);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fileList = (LinearLayout) findViewById(R.id.fileListView);
        fileListPseudoAdapter = new FileListPseudoAdapter(fileList, this);

        if (item != null) {
            String todoFilesRequestUrl = endpoint + "?action=get_todo_files&todo_id=" + item.getID();
            StringRequest todoFilesRequest = new StringRequest(Request.Method.GET, todoFilesRequestUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray filesArray = new JSONArray(response);
                                int len = filesArray.length();
                                if (len > 0) {
                                    for (int i = 0; i < len; i++) {
                                        JSONObject file = filesArray.getJSONObject(i);
                                        fileListPseudoAdapter.add(Integer.parseInt(file.getString("FILE_ID")),
                                                file.getString("FILENAME"));
                                    }
                                    noFileLabel.setVisibility(View.GONE);
                                } else {
                                    noFileLabel.setVisibility(View.VISIBLE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });

            AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(todoFilesRequest, "todo_item_files");
        }

        addEditDueDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "Test");
            }
        });
        deleteDueDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDueDateInput((Calendar) null);
            }
        });

        addFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.task_details_actionbar_menu, menu);

        //------------------------------------------------------------------------------------------
        //START Menu Icon Tinting

        //Retrieve all Menu Items
        final MenuItem deleteBtn = menu.findItem(R.id.deleteTaskBtn);
        final MenuItem completeBtn = menu.findItem(R.id.finishEditBtn);

        //Retrieve all Icons
        Drawable deleteBtnIcon = deleteBtn.getIcon();
        Drawable completeBtnIcon = completeBtn.getIcon();

        //Tint the shit out <-- bukan aku pak
        deleteBtnIcon.mutate().setColorFilter(Color.argb(255, 255, 255, 255), PorterDuff.Mode.SRC_IN);
        completeBtnIcon.mutate().setColorFilter(Color.argb(255, 255, 255, 255), PorterDuff.Mode.SRC_IN);

        //END Menu Icon Tinting
        //------------------------------------------------------------------------------------------

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.finishEditBtn:
                startUpdateProcedure();
                return true;
            case R.id.deleteTaskBtn:
                showDeleteConfirmationDialog();
                return true;
            default:
                break;
        }

        return true;
    }

    private void deleteItem(){
        Intent returnIntent = new Intent();

        Log.e("DELETION","DeleteItem called");

        returnIntent.putExtra(EXTRA_USER_ACTION, USER_ACT_DELETE);
        returnIntent.putExtra(EXTRA_ITEM_DATA, item);
        setResult(RESULT_OK,returnIntent);
        finish();
    }

    private void setDateBtnsVisibility(boolean isDateSet) {
        if (isDateSet) {
            deleteDueDateBtn.setVisibility(View.VISIBLE);
            addEditDueDateBtn.setText("Edit Due Date");
        } else {
            addEditDueDateBtn.setText("Set Due Date");
            deleteDueDateBtn.setVisibility(View.GONE);
        }
    }

    private void setupInputFields(TodoItem item) {
        taskNameInput.setText(item.getDescription());

        if (item.getDueDate() != null) {
            this.tempDate = item.getDueDate();
            this.updateDueDateInput(item.getDueDate());
        }

        if (item.getNote() != null) {
            this.noteInput.setText(item.getNote());
        }
    }

    private String checkNullable(String str) {
        if (str.isEmpty()) {
            return null;
        }
        return str;
    }

    private String getNewDesc() {
        return checkNullable(taskNameInput.getText().toString());
    }

    private String getNewNote() {
        return checkNullable(noteInput.getText().toString());
    }

    private Boolean isDescriptionValid(String desc) {
        //------------------------------------------------------------------------------------------
        //START AlertDialog Definition
        final AlertDialog.Builder invalidDescriptionDialog = new AlertDialog.Builder(this);

        //Set its title and view
        String dialogMsg = "Task name is empty. Task must have a name.";

        //Set title and message
        invalidDescriptionDialog.setTitle("Error").setMessage(dialogMsg);

        //Add the "Positive" (Right button) logic
        invalidDescriptionDialog.setPositiveButton(getString(R.string.dialog_default_positive_labeal), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //END AlertDialog Definition
        //------------------------------------------------------------------------------------------

        if (desc == null) {
            invalidDescriptionDialog.show();
        }

        return desc != null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data == null) {
                    return;
                }

                Uri selectedFileUri = data.getData();
                selectedFilePath = FilePath.getPath(this, selectedFileUri);
                Log.e("ON_ACTIVITY_RESULT", "Selected file path: " + selectedFilePath);
                if (selectedFilePath != null && !selectedFilePath.equals("")) {
                    Log.e("ON_ACTVT_RESULT_SUCCESS", "Selected file path: " + selectedFilePath);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FILE_REQUEST);
                        Log.e("REQUEST_PERMISSION", "TRUE");
                    }

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("todo_id", String.valueOf(item.getID()));

                    File uploadFile = new File(selectedFilePath);
                    new FileUploaderTask(endpoint, uploadFile,
                            TaskDetailsActivity.this,
                            TaskDetailsActivity.this,
                            params).execute();
                } else {
                    Log.e("ON_ACTIVITY_RESULT", "Cannot upload file to server");
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        startBackProcedure();
    }

    @Override
    public boolean onSupportNavigateUp() {
        startBackProcedure();
        return true;
    }

    private void startBackProcedure() {

        String changedItems = new String();
        boolean commaFlag = false;
        String newTaskDesc = getNewDesc();
        String newTaskNote = getNewNote();

        boolean isChanged = false;
        boolean isCreateNew = false;

        //If there's no existing item, flag the activity as Create New
        if (item == null) {
            isCreateNew = true;
        }
        //Else check for changes
        else {
            //Check for description change
            if (!item.getDescription().equals(newTaskDesc)) {
                changedItems = changedItems.concat("task name");
                isChanged = true;
                commaFlag = true;
            }

            //Check for due date change
            //first check for when the Date is unset (NULL)
            if (item.getDueDate() == null) {
                if (tempDate != null) {
                    changedItems = changedItems.concat(commaFlag ? ", due date" : "due date");
                    isChanged = true;
                    commaFlag = true;
                }
            } else if (!item.getDueDate().equals(tempDate)) {
                changedItems = changedItems.concat(commaFlag ? ", due date" : "due date");
                isChanged = true;
                commaFlag = true;
            }

            //Check for notes change
            if (item.getNote() == null) {
                if (newTaskNote != null) {
                    changedItems = changedItems.concat(commaFlag ? ", note" : "note");
                    isChanged = true;
                }
            } else if (!item.getNote().equals(newTaskNote)) {
                changedItems = changedItems.concat(commaFlag ? ", note" : "note");
                isChanged = true;
            }
        }

        if (!isCreateNew) {
            if (!isChanged) {
                finish();
            } else {
                showEditConfirmationDialog(changedItems);
            }
        } else {
            showAddConfirmationDialog();
            finish();
        }

    }

    private void startUpdateProcedure() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_USER_ACTION, USER_ACT_FINISH);

        String taskName = getNewDesc();
        String note = getNewNote();
        String dueDate = null;

        if (!isDescriptionValid(taskName)) {
            return;
        }

        SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (tempDate != null) {
            dueDate = sqlDateFormat.format(tempDate);
            Log.e("DATE", dueDate);
        }

        Map<String, String> data = new HashMap<String, String>();
        if (isUpdate) {
            data.put("todo_id", String.valueOf(item.getID()));

            //If the item is completed, set the value as 1. Otherwise set as 0.
            data.put("is_completed",
                    String.valueOf(item.isCompleted() ? 1 : 0));
        }

        data.put("task_name", taskName);
        data.put("list_id", listID);
        data.put("due_date", dueDate);
        data.put("note", note);
        updateTask(data, returnIntent);
    }

    private void showAddConfirmationDialog() {

        //------------------------------------------------------------------------------------------
        //START AlertDialog Definition
        final AlertDialog.Builder confirmationDialogBuilder = new AlertDialog.Builder(this);

        //Set its title and view
        String dialogMsg = getString(R.string.add_dialog);

        //Set title and message
        confirmationDialogBuilder.setTitle("Quit").setMessage(dialogMsg);

        //Add the "Positive" (Right button) logic
        confirmationDialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Call the update function
                startUpdateProcedure();
            }
        });
        //Add the "Negative" (Left button) logic
        confirmationDialogBuilder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        //Add the "Neutral" button logic
        confirmationDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        //END AlertDialog Definition
        //------------------------------------------------------------------------------------------

        AlertDialog quitConfirmation = confirmationDialogBuilder.create();
        quitConfirmation.show();

    }

    private void showEditConfirmationDialog(String changedItems) {

        //------------------------------------------------------------------------------------------
        //START AlertDialog Definition
        final AlertDialog.Builder confirmationDialogBuilder = new AlertDialog.Builder(this);

        //Set its title and view
        String dialogMsg = getString(R.string.edit_dialog_start).
                concat(changedItems).concat(getString(R.string.edit_dialog_end));

        //Set title and message
        confirmationDialogBuilder.setTitle("Quit").setMessage(dialogMsg);

        //Add the "Positive" (Right button) logic
        confirmationDialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Call the update function
                startUpdateProcedure();
            }
        });
        //Add the "Negative" (Left button) logic
        confirmationDialogBuilder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        //Add the "Neutral" button logic
        confirmationDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        //END AlertDialog Definition
        //------------------------------------------------------------------------------------------

        AlertDialog quitConfirmation = confirmationDialogBuilder.create();
        quitConfirmation.show();
    }

    private void showDeleteConfirmationDialog() {

        //------------------------------------------------------------------------------------------
        //START AlertDialog Definition
        final AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this);

        //Set its title and view
        String dialogMsg = "Are you sure you want to delete this task?";

        //Set title and message
        deleteDialogBuilder.setTitle("Delete Task").setMessage(dialogMsg);

        //Add the "Positive" (Right button) logic
        deleteDialogBuilder.setPositiveButton(R.string.dialog_default_positive_labeal, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteItem();
            }
        });
        //Add the "Negative" (Left button) logic
        deleteDialogBuilder.setNegativeButton(R.string.dialog_default_negative_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //END AlertDialog Definition
        //------------------------------------------------------------------------------------------

        AlertDialog deleteConfirmation = deleteDialogBuilder.create();
        deleteConfirmation.show();
    }


    public void updateDueDateInput(java.util.Date d) {
        if (d == null) {
            updateDueDateInput((Calendar) null);
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        updateDueDateInput(cal);
    }

    public void updateDueDateInput(Calendar c) {
        if (c == null) {
            dueDateInput.setText(R.string.null_due_date_label);
            tempDate = null;
            this.setDateBtnsVisibility(false);
            return;
        }

        dueDateInput.setText(AppHelper.formatDate(c, true));
        tempDate = c.getTime();

        this.setDateBtnsVisibility(true);
    }

    private void saveItemToLocalStorage(int todoID, int listID, String itemDesc, String dueDate, String note,
                                        int completed, int status, boolean success, String mode) {
        Map<String, String> contentValues = new HashMap<>();
        contentValues.put("TODO_ID", String.valueOf(todoID));
        contentValues.put("LIST_ID", String.valueOf(listID));
        contentValues.put("ITEM_DESC", itemDesc);
        contentValues.put("DUE_DATE", dueDate);
        contentValues.put("NOTE", note);
        contentValues.put("IS_COMPLETED", String.valueOf(completed));
        contentValues.put("STATUS", String.valueOf(status));
        if (success) {
            contentValues.put("SERVER_ID", String.valueOf(todoID));
        } else {
            contentValues.put("SERVER_ID", "0");
        }

        if (mode.equals("insert_todo_item")) {
            contentValues.put("ACTION", "0");
            db.insert("todo_items", contentValues);
        } else if (mode.equals("update_todo_item")) {
            if (success) {
                contentValues.put("ACTION", "0");
            } else {
                contentValues.put("ACTION", "1");
            }
            db.update("todo_items", contentValues, "TODO_ID=" + todoID);
        }
    }

    private void updateTask(final Map<String, String> paramData, final Intent intent) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        final String updateMode;

        progressDialog.setMessage("Processing...");
        progressDialog.show();

        final String INSERT_TAG = "insert_todo_item";
        final String UPDATE_TAG = "update_todo_item";

        if (paramData.containsKey("todo_id")) {
            paramData.put("action", UPDATE_TAG);
            updateMode = UPDATE_TAG;
        } else {
            paramData.put("action", INSERT_TAG);
            updateMode = INSERT_TAG;
            paramData.put("insert_type", "regular_add");
        }

        final Map<String, String> data = AppHelper.validateJSONMap(paramData);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e(updateMode, response);
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status == 0) {
                                saveItemToLocalStorage(jsonObject.getInt("todo_id"),
                                        Integer.parseInt(paramData.get("list_id")),
                                        jsonObject.getString("item_desc"),
                                        jsonObject.getString("due_date"),
                                        jsonObject.getString("note"),
                                        jsonObject.getInt("is_completed"), 1, true,
                                        updateMode);

                                setResult(RESULT_OK, intent);
                                progressDialog.dismiss();
                            } else if (status == 1) {
                                Toast.makeText(TaskDetailsActivity.this, "Insert task failed!", Toast.LENGTH_LONG).show();
                                setResult(RESULT_CANCELED);
                                progressDialog.dismiss();
                            } else {
                                Log.e(updateMode, "Error: " + response);
                                setResult(RESULT_CANCELED);
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            String msg = e.getMessage();
                            if (msg != null) {
                                Log.e(updateMode + "_EXCEPTION", msg);
                                setResult(RESULT_CANCELED);
                                progressDialog.dismiss();
                            }
                        }

                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if (msg != null) {
                            Log.e(updateMode + "_ERROR", msg);
                        }
//                        Random randId = new Random();
//                        saveItemToLocalStorage(randId.nextInt(Integer.MAX_VALUE),
//                                Integer.parseInt(paramData.get("list_id")),
//                                paramData.get("task_name"),
//                                paramData.get("due_date"),
//                                paramData.get("note"),
//                                0, 0, false,
//                                updateMode);
                        progressDialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return data;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest, updateMode);
    }


    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose file to upload!"), PICK_FILE_REQUEST);
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDay = Calendar.getInstance();
            selectedDay.set(year, month, day);

            ((TaskDetailsActivity) getActivity()).updateDueDateInput(selectedDay);
        }
    }
}
