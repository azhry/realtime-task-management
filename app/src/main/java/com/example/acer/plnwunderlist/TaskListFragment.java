package com.example.acer.plnwunderlist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TaskListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TaskListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskListFragment extends Fragment implements CustomAdapter.OnCheckboxClickedListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "isStrikethrough";
    private static final String ARG_PARAM2 = "listID";

    //Variables to store param
    private Boolean mIsStrikethrough;
    private String listID;

    private CustomAdapter adapter;
    private ArrayList<TodoItem> taskList;
    private ListView listView;
    private Boolean isOngoingFragment;

    private TaskListFragment.OnFragmentInteractionListener mListener;

    private String endpoint;
    private JSONArray todoItems;

    private DBPLNHelper db;

    public TaskListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param isStrikethrough a flag to decide if the view needs Strikethrough.
     * @return A new instance of fragment TaskListFragment.
     */
    public static TaskListFragment newInstance(Boolean isStrikethrough, String listID) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, isStrikethrough);
        args.putString(ARG_PARAM2, listID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsStrikethrough = getArguments().getBoolean(ARG_PARAM1);
            listID = getArguments().getString(ARG_PARAM2);
            //isOngoing is the opposite of mIsStrikethroughm because if the strikethrough
            //flag is set to true, then the fragment is supposed to be a completedFragment,
            //and vice versa.
            isOngoingFragment = !mIsStrikethrough;
        }
        endpoint = getString(R.string.uri_endpoint);
        db = new DBPLNHelper(getContext());
    }

    public void refreshList(){
        adapter.clear();
        //getItemsList(this.listID);
    }



    public void addTask(TodoItem task) {
        adapter.add(task);
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_completed_list, container, false);

        //Initialize listView here, as the view wasn't inflated yet in onCreate.
        listView = (ListView) rootView.findViewById(R.id.completedListView);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            this.mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void checkboxClicked(int pos) {
        mListener.fragmentCheckboxClicked(adapter.getItem(pos),
                isOngoingFragment);
        adapter.remove(adapter.getItem(pos));
        adapter.notifyDataSetChanged();

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initialize ArrayList and CustomAdapter
        taskList = new ArrayList<>();
        this.adapter = new CustomAdapter(taskList, getContext(), mIsStrikethrough);
        //Initialize OnCheckboxClickedListener to make the damn thing work.
        this.adapter.setOnCheckboxClickedListener(this);
        //asasa
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {

                //Initialize the Intent
                Intent taskDetailsIntent = new Intent(getActivity().getApplicationContext(), TaskDetailsActivity.class);

                //Get selected TodoItem object
                TodoItem clickedTask = (TodoItem) listView.getItemAtPosition(position);
                //parcel the TodoItem
                taskDetailsIntent.putExtra("TODO_OBJECT",clickedTask);
                //and extract its name for the page title, and ID for reference.

                startActivity(taskDetailsIntent);

            }
        });

        getItemsList(listID);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check should we need to refresh the fragment
        refreshList();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        boolean fragmentCheckboxClicked(TodoItem data, boolean isOngoingFragment);
    }

    private void getItemsList(final String listID) {
        final String REQUEST_TAG = "get_todo_item";
        String REQUEST_URI = endpoint + "?action=" + REQUEST_TAG + "&list_id=" + listID;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, REQUEST_URI, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        todoItems = response;
                        for (int i = 0; i < todoItems.length(); i++) {
                            try {
                                JSONObject item = todoItems.getJSONObject(i);
                                boolean is_completed = item.getInt("IS_COMPLETED") == 1 ? true : false;
                                if (is_completed == mIsStrikethrough) {
                                    adapter.add(TodoItem.newInstance(item));
                                }
                                Cursor c = db.select("todo_items", "SERVER_ID=" + item.getInt("TODO_ID") +
                                            " AND LIST_ID=" + listID + " AND STATUS=1");
                                if (!c.moveToFirst()) {
                                    Map<String, String> contentValues = new HashMap<>();
                                    contentValues.put("TODO_ID", item.getString("TODO_ID"));
                                    contentValues.put("LIST_ID", item.getString("LIST_ID"));
                                    contentValues.put("ITEM_DESC", item.getString("ITEM_DESC"));
                                    contentValues.put("DUE_DATE", item.getString("DUE_DATE"));
                                    contentValues.put("NOTE", item.getString("NOTE"));
                                    contentValues.put("IS_COMPLETED", item.getString("IS_COMPLETED"));
                                    contentValues.put("SERVER_ID", item.getString("TODO_ID"));
                                    contentValues.put("STATUS", "1");
                                    contentValues.put("ACTION", "0");
                                    db.insert("todo_items", contentValues);
                                } else {

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if (msg != null)
                            Log.e(REQUEST_TAG, msg);
                    }
                });
        AppSingleton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(jsonArrayRequest, REQUEST_TAG);
    }
}
