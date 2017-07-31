package com.example.acer.plnwunderlist;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


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

    private OnFragmentInteractionListener mListener;
    private CustomAdapter adapter;
    private ArrayList<DataModel> taskList;
    private ListView listView;

    private String endpoint;
    private JSONArray todoItems;

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
        }

        endpoint = getString(R.string.uri_endpoint);

        //Initialize ArrayList and CustomAdapter
        taskList = new ArrayList<>();
        adapter = new CustomAdapter(taskList, this.getContext(), mIsStrikethrough);
        //Initialize OnCheckboxClickedListener to make the damn thing work.
        adapter.setOnCheckboxClickedListener(this);
    }

    public void refreshList(){
        adapter.notifyDataSetChanged();
    }

    public void addTask(DataModel task) {
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
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void checkboxClicked(int pos) {
            adapter.remove(adapter.getItem(pos));
            adapter.notifyDataSetChanged();
        }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getItemsList(listID);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        boolean completedItemClicked(DataModel data);
    }

    private void getItemsList(String listID) {
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
                                    adapter.add(new DataModel(item.getString("ITEM_DESC"), is_completed));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        adapter.notifyDataSetChanged();

                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView parent, View view, int position, long id) {

                                DataModel dataModel= (DataModel) taskList.get(position);
                                dataModel.checked = !dataModel.checked;
                                adapter.notifyDataSetChanged();
                            }
                        });
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
