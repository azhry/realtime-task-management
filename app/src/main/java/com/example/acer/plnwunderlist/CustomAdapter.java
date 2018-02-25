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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Ryan Fadholi on 24/07/2017.
 */

public class CustomAdapter extends ArrayAdapter {

    private ArrayList<TodoItem> dataSet;
    private OnCheckboxClickedListener mCallback;
    private Context mContext;
    private Boolean isStrikethrough;


    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtDate;
        CheckBox checkBox;
        ImageView noteIcon;
        ImageView fileIcon;
    }

    final static public Comparator<TodoItem> TodoItemComparator = new Comparator<TodoItem>() {
        public int compare(TodoItem e1, TodoItem e2) {
            int dateCompare = 0;

            boolean isFirstDueDateNull = e1.getDueDate() == null;
            boolean isSecondDueDateNull = e2.getDueDate() == null;

            if (!isFirstDueDateNull && !isSecondDueDateNull) {
                dateCompare = e1.getDueDate().compareTo(e2.getDueDate());
            } else if (isFirstDueDateNull && !isSecondDueDateNull) {
                dateCompare = 1;
            } else if (!isFirstDueDateNull && isSecondDueDateNull) {
                dateCompare = -1;
            }

            if (dateCompare != 0) {
                return dateCompare;
            }

            String e1desc = e1.getDescription().toLowerCase();
            String e2desc = e2.getDescription().toLowerCase();
            return e1desc.compareTo(e2desc);
        }
    };

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public interface OnCheckboxClickedListener {
        public void checkboxClicked(int pos);
    }

    public CustomAdapter(ArrayList data, Context context, Boolean isStrikethrough) {
        super(context, R.layout.list_menu_list, data);
        this.dataSet = data;
//        Log.e("ADAPTER", "CUSTOMADAPTER");
//        for (TodoItem t : dataSet) {
//            Log.e("ITEM", t.getDescription());
//        }
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

        final CheckBox itemCheckBox;

        final int callbackPosition = position;
        ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_menu_list, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            viewHolder.txtDate = (TextView) convertView.findViewById(R.id.txtDate);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            viewHolder.noteIcon = (ImageView) convertView.findViewById(R.id.noteIcon);
            viewHolder.fileIcon = (ImageView) convertView.findViewById(R.id.fileIcon);

            if (isStrikethrough) {
                viewHolder.txtName.setPaintFlags(viewHolder.txtName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            convertView.setTag(viewHolder);
            result = convertView;
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.noteIcon = (ImageView) convertView.findViewById(R.id.noteIcon);
            viewHolder.fileIcon = (ImageView) convertView.findViewById(R.id.fileIcon);
            result = convertView;
        }

        TodoItem item = getItem(position);
        viewHolder.txtName.setText(item.getDescription());

        //Check if there's due date set
        if (item.getDueDate() != null) {
            viewHolder.txtDate.setText("Due " + AppHelper.formatDate(item.getDueDate(), false));
            viewHolder.txtDate.setVisibility(View.VISIBLE);
        } else {
            viewHolder.txtDate.setText("");
            viewHolder.txtDate.setVisibility(View.GONE);
        }

        //Check if there's file attached
        if (item.hasFiles()) { //TODO: write logic here
            viewHolder.fileIcon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.fileIcon.setVisibility(View.GONE);
        }

        //Check if there's note attached
        if (item.getNote() != null) {
            //If the file icon is visible, move the note to the left of file icon.
            android.widget.RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) viewHolder.noteIcon.getLayoutParams();
            if(viewHolder.fileIcon.getVisibility() == View.VISIBLE){
                params.addRule(RelativeLayout.LEFT_OF, R.id.fileIcon);
                params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            } else {
                params.removeRule(RelativeLayout.LEFT_OF);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            }

            viewHolder.noteIcon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.noteIcon.setVisibility(View.GONE);
        }


        itemCheckBox = (CheckBox) result.findViewById(R.id.checkBox);
        itemCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.checkboxClicked(callbackPosition);
            }
        });
        itemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                itemCheckBox.setChecked(!b);
                compoundButton.jumpDrawablesToCurrentState();
            }
        });
        return result;
    }
}
