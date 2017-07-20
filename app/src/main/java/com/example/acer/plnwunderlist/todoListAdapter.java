package com.example.acer.plnwunderlist;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ryan Fadholi on 20/07/2017.
 */

public class todoListAdapter extends ArrayAdapter<String> {

    public todoListAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.main_menu_list, parent, false);
        }

        // Get the {@link Word} object located at this position in the list
        String currentList = getItem(position);

        // Find the TextView in the main_menu_list.xml layout with the ID ListTitle
        TextView todoListTextView = (TextView) listItemView.findViewById(R.id.listTitle);
        // Get the list title from the current object and
        // set this text on the name TextView
        todoListTextView.setText(currentList);

        // Return the whole list item layout (containing an ImageView and a TextView)
        // so that it can be shown in the ListView
        return listItemView;
    }
}
