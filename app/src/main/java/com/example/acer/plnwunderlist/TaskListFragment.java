package com.example.acer.plnwunderlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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

    //Variables to store param
    private Boolean mIsStrikethrough;

    private CustomAdapter adapter;
    private ArrayList<DataModel> taskList;
    private ListView listView;

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
    public static TaskListFragment newInstance(Boolean isStrikethrough) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, isStrikethrough);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsStrikethrough = getArguments().getBoolean(ARG_PARAM1);
        }

        //Initialize ArrayList and CustomAdapter
        taskList = new ArrayList<>();
        adapter = new CustomAdapter(taskList, this.getContext(), mIsStrikethrough);
        //Initialize OnCheckboxClickedListener to make the damn thing work.
        adapter.setOnCheckboxClickedListener(this);
    }

    public void refreshList(){

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
        
        adapter.add(new DataModel("Completed 1", true));
        adapter.add(new DataModel("Completed 2", true));
        adapter.add(new DataModel("Completed 3", true));
        adapter.add(new DataModel("Completed 4", true));
        adapter.add(new DataModel("Completed 5", true));
        adapter.add(new DataModel("Completed 6", true));
        adapter.add(new DataModel("Completed 7", true));
        adapter.add(new DataModel("Completed 8", true));
        adapter.add(new DataModel("Completed 9", true));
        adapter.add(new DataModel("Completed 10", true));
        adapter.add(new DataModel("Completed 11", true));
        adapter.add(new DataModel("Completed 12", true));
        adapter.notifyDataSetChanged();

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {

                //Initialize the Intent
                Intent taskDetailsIntent = new Intent(getActivity().getApplicationContext(), TaskDetailsActivity.class);
                //Get selected Todolist object, and extract its name for the page title.
                String pageTitle = ((DataModel) listView.getItemAtPosition(position)).name;
                //Send the to-do list title as extra information to the ListMenuActivity
                taskDetailsIntent.putExtra("TASK_NAME", pageTitle);
                taskDetailsIntent.putExtra("INTENT_TYPE", "EDIT");
                startActivity(taskDetailsIntent);
            }
        });
        
        
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
        boolean completedItemClicked(DataModel data);
    }
}
