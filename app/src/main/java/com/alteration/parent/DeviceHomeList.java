package com.alteration.parent;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.SOSActivity;
import com.alteration.albluetooth.DeviceListBluetooth;
import com.alteration.preferences.UserPreferences;
import com.alteration.settings.ChangePin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.particle.android.sdk.cloud.ApiFactory;
import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.devicesetup.ParticleDeviceSetupLibrary;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class DeviceHomeList extends AppCompatActivity {

    //Widgets
    private TextView tvNoD;
    private ImageView imgNoD;
    private SwipeRefreshLayout swipeRefreshLayout;
    //Functions
    private ListView listView;
    private HomeListAdapter listAdapter;
    private ArrayList<HomeListHandler> arrayList = new ArrayList<>();
    private ArrayList<String> arrayListDevNames = new ArrayList<>();

    private Set<String> savedDevNames = new HashSet<>();

    private ProgressDialog dialog;
    private UserPreferences pref;
    //Data types

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_device_list_home);
        setTitle(R.string.dev_list);

        tvNoD = findViewById(R.id.noDeviceText);
        imgNoD = findViewById(R.id.noDeviceImg);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        listAdapter = new HomeListAdapter(getApplicationContext(), R.layout.al_device_list_home_items);
        listView = findViewById(R.id.listViewDevices);
        pref = new UserPreferences(this);

        try {
            ParticleDeviceSetupLibrary.init(this.getApplicationContext());
//            ParticleCloudSDK.init(DeviceHomeList.this);
            ParticleCloudSDK.initWithOauthCredentialsProvider(DeviceHomeList.this,
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

        listView.setAdapter(listAdapter);
        getDeviceStatus();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DeviceHomeList.this.recreate();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        //checkCalendarSetAlarm();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_device:
                invokeDeviceSetup();
                break;
            case R.id.action_logout:
                AlertDialog.Builder alertDialogBuilderex = new AlertDialog.Builder(this);
                alertDialogBuilderex.setTitle("Sure to logout?");
                alertDialogBuilderex
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int arg1) {
                                        cloudLogout();
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
            case R.id.action_share:
                Intent share = new Intent(DeviceHomeList.this, ShareDeviceActivity.class);
                share.putExtra("share", "listShare");
                startActivity(share);
                break;
            case R.id.action_usage_logs:
                Intent logs = new Intent(DeviceHomeList.this, ShareDeviceActivity.class);
                logs.putExtra("share", "logs");
                startActivity(logs);
                break;
//            case R.id.action_unclaim:
//                Toast.makeText(getApplicationContext(), "Feature Yet to Come", Toast.LENGTH_SHORT).show();
//                break;
            case R.id.action_camera:
                startNewActivity(getApplicationContext(), "com.diycamlite");
                break;
            case R.id.action_bluetooth:
                Intent bluetooth = new Intent(DeviceHomeList.this, DeviceListBluetooth.class);
                if (!arrayListDevNames.isEmpty()) {
                    bluetooth.putStringArrayListExtra("ble", arrayListDevNames);
                }
                startActivity(bluetooth);
                break;
            case R.id.action_change_pin:
                Intent changePin = new Intent(DeviceHomeList.this, ChangePin.class);
                startActivity(changePin);
                break;
            case R.id.action_sos:
                Intent sos = new Intent(DeviceHomeList.this, SOSActivity.class);
                startActivity(sos);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            try {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + packageName));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Market not supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void getDeviceStatus() {

        dialog = ProgressDialog.show(this, "", "Connecting...", true);
        dialog.show();

        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {

                List<ParticleDevice> devices = ParticleCloudSDK.getCloud().getDevices();

                for (ParticleDevice device : devices) {
                    HomeListHandler handler = new HomeListHandler(device.getID(), device.getName(), 0);
                    arrayList.add(handler);
                }

                return null;
            }

            @Override
            public void onSuccess(@NonNull Object o) {
                if (arrayList.isEmpty()) {
                    listView.setVisibility(View.GONE);
                    tvNoD.setVisibility(View.VISIBLE);
                    imgNoD.setVisibility(View.VISIBLE);
                } else {
                    tvNoD.setVisibility(View.GONE);
                    imgNoD.setVisibility(View.GONE);
                    for (int i = 0; i < arrayList.size(); i++) {
                        listAdapter.add(arrayList.get(i));
                        arrayListDevNames.add(arrayList.get(i).getDevName());
                    }
                    savedDevNames.addAll(arrayListDevNames);
                    saveAllNames();
                }
                dialog.dismiss();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull ParticleCloudException exception) {
                Toast.makeText(getApplicationContext(), "Error Fetching, try loggin in again", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilderex = new AlertDialog.Builder(this);
        alertDialogBuilderex.setTitle("Sure to exit?");
        alertDialogBuilderex
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                endActivity();
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
    }

    private void invokeDeviceSetup() {
        ParticleDeviceSetupLibrary.DeviceSetupCompleteReceiver receiver = new ParticleDeviceSetupLibrary.DeviceSetupCompleteReceiver() {
            @Override
            public void onSetupSuccess(@NonNull String configuredDeviceId) {
                Toast.makeText(getApplicationContext(), "Add device successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSetupFailure() {
                Toast.makeText(getApplicationContext(), "Add device failed", Toast.LENGTH_SHORT).show();
            }
        };
        receiver.register(this);
        ParticleDeviceSetupLibrary.startDeviceSetup(this, DeviceHomeList.class);
    }

    private void cloudLogout() {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                particleCloud.logOut();
                return null;
            }

            @Override
            public void onSuccess(@NonNull Object o) {
                Intent login = new Intent(DeviceHomeList.this, PCSelectActivity.class);
                Toast.makeText(getApplicationContext(), "You have been logged out !!", Toast.LENGTH_SHORT).show();
                pref.removeAllWithLogout();
                pref.setLoggedin(false);
                pref.setIsCloudLogged(false);
                startActivity(login);
                endActivity();
            }

            @Override
            public void onFailure(@NonNull ParticleCloudException exception) {
                Toaster.s(DeviceHomeList.this, "Error logging out !!");
            }
        });
    }

    private void saveAllNames() {
        SharedPreferences preferences = getSharedPreferences("devNames", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("names", savedDevNames);
        editor.apply();

        for (String s : savedDevNames) {
            Log.d("MYNAMESSAVED", s);
        }
    }

//    private void checkCalendarSetAlarm() {
//
//        int hour = 22;
//        int min = 0;
//
//        SharedPreferences preferences = getSharedPreferences("alarmStat", Context.MODE_PRIVATE);
//
//        if (preferences.getBoolean("alarm", false)) {
//            Log.i("ALARM STATUS", "Already Set");
//        } else {
//            Log.i("ALARM STATUS", "No alarm, setting now");
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putBoolean("alarm", true);
//            editor.apply();
//
//            final Calendar calendar = Calendar.getInstance();
//
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                calendar.set(calendar.get(Calendar.YEAR),
//                        calendar.get(Calendar.MONTH),
//                        calendar.get(Calendar.DAY_OF_MONTH),
//                        hour,
//                        min,
//                        0
//                );
//            } else {
//                calendar.set(calendar.get(Calendar.YEAR),
//                        calendar.get(Calendar.MONTH),
//                        calendar.get(Calendar.DAY_OF_MONTH),
//                        hour,
//                        min,
//                        0
//                );
//            }
//            setAlarm(calendar.getTimeInMillis());
//        }
//    }
//
//    private void setAlarm(long time) {
//        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        Intent i = new Intent(DeviceHomeList.this, NotificationReciever.class);
//        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
//        if (am != null) {
//            //am.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, pi);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                am.set(AlarmManager.RTC, time, pi);
//            }
//        }
//    }

    private void endActivity() {
        this.finish();
    }
}
