package com.alteration.parent;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.albluetooth.BluetoothActivity;
import com.alteration.logs.LogListView;
import com.alteration.preferences.UserPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ApiFactory;
import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.devicesetup.ParticleDeviceSetupLibrary;
import io.particle.android.sdk.utils.Async;

public class ShareDeviceActivity extends AppCompatActivity {

    //Functions
    private ShareListAdapter listAdapter;
    private ArrayAdapter<String> listItemAdapter;
    private ArrayList<ShareListHandler> arrayList = new ArrayList<>();
    private ProgressDialog dialog;
    private ListView listView;
    private Button shareAll, shareSelected;
    private Spinner sDay, sHour, sMin;
    //Data types
    private String id, devName, callType;
    private int day, hour, min;
    private ArrayList<String> listItemShare = new ArrayList<>();
    private UserPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_share_device);
        setTitle(R.string.share_device);

        shareAll = findViewById(R.id.bShareAll);
        shareSelected = findViewById(R.id.bShareSelected);
        listView = findViewById(R.id.listViewDevicesShare);
        listAdapter = new ShareListAdapter(getApplicationContext(), R.layout.al_device_list_share_items);
        Bundle extras = getIntent().getExtras();
        pref = new UserPreferences(this);

        //Checking Call Type
        if (extras != null) {
            callType = extras.getString("share");
            if (callType != null && !callType.equalsIgnoreCase("listShare")) {
                shareAll.setVisibility(View.GONE);
                shareSelected.setVisibility(View.GONE);
                setTitle(R.string.log);
            }
        } else {
            callType = "share";
            setTitle(R.string.share_device);
        }

        try {
            ParticleDeviceSetupLibrary.init(this.getApplicationContext());
            ParticleCloudSDK.initWithOauthCredentialsProvider(ShareDeviceActivity.this,
                    new ApiFactory.OauthBasicAuthCredentialsProvider() {

                        public String getClientId() {
                            return pref.getOauth_id();
                        }

                        public String getClientSecret() {
                            return pref.getOauth_secret();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog = ProgressDialog.show(this, "", "Fetching devices..", true);
        dialog.show();

        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                List<ParticleDevice> devices = ParticleCloudSDK.getCloud().getDevices();

                for (ParticleDevice device : devices) {
                    ShareListHandler handler = new ShareListHandler(device.getName());
                    device.getID();
                    arrayList.add(handler);
                    listItemShare.add(device.getName());
                }
                return null;
            }

            @Override
            public void onSuccess(@NonNull Object o) {

                //Assuring Calltype
                if (callType != null && callType.equalsIgnoreCase("listShare")) {
                    setupSelectionAdapter();
                } else {
                    if (arrayList.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Sorry no device found !!", Toast.LENGTH_SHORT).show();
                    } else {
                        for (int i = 0; i < arrayList.size(); i++) {
                            listAdapter.add(arrayList.get(i));
                        }
                        Toast.makeText(getApplicationContext(), "Click on device name to access", Toast.LENGTH_SHORT).show();
                    }
                    listView.setAdapter(listAdapter);
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull ParticleCloudException exception) {
                Toast.makeText(getApplicationContext(), "Error Fetching", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        shareSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilderex = new AlertDialog.Builder(ShareDeviceActivity.this);
                alertDialogBuilderex.setTitle("Select Sharing Method!");
                alertDialogBuilderex
                        .setCancelable(false)
                        .setPositiveButton("Permanent",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int arg1) {
                                        initiateSharingWithTimer(0);
                                    }
                                })
                        .setNegativeButton("With Timer", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                getTimerFromDialog(1);
                            }
                        });
                AlertDialog alertDialogex = alertDialogBuilderex.create();
                alertDialogex.show();
            }
        });

        shareAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilderex = new AlertDialog.Builder(ShareDeviceActivity.this);
                alertDialogBuilderex.setTitle("Select Sharing Method!");
                alertDialogBuilderex
                        .setCancelable(false)
                        .setPositiveButton("Permanent",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int arg1) {
                                        Intent generateQR = new Intent(ShareDeviceActivity.this, GenerateQR.class);
                                        startActivity(generateQR);
                                        endActivity();
                                    }
                                })
                        .setNegativeButton("With Timer", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                getTimerFromDialog(0);
                            }
                        });
                AlertDialog alertDialogex = alertDialogBuilderex.create();
                alertDialogex.show();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                if (!callType.equalsIgnoreCase("listShare")) {
                    Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

                        @Override
                        public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                            List<ParticleDevice> devices = ParticleCloudSDK.getCloud().getDevices();

                            for (ParticleDevice device : devices) {
                                if (device.getName().equalsIgnoreCase(arrayList.get(i).getDevName())) {
                                    Log.d("MYTAGS", "Reached " + arrayList.get(i).getDevName() + ", " + device.getName());
                                    id = device.getID();
                                    devName = device.getName();
                                }
                            }
                            return null;
                        }

                        @Override
                        public void onSuccess(@NonNull Object o) {
                            if (callType.equalsIgnoreCase("bluetooth")) {
                                Intent qr = new Intent(ShareDeviceActivity.this, BluetoothActivity.class);
                                qr.putExtra("devName", devName);
                                startActivity(qr);
                                endActivity();
                            } else if (callType.equalsIgnoreCase("logs")) {
                                Intent logs = new Intent(ShareDeviceActivity.this, LogListView.class);
                                logs.putExtra("devID", id);
                                logs.putExtra("devName", devName);
                                startActivity(logs);
                                endActivity();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull ParticleCloudException exception) {
                            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void getTimerFromDialog(int flag) {
        day = hour = min = 1;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.al_custom_timershare_activity, null);
        dialogBuilder.setView(dialogView);

        //Declare
        sDay = dialogView.findViewById(R.id.spDays);
        sHour = dialogView.findViewById(R.id.spHours);
        sMin = dialogView.findViewById(R.id.spMins);

        ArrayList<Integer> aDays = new ArrayList<>();
        ArrayList<Integer> aHour = new ArrayList<>();
        ArrayList<Integer> aMins = new ArrayList<>();

        for (int i = 0; i <= 30; i++) {
            aDays.add(i);
        }

        for (int i = 0; i <= 23; i++) {
            aHour.add(i);
        }

        for (int i = 10; i <= 59; i++) {
            aMins.add(i);
        }

        ArrayAdapter<Integer> arrayDay = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aDays);
        ArrayAdapter<Integer> arrayHour = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aHour);
        ArrayAdapter<Integer> arrayMin = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aMins);
        arrayDay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayHour.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayMin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sDay.setAdapter(arrayDay);
        sHour.setAdapter(arrayHour);
        sMin.setAdapter(arrayMin);

        sDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                day = (int) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                hour = (int) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sMin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                min = (int) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Dialog Starts
        dialogBuilder.setTitle("Enter Share Access Timer");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Yes
                if (flag == 1) {
                    initiateSharingWithTimer(1);
                }else if(flag == 0){
                    Intent generateQR = new Intent(ShareDeviceActivity.this, GenerateQR.class);
                    generateQR.putExtra("day", day);
                    generateQR.putExtra("hour", hour);
                    generateQR.putExtra("min", min);
                    startActivity(generateQR);
                    endActivity();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //No
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void initiateSharingWithTimer(int flag) {
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        if (checked.size() <= 0) {
            Toast.makeText(getApplicationContext(), "Please select a device !", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> selectedItems = new ArrayList<>();
        for (int i = 0; i < checked.size(); i++) {
            int position = checked.keyAt(i);
            if (checked.valueAt(i))
                selectedItems.add(listItemAdapter.getItem(position));
        }

        String[] outputStrArr = new String[selectedItems.size()];
        for (int i = 0; i < selectedItems.size(); i++) {
            outputStrArr[i] = selectedItems.get(i);
        }
        Intent generateQR = new Intent(ShareDeviceActivity.this, GenerateQR.class);

        Bundle b = new Bundle();
        b.putStringArray("selectedItems", outputStrArr);

        generateQR.putExtras(b);
        if (flag == 1) {
            generateQR.putExtra("day", day);
            generateQR.putExtra("hour", hour);
            generateQR.putExtra("min", min);
        }
        startActivity(generateQR);
        endActivity();
    }

    private void setupSelectionAdapter() {
        listItemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, listItemShare);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(listItemAdapter);
    }

    private void endActivity() {
        this.finish();
    }
}
