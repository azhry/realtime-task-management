package com.example.acer.plnwunderlist;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.zip.Inflater;

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

        //the scale. Refer to:
        //https://stackoverflow.com/questions/4275797/view-setpadding-accepts-only-in-px-is-there-anyway-to-setpadding-in-dp
        final float scale = getResources().getDisplayMetrics().density;

        ListView todoListsList = (ListView) findViewById(R.id.todolistslist);
        todoListsList.setDivider(null);
        todoListsList.setDividerHeight(0);
        todoLists.add("Kerjaan");
        todoLists.add("Gawean");
        todoLists.add("Lokak");
        todoLists.add("Belajar");


        todoListAdapter adapter = new todoListAdapter(this, todoLists);
        todoListsList.setAdapter(adapter);

        //Ibflate the create list button
        View layout = getLayoutInflater().inflate(R.layout.main_menu_create_list_btn,null);
        todoListsList.addFooterView(layout);

        //FINISH (CREATE NEW LIST) BUTTON LOGIC
        //---------------------------------------------------------------------------------

        TextView emptyListText = new TextView(this);
        emptyListText.setText(R.string.emptyList_Text);
        emptyListText.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        if(todoLists.size() == 0){
            emptyListText.setGravity(Gravity.CENTER_HORIZONTAL);
            todoListsList.addHeaderView(emptyListText);
        }

    }

}
