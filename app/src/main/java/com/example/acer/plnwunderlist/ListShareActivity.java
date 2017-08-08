package com.example.acer.plnwunderlist;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;
import com.example.acer.plnwunderlist.Singleton.WebSocketClientManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

public class ListShareActivity extends AppCompatActivity {

    private ArrayList<User> listMembers;

    private ListMemberAdapter memberAdapter;
    private ListView memberList;
    private Button inviteBtn;

    private String listID;
    private String endpoint;

    private SessionManager sessionManager;
    private HashMap<String,String> userDetails;
    private String thisUserID;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_menu_share);

        setTitle("Share List");

        endpoint = getString(R.string.uri_endpoint);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        sessionManager = new SessionManager(this);

        //get current user details, and fetch its ID
        userDetails = sessionManager.getUserDetails();
        thisUserID = userDetails.get(SessionManager.KEY_ID);

        //get textview
        TextView listTitle = (TextView) findViewById(R.id.share_list_title);


        //setup listview
        listMembers = new ArrayList<>();
        memberAdapter = new ListMemberAdapter(ListShareActivity.this, listMembers);
        memberList = (ListView) findViewById(R.id.member_listview);
        memberList.setAdapter(memberAdapter);

        //get data sent from intent
        if (getIntent().hasExtra("TODO_LIST_ID")) {
            listID= getIntent().getStringExtra("TODO_LIST_ID");
        }
        if (getIntent().hasExtra("TODO_LIST_NAME")) {
            String listName = getIntent().getStringExtra("TODO_LIST_NAME");
            listTitle.setText(listName);
        }

        View layout = getLayoutInflater().inflate(R.layout.list_share_invite_btn, null);
        memberList.addFooterView(layout);

        inviteBtn = (Button) findViewById(R.id.invite_member_btn);

        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInviteDialog(ListShareActivity.this);
            }
        });

        retrieveMembers();
        //showNotification();
    }

    private void retrieveMembers() {
        String requestURL = endpoint + "?action=get_list_members&list_id=" + listID;
        Log.e("RESPONSE_URL", requestURL);
        StringRequest getMembersRequest = new StringRequest(Request.Method.GET, requestURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("RESPONSE", response);
                        try {
                            JSONArray membersJSON = new JSONArray(response);
                            for (int i = 0; i < membersJSON.length(); i++) {
                                JSONObject memberJSON = membersJSON.getJSONObject(i);
                                User currentUser =
                                        new User(memberJSON.getInt("USER_ID"),
                                                memberJSON.getString("EMAIL"),
                                        memberJSON.getString("NAME"));

                                //Check if this user is the owner of the list.
                                //if true, set adapter OwnerID as such
                                int memberAccessType = memberJSON.getInt("ACCESS_TYPE");
                                if(memberAccessType == AppHelper.TODOLIST_ACCESS_CODE_OWNER) {
                                    memberAdapter.setListOwnerID(String.valueOf(memberJSON.getInt("USER_ID")));
                                }

                                if(currentUser.getUserID() == Integer.parseInt(thisUserID)){
                                    currentUser.setName("You");
                                    memberAdapter.insert(currentUser, 0);
                                } else {
                                    memberAdapter.add(currentUser);
                                }
                            }
                            memberAdapter.notifyDataSetChanged();
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
        final EditText editTextDialog = (EditText) inviteDialogView.findViewById(R.id.newListTitleText);
        editTextDialog.setHint("Email");
        final AlertDialog.Builder inviteBuilder = new AlertDialog.Builder(context);
        inviteBuilder.setTitle("Invite new member");
        inviteBuilder.setView(inviteDialogView);
        inviteBuilder.setPositiveButton("INVITE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inviteUser(editTextDialog.getText().toString());
                memberAdapter.notifyDataSetChanged();
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

    private void inviteUser(final String email) {
        progressDialog.setMessage("Processing...");
        showDialog();

        final String REQUEST_TAG = "invite_request";
        StringRequest inviteRequest = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideDialog();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status == 0) {
                                Map<String, String> msg = new HashMap<>();
                                msg.put("type", "invite_notification");
                                msg.put("email", email);
                                msg.put("list_id", listID);
                                JSONObject msgObj = new JSONObject(msg);
                                WebSocketClientManager.getInstance().send(msgObj.toString());
                                memberAdapter.add(new User(jsonObject.getInt("USER_ID"), jsonObject.getString("EMAIL"),
                                        jsonObject.getString("NAME")));
                            } else if (status == 1) {
                                Toast.makeText(ListShareActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                            } else if (status == 2) {
                                Toast.makeText(ListShareActivity.this, "This user is already a member of this list", Toast.LENGTH_SHORT).show();
                            } else if (status == 3) {
                                Toast.makeText(ListShareActivity.this, "Invite failed", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ListShareActivity.this, "Unknown response", Toast.LENGTH_SHORT).show();
                                Log.e(REQUEST_TAG, response + "Unknown response");
                            }
                        } catch (JSONException e) {
                            String msg = e.getMessage();
                            if (msg != null) {
                                Log.e(REQUEST_TAG, msg + "Exception");
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideDialog();
                        String msg = error.getMessage();
                        if (msg != null) {
                            Log.e(REQUEST_TAG, msg + "ErrorListener");
                        }
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "share_todo_list");
                params.put("email", email);
                params.put("list_id", listID);
                return params;
            }
        };

        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(inviteRequest, REQUEST_TAG);
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
