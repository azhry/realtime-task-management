package com.example.acer.plnwunderlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainGatewayActivity extends AppCompatActivity {

    //DO NOT DELETE!
    //This activity is the one called first when the app is invoked.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent activityIntent;

        //Based on the user log status, invoke different activity.
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isUserLoggedIn()) {
            activityIntent = new Intent(this, MainMenuActivity.class);
        } else {
            activityIntent = new Intent(this, LoginActivity.class);
        }

        startActivity(activityIntent);
        finish();
    }

}
