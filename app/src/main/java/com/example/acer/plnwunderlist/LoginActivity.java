package com.example.acer.plnwunderlist;

/**
 * Created by Azhary Arliansyah on 20/07/2017.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private String URL_FOR_LOGIN;
    ProgressDialog progressDialog;
    private EditText loginInputEmail, loginInputPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginInputEmail = (EditText)findViewById(R.id.email);
        loginInputPassword = (EditText)findViewById(R.id.password);
        btnLogin = (Button)findViewById(R.id.login_btn);

        URL_FOR_LOGIN = getString(R.string.uri_endpoint);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(loginInputEmail.getText().toString(), loginInputPassword.getText().toString());
            }
        });
    }

    private void loginUser(final String email, final String password) {
        String cancel_req_tag = "login";

        /** Displaying progress dialog while logging in, for user experience */
        progressDialog.setMessage("Logging in...");
        showDialog();

        /** Listener to handle server 'OK' response from login request */
        final Context loginContext = this;
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                /** Dismiss progress dialog after receiving response */
                hideDialog();

                // Log.e("LOGIN_JSON", response);

                try {
                    /** Convert string response to JSON object */
                    JSONObject jObj = new JSONObject(response);

                    /**
                     * Check whether the user entered valid credentials.
                     * Error is occured when user entered wrong credentials or required params is missing.
                     */
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        JSONObject userJson = jObj.getJSONObject("user");
                        String user_email   = userJson.getString("email");
                        String user_name    = userJson.getString("name");
                        int user_id         = userJson.getInt("user_id");
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        SessionManager sessionManager = new SessionManager(loginContext);
                        sessionManager.createLoginSession(user_name, user_email, String.valueOf(user_id));
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    /** Exception is thrown if the response is not valid JSON string */
                    Log.e("LOGIN", "exception - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        /**
         * Listener to handle server 'NOT OK' response.
         * Usually happens when there is network error or internal server error (HTTP 500).
         */
        Response.ErrorListener responseErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("LOGIN", error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        };

        /** Attach the listeners to string request object with POST method */
        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_FOR_LOGIN, responseListener, responseErrorListener) {

            /** Set required parameters to be sent to server */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "login");
                params.put("email", email);
                params.put("password", password);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("action", "login");
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        /** Add request to request queue */
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, cancel_req_tag);
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
