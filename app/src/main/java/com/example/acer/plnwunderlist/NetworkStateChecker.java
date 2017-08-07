package com.example.acer.plnwunderlist;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NetworkStateChecker extends BroadcastReceiver {

    private Context context;
    private DBPLNHelper db;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        db = new DBPLNHelper(context);
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        Log.e("BROADCAST", "ONRECEIVED");
        /** if there is a network */
        if (activeNetwork != null) {
            /** if connected to wifi or mobile data */
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                    || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Synching to-do lists");
                progressDialog.show();

                Cursor c = db.select("todo_lists", "STATUS=0 AND SERVER_ID=0");
                if (c.moveToFirst()) {
                    do {
                        Log.e("SYNC", "SYNCING");
                        syncTodoList(c.getString(c.getColumnIndex("LIST_ID")),
                                c.getString(c.getColumnIndex("LIST_NAME")));
                    } while (c.moveToNext());
                }

//                progressDialog.setMessage("Synching to-do items");
//
//                c = db.select("todo_items", "STATUS=0 AND SERVER_ID=0");
//                if (c.moveToFirst()) {
//                    do {
//                        syncTodoItem(c.getString(c.getColumnIndex("LIST_ID")),
//                                c.getString(c.getColumnIndex("LIST_NAME")));
//                    } while (c.moveToNext());
//                }

                progressDialog.dismiss();
                c.close();
            }
        }
    }

    private void syncTodoList(final String listID, final String listName) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, this.context.getString(R.string.uri_endpoint),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status == 0) {
                                Map<String, String> contentValues = new HashMap<>();
                                contentValues.put("STATUS", "1");
                                contentValues.put("LIST_ID", jsonObject.getString("list_id"));
                                contentValues.put("SERVER_ID", jsonObject.getString("list_id"));
                                db.update("todo_lists", contentValues, "LIST_ID="+ listID);
                                //context.sendBroadcast(new Intent(MainMenuActivity.DATA_SAVED_BROADCAST));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                SessionManager sessionManager = new SessionManager(context);
                Map<String, String> userData = sessionManager.getUserDetails();

                Map<String, String> params = new HashMap<>();
                params.put("action", "insert_todo_list");
                params.put("list_name", listName);
                params.put("user_id", userData.get("user_id"));
                return params;
            }
        };
        AppSingleton.getInstance(this.context).addToRequestQueue(stringRequest, "SYNC_TODO_LIST");
    }

    private void syncTodoItem(final String itemID, final String listID, final String itemDesc,
                              final String dueDate, final String note) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, this.context.getString(R.string.uri_endpoint),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status == 0) {
                                Map<String, String> contentValues = new HashMap<>();
                                contentValues.put("STATUS", "1");
                                db.update("todo_items", contentValues, "TODO_ID="+ itemID);
                                context.sendBroadcast(new Intent(MainMenuActivity.DATA_SAVED_BROADCAST));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "insert_todo_item");
                params.put("insert_type", "regular_add");
                params.put("task_name", itemDesc);
                params.put("list_id", listID);
                params.put("due_date", dueDate);
                params.put("note", note);
                return params;
            }
        };
        AppSingleton.getInstance(this.context).addToRequestQueue(stringRequest, "SYNC_TODO_ITEM");
    }

}
