/*
 *
 *  Class to handle bluetooth connection
 *  Operations
 *
 */

package com.alteration.albluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class ConnectDevice {

    //Data Types
    private int state;
    //Functions
    private Context context;
    private BluetoothAdapter mba;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private OutputStream outs;
    private InputStream inps;

    public ConnectDevice(Context context) {
        this.context = context;
    }

    public boolean initiateBluetoothConnection(String blDeviceName) {
        boolean check = checkSupportBluetooth();
        if (check) {
            Set<BluetoothDevice> pairedDevices = mba.getBondedDevices();
            try {
                if (pairedDevices.size() > 0) {
                    Toast.makeText(context, "Please Wait for a Moment !!", Toast.LENGTH_SHORT).show();
                    for (BluetoothDevice device : pairedDevices) {
                        if (device.getName().contains(blDeviceName)) {
                            bluetoothDevice = device;
                            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                            bluetoothSocket.connect();
                            outs = bluetoothSocket.getOutputStream();
                            inps = bluetoothSocket.getInputStream();
                            Toast.makeText(context, "Connected", Toast.LENGTH_LONG).show();
                            Log.i("MYTAGS", "Connected");
                            state = 1;
                            break;
                        } else {
                            //Toast.makeText(context, "Error, No Device", Toast.LENGTH_SHORT).show();
                            state = 0;
                        }
                    }
                    if (state == 0) {
                        Toast.makeText(context, "Error, No device found", Toast.LENGTH_SHORT).show();
                        Log.i("MYTAGS", "No device found");
                    }
                } else if (pairedDevices.isEmpty()) {
                    Toast.makeText(context, "Paired devices empty", Toast.LENGTH_SHORT).show();
                    Log.i("MYTAGS", "Paired devices empty");
                    state = 0;
                }
            } catch (IOException e) {
                state = 0;
                e.printStackTrace();
                Toast.makeText(context, "Error in Establishing Connection", Toast.LENGTH_SHORT).show();
                Log.i("MYTAGS", "Error in Establishing Connection");
            }
        }
        return state == 1;
    }

    public void sendData(int data) {
        if (this.state == 1) {
            try {
                outs.write(data + 48);
                Log.i("MYTAGS", "Sent " + String.valueOf(data));
                //closeConnection();
            } catch (IOException e) {
                Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
                Log.i("MYTAGS", "Connection failed");
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "No connection", Toast.LENGTH_SHORT).show();
            Log.i("MYTAGS", "No connection");
        }
    }

    private boolean checkSupportBluetooth() {
        mba = BluetoothAdapter.getDefaultAdapter();
        if (mba == null) {
            Toast.makeText(context, "Bluetooth not supported in this device", Toast.LENGTH_LONG).show();
            return false;
        } else if (!mba.isEnabled()) {
            mba.enable();
            Toast.makeText(context, "Bluetooth Enabled Automatically", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(context, "Bluetooth On", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    public void closeConnection() {
        try {
            this.bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

