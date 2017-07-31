package com.example.acer.plnwunderlist;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskDetailsActivity extends AppCompatActivity {

    private Button addEditDueDateBtn;
    private Button deleteDueDateBtn;
    private Button addTaskBtn;
    private FileListPseudoAdapter fileListPseudoAdapter;
    private LinearLayout fileList;
    private String endpoint;

    public void setDateBtnsVisibility(boolean isDateSet) {
        if (isDateSet) {
            deleteDueDateBtn.setVisibility(View.VISIBLE);
            addEditDueDateBtn.setText("Edit Due Date");
        } else {
            addEditDueDateBtn.setText("Set Due Date");
            deleteDueDateBtn.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        addEditDueDateBtn   = (Button) findViewById(R.id.addEditDueDateBtn);
        deleteDueDateBtn    = (Button) findViewById(R.id.deleteDueDateBtn);
        addTaskBtn          = (Button) findViewById(R.id.addTask);

        endpoint = getString(R.string.uri_endpoint);

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
                TextView dateTextView = (TextView) findViewById(R.id.taskDueDateEdit);
                dateTextView.setText("No Due Date Set");
                setDateBtnsVisibility(false);
            }
        });
        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fileList = (LinearLayout) findViewById(R.id.fileListView);
        fileListPseudoAdapter = new FileListPseudoAdapter(fileList, this);

        fileListPseudoAdapter.add("Data Azhary");
        fileListPseudoAdapter.add("Data Ryan");
    }

    private void addTask(JSONObject data) throws JSONException {
        String REQUEST_TAG = "insert_todo_item";
        data.put("action", REQUEST_TAG);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, endpoint, data,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest, REQUEST_TAG);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
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
            TextView dateTextView = (TextView) getActivity().findViewById(R.id.taskDueDateEdit);
            Calendar selectedDay = Calendar.getInstance();
            selectedDay.set(year, month, day);
            String test = selectedDay.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
            dateTextView.setText(test + ", " + String.valueOf(year) + "/" + String.valueOf(month) + "/" + String.valueOf(day));
            ((TaskDetailsActivity) getActivity()).setDateBtnsVisibility(true);
        }
    }
}
