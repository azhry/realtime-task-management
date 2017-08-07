package com.example.acer.plnwunderlist;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.jar.Pack200;

/**
 * Created by Ryan Fadholi on 31/07/2017.
 */

public class ListMemberAdapter extends ArrayAdapter<User> {

    private String listOwnerID;
    private HashMap<String, String> userData;
    public ListMemberAdapter(@NonNull Context context, @NonNull List<User> objects) {
        super(context, 0, objects);

        this.userData = new SessionManager(getContext()).getUserDetails();
    }

    public void setListOwnerID(String listOwnerID){
        this.listOwnerID = listOwnerID;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_share_list, parent, false);
        }

        // Get the {@link Word} object located at this position in the list
        User currentUser = getItem(position);
        String currentUserID = String.valueOf(currentUser.getUserID());
        String currentUserName = currentUser.getName();
        String currentEmail = currentUser.getEmail();

        // Find the TextView in the main_menu_list.xml layout with the ID ListTitle
        TextView nameTextView = (TextView) listItemView.findViewById(R.id.txtRealName);
        TextView emailTextView = (TextView) listItemView.findViewById(R.id.txtEmail);

        //If this row is User's, change the view
        if(currentUserID.equals(this.userData.get(SessionManager.KEY_ID))){
            nameTextView.setTypeface(null, Typeface.BOLD);
            emailTextView.setTypeface(emailTextView.getTypeface(), Typeface.BOLD);
        } else {
            nameTextView.setTypeface(null, Typeface.NORMAL);
            emailTextView.setTypeface(null, Typeface.ITALIC);
        }

        // Get the list title from the current object and
        // set this text on the name TextView
        nameTextView.setText(currentUserName);
        emailTextView.setText(currentEmail);

        ImageView ownerIcon = (ImageView) listItemView.findViewById(R.id.ownerIcon);

        if(this.listOwnerID != null && currentUserID.equals(this.listOwnerID)){
                ownerIcon.setVisibility(View.VISIBLE);
        } else {
            ownerIcon.setVisibility(View.INVISIBLE);
        }

        // Return the whole list item layout (containing an ImageView and a TextView)
        // so that it can be shown in the ListView
        return listItemView;
    }
}
