package com.example.acer.plnwunderlist.Singleton;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.example.acer.plnwunderlist.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import tech.gusavila92.websocketclient.WebSocketClient;

/**
 * Created by Azhary Arliansyah on 25/07/2017.
 */

public class WebSocketClientManager {

    private static WebSocketClient mWebSocketClient;
    private static URI uri;
    private static boolean isConnected;
    private static HashMap<String, String> userData;
    private static Context mContext;

    public static void createWebSocketConnection(Context context, String wsUri) {
        mContext = context;
        try {
            isConnected = false;
            uri = new URI(wsUri);
            SessionManager sessionManager = new SessionManager(mContext);
            userData = sessionManager.getUserDetails();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        getInstance();

        if (!isConnected)
        {
            mWebSocketClient.setConnectTimeout(10000);
            mWebSocketClient.setReadTimeout(60000);
            mWebSocketClient.enableAutomaticReconnection(5000);
            mWebSocketClient.connect();
        }
    }

    public static boolean connected() {
        return isConnected;
    }

    public static void send(String data) {
        if (isConnected)
            mWebSocketClient.send(data);
    }

    public static void close() {
        mWebSocketClient.close();
        isConnected = false;
    }

    public static WebSocketClient getInstance() {
        if (mWebSocketClient == null)
            mWebSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen() {
                    Log.e("WS", "open");
                    isConnected = true;
                    Map<String, String> msg = new HashMap<>();
                    msg.put("type", "establishing_connection");
                    msg.put("user", userData.get("email"));
                    msg.put("user_id", userData.get("user_id"));
                    msg.put("msg", " is online!");
                    JSONObject jsonMsg = new JSONObject(msg);
                    mWebSocketClient.send(jsonMsg.toString());
                }

                @Override
                public void onTextReceived(String message) {
                    try {
                        JSONObject response = new JSONObject(message);
                        String action = response.getString("action");
                        if (action.equals("connection_status")) {
                            isConnected = false;
                            Log.e("ConnectionStatus", "Disconnect");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onBinaryReceived(byte[] data) {

                }

                @Override
                public void onPingReceived(byte[] data) {

                }

                @Override
                public void onPongReceived(byte[] data) {

                }

                @Override
                public void onException(Exception e) {
                    String msg = e.getMessage();
                    if (msg != null)
                        Log.e("WebSocket", msg);
                    isConnected = false;
                }

                @Override
                public void onCloseReceived() {
                    isConnected = false;
                }
            };
        return mWebSocketClient;
    }
}
