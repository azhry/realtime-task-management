package com.example.acer.plnwunderlist;

import android.content.Context;
import android.net.Uri;
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
 * {@link CompletedListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CompletedListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompletedListFragment extends Fragment implements CustomAdapter.OnCheckboxClickedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private CustomAdapter adapter;
    private ArrayList<DataModel> taskList;
    private ListView listView;

    public CompletedListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CompletedListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CompletedListFragment newInstance(String param1, String param2) {
        CompletedListFragment fragment = new CompletedListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //Initialize ArrayList and CustomAdapter
        taskList = new ArrayList<>();
        adapter = new CustomAdapter(taskList, this.getContext());
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

                DataModel dataModel= (DataModel) taskList.get(position);
                dataModel.checked = !dataModel.checked;
                adapter.notifyDataSetChanged();
            }
        });
        
        
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
}
