package com.example.acer.plnwunderlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
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

    public void add(String newData){
        View newView = createNewItem(newData);

        dataSource.add(newData);
        dataView.add(newView);

        managedLayout.addView(newView);
    }

    private View createNewItem(String data){
        View result = LayoutInflater.from(mContext).inflate(R.layout.task_details_file_list, managedLayout, false);
        TextView fileTitle = (TextView) result.findViewById(R.id.fileNameLabel);
        fileTitle.setText(data);

        return result;
    }
}
