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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;
import com.github.clans.fab.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.clans.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ryan Fadholi on 24/07/2017.
 */

public class ListMenuActivity extends AppCompatActivity implements
        TaskListFragment.OnFragmentInteractionListener {
    ArrayList<DataModel> dataModels;
    ListView listView;
    private CustomAdapter adapter;

    private int currentListID;
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
                        shareIntent.putExtra("TODO_LIST_ID", currentListID);
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
        setContentView(R.layout.activity_testcoordinatorlayout);

        this.currentListID = getIntent().getIntExtra("TODO_LIST_ID",-1);

        this.toolbar = (Toolbar) findViewById(R.id.list_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        endpoint = getString(R.string.uri_endpoint);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        if (getIntent().hasExtra("TODO_LIST_ID")) {
            listID = getIntent().getStringExtra("TODO_LIST_ID");
        }

        //Initialize Fragments
        this.onGoingFragment = TaskListFragment.newInstance(false, listID);
        this.completedFragment = TaskListFragment.newInstance(true, listID);

        this.viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //If calling activity supplies current to-do list name,
        //Change App Title to the supplied name.
        if(getIntent().hasExtra("TODO_LIST_NAME")){
            this.setTitle(getIntent().getStringExtra("TODO_LIST_NAME"));
        }

        quickAddTask    = (FloatingActionButton) findViewById(R.id.quickAddTask);
        addTask         = (FloatingActionButton) findViewById(R.id.addTask);

        quickAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuickAddDialog(ListMenuActivity.this);
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(onGoingFragment, "ONGOING");
        adapter.addFragment(completedFragment, "COMPLETED");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean completedItemClicked(DataModel data) {
        return true;
    }

    private void showQuickAddDialog(Context context) {
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
                                onGoingFragment.addTask(new DataModel(name, false));
                                Toast.makeText(ListMenuActivity.this, name + " added!", Toast.LENGTH_LONG);
                            } else if (status == 1) {
                                Toast.makeText(ListMenuActivity.this, "Quick add failed!", Toast.LENGTH_LONG);
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

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
