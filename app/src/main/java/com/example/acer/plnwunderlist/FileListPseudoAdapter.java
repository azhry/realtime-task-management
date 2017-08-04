package com.example.acer.plnwunderlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ryan Fadholi on 28/07/2017.
 */

public class FileListPseudoAdapter {

    private LinearLayout managedLayout;
    private ArrayList<String> dataSource;
    private ArrayList<View> dataView;
    private Context mContext;

    public FileListPseudoAdapter(LinearLayout managedLayout, Context mContext) {
        this.managedLayout = managedLayout;
        this.mContext = mContext;
        this.dataSource = new ArrayList<>();
        this.dataView = new ArrayList<>();
    }

    public void add(int fileID, String newData){
        View newView = createNewItem(fileID, newData);

        dataSource.add(newData);
        dataView.add(newView);

        managedLayout.addView(newView);
    }

    private View createNewItem(int fileID, final String data){
        View result = LayoutInflater.from(mContext).inflate(R.layout.task_details_file_list, managedLayout, false);
        TextView fileTitle = (TextView) result.findViewById(R.id.fileNameLabel);
        fileTitle.setText(data);
        ImageView downloadBtn = (ImageView) result.findViewById(R.id.downloadFileBtn);

        final String url = mContext.getString(R.string.uri_server) + "/uploads/" + data;

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FileDownloaderTask(url, data, mContext).execute();
            }
        });
        ImageView deleteBtn = (ImageView) result.findViewById(R.id.deleteFileBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return result;
    }
}
