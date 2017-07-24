package com.example.acer.plnwunderlist;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

        ListView todoListsList = (ListView) findViewById(R.id.todolistslist);
        todoListsList.setDivider(null);
        todoListsList.setDividerHeight(0);
        todoLists.add("Kerjaan");
        todoLists.add("Gawean");
        todoLists.add("Lokak");
        todoLists.add("Belajar");

        todoListAdapter adapter = new todoListAdapter(this, todoLists);
        todoListsList.setAdapter(adapter);

        View layout = getLayoutInflater().inflate(R.layout.main_menu_create_list_btn,null);
        todoListsList.addFooterView(layout);


        Button newListBtn = (Button) findViewById(R.id.add_list_btn);
        newListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddListDialog(MainMenuActivity.this);
            }
        });

        TextView emptyListText = new TextView(this);
        emptyListText.setText(R.string.emptyList_Text);
        emptyListText.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        if(todoLists.size() == 0){
            emptyListText.setGravity(Gravity.CENTER_HORIZONTAL);
            todoListsList.addHeaderView(emptyListText);
        }

    }

    private void addNewList(String newListName){
        todoLists.add(newListName);
    }

    private void showAddListDialog(Context context){

        //Get inflater from current context
        LayoutInflater inflater = LayoutInflater.from(context);
        final View addListDialogView = inflater.inflate(R.layout.main_menu_create_list_dialog, null);

        //------------------------------------------------------------------------------------------
        //START AlertDialog Definition
        final AlertDialog.Builder addListBuilder = new AlertDialog.Builder(context);

        //Set its title and view
        addListBuilder.setTitle("Create New To-Do List");
        addListBuilder.setView(addListDialogView);

        //Add the "Positive" (Right button) logic
        addListBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Because the EditText is not from the activity's view,
                //Explicitly call findViewById from addListDialogView to access the EditText from dialog.
                EditText newListText = (EditText) addListDialogView.findViewById(R.id.newListTitleText);

                //Call the activity's addNewList function using user's string.
                addNewList(newListText.getText().toString());
            }
        });
        //Add the "Negative" (Left button) logic
        addListBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //END AlertDialog Definition
        //------------------------------------------------------------------------------------------


        AlertDialog newList = addListBuilder.create();
        newList.show();
    }
}
