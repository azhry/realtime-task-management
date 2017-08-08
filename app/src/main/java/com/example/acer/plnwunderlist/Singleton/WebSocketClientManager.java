package com.example.acer.plnwunderlist.Singleton;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.example.acer.plnwunderlist.ListShareActivity;
import com.example.acer.plnwunderlist.MainGatewayActivity;
import com.example.acer.plnwunderlist.R;
import com.example.acer.plnwunderlist.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tech.gusavila92.websocketclient.WebSocketClient;

/**
 * Created by Azhary Arliansyah on 25/07/2017.
 */

public class WebSocketClientManager {

    private static WebSocketClient mWebSocketClient;
    private static URI uri;
    public static boolean isConnected;
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
            //mWebSocketClient.close();
            mWebSocketClient.setConnectTimeout(10000);
            mWebSocketClient.setReadTimeout(60000);
            mWebSocketClient.enableAutomaticReconnection(5000);
            mWebSocketClient.connect();
            Log.e("WS", "connect");
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
                    Log.e("WSHandler", "on text received");
                    try {
                        JSONObject response = new JSONObject(message);
                        String action = response.getString("action");
                        if (action.equals("connection_status")) {
                            isConnected = false;
                            Log.e("ConnectionStatus", "Disconnect");
                        } else if (action.equals("invite_notification")) {
                            String listName = response.getString("LIST_NAME");
                            int listID = response.getInt("LIST_ID");
                            showInviteNotification(listID, listName);
                            Log.e("InvitationStatus", "Success");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onBinaryReceived(byte[] data) {
                    Log.e("WSHandler", "on binary received");
                }

                @Override
                public void onPingReceived(byte[] data) {
                    Log.e("WSHandler", "on ping received");
                }

                @Override
                public void onPongReceived(byte[] data) {
                    Log.e("WSHandler", "on pong received");
                }

                @Override
                public void onException(Exception e) {
                    String msg = e.getMessage();
                    if (msg != null)
                        Log.e("WebSocket", msg);
                    Log.e("WebSocket", "EXCEPTION");
                    isConnected = false;
                }

                @Override
                public void onCloseReceived() {
                    isConnected = false;
                }
            };
        return mWebSocketClient;
    }

    private static void showInviteNotification(int notificationID, String listName) {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_add_white_24dp)
                        .setContentTitle("Invitation")
                        .setContentText("You've been invited to " + listName + " list")
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setVibrate(new long[] {1000, 1000})
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        Intent shareNotificationIntent = new Intent(mContext, MainGatewayActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainGatewayActivity.class);
        stackBuilder.addNextIntent(shareNotificationIntent);
        PendingIntent shareNotificationPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(shareNotificationPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationID, mBuilder.build());
    }
}
