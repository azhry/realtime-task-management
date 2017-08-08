package com.example.acer.plnwunderlist;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Comparator;
import java.util.List;

/**
 * Created by Ryan Fadholi on 20/07/2017.
 */

public class TodoListAdapter extends ArrayAdapter<TodoList> {

    public TodoListAdapter(@NonNull Context context, @NonNull List<TodoList> objects) {
        super(context, 0, objects);
    }

    final static public Comparator<TodoList> TodoListComparator = new Comparator<TodoList>() {
        public int compare(TodoList e1, TodoList e2) {
        String listName1 = e1.getName().toLowerCase();
        String listName2 = e2.getName().toLowerCase();

           return listName1.compareTo(listName2);
        }
    };

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
        TodoList currentList = getItem(position);
        String currentListName = currentList.getName();

        // Find the TextView in the main_menu_list.xml layout with the ID ListTitle
        TextView todoListTextView = (TextView) listItemView.findViewById(R.id.listTitle);
        // Get the list title from the current object and
        // set this text on the name TextView
        todoListTextView.setText(currentListName);

        // Return the whole list item layout (containing an ImageView and a TextView)
        // so that it can be shown in the ListView
        return listItemView;
    }
}
