package com.example.acer.plnwunderlist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    private TextView textView;
    private Button btnLogout;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> userData = sessionManager.getUserDetails();

        btnLogout = (Button)findViewById(R.id.logout_btn);
        textView = (TextView)findViewById(R.id.email);
        textView.setText(userData.get("email"));
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
    }
}
