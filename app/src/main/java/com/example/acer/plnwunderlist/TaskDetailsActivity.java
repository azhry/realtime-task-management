package com.example.acer.plnwunderlist;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    //Updateable views
    private TodoItem item;
    private Button addEditDueDateBtn;
    private Button deleteDueDateBtn;
    private Button addTaskBtn;
    private Button addFileBtn;
    private Button uploadFileBtn;
    private EditText taskNameInput, noteInput;
    private TextView dueDateInput;

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

        //Initialize TodoItem and temporary values
        item = null;
        tempDate = null;

        if (getIntent().hasExtra("TODO_OBJECT")) {
            item = getIntent().getParcelableExtra("TODO_OBJECT");
            setupInputFields(item);
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
                String taskName = taskNameInput.getText().toString();
                String note = noteInput.getText().toString();
                String dueDateString = dueDateInput.getText().toString();

                String[] dueDateDummySringArray = dueDateString.split(" ");
                String[] dueDateStringArray = dueDateDummySringArray[1].split("/");

                String dueDate = dueDateStringArray[0] + "-"
                        + dueDateStringArray[1] + "-"
                        + dueDateStringArray[2];

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

    private void showEditConfirmationDialog(Context context, final TodoList todolist) {

        String
        String newTaskDesc = taskNameInput.getText().toString();
        String newTaskNote = noteInput.getText().toString();
        boolean isChanged  = false;


        //------------------------------------------------------------------------------------------
        //START AlertDialog Definition
        final AlertDialog.Builder deleteListBuilder = new AlertDialog.Builder(context);

        //Set its title and view
        String dialogMsg = getString(R.string.delete_dialog_start).
                concat(" ").concat(todolist.getName()).
                concat(" ").concat(getString(R.string.delete_dialog_end));

        //Set title and message
        deleteListBuilder.setTitle(R.string.delete_dialog_title).setMessage(dialogMsg);

        //Add the "Positive" (Right button) logic
        deleteListBuilder.setPositiveButton(R.string.dialog_default_positive_labeal, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Call the delete function
                deleteList(todolist);
            }
        });
        //Add the "Negative" (Left button) logic
        deleteListBuilder.setNegativeButton(R.string.dialog_default_negative_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //END AlertDialog Definition
        //------------------------------------------------------------------------------------------

        AlertDialog newList = deleteListBuilder.create();
        newList.show();
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
        super.onBackPressed();
        showEditConfirmationDialog();
    }

    private void addTask(final Map<String, String> data) {
        progressDialog.setMessage("Processing...");
        showDialog();

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
                                hideDialog();
                                Intent listMenuIntent = new Intent(TaskDetailsActivity.this, ListMenuActivity.class);
                                listMenuIntent.putExtra("TODO_LIST_ID", listID);
                                listMenuIntent.putExtra("TODO_LIST_NAME", listName);
                                startActivity(listMenuIntent);
                                finish();
                            } else if (status == 1) {
                                Toast.makeText(TaskDetailsActivity.this, "Insert task failed!", Toast.LENGTH_LONG).show();
                                hideDialog();
                            } else {
                                Log.e(REQUEST_TAG, response);
                                hideDialog();
                            }
                        } catch (JSONException e) {
                            String msg = e.getMessage();
                            if (msg != null)
                                Log.e(REQUEST_TAG + "_EXCEPTION", msg);
                            hideDialog();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if (msg != null)
                            Log.e(REQUEST_TAG + "_ERROR", msg);
                        hideDialog();
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
        finish();
        return true;
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
