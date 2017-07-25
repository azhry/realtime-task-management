package com.example.acer.plnwunderlist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Ryan Fadholi on 20/07/2017.
 */

public class MainMenuActivity extends AppCompatActivity {

    //Statics
    private static final String TAG = "MainMenuActivity";

    //Pseudo-statics. Cannot be initialized because it's fetched from resources XML.
    private String endpoint;
    private String postEndpoint;

    //Declare attributes necessary for UI black magic.
    //Everything is assumed to be initialized in onCreate() method.
    private ArrayList<String> todoLists = new ArrayList<>();//Used to hold data
    private TodoListAdapter adapter; //Used to bridge todoLists and todoListsList
    private ListView todoListsList; //The RecyclerView.
    private View emptyTextView; //Header that pops up when the list is empty.
    ProgressDialog progressDialog; //'nuff said.

    HashMap<String, String> userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        //Initalize the pseudo-statics
        endpoint = getString(R.string.uri_endpoint);
        postEndpoint = getString(R.string.uri_post_endpoint);

        //Initialize progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading data...");
        showDialog();

        todoListsList = (ListView) findViewById(R.id.todolistslist);
        todoListsList.setDivider(null);
        todoListsList.setDividerHeight(0);

        emptyTextView = getLayoutInflater().inflate(R.layout.main_menu_empty_list_text, null);

        todoListsList.addHeaderView(emptyTextView, null, false);
        SessionManager sessionManager = new SessionManager(this);
        userData = sessionManager.getUserDetails();

        adapter = new TodoListAdapter(this, todoLists);

        todoListsList.setAdapter(adapter);
        //Set onclick listener; send user to the item's respective to-do list.
        todoListsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                //Initialize the Intent
                Intent todolistIntent = new Intent(getApplicationContext(), ListMenuActivity.class);
                //Send the to-do list title as extra information to the ListMenuActivity
                todolistIntent.putExtra("TODO_LIST_NAME", (String)todoListsList.getItemAtPosition(position));
                startActivity(todolistIntent);

            }
        });

        String reqUrl = endpoint + "?action=get_list&user_id=" + userData.get("user_id");
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, reqUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("JSON", response.toString());
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                adapter.add(jsonObject.getString("LIST_NAME"));
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        setEmptyTextVisibility(emptyTextView);

                        hideDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.getMessage());
                        hideDialog();
                    }
                }

        );

        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(getRequest, "retrieve_list");


        View layout = getLayoutInflater().inflate(R.layout.main_menu_create_list_btn, null);
        todoListsList.addFooterView(layout);


        Button newListBtn = (Button) findViewById(R.id.add_list_btn);
        newListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddListDialog(MainMenuActivity.this);
            }
        });

    }

    private void addNewList(final String newListName) {
        progressDialog.setMessage("Processing...");
        showDialog();
        StringRequest addRequest = new StringRequest(Request.Method.POST, postEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status == 0) {
                                adapter.add(newListName);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), newListName + " has been added", Toast.LENGTH_LONG).show();
                            } else if (status == 1)
                                Toast.makeText(getApplicationContext(), "Insert list failed!", Toast.LENGTH_LONG).show();
                            else if (status == 2)
                                Toast.makeText(getApplicationContext(), "Insert access failed!", Toast.LENGTH_LONG).show();
                            else if (status == -1)
                                Toast.makeText(getApplicationContext(), "Unknown attempt!", Toast.LENGTH_LONG).show();
                            else Log.e("RESPONSEe", response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String msg = e.getMessage();
                            if (msg != null) {
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
                                Log.e("JSONException", msg);
                            }
                        }

                        hideDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if (msg != null)
                            Log.e("ADD_ERROR", error.getMessage());
                        hideDialog();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "insert");
                params.put("list_name", newListName);
                params.put("user_id", userData.get("user_id"));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("action", "insert");
                params.put("list_name", newListName);
                params.put("user_id", userData.get("user_id"));
                return params;
            }
        };

        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(addRequest, "add_list");
        setEmptyTextVisibility(emptyTextView);
    }

    private void setEmptyTextVisibility(View headerView){
        //Set the visibility of empty to-do lists notice.
        if(adapter.isEmpty()){
            //if the adapter is empty, check the number of views in the header.
            //If it's zero, then add the notice. otherwise continue.
            if(todoListsList.getHeaderViewsCount() == 0) {
                todoListsList.addHeaderView(headerView, null, false);
            }
        } else {
            //else if the adapter is NOT empty, check the number of views in the header.
            //If it's more than zero (meaning there's something there, then remove the notice).
            //otherwise continue.
            if(todoListsList.getHeaderViewsCount() > 0) {
                todoListsList.removeHeaderView(headerView);
            }
        }
    }

    private void showAddListDialog(Context context) {

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

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void setText(final TextView text, final String value, final int unit, final float textSize) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
                text.setTextSize(unit, textSize);
            }
        });
    }
}
