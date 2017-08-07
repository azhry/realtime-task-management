package com.example.acer.plnwunderlist;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.acer.plnwunderlist.Singleton.AppSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Created by Ryan Fadholi on 20/07/2017.
 */

public class MainMenuActivity extends AppCompatActivity {

    //Statics
    private static final String TAG = "MainMenuActivity";
    ProgressDialog progressDialog;      //'nuff said.
    HashMap<String, String> userData;
    //Pseudo-statics. Cannot be initialized because it's fetched from resources XML.
    private String endpoint;
    //Declare attributes necessary for UI black magic.
    //Everything is assumed to be initialized in onCreate() method.
    private ArrayList<TodoList> todoLists = new ArrayList<>();//Used to hold data
    private TodoListAdapter adapter;    //Used to bridge todoLists and todoListsList
    private ListView todoListsList;     //The RecyclerView.
    private View emptyTextView;         //Header that pops up when the list is empty.

    private DBPLNHelper db;
    private NetworkStateChecker networkStateReceiver;

    public static final int SYNCHED = 1;
    public static final int UNSYNCHED = 0;
    public static final String DATA_SAVED_BROADCAST = "com.example.acer.plnwunderlist";
    public final static int PERMISSIONS_REQUEST_READ_PHONE_STATE = 11;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        db = new DBPLNHelper(this);

        //Initalize the pseudo-statics
        endpoint = getString(R.string.uri_endpoint);

        //Initialize progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        todoListsList = (ListView) findViewById(R.id.todolistslist);
        todoListsList.setDivider(null);
        todoListsList.setDividerHeight(0);

        emptyTextView = getLayoutInflater().inflate(R.layout.main_menu_empty_list_text, null);

        todoListsList.addHeaderView(emptyTextView, null, false);
        SessionManager sessionManager = new SessionManager(this);
        userData = sessionManager.getUserDetails();

        loadLists();

        networkStateReceiver = new NetworkStateChecker();

        loadListsFromServer();

        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        View layout = getLayoutInflater().inflate(R.layout.main_menu_create_list_btn, null);
        todoListsList.addFooterView(layout);

