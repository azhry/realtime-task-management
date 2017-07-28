package com.example.acer.plnwunderlist;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan Fadholi on 24/07/2017.
 */

public class ListMenuActivityTabbed extends AppCompatActivity implements
        CompletedListFragment.OnFragmentInteractionListener, OngoingListFragment.OnFragmentInteractionListener {
    ArrayList<DataModel> dataModels;
    ListView listView;
    private CustomAdapter adapter;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

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
    public void onFragmentInteraction(Uri uri) {

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.list_share_btn:
                Log.d("MENUEXAMPLE","Yooo it works!");
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testcoordinatorlayout);

        this.toolbar = (Toolbar) findViewById(R.id.list_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        //If calling activity supplies current to-do list name,
        //Change App Title to the supplied name.
        if(getIntent().hasExtra("TODO_LIST_NAME")){
            this.setTitle(getIntent().getStringExtra("TODO_LIST_NAME"));
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OngoingListFragment(), "ONGOING");
        adapter.addFragment(new CompletedListFragment(), "COMPLETED");
        viewPager.setAdapter(adapter);
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
