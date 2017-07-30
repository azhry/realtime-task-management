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

import com.android.volley.toolbox.JsonObjectRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TaskDetailsActivity extends AppCompatActivity {

    private Button addEditDueDateBtn;
    private Button deleteDueDateBtn;
    private Button addTaskBtn;
    private FileListPseudoAdapter fileListPseudoAdapter;
    private LinearLayout fileList;

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


        fileList = (LinearLayout) findViewById(R.id.fileListView);
        fileListPseudoAdapter = new FileListPseudoAdapter(fileList, this);

        fileListPseudoAdapter.add("Data Azhary");
        fileListPseudoAdapter.add("Data Ryan");
    }

    private void addTask() {
        //JsonObjectRequest jsonObjectRequest = new JsonObjectRequest();
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