        Button newListBtn = (Button) findViewById(R.id.add_list_btn);
        newListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddListDialog(MainMenuActivity.this);
            }
        });

        //Lastly, register the specified ContextMenu to the Listview.
        registerForContextMenu(todoListsList);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.todolistlists_context_menu, menu);

        //Catch the ContextMenu.ContextMenuItem sent from parameter
        //as AdapterView.AdapterContextMenuInfo to itemInfo variable.
        AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
        //Get the Item at the specified position, and get the value.
        TodoList selectedList  = (TodoList) todoListsList.getItemAtPosition(itemInfo.position);
        String titleName = selectedList.getName();
        //Set the header title
        menu.setHeaderTitle(titleName);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {

            case R.id.deleteTodoList:
                showDeleteListDialog(MainMenuActivity.this,
                        (TodoList) todoListsList.getItemAtPosition(info.position));
                return true;
            case R.id.EditTodoList:
                showEditListDialog(MainMenuActivity.this,
                        (TodoList) todoListsList.getItemAtPosition(info.position));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(networkStateReceiver);
        Log.e("FINISH", "DESTROYYYY");
        super.onDestroy();
    }

    private void loadListsFromServer() {
        progressDialog.setMessage("Loading data...");
        showDialog();

        adapter = new TodoListAdapter(this, todoLists);

        todoListsList.setAdapter(adapter);
        //Set onclick listener; send user to the item's respective to-do list.
        todoListsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                //Initialize the Intent
                Intent todolistIntent = new Intent(getApplicationContext(), ListMenuActivity.class);

                //Get selected Todolist object
                TodoList clickedList = (TodoList) todoListsList.getItemAtPosition(position);
                //and extract its name for the page title, and ID for reference.
                todolistIntent.putExtra("TODO_LIST_ID", clickedList.getID());
                todolistIntent.putExtra("TODO_LIST_NAME", clickedList.getName());

                startActivity(todolistIntent);

            }
        });

        String reqUrl = endpoint + "?action=get_todo_list&user_id=" + userData.get("user_id");
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, reqUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Log.e("JSON", response.toString());
                        todoLists.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String listID = jsonObject.getString("LIST_ID");
                                String listName = jsonObject.getString("LIST_NAME");
                                TodoList loadedTodoList = new TodoList(listID, listName);
                                Cursor c = db.select("todo_lists", "SERVER_ID=" + listID + " AND STATUS=1");
                                if (!c.moveToFirst()) {
                                    Map<String, String> contentValues = new HashMap<>();
                                    contentValues.put("LIST_ID", listID);
                                    contentValues.put("LIST_NAME", listName);
                                    contentValues.put("SERVER_ID", listID);
                                    contentValues.put("ACTION", "0");
                                    contentValues.put("STATUS", "1");
                                    db.insert("todo_lists", contentValues);
                                } else {
                                    do {
                                        String action = c.getString(c.getColumnIndex("ACTION"));
                                        if (action.equals("1")) {
                                            // local list on edited state, sync edit to server
                                            String serverId = c.getString(c.getColumnIndex("SERVER_ID"));
                                            String editedListName = c.getString(c.getColumnIndex("LIST_NAME"));
                                            TodoList editedTodoList = new TodoList(serverId, editedListName);
                                            editList(editedListName, editedTodoList);
                                            loadedTodoList.setName(editedListName);
                                        }
                                    } while (c.moveToNext());
                                }
                                c.close();

                                adapter.add(loadedTodoList);
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                Log.e("JSON_Exception", e.getMessage());
                                e.printStackTrace();
                            }
                        }

                        setEmptyTextVisibility(emptyTextView);

                        hideDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        String msg = error.getMessage();
                        if (msg != null)
                            Log.d("Error.Response", error.getMessage());
                        hideDialog();
                    }
                }

        );

        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(getRequest, "retrieve_list");
    }

    private void loadLists() {
        todoLists.clear();
        Cursor cursor = db.select("todo_lists");
        if (cursor.moveToFirst()) {
            do {
                String status = cursor.getString(cursor.getColumnIndex("STATUS"));
                if (status.equals("1")) {
                    TodoList todoList = new TodoList(
                            cursor.getString(cursor.getColumnIndex("SERVER_ID")),
                            cursor.getString(cursor.getColumnIndex("LIST_NAME"))
                    );
                    todoLists.add(todoList);
                } else {

                }
            } while (cursor.moveToNext());
        }

        adapter = new TodoListAdapter(this, todoLists);
        todoListsList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        todoListsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                //Initialize the Intent
                Intent todolistIntent = new Intent(getApplicationContext(), ListMenuActivity.class);

                //Get selected Todolist object
                TodoList clickedList = (TodoList) todoListsList.getItemAtPosition(position);
                //and extract its name for the page title, and ID for reference.
                todolistIntent.putExtra("TODO_LIST_ID", clickedList.getID());
                todolistIntent.putExtra("TODO_LIST_NAME", clickedList.getName());

                startActivity(todolistIntent);

            }
        });

        setEmptyTextVisibility(emptyTextView);
    }

    private void saveListToLocalStorage(int listID, String listName, int status, boolean success) {
        Map<String, String> contentValues = new HashMap<>();
        contentValues.put("LIST_ID", String.valueOf(listID));
        contentValues.put("LIST_NAME", listName);
        contentValues.put("STATUS", String.valueOf(status));
        contentValues.put("ACTION", "0");
        if (success) {
            contentValues.put("SERVER_ID", String.valueOf(listID));
        } else {
            contentValues.put("SERVER_ID", "0");
        }
        db.insert("todo_lists", contentValues);
        TodoList todoList = new TodoList(String.valueOf(listID), listName);
        todoLists.add(todoList);
        adapter.notifyDataSetChanged();
    }

    private void editListInLocalStorage(int listID, String listName, boolean success) {
        Map<String, String> updatedValues = new HashMap<>();
        updatedValues.put("LIST_NAME", listName);
        if (!success) {
            updatedValues.put("ACTION", "1");
        } else {
            updatedValues.put("ACTION", "0");
        }
        db.update("todo_lists", updatedValues, "LIST_ID=" + listID);
        adapter.notifyDataSetChanged();
    }

    private void saveListToServer(final String newListName) {
        progressDialog.setMessage("Saving data...");
        showDialog();

        StringRequest addRequest = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("RESPONSE", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status == 0) {
                                String newListID = jsonObject.getString("list_id");
                                String newListName = jsonObject.getString("list_name");
                                adapter.add(new TodoList(newListID, newListName));
                                adapter.notifyDataSetChanged();
                                saveListToLocalStorage(Integer.parseInt(newListID), newListName, SYNCHED, true);
                                setEmptyTextVisibility(emptyTextView);
                                Toast.makeText(getApplicationContext(), newListName + " has been added", Toast.LENGTH_LONG).show();
                            } else if (status == 1)
                                Toast.makeText(getApplicationContext(), "Insert list failed!", Toast.LENGTH_LONG).show();
                            else if (status == 2)
                                Toast.makeText(getApplicationContext(), "Insert access failed!", Toast.LENGTH_LONG).show();
                            else if (status == -1)
                                Toast.makeText(getApplicationContext(), "Unknown attempt!", Toast.LENGTH_LONG).show();
                            else {
                                Log.e("RESPONSE_UNSYNCHED", response);
//                                Random randId = new Random();
                                //saveListToLocalStorage(randId.nextInt(Integer.MAX_VALUE), newListName, UNSYNCHED, false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String msg = e.getMessage();
                            if (msg != null) {
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
                                Log.e("JSONException", msg);
                            }
                        }

                        hideDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if (msg != null)
                            Log.e("ADD_ERROR", error.getMessage());
                        Random randId = new Random();
                        saveListToLocalStorage(randId.nextInt(Integer.MAX_VALUE), newListName, UNSYNCHED, false);
                        hideDialog();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "insert_todo_list");
                params.put("list_name", newListName);
                params.put("user_id", userData.get("user_id"));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("action", "insert_todo_list");
                params.put("list_name", newListName);
                params.put("user_id", userData.get("user_id"));
                return params;
            }
        };

        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(addRequest, "add_list");
    }

    private void showDeleteListDialog(Context context,final TodoList todolist){

        //------------------------------------------------------------------------------------------
        //START AlertDialog Definition
        final AlertDialog.Builder deleteListBuilder = new AlertDialog.Builder(context);

        //Set its title and view
        String dialogMsg = getString(R.string.delete_dialog_start).
                concat(" ").concat(todolist.getName()).
                concat(" ").concat(getString(R.string.delete_dialog_end));

        //Set title and message
        deleteListBuilder.setTitle(R.string.delete_dialog_title).setMessage(dialogMsg);

        //Add the "Positive" (Right button) logic
        deleteListBuilder.setPositiveButton(R.string.dialog_default_positive_labeal, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Call the delete function
                deleteList(todolist);
            }
        });
        //Add the "Negative" (Left button) logic
        deleteListBuilder.setNegativeButton(R.string.dialog_default_negative_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //END AlertDialog Definition
        //------------------------------------------------------------------------------------------

        AlertDialog newList = deleteListBuilder.create();
        newList.show();
    }

    private void deleteList(final TodoList list){
        progressDialog.setMessage("Deleting...");
        showDialog();
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("DELETE_RESPONSE", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");
                    if (status == 0) {
                        String listName = jsonObject.getString("list_name");
                        adapter.remove(list);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MainMenuActivity.this, listName + " has been deleted", Toast.LENGTH_LONG).show();
                        setEmptyTextVisibility(emptyTextView);
                    } else if (status == 1) {
                        Toast.makeText(MainMenuActivity.this, "You don't have access to delete this list!", Toast.LENGTH_LONG).show();
                    } else if (status == 2) {
                        Toast.makeText(MainMenuActivity.this, "Delete failed!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainMenuActivity.this, "Unknown attempt!", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    hideDialog();
                }
            }
        };
        Response.ErrorListener responseErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String msg = error.getMessage();
                if (msg != null)
                    Log.e("DELETE_REQUEST", msg);
                else Log.e("DELETE_REQUEST", "An error occured but the error message is empty. You must chase the bug yourself, good luck!");
                hideDialog();
            }
        };

        StringRequest deleteRequest = new StringRequest(Request.Method.POST, endpoint,
                responseListener, responseErrorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "delete_todo_list");
                params.put("list_id", list.getID());
                params.put("user_id", userData.get("user_id"));
                return params;
            }
        };

        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(deleteRequest, "delete_list");

    }

    private void addNewList(final String newListName) {
        progressDialog.setMessage("Processing...");
        showDialog();
        StringRequest addRequest = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("RESPONSE", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status == 0) {
                                String newListID = jsonObject.getString("list_id");
                                String newListName = jsonObject.getString("list_name");
                                adapter.add(new TodoList(newListID, newListName));
                                adapter.notifyDataSetChanged();
                                setEmptyTextVisibility(emptyTextView);
                                Toast.makeText(getApplicationContext(), newListName + " has been added", Toast.LENGTH_LONG).show();
                            } else if (status == 1)
                                Toast.makeText(getApplicationContext(), "Insert list failed!", Toast.LENGTH_LONG).show();
                            else if (status == 2)
                                Toast.makeText(getApplicationContext(), "Insert access failed!", Toast.LENGTH_LONG).show();
                            else if (status == -1)
                                Toast.makeText(getApplicationContext(), "Unknown attempt!", Toast.LENGTH_LONG).show();
                            else Log.e("RESPONSE", response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String msg = e.getMessage();
                            if (msg != null) {
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
                                Log.e("JSONException", msg);
                            }
                        }

                        hideDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if (msg != null)
                            Log.e("ADD_ERROR", error.getMessage());
                        hideDialog();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "insert_todo_list");
                params.put("list_name", newListName);
                params.put("user_id", userData.get("user_id"));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("action", "insert_todo_list");
                params.put("list_name", newListName);
                params.put("user_id", userData.get("user_id"));
                return params;
            }
        };

        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(addRequest, "add_list");
    }

    private void editList(final String newListName, final TodoList list) {
        progressDialog.setMessage("Editing...");
        showDialog();
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.e("EDIT_RESPONSE", response);
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");
                    if (status == 0) {
                        int listID = jsonObject.getInt("list_id");
                        editListInLocalStorage(listID, newListName, true);
                        String oldListName = list.getName();
                        list.setName(newListName);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MainMenuActivity.this, oldListName + " changed to " + newListName, Toast.LENGTH_LONG).show();
                    } else if (status == 1) {
                        Toast.makeText(MainMenuActivity.this, "You don't have access to edit this list!", Toast.LENGTH_LONG).show();
                    } else if (status == 2) {
                        Toast.makeText(MainMenuActivity.this, "Edit failed!", Toast.LENGTH_LONG).show();
                    } else if (status == -1) {
                        Toast.makeText(MainMenuActivity.this, "Unknown attempt!", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d("EDIT_RESPONSE", response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    hideDialog();
                }
            }
        };
        Response.ErrorListener responseErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String msg = error.getMessage();
                if (msg != null)
                    Log.e("EDIT_REQUEST", msg);
                else Log.e("EDIT_REQUEST", "An error occured but the error message is empty. You must chase the bugs yourself, good luck!");
                editListInLocalStorage(Integer.parseInt(list.getID()), newListName, false);
                list.setName(newListName);
                adapter.notifyDataSetChanged();
                hideDialog();
            }
        };
        StringRequest editRequest = new StringRequest(Request.Method.POST, endpoint,
                responseListener, responseErrorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "update_todo_list");
                params.put("user_id", userData.get("user_id"));
                params.put("list_id", list.getID());
                params.put("new_list_name", newListName);
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(editRequest, "edit_list");
    }

    private void setEmptyTextVisibility(View headerView) {
        //Set the visibility of empty to-do lists notice.
        if (adapter.isEmpty()) {
            //if the adapter is empty, check the number of views in the header.
            //If it's zero, then add the notice. otherwise continue.
            if (todoListsList.getHeaderViewsCount() == 0) {
                todoListsList.addHeaderView(headerView, null, false);
            }
        } else {
            //else if the adapter is NOT empty, check the number of views in the header.
            //If it's more than zero (meaning there's something there, then remove the notice).
            //otherwise continue.
            if (todoListsList.getHeaderViewsCount() > 0) {
                todoListsList.removeHeaderView(headerView);
            }
        }
    }

    private void showAddListDialog(Context context) {

        //Get inflater from current context
        LayoutInflater inflater = LayoutInflater.from(context);
        final View addListDialogView = inflater.inflate(R.layout.main_menu_create_list_dialog, null);

        //------------------------------------------------------------------------------------------
        //START AlertDialog Definition
        final AlertDialog.Builder addListBuilder = new AlertDialog.Builder(context);

        //Set its title and view
        addListBuilder.setTitle(R.string.add_dialog_title);
        addListBuilder.setView(addListDialogView);

        //Add the "Positive" (Right button) logic
        addListBuilder.setPositiveButton(R.string.dialog_default_positive_labeal, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Because the EditText is not from the activity's view,
                //Explicitly call findViewById from addListDialogView to access the EditText from dialog.
                EditText newListText = (EditText) addListDialogView.findViewById(R.id.newListTitleText);
                //Call the activity's addNewList function using user's string.
                //addNewList(newListText.getText().toString());
                saveListToServer(newListText.getText().toString());
            }
        });
        //Add the "Negative" (Left button) logic
        addListBuilder.setNegativeButton(R.string.dialog_default_negative_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //END AlertDialog Definition
        //------------------------------------------------------------------------------------------

        AlertDialog newList = addListBuilder.create();
        newList.show();
    }

    private void showEditListDialog(Context context, final TodoList list) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View editListDialogView = inflater.inflate(R.layout.main_menu_create_list_dialog, null);
        final EditText editText = (EditText) editListDialogView.findViewById(R.id.newListTitleText);
        editText.setText(list.getName());
        AlertDialog.Builder editListDialog = new AlertDialog.Builder(context);
        editListDialog.setTitle("Edit " + list.getName());
        editListDialog.setView(editListDialogView);
        editListDialog.setPositiveButton("EDIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editList(editText.getText().toString(), list);
            }
        });
        editListDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog editDialog = editListDialog.create();
        editDialog.show();
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void setText(final TextView text, final String value, final int unit, final float textSize) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
                text.setTextSize(unit, textSize);
            }
        });
    }
}
