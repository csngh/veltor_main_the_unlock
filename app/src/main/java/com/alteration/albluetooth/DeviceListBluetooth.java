/*
 *
 *  Class to handle bluetooth connection
 *  Operations
 *
 */

package com.alteration.albluetooth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.alteration.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DeviceListBluetooth extends AppCompatActivity {

    private ArrayList<String> devNamesList = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private ListView listView;
    private Bundle extras;
    private Set<String> savedNames = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_list_bluetooth);
        setTitle("Bluetooth Access List");

        extras = getIntent().getExtras();
        listView = findViewById(R.id.deviceListBL);

        if (extras != null) {
            devNamesList = extras.getStringArrayList("ble");
            Log.d("BLEGOT", "From Passing");
        } else {
            SharedPreferences preferences = getSharedPreferences("devNames", Context.MODE_PRIVATE);
            savedNames = preferences.getStringSet("names", null);
            if (savedNames != null) {
                devNamesList.addAll(savedNames);
            } else {
                Toast.makeText(getApplicationContext(), "No device found!", Toast.LENGTH_SHORT).show();
            }
            Log.d("BLEGOT", "From Saved");
        }

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devNamesList);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent ble = new Intent(DeviceListBluetooth.this, BluetoothActivity.class);
                ble.putExtra("devName", adapterView.getItemAtPosition(i).toString());
                startActivity(ble);
                endActivity();
            }
        });
    }

    private void endActivity() {
        this.finish();
    }
}
