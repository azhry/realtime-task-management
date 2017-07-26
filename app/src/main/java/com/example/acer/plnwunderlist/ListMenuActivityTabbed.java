package com.example.acer.plnwunderlist;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Ryan Fadholi on 24/07/2017.
 */

public class ListMenuActivityTabbed extends AppCompatActivity {
    ArrayList<DataModel> dataModels;
    ListView listView;
    private CustomAdapter adapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.todo_list_menu, menu);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //------------------------------------------------------------------------------------------
        //START Menu Icon Tinting

        //Retrieve all Menu Items
        final MenuItem shareBtn = (MenuItem) menu.findItem(R.id.list_share_btn);

        //Retrieve all Icons
        Drawable shareBtnIcon = (Drawable)shareBtn.getIcon();
        shareBtnIcon.mutate().setColorFilter(Color.argb(255, 255, 255, 255), PorterDuff.Mode.SRC_IN);

        shareBtn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.list_share_btn:
                        //Initialize the Intent
                        Intent shareIntent = new Intent(getApplicationContext(), ListShareActivity.class);
                        startActivity(shareIntent);
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.list_share_btn:
                Log.d("MENUEXAMPLE","Yooo it works!");
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testcoordinatorlayout);

        //If calling activity supplies current to-do list name,
        //Change App Title to the supplied name.
        if(getIntent().hasExtra("TODO_LIST_NAME")){
            this.setTitle(getIntent().getStringExtra("TODO_LIST_NAME"));
        }
    }
}
