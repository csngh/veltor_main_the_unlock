package com.alteration.child;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.albluetooth.DeviceListBluetooth;
import com.alteration.parent.PCSelectActivity;
import com.alteration.preferences.Globals;
import com.alteration.preferences.UserPreferences;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ChildDeviceListActivity extends AppCompatActivity {

    private ListView listView;
    private ChildDeviceListAdapter listAdapter;
    private ArrayList<ChildDeviceListHandler> arrayList = new ArrayList<>();
    private ArrayList<String> devNameArrayList = new ArrayList<>();
    private ProgressDialog dialog;
    private ChildDatabase childDatabase;
    private UserPreferences pref;
    private int sessionMins;
    private Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_child_device_list);

        childDatabase = new ChildDatabase(this);
        pref = new UserPreferences(this);
        extras = getIntent().getExtras();

        listView = findViewById(R.id.listViewDevicesChild);
        listAdapter = new ChildDeviceListAdapter(getApplicationContext(), R.layout.al_device_list_home_items);
        listView.setAdapter(listAdapter);

        dialog = ProgressDialog.show(ChildDeviceListActivity.this, "", "Please wait...", true);
        dialog.show();

        if (extras.get("sessionMins") != null) {
            final int[] count = {0};
            sessionMins = (int) extras .get("sessionMins");
            final Timer T = new Timer();
            T.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        count[0]++;
                        if (count[0] >= (sessionMins * 60)) {
                            logoutEverything();
                            Toast.makeText(getApplicationContext(), "Your session is expired!", Toast.LENGTH_LONG).show();
                            endActivity();
                            T.cancel();
                        }
                    });
                }
            }, 1000, 1000);
            Log.i(Globals.LOGTAG, String.valueOf(sessionMins));
        }else {
            Toast.makeText(getApplicationContext(), "Error in initialization, please rescan QRCode!", Toast.LENGTH_SHORT).show();
            childDatabase.removeAllWithLogout();
            Intent fallback = new Intent(ChildDeviceListActivity.this, PCSelectActivity.class);
            startActivity(fallback);
            endActivity();
        }

        for (int i = 0; i < childDatabase.getDataCount(); i++) {
            ChildDeviceListHandler childDeviceListHandler =
                    new ChildDeviceListHandler(childDatabase.getDevName(i), childDatabase.getDevID(i));
            arrayList.add(childDeviceListHandler);
            devNameArrayList.add(arrayList.get(i).getDevName());
            listAdapter.add(childDeviceListHandler);
        }

        dialog.dismiss();
    }

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
                                (dialog, arg1) -> logoutEverything())
                        .setNegativeButton("No", (dialog, arg1) -> dialog.cancel());
                AlertDialog alertDialogex = alertDialogBuilderex.create();
                alertDialogex.show();
                break;
            case R.id.action_bluetooth:
                Intent bluetooth = new Intent(ChildDeviceListActivity.this, DeviceListBluetooth.class);
                bluetooth.putStringArrayListExtra("ble", devNameArrayList);
                startActivity(bluetooth);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilderex = new AlertDialog.Builder(this);
        alertDialogBuilderex.setTitle("Sure to exit?");
        alertDialogBuilderex
                .setCancelable(false)
                .setPositiveButton("Yes",
                        (dialog, arg1) -> endActivity())
                .setNegativeButton("No", (dialog, arg1) -> dialog.cancel());
        AlertDialog alertDialogex = alertDialogBuilderex.create();
        alertDialogex.show();
    }

    private void logoutEverything() {
        Intent login = new Intent(ChildDeviceListActivity.this, PCSelectActivity.class);
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
