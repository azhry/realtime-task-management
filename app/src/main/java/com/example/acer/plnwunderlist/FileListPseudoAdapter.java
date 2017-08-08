package com.example.acer.plnwunderlist;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FileListPseudoAdapter {

    private LinearLayout managedLayout;
    private ArrayList<String> dataSource;
    private ArrayList<View> dataView;
    private Context mContext;
    private int viewCount;

    public FileListPseudoAdapter(LinearLayout managedLayout, Context mContext) {
        this.managedLayout = managedLayout;
        this.mContext = mContext;
        this.dataSource = new ArrayList<>();
        this.dataView = new ArrayList<>();
        this.viewCount = 0;
    }

    public void add(int fileID, String newData) {
        View newView = createNewItem(fileID, newData);

        dataSource.add(newData);
        dataView.add(newView);

        managedLayout.addView(newView);
        viewCount = managedLayout.getChildCount();
    }

    public int getFileIndex(View file) {
        for (int i = 0; i < viewCount; i++) {
            if (managedLayout.getChildAt(i) == file) {
                return i;
            }
        }
        return -1;
    }

    private View createNewItem(final int fileID, final String data) {
        final View result = LayoutInflater.from(mContext).inflate(R.layout.task_details_file_list, managedLayout, false);
        TextView fileTitle = (TextView) result.findViewById(R.id.fileNameLabel);
        fileTitle.setText(data);
        ImageView downloadBtn = (ImageView) result.findViewById(R.id.downloadFileBtn);

        final String url = mContext.getString(R.string.uri_server) + "/uploads/" + data.replace(" ", "%20");

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new FileDownloaderTask(mContext, data).execute(url);
                downloadFile(url, data);
            }
        });
        ImageView deleteBtn = (ImageView) result.findViewById(R.id.deleteFileBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringRequest deleteFileRequest = new StringRequest(Request.Method.POST, mContext.getString(R.string.uri_endpoint),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                managedLayout.removeViewAt(getFileIndex(result));
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String msg = error.getMessage();
                                if (msg != null) {
                                    Log.e("DELETE_ERROR", msg);
                                }
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("action", "delete_item_files");
                        params.put("file_id", String.valueOf(fileID));
                        return params;
                    }
                };

                AppSingleton.getInstance(mContext).addToRequestQueue(deleteFileRequest, "DELETE_FILE_REQUEST");
            }

        });
        return result;
    }

    public void downloadFile(String DownloadUrl, String FileName){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadUrl));
        request.setDescription("Downloading " + FileName);   //appears the same in Notification bar while downloading
        request.setTitle(FileName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalFilesDir(mContext.getApplicationContext(),null, FileName);

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
}
