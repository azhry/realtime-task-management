package com.example.acer.plnwunderlist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Ryan Fadholi on 24/07/2017.
 */

public class ListMenuActivity extends AppCompatActivity {
    ArrayList<DataModel> dataModels;
    ListView listView;
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_menu);

        //If calling activity supplies current to-do list name,
        //Change App Title to the supplied name.
        if(getIntent().hasExtra("TODO_LIST_NAME")){
            this.setTitle(getIntent().getStringExtra("TODO_LIST_NAME"));
        }

        listView = (ListView) findViewById(R.id.listView);

        dataModels = new ArrayList();

        dataModels.add(new DataModel("Apple Pie", false));
        dataModels.add(new DataModel("Banana Bread", false));
        dataModels.add(new DataModel("Cupcake", false));
        dataModels.add(new DataModel("Donut", true));
        dataModels.add(new DataModel("Eclair", true));
        dataModels.add(new DataModel("Froyo", true));
        dataModels.add(new DataModel("Gingerbread", true));
        dataModels.add(new DataModel("Honeycomb", false));
        dataModels.add(new DataModel("Ice Cream Sandwich", false));
        dataModels.add(new DataModel("Jelly Bean", false));
        dataModels.add(new DataModel("Kitkat", false));
        dataModels.add(new DataModel("Lollipop", false));
        dataModels.add(new DataModel("Marshmallow", false));
        dataModels.add(new DataModel("Nougat", false));

        adapter = new CustomAdapter(dataModels, getApplicationContext());

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {

                DataModel dataModel= dataModels.get(position);
                dataModel.checked = !dataModel.checked;
                adapter.notifyDataSetChanged();
            }
        });
//        CheckBox test = (CheckBox)listView.findViewById(R.id.checkBox);
    }
}
