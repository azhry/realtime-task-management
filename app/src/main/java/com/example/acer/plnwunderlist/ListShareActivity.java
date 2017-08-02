package com.example.acer.plnwunderlist;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

public class ListShareActivity extends AppCompatActivity {

    private ListMemberAdapter memberAdapter;
    private ListView memberList;
    private Button inviteBtn;

    private String listID;
    private String endpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_menu_share);

        endpoint = getString(R.string.uri_endpoint);

        //get textview
        TextView listTitle = (TextView) findViewById(R.id.share_list_title);
        inviteBtn = (Button) findViewById(R.id.inviteBtn);

        //get data sent from intent
        if (getIntent().hasExtra("TODO_LIST_ID")) {
            listID= getIntent().getStringExtra("TODO_LIST_ID");
        }
        if (getIntent().hasExtra("TODO_LIST_NAME")) {
            String listName = getIntent().getStringExtra("TODO_LIST_NAME");
            listTitle.setText(listName);
        }

        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInviteDialog(ListShareActivity.this);
            }
        });

        retrieveMembers();
    }

    private void retrieveMembers() {
        String requestURL = endpoint + "?action=get_list_members&list_id=" + listID;
        Log.e("RESPONSE_URL", requestURL);
        StringRequest getMembersRequest = new StringRequest(Request.Method.GET, requestURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("RESPONSE", response);
                        ArrayList<User> listMembers = new ArrayList<>();
                        memberAdapter = new ListMemberAdapter(ListShareActivity.this, listMembers);
                        memberList = (ListView) findViewById(R.id.member_listview);

                        try {
                            JSONArray membersJSON = new JSONArray(response);
                            for (int i = 0; i < membersJSON.length(); i++) {
                                JSONObject memberJSON = membersJSON.getJSONObject(i);
                                memberAdapter.add(new User(memberJSON.getInt("USER_ID"), memberJSON.getString("EMAIL"),
                                        memberJSON.getString("NAME")));
                            }
                            memberList.setAdapter(memberAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(getMembersRequest, "GET_LIST_MEMBERS");
    }

    private void showInviteDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View inviteDialogView = inflater.inflate(R.layout.main_menu_create_list_dialog, null);
        EditText dialog = (EditText) inviteDialogView.findViewById(R.id.newListTitleText);
        dialog.setHint("Email");
        final AlertDialog.Builder inviteBuilder = new AlertDialog.Builder(context);
        inviteBuilder.setTitle("Invite new member");
        inviteBuilder.setView(inviteDialogView);
        inviteBuilder.setPositiveButton("INVITE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        inviteBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog invite = inviteBuilder.create();
        invite.show();
    }
}
