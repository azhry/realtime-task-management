package com.example.acer.plnwunderlist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Ryan Fadholi on 20/07/2017.
 */

public class MainMenuActivity extends AppCompatActivity {

    private static final String TAG = "MainMenuActivity";
    private ArrayList<String> todoLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        ListView todoListsList = (ListView) findViewById(R.id.todolistslist);
        todoLists.add("Kerjaan");
        todoLists.add("Gawean");
        todoLists.add("Lokak");
        todoLists.add("Belajar");

        todoListAdapter adapter = new todoListAdapter(this, todoLists);
        todoListsList.setAdapter(adapter);

    }

}
