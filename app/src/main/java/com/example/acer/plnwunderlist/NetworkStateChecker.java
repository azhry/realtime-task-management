package com.example.acer.plnwunderlist;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.DisplayContext;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;
import com.example.acer.plnwunderlist.Singleton.WebSocketClientManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class NetworkStateChecker extends BroadcastReceiver {

    private Context context;
    private DBPLNHelper db;

    public static boolean checkServerReachability(Context context){
        if(isNetworkActive(context)){
            if(isServerReachable()) return true;
            else {
                Toast.makeText(context, "Server unreachable, please try again.",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }

        Toast.makeText(context, "Device is offline. Please connect to Internet first.",
                Toast.LENGTH_LONG).show();
        return false;
    }

    public static boolean isServerReachable(){
       return WebSocketClientManager.isConnected;
    }

    public static boolean isNetworkActive(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null;
    }


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
                progressDialog.setMessage("Synchronizing");
                progressDialog.show();

                Cursor c = db.select("todo_lists", "STATUS=0 AND SERVER_ID=0");
                if (c.moveToFirst()) {
                    do {
                        Log.e("SYNC", "LIST");
                        syncTodoList(c.getString(c.getColumnIndex("LIST_ID")),
                                c.getString(c.getColumnIndex("LIST_NAME")));
                    } while (c.moveToNext());
                }

                c = db.select("todo_items", "STATUS=0 AND SERVER_ID=0");
                if (c.moveToFirst()) {
                    do {
                        Log.e("SYNC", "ITEM");
                        syncTodoItem(c.getString(c.getColumnIndex("TODO_ID")),
                                c.getString(c.getColumnIndex("LIST_ID")),
                                c.getString(c.getColumnIndex("ITEM_DESC")),
                                c.getString(c.getColumnIndex("DUE_DATE")),
                                c.getString(c.getColumnIndex("NOTE")));
                    } while (c.moveToNext());
                }

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
                        Log.e("RESPONSEE", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status == 0) {
                                Map<String, String> contentValues = new HashMap<>();
                                contentValues.put("STATUS", "1");
                                contentValues.put("SERVER_ID", jsonObject.getString("todo_id"));
                                contentValues.put("TODO_ID", jsonObject.getString("todo_id"));
                                db.update("todo_items", contentValues, "TODO_ID=" + itemID);
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
                        String msg = error.getMessage();
                        if (msg != null) {
                            Log.e("ERROR_SYNC", msg);
                        }
                        Log.e("SYNC", "FAILED?");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "insert_todo_item");
                params.put("insert_type", "regular_add");
                params.put("task_name", itemDesc);
                params.put("list_id", listID);
                if (dueDate != null) {
                    params.put("due_date", dueDate);
                }
                if (note != null) {
                    params.put("note", note);
                }
                return params;
            }
        };
        AppSingleton.getInstance(this.context).addToRequestQueue(stringRequest, "SYNC_TODO_ITEM");
    }

}
