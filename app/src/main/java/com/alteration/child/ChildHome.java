package com.alteration.child;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.alteration.R;
import com.alteration.parent.PCSelectActivity;
import com.alteration.preferences.MySingleton;
import com.alteration.preferences.UserPreferences;

import java.util.HashMap;
import java.util.Map;

public class ChildHome extends AppCompatActivity {

    //Widgets
    private ImageView imgLock;
    private ProgressDialog dialog;
    //Functions
    private UserPreferences pref;
    private ChildDatabase childDatabase;
    //Data types
    private int resultCode, isLock;
    private String toggle = "LOCK", devID, token, nickName, WEB_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_child_home);
        setTitle(R.string.child_home);

        imgLock = findViewById(R.id.imgChildLock);
        pref = new UserPreferences(this);
        childDatabase = new ChildDatabase(this);

        devID = pref.getemailID();
        token = pref.getPassword();
        nickName = pref.getNickName();

        imgLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = ProgressDialog.show(ChildHome.this, "", "Connecting", true);
                dialog.show();
                if (toggle.equalsIgnoreCase(getResources().getString(R.string.call_lock))) {
                    toggle = getResources().getString(R.string.call_unlock);
                    imgLock.setImageResource(R.drawable.ic_unlock);
                    performAction(toggle);
                    //setNameForLogonDevice(toggle);
                } else if (toggle.equalsIgnoreCase(getResources().getString(R.string.call_unlock))) {
                    toggle = getResources().getString(R.string.call_lock);
                    imgLock.setImageResource(R.drawable.ic_lock);
                    performAction(toggle);
                    //setNameForLogonDevice(toggle);
                }
            }
        });
    }

    private void checkStatus() {
        WEB_URL = "https://api.particle.io/v1/devices/" + devID + "/" + getResources().getString(R.string.isLocked) + "?access_token=" + token;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WEB_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error, Check Status Failed!", Toast.LENGTH_SHORT).show();
            }
        });
        MySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void performAction(final String param) {

        WEB_URL = "https://api.particle.io/v1/devices/" + devID + "/" + getResources().getString(R.string.lockDoor) + "?access_token=" + token;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WEB_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                resultCode = 1;
                Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error, Action Failed!", Toast.LENGTH_SHORT).show();
                resultCode = 0;
                dialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("args", param + nickName + "$" + param + "ED");
                return params;
            }
        };
        MySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    /*private void setNameForLogonDevice(final String param) {
        WEB_URL = "https://api.particle.io/v1/devices/" + devID + "/" + getResources().getString(R.string.setName) + "?access_token=" + token;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WEB_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("args", nickName + "$" + param + "ED");
                return params;
            }
        };
        MySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_child, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_child_logout:
                AlertDialog.Builder alertDialogBuilderex = new AlertDialog.Builder(this);
                alertDialogBuilderex.setTitle("Sure to logout?");
                alertDialogBuilderex
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int arg1) {
                                        logoutEverything();
                                    }
                                })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialogex = alertDialogBuilderex.create();
                alertDialogex.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutEverything() {
        Intent login = new Intent(ChildHome.this, PCSelectActivity.class);
        Toast.makeText(getApplicationContext(), "You have been logged out !!", Toast.LENGTH_SHORT).show();
        pref.removeAllWithLogout();
        childDatabase.removeAllWithLogout();
        pref.setLoggedin(false);
        pref.setIsCloudLogged(false);
        startActivity(login);
        endActivity();
    }

    private void endActivity() {
        this.finish();
    }
}
