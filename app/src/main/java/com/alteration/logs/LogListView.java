package com.alteration.logs;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.alteration.R;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;

import static io.particle.android.sdk.utils.Py.list;

public class LogListView extends AppCompatActivity {

    //Functions
    private LogListAdapter listAdapter;
    private ProgressDialog dialog;
    private LogsPreferences logsPreferences;
    //Data types
    private String logData[] = new String[100];
    private String devID, devName;
    private int totalIndexCountOnPhoton, totalIndexCountOnDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_log_list_view);

        ListView listView = findViewById(R.id.listViewLogs);
        listAdapter = new LogListAdapter(getApplicationContext(), R.layout.al_log_list_view_items);
        Bundle extras = getIntent().getExtras();
        logsPreferences = new LogsPreferences(this);

        if (extras != null) {
            devID = extras.getString("devID");
            devName = extras.getString("devName");
            Log.d("MYTAGS", devID + ", " + devName);
        }
        setTitle(devName + " Logs");

        listView.setAdapter(listAdapter);
        dialog = ProgressDialog.show(this, "", "Fetching Logs..", true);
        dialog.show();

        totalIndexCountOnDatabase = logsPreferences.getLogCount();
        Log.i("KEYU: ", "Database: " + String.valueOf(totalIndexCountOnDatabase));
        getTotalCount();

    }

    private void getTotalCount() {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                ParticleDevice myDevice = particleCloud.getDevice(devID);
                try {
                    totalIndexCountOnPhoton = myDevice.getIntVariable(getResources().getString(R.string.indexArr));
                } catch (ParticleDevice.VariableDoesNotExistException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onSuccess(@NonNull Object o) {
                Log.i("KEYU: ", "Photon: " + String.valueOf(totalIndexCountOnPhoton));
                getLogData();
            }

            @Override
            public void onFailure(@NonNull ParticleCloudException exception) {
                Log.i("KEYU: ", "Failed: Reaching Device");
                displayLogs();
                dialog.dismiss();
            }
        });
    }

    private void getLogData() {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                ParticleDevice myDevice = particleCloud.getDevice(devID);
                try {
                    for (int i = 0; i < totalIndexCountOnPhoton; i++) {
                        myDevice.callFunction(getResources().getString(R.string.getName), list(String.valueOf(i)));
                        logData[i] = myDevice.getStringVariable(getResources().getString(R.string.tLog));
                    }
                } catch (ParticleDevice.FunctionDoesNotExistException | ParticleDevice.VariableDoesNotExistException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onSuccess(@NonNull Object o) {
                /*if (totalIndexCountOnDatabase > 0) {
                    //If logs are same - Half parse
                    if (logsPreferences.getNickName(0).equalsIgnoreCase(logData[0])) {
                        if ((totalIndexCountOnPhoton - totalIndexCountOnDatabase) != 0) {
                            for (int j = totalIndexCountOnDatabase; j < totalIndexCountOnPhoton; j++) {
                                setLogsIntoDB(logData[j], j);
                            }
                        }
                    }//If logs are different - Full Parse
                    else {
                        //Full parsing
                        Log.i("KEYU: ", "Found Different DataLogs");
                        for (int j = 0; j < totalIndexCountOnPhoton; j++) {
                            //checkIfExistInDatabase(logData[j]);
                        }
                    }
                } else {
                    for (int j = 0; j < totalIndexCountOnPhoton; j++) {
                        setLogsIntoDB(logData[j], j);
                    }
                }
                displayLogs();

                dialog.dismiss();*/
                if (totalIndexCountOnPhoton != 0) {
                    for (int i = 0; i < totalIndexCountOnPhoton; i++) {
                        int dbCount = logsPreferences.getLogCount();
                        setLogsIntoDB(logData[i], dbCount);
                    }
                    displayLogs();
                    deleteAllLogsFromDevice();
                } else {
                    displayLogs();
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull ParticleCloudException exception) {
                Log.i("KEYU: ", "Failed: Gettings Logs");
                displayLogs();
                dialog.dismiss();
            }
        });
    }

    private void setLogsIntoDB(String dataPassed, int count) {
        logsPreferences.setNickName(dataPassed, count);
        logsPreferences.setLogCount(count + 1);
    }

    private void deleteAllLogsFromDevice() {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                ParticleDevice myDevice = particleCloud.getDevice(devID);
                try {
                    myDevice.callFunction("deleteLogs", list(""));
                } catch (ParticleDevice.FunctionDoesNotExistException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onSuccess(@NonNull Object o) {

            }

            @Override
            public void onFailure(@NonNull ParticleCloudException exception) {

            }
        });
    }

    private void displayLogs() {
        for (int index = 0; index < logsPreferences.getLogCount(); index++) {
            LogsHandler handler = new LogsHandler(logsPreferences.getNickName(index));
            Log.i("KEYU: ", logsPreferences.getNickName(index));
            listAdapter.add(handler);
        }
        deleteAllLogsFromDevice();
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}
