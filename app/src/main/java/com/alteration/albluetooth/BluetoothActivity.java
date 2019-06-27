/*
 *
 *  Activity to handle bluetooth
 *  Operation
 *
 */

package com.alteration.albluetooth;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.preferences.Globals;

public class BluetoothActivity extends AppCompatActivity {

    //Widgets
    //
    //Data Types
    private String devName;
    private ConnectDevice connectDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_bluetooth);
        setTitle(R.string.bluetooth);

        //Definations
        connectDevice = new ConnectDevice(this);

        //Getting data from previous intents
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //devName = extras.getString("devName");
            setTitle(extras.getString("devName"));
            devName = "AIPL";
        } else {
            devName = "AIPL";
        }

        findViewById(R.id.bConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Please wait...", Toast.LENGTH_SHORT).show();
                createConnection();
            }
        });

        findViewById(R.id.bDisconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeConnection();
            }
        });

        //Locking
        findViewById(R.id.b_bLock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDevice.sendData(Globals.LOCK_ID);
            }
        });

        //Unlocking
        findViewById(R.id.b_bUnlock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDevice.sendData(Globals.UNLOCK_ID);
            }
        });
    }

    private void closeConnection() {
        connectDevice.closeConnection();
        findViewById(R.id.bDisconnect).setEnabled(false);
        findViewById(R.id.bDisconnect).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray));
        findViewById(R.id.bConnect).setEnabled(true);
        findViewById(R.id.bConnect).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_green_light));
    }

    private void createConnection() {
        boolean st = connectDevice.initiateBluetoothConnection(devName);
        if (!st) {
            Toast.makeText(getApplicationContext(), "Please try again", Toast.LENGTH_SHORT).show();
        } else {
            findViewById(R.id.bConnect).setEnabled(false);
            findViewById(R.id.bConnect).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray));
            findViewById(R.id.bDisconnect).setEnabled(true);
            findViewById(R.id.bDisconnect).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_light));
        }
    }
}
