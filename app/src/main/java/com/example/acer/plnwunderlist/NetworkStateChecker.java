package com.example.acer.plnwunderlist;

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
    private DatabaseHelper db;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        db = new DatabaseHelper(context);
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        Log.e("BROADCAST", "ONRECEIVED");
        /** if there is a network */
        if (activeNetwork != null) {
            /** if connected to wifi or mobile data */
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                    || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                /** get all unsynced items */
                Cursor cursor = db.getUnsyncedItem();
                if (cursor.moveToFirst()) {
                    do {
                        saveItem(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_LIST_ID)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LIST_NAME)));
                    } while (cursor.moveToNext());
                }
            }
        }
    }

    private void saveItem(final int listID, final String listName) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, this.context.getString(R.string.uri_endpoint),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("NSC", response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            int status = obj.getInt("status");
                            if (status == 0) {
                                db.updateItemStatus(listID, MainMenuActivity.SYNCHED);
                                context.sendBroadcast(new Intent());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("NETWORK_STATE", error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                SessionManager sessionManager = new SessionManager(context);
                Map<String, String> userData = sessionManager.getUserDetails();

                Map<String, String> params = new HashMap<>();
                params.put("action", "insert_todo_list");
                params.put("list_id", String.valueOf(listID));
                params.put("list_name", listName);
                params.put("user_id", userData.get("user_id"));
                return params;
            }
        };
        AppSingleton.getInstance(this.context).addToRequestQueue(stringRequest, "SAVE_LIST");
    }
}
