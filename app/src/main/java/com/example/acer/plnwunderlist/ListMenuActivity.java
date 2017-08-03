package com.example.acer.plnwunderlist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;
import com.github.clans.fab.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ryan Fadholi on 24/07/2017.
 */

public class ListMenuActivity extends AppCompatActivity implements
        TaskListFragment.OnFragmentInteractionListener {

    static int taskTabCount = 2;

    ArrayList<TodoItem> dataModels;
    ListView listView;
    private CustomAdapter adapter;

    private String currentListID;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String listID;

    private FloatingActionButton quickAddTask, addTask;
    private String endpoint;

    private TaskListFragment onGoingFragment, completedFragment;

    ProgressDialog progressDialog;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.todo_list_menu, menu);

        //------------------------------------------------------------------------------------------
        //START Menu Icon Tinting

        //Retrieve all Menu Items
        final MenuItem shareBtn = menu.findItem(R.id.list_share_btn);

        //Retrieve all Icons
        Drawable shareBtnIcon = shareBtn.getIcon();
        shareBtnIcon.mutate().setColorFilter(Color.argb(255, 255, 255, 255), PorterDuff.Mode.SRC_IN);

        shareBtn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.list_share_btn:
                        //Initialize the Intent
                        Intent shareIntent = new Intent(getApplicationContext(), ListShareActivity.class);
                        //Setup data to pass w/ the intent
                        shareIntent.putExtra("TODO_LIST_ID", listID);
                        if(getIntent().hasExtra("TODO_LIST_NAME")){
                            shareIntent.putExtra("TODO_LIST_NAME", getIntent().getStringExtra("TODO_LIST_NAME"));
                        }
                        startActivity(shareIntent);
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.list_share_btn:
                Log.d("MENUEXAMPLE","Yooo it works!");
                return true;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_task);

        //Define endpoint
        endpoint = getString(R.string.uri_endpoint);

        //Define toolbar menu icons, and enable back arrow
        this.toolbar = (Toolbar) findViewById(R.id.list_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Define progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        if (getIntent().hasExtra("TODO_LIST_ID")) {
            listID = getIntent().getStringExtra("TODO_LIST_ID");
        }

        quickAddTask    = (FloatingActionButton) findViewById(R.id.quickAddTask);
        addTask         = (FloatingActionButton) findViewById(R.id.addTask);

        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addTaskFormIntent = new Intent(ListMenuActivity.this, TaskDetailsActivity.class);
                addTaskFormIntent.putExtra("TODO_LIST_ID", listID);
                addTaskFormIntent.putExtra("TODO_LIST_NAME", getIntent().getStringExtra("TODO_LIST_NAME"));
                startActivity(addTaskFormIntent);
            }
        });

        this.viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new TaskListPagerAdapter(getSupportFragmentManager()));

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //If calling activity supplies current to-do list name,
        //Change App Title to the supplied name.
        if(getIntent().hasExtra("TODO_LIST_NAME")){
            this.setTitle(getIntent().getStringExtra("TODO_LIST_NAME"));
        }

    }

    @Override
    public boolean fragmentCheckboxClicked(TodoItem data, boolean isOngoingFragment) {
        //The boolean is used to identify which fragment called the callback.
        //true means the calling fragment is ongoingFragment,
        //false means the calling fragment is completedFragment.

        if(isOngoingFragment){
            //TODO change isComplete to 1
            updateItemStatus(1, data);
            completedFragment.addTask(data);
        } else {
            //TODO change isComplete to 0
            updateItemStatus(0, data);
            onGoingFragment.addTask(data);
        }

        return true;
    }

    private void updateItemStatus(final int flag, final TodoItem item) {
        final String REQUEST_TAG = "update_item_status";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", REQUEST_TAG);
                params.put("is_completed", String.valueOf(flag));
                params.put("todo_id", String.valueOf(item.getID()));
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest, REQUEST_TAG);
    }

    //OnClickListener for "Quick Add" FloatingActionButton.
    //Defined in the XML file.
    public void showQuickAddDialog(View v) {
        Context context = ListMenuActivity.this;

        LayoutInflater inflater = LayoutInflater.from(context);
        final View quickAddDialogView = inflater.inflate(R.layout.main_menu_create_list_dialog, null);
        final AlertDialog.Builder quickAddBuilder = new AlertDialog.Builder(context);
        quickAddBuilder.setTitle("Create New Task");
        quickAddBuilder.setView(quickAddDialogView);
        quickAddBuilder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText newTaskText = (EditText) quickAddDialogView.findViewById(R.id.newListTitleText);
                quickAddTask(newTaskText.getText().toString());
            }
        });
        quickAddBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog newList = quickAddBuilder.create();
        newList.show();
    }


    private void quickAddTask(final String name) {
        progressDialog.setMessage("Processing...");
        showDialog();

        String REQUEST_TAG = "quick_add_task";
        StringRequest quickAddRequest = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("JSON", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status == 0) {
                                onGoingFragment.addTask(TodoItem.newInstance(jsonObject));
                                Toast.makeText(ListMenuActivity.this, name + " added!", Toast.LENGTH_LONG).show();
                            } else if (status == 1) {
                                Toast.makeText(ListMenuActivity.this, "Quick add failed!", Toast.LENGTH_LONG).show();
                            } else {
                                Log.e("QUICK_ADD_SUCCESS", response.toString());
                            }
                        } catch (JSONException e) {
                            Log.e("QUICK_ADD_EXCEPTION", e.getMessage());
                            e.printStackTrace();
                        }
                        hideDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if (msg != null) {
                            Log.e("QUICK_ADD_ERROR", msg);
                        }
                        hideDialog();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "insert_todo_item");
                params.put("insert_type", "quick_add");
                params.put("list_id", listID);
                params.put("task_name", name);
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(quickAddRequest, REQUEST_TAG);
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public class TaskListPagerAdapter extends FragmentPagerAdapter {

        public TaskListPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            // Do NOT try to save references to the Fragments in getItem(),
            // because getItem() is not always called. If the Fragment
            // was already created then it will be retrieved from the FragmentManger
            // and not here (i.e. getItem() won't be called again).

            switch (position) {
                case 0:
                    return TaskListFragment.newInstance(false, listID);
                case 1:
                    return TaskListFragment.newInstance(true, listID);
                default:
                    // This should never happen. Always account for each position above
                    return null;
            }
        }

        @Override
        public int getCount() {
            return ListMenuActivity.taskTabCount;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    onGoingFragment = (TaskListFragment) createdFragment;
                    break;
                case 1:
                    completedFragment = (TaskListFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "Ongoing";
                case 1: return "Completed";
                // This should never happen. Always account for each position above
                default: return "undefined";
            }
        }
    }
}
