package com.example.acer.plnwunderlist;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import tech.gusavila92.websocketclient.WebSocketClient;

public class MainActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    private TextView textView, connectionStatus;
    private Button btnLogout, btnSubmit;
    private EditText editText;
    Context context;
    private WebSocketClient webSocketClient;
    HashMap<String, String> userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager sessionManager = new SessionManager(this);
        userData = sessionManager.getUserDetails();

        btnLogout = (Button)findViewById(R.id.logout_btn);
        btnSubmit = (Button)findViewById(R.id.submit);
        textView = (TextView)findViewById(R.id.email);
        textView.setText(userData.get("email"));
        connectionStatus = (TextView)findViewById(R.id.connection_status);
        editText = (EditText)findViewById(R.id.something);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        context = this;
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionManager sessionManager = new SessionManager(context);
                sessionManager.logoutUser();
                finish();
            }
        });

        createWebSocketClient();
    }

    private void createWebSocketClient() {
        connectionStatus.setText("Connecting...");
        URI uri;
        try {
            uri = new URI("ws://pudinglab.id:2000/puding-master/PLN");
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                System.out.println("onOpen");
                Log.i("WEBUSOCKETO: ", "on open!");
                webSocketClient.send(userData.get("email") + " is online!");
                connectionStatus.setText("Online");
            }

            @Override
            public void onTextReceived(String message) {
                System.out.println("onTextReceived");
            }

            @Override
            public void onBinaryReceived(byte[] data) {
                System.out.println("onBinaryReceived");
            }

            @Override
            public void onPingReceived(byte[] data) {
                System.out.println("onPingReceived");
            }

            @Override
            public void onPongReceived(byte[] data) {
                System.out.println("onPongReceived");
            }

            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onCloseReceived() {
                System.out.println("onCloseReceived");
                connectionStatus.setText("Disconnected");
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                webSocketClient.send(text);
                editText.setText("");
            }
        });
    }
}
