package com.example.acer.plnwunderlist;

import android.content.Context;
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

    private ArrayList<DataModel> dataSet;
    OnCheckboxClickedListener mCallback;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        CheckBox checkBox;
    }

    public interface OnCheckboxClickedListener {
        public void checkboxClicked(int pos);
    }

    public CustomAdapter(ArrayList data, Context context) {
        super(context, R.layout.list_menu_list, data);
        this.dataSet = data;
        this.mContext = context;

    }
    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public DataModel getItem(int position) {
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

            result=convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        DataModel item = getItem(position);

        viewHolder.txtName.setText(item.name);
        viewHolder.checkBox.setChecked(item.checked);
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
