package com.example.acer.plnwunderlist;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Ryan Fadholi on 24/07/2017.
 */

public class CustomAdapter extends ArrayAdapter{

    private ArrayList<TodoItem> dataSet;
    private OnCheckboxClickedListener mCallback;
    private Context mContext;
    private Boolean isStrikethrough;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        CheckBox checkBox;
    }

    public interface OnCheckboxClickedListener {
        public void checkboxClicked(int pos);
    }

    public CustomAdapter(ArrayList data, Context context, Boolean isStrikethrough) {
        super(context, R.layout.list_menu_list, data);
        this.dataSet = data;
        this.mContext = context;
        this.isStrikethrough = isStrikethrough;
    }
    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public TodoItem getItem(int position) {
        return dataSet.get(position);
    }

    public void setOnCheckboxClickedListener(OnCheckboxClickedListener mCallback) {
        this.mCallback = mCallback;
    }
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        CheckBox itemCheckBox;

        final int callbackPosition = position;
        ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_menu_list, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

            if(isStrikethrough){
                viewHolder.txtName.setPaintFlags(viewHolder.txtName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            result=convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        TodoItem item = getItem(position);

        viewHolder.txtName.setText(item.getDescription());
        //viewHolder.checkBox.setChecked(item.checked);
        itemCheckBox = (CheckBox) result.findViewById(R.id.checkBox);
        itemCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.checkboxClicked(callbackPosition);
            }
        });
        return result;
    }
}
