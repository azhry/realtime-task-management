package com.example.acer.plnwunderlist;

import android.os.Bundle;
import android.widget.AbsListView;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Ryan Fadholi on 20/07/2017.
 */

public class MainMenuActivity extends AppCompatActivity {

    private static final String TAG = "MainMenuActivity";
    private ArrayList<String> todoLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        //the scale. Refer to:
        //https://stackoverflow.com/questions/4275797/view-setpadding-accepts-only-in-px-is-there-anyway-to-setpadding-in-dp
        final float scale = getResources().getDisplayMetrics().density;

        ListView todoListsList = (ListView) findViewById(R.id.todolistslist);
        todoLists.add("Kerjaan");
        todoLists.add("Gawean");
        todoLists.add("Lokak");
        todoLists.add("Belajar");
        todoLists.add("Kerjaan");
        todoLists.add("Gawean");
        todoLists.add("Lokak");
        todoLists.add("Belajar");
        todoLists.add("Kerjaan");
        todoLists.add("Gawean");
        todoLists.add("Lokak");
        todoLists.add("Belajar");
        todoLists.add("Kerjaan");
        todoLists.add("Gawean");
        todoLists.add("Lokak");
        todoLists.add("Belajar");
        todoLists.add("Kerjaan");
        todoLists.add("Gawean");
        todoLists.add("Lokak");
        todoLists.add("Belajar");

        todoListAdapter adapter = new todoListAdapter(this, todoLists);
        todoListsList.setAdapter(adapter);

        //Get the pre-defined padding and convert it to px using the scale defined before.
        int dpBtnPadding = getResources().getInteger(R.integer.normal_btn_margin_in_dp);
        int pxBtnPadding = (int) (dpBtnPadding * scale + 0.5f);

        //Create a LinearLayout to wrap the buttons.
        //It's needed to force the button to center.
        LinearLayout layout = new LinearLayout(this);
        //The LayoutParams used are from AbsListView,
        // because it'll be a ListView footer.
        layout.setLayoutParams(new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
        //Set the gravity, which is the only reason we need to implement a LinearLayout
        //You can't set the goddamn gravity without wrapping the button.
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        //Create a new button View
        Button newListBtn = new Button(this);
        //Set the layout parameter as needed.
        newListBtn.setLayoutParams(new AbsListView.LayoutParams(
                AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.MATCH_PARENT));
        //Set the padding. The calculations are done in the upside.
        newListBtn.setPadding(pxBtnPadding, pxBtnPadding, pxBtnPadding, pxBtnPadding);

        //Set the text and its color, also the button's background color.
        newListBtn.setText(getResources().getString(R.string.create_new_list_label));
        newListBtn.setBackground(getDrawable(R.color.btn_blue));
        newListBtn.setTextColor(getResources().getColor(android.R.color.white));

        //Wrap the button in the LinearLayout we described before
        layout.addView(newListBtn);

        //Aaaand done. Tens of complicated lines of code just for implementing this shit.
        todoListsList.addFooterView(layout);

    }

}
