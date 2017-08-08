package com.example.acer.plnwunderlist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amitshekhar.DebugDB;
import com.example.acer.plnwunderlist.Singleton.WebSocketClientManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Azhary Arliansyah on 20/07/2017.
 */

public class MainActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    private TextView textView;
    private Button btnLogout, btnSubmit, btnTodoListsList, btnListMenu;
    private EditText editText;

    Context context;
    HashMap<String, String> userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager sessionManager = new SessionManager(this);
        userData = sessionManager.getUserDetails();

        btnLogout = (Button)findViewById(R.id.logout_btn);
        btnSubmit = (Button)findViewById(R.id.submit);
        btnTodoListsList = (Button) findViewById(R.id.todolistlist_btn);
        btnListMenu = (Button) findViewById(R.id.listmenu_btn);
        textView = (TextView)findViewById(R.id.email);
        setText(textView, userData.get("email"));
        editText = (EditText)findViewById(R.id.something);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        context = this;

        // Set click listeners
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionManager sessionManager = new SessionManager(context);
                sessionManager.logoutUser();
                WebSocketClientManager.close();
                finish();
            }
        });

        btnTodoListsList.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the numbers View is clicked on.
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(MainActivity.this, MainMenuActivity.class);
                startActivity(mainIntent);
            }
        });

        btnListMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent listIntent = new Intent(MainActivity.this,TaskDetailsActivity.class);
                startActivity(listIntent);
            }
        });

        Log.e("WS", String.valueOf(WebSocketClientManager.connected()));
        if (!WebSocketClientManager.connected()) {
            WebSocketClientManager.createWebSocketConnection(getApplicationContext(), getString(R.string.uri_websocket));
        }
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                Map<String, String> msg = new HashMap<>();
                msg.put("type", "sending_info");
                msg.put("user", userData.get("email"));
                msg.put("msg", text);
                JSONObject jsonMsg = new JSONObject(msg);
                WebSocketClientManager.send(jsonMsg.toString());
                editText.setText("");
                DebugDB.getAddressLog();
            }
        });
    }

    private void setText(final TextView text, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }
}
