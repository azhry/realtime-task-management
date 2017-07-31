package com.example.acer.plnwunderlist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListShareActivity extends AppCompatActivity {

    private ListMemberAdapter memberAdapter;
    private ListView memberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_menu_share);

        //get textview
        TextView listTitle = (TextView) findViewById(R.id.share_list_title);

        //get data sent from intent
        int listID = getIntent().getIntExtra("TODO_LIST_ID", -1);
        if (getIntent().hasExtra("TODO_LIST_NAME")) {
            String listName = getIntent().getStringExtra("TODO_LIST_NAME");
            listTitle.setText(listName);
        }

        memberAdapter = new ListMemberAdapter(this, retrieveMembers(listID));
        memberList = (ListView) findViewById(R.id.member_listview);
        memberAdapter.add(new User(1, "garok@disini", "Garok"));
        memberAdapter.add(new User(2, "atma@disini", "Atma"));
        memberAdapter.add(new User(3, "rizki@disini", "Rizki"));

        memberList.setAdapter(memberAdapter);

    }

    private ArrayList<User> retrieveMembers(int listID) {
        ArrayList<User> result = new ArrayList<>();

        //TODO: Ambek list usernyo dari db Az, terus simpen di arraylist

        return result;
    }
}
