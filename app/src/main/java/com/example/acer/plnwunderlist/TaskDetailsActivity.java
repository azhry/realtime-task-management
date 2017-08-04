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
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class TaskDetailsActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private FileListPseudoAdapter fileListPseudoAdapter;
    private LinearLayout fileList;

    private String endpoint;
    private String listID;
    private String listName;
    private String selectedFilePath;

    //Updateable views
    private Button addEditDueDateBtn;
    private Button deleteDueDateBtn;
    private Button addTaskBtn;
    private Button addFileBtn;
    private Button uploadFileBtn;
    private EditText taskNameInput, noteInput;
    private TextView dueDateInput;

    //Extra state-changeable views
    private View fileDivider;
    private TextView fileLabel;
    private LinearLayout fileListLayout;
    private LinearLayout fileBtns;

    private TodoItem item;
    private Date tempDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_FILE_REQUEST);
            Log.e("REQUEST_PERMISSION", "TRUE");
        }

        endpoint = getString(R.string.uri_endpoint);

        //Initialize Button values
        addEditDueDateBtn = (Button) findViewById(R.id.addEditDueDateBtn);
        deleteDueDateBtn = (Button) findViewById(R.id.deleteDueDateBtn);
        addTaskBtn = (Button) findViewById(R.id.addTask);
        addFileBtn = (Button) findViewById(R.id.addFileBtn);
        uploadFileBtn = (Button) findViewById(R.id.uploadFileBtn);

        //Initialize EditText values
        taskNameInput = (EditText) findViewById(R.id.taskNameEdit);
        noteInput = (EditText) findViewById(R.id.editTaskNote);
        dueDateInput = (TextView) findViewById(R.id.taskDueDateEdit);

        //Initialize extra views
        fileDivider = findViewById(R.id.fileDivider);
        fileLabel = (TextView) findViewById(R.id.taskFileLabel);
        fileListLayout = (LinearLayout) findViewById(R.id.fileListView);
        fileBtns = (LinearLayout) findViewById(R.id.uploadBtnParent);

        //Initialize TodoItem and temporary values
        item = null;
        tempDate = null;

        if (getIntent().hasExtra("TODO_OBJECT")) {
            item = getIntent().getParcelableExtra("TODO_OBJECT");
            setupInputFields(item);
        } else {
            fileDivider.setVisibility(View.GONE);
            fileLabel.setVisibility(View.GONE);
            fileListLayout.setVisibility(View.GONE);
            fileBtns.setVisibility(View.GONE);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fileList = (LinearLayout) findViewById(R.id.fileListView);
        fileListPseudoAdapter = new FileListPseudoAdapter(fileList, this);

        String todoFilesRequestUrl = endpoint + "?action=get_todo_files&todo_id=" + item.getID();
        StringRequest todoFilesRequest = new StringRequest(Request.Method.GET, todoFilesRequestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray filesArray = new JSONArray(response);
                            for (int i = 0; i < filesArray.length(); i++) {
                                JSONObject file = filesArray.getJSONObject(i);
                                fileListPseudoAdapter.add(Integer.parseInt(file.getString("FILE_ID")),
                                        file.getString("FILENAME"));
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

        uploadFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFilePath != null) {
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
                    Log.e("UPLOAD", "Please choose a file first");
                }
            }
        });

        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = getNewDesc();

                if(!isDescriptionValid(taskName)){
                    return;
                }

                String note = getNewNote();

                SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dueDate = sqlDateFormat.format(tempDate);

                Map<String, String> data = new HashMap<String, String>();
                data.put("task_name", taskName);
                data.put("list_id", listID);
                data.put("due_date", dueDate);
                data.put("note", note);
                addTask(data);
            }
        });
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

    private Boolean isDescriptionValid(String desc){
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

        if(desc == null){
            invalidDescriptionDialog.show();
        }

        return desc != null;
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
                showEditConfirmationDialog(changedItems, newTaskDesc, tempDate, newTaskNote);
            }
        } else {
            //showAddConfirmationDialog();
            finish();
        }

    }

    private void showAddConfirmationDialog(final String newDesc,
                                           final Date newDate, final String newNote) {

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
                //addTask(newDesc, newDate, newNote);
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

    private void showEditConfirmationDialog(String changedItems, final String newDesc,
                                            final Date newDate, final String newNote) {

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
                updateTaskDetails(newDesc, newDate, newNote);
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

    private void updateTaskDetails(String newDesc, Date newDate, String newNote) {
        TodoItem oldItem = this.item;

        if(!isDescriptionValid(newDesc)){
            return;
        }

        //TODO Lajukela az update
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

        String dayofweek = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
        SimpleDateFormat dateString = new SimpleDateFormat("dd-MM-yyyy");

        //Update label and temporary values accordingly.
        dueDateInput.setText(dayofweek + ", " + dateString.format(c.getTime()));
        tempDate = c.getTime();

        this.setDateBtnsVisibility(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data == null) {
                    return;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_FILE_REQUEST);
                    Log.e("REQUEST_PERMISSION", "TRUE");
                }

                Uri selectedFileUri = data.getData();
                selectedFilePath = FilePath.getPath(this, selectedFileUri);
                Log.e("ON_ACTIVITY_RESULT", "Selected file path: " + selectedFilePath);
                if (selectedFilePath != null && !selectedFilePath.equals("")) {
                    Log.e("ON_ACTVT_RESULT_SUCCESS", "Selected file path: " + selectedFilePath);
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

    private void addTask(final Map<String, String> data) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Processing...");
        progressDialog.show();

        final String REQUEST_TAG = "insert_todo_item";
        data.put("action", REQUEST_TAG);
        data.put("insert_type", "regular_add");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status == 0) {
                                progressDialog.dismiss();
                                Intent listMenuIntent = new Intent(TaskDetailsActivity.this, ListMenuActivity.class);
                                listMenuIntent.putExtra("TODO_LIST_ID", listID);
                                listMenuIntent.putExtra("TODO_LIST_NAME", listName);
                                startActivity(listMenuIntent);
                                finish();
                            } else if (status == 1) {
                                Toast.makeText(TaskDetailsActivity.this, "Insert task failed!", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            } else {
                                Log.e(REQUEST_TAG, response);
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            String msg = e.getMessage();
                            if (msg != null)
                                Log.e(REQUEST_TAG + "_EXCEPTION", msg);
                            progressDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if (msg != null)
                            Log.e(REQUEST_TAG + "_ERROR", msg);
                        progressDialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return data;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest, REQUEST_TAG);
    }

    @Override
    public boolean onSupportNavigateUp() {
        startBackProcedure();
        return true;
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
