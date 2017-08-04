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
import com.example.acer.plnwunderlist.Singleton.WebSocketClientManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
    private ProgressDialog progressDialog;

    private String endpoint;
    private String listID;
    private String listName;
    private String selectedFilePath;
    private boolean isUpdate;

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

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

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

        fileListPseudoAdapter.add("Data Azhary");
        fileListPseudoAdapter.add("Data Ryan");

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
                    progressDialog.setMessage("Uploading file...");
                    showDialog();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FILE_REQUEST);
                                Log.e("REQUEST_PERMISSION", "TRUE");
                            }

                            uploadFile(selectedFilePath);
                        }
                    }).start();
                } else {
                    Log.e("UPLOAD", "Please choose a file first");
                }
            }
        });

        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = getNewDesc();
                String note = getNewNote();
                String dueDate = null;

                if (!isDescriptionValid(taskName)) {
                    return;
                }

                SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                if (tempDate != null) {
                    dueDate = sqlDateFormat.format(tempDate);
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
                updateTask(data);
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

    private void startUpdateProcedure(){
        String taskName = getNewDesc();
        String note = getNewNote();
        String dueDate = null;

        Log.e("UPDT","Update procedure called!");
        if (!isDescriptionValid(taskName)) {
            return;
        }

        SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (tempDate != null) {
            dueDate = sqlDateFormat.format(tempDate);
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
        updateTask(data);
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


    private void updateTask(final Map<String, String> paramData) {

        final String updateMode;

        progressDialog.setMessage("Processing...");
        showDialog();

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

        final Map<String, String> data = WebSocketClientManager.validateJSONMap(paramData);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e(updateMode + "_EXCEPTION", response);
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status == 0) {
                                hideDialog();
                                finish();
                            } else if (status == 1) {
                                Toast.makeText(TaskDetailsActivity.this, "Insert task failed!", Toast.LENGTH_LONG).show();
                                hideDialog();
                            } else {
                                Log.e(updateMode, "Error: " + response);
                                hideDialog();
                            }
                        } catch (JSONException e) {
                            String msg = e.getMessage();
                            if (msg != null)
                                Log.e(updateMode + "_EXCEPTION", msg);
                            hideDialog();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if (msg != null)
                            Log.e(updateMode + "_ERROR", msg);
                        hideDialog();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return data;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest, updateMode);
    }



    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose file to upload!"), PICK_FILE_REQUEST);
    }

    private int uploadFile(final String selectedFilePath) {
        int serverResponseCode = 0;

        final HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);

        String[] parts = selectedFilePath.split("/");

        if (!selectedFile.isFile()) {
            hideDialog();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("FILE_DOESNT_EXIST", "Source file doesn't exist: " + selectedFilePath);
                }
            });

            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", selectedFilePath);

                /**
                 *    Content-Type: multipart/form-data; boundary="BOUNDARY"
                 *
                 *    --BOUNDARY
                 *    Content-Disposition: form-data; name="param"
                 *
                 *    123456
                 *    --BOUNDARY
                 *    Content-Disposition: form-data; name="test"; filename="test.zip"
                 *    Content-Type: application/zip
                 *
                 *    BINARY_DATA
                 *
                 *    --BOUNDARY--
                 */

                dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"action\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes("upload_file" + lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                final StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }

                Log.e("SERVER_RESPONSE", "Server response is: " + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("RESPONSE", sb.toString());
                        }
                    });
                }

                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();

            } catch (FileNotFoundException e) {
                Log.e("EXCEPTION", "File not found" + e.getMessage());
                e.printStackTrace();
            } catch (MalformedURLException e) {
                Log.e("EXCEPTION", "Malformed");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("EXCEPTION", "IO" + e.getMessage());
                e.printStackTrace();
            }
        }

        hideDialog();
        return serverResponseCode;
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
