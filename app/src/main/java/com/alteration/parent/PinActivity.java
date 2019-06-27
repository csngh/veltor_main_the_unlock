package com.alteration.parent;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.child.ChildDatabase;
import com.alteration.child.ChildDeviceListActivity;
import com.alteration.preferences.Globals;
import com.alteration.preferences.UserPreferences;
import com.chaos.view.PinView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;

public class PinActivity extends AppCompatActivity {

    private PinView etPinLogin;
    private PinView etPinVerify;
    private Button bDigLogin;
    private TextView txtSetup;
    private ImageView imageExpired;
    //Functions
    private UserPreferences pref;
    private ChildDatabase childDatabase;
    //Data types
    private String strPin;
    private String strPinV;
    private int wrongPinCount;
    private int sessionMins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_enter_pin);

        imageExpired = findViewById(R.id.imageExpired);
        etPinLogin = findViewById(R.id.editPin);
        etPinVerify = findViewById(R.id.editPin2);
        bDigLogin = findViewById(R.id.bConfPin);
        txtSetup = findViewById(R.id.txtSetup);
        pref = new UserPreferences(this);
        childDatabase = new ChildDatabase(this);
        imageExpired.setVisibility(GONE);

        if (pref.getUserPin().equalsIgnoreCase("")) {
            txtSetup.setText(R.string.set_pin);
            setTitle(R.string.pin_setup);
        } else {
            txtSetup.setVisibility(GONE);
            etPinVerify.setVisibility(GONE);
            setTitle("Welcome, " + pref.getNickName());
        }

        checkForLockOut();

        if (pref.getLoginType().equalsIgnoreCase("parent") && pref.getUserPin().equalsIgnoreCase("")) {
            mailTo();
        }

        if (!checkInternetConnection() && pref.getLoginType().equalsIgnoreCase("child")) {
            Toast.makeText(getApplicationContext(), "Sorry! you're not connected to internet.", Toast.LENGTH_SHORT).show();
            bDigLogin.setVisibility(GONE);
        } else if (checkInternetConnection() && pref.getLoginType().equalsIgnoreCase("child")) {
                checkForSession();
        }

        bDigLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                strPin = etPinLogin.getText().toString();

                if (pref.getUserPin().equalsIgnoreCase("")) {
                    strPinV = etPinVerify.getText().toString();
                } else {
                    strPinV = pref.getUserPin();
                }

                Log.d("CHETANDET", "PINENTERED: " + strPin + ", SAVED: " + pref.getUserPin());

                if (strPin.equalsIgnoreCase("")) {
                    etPinLogin.setError("Cannot be empty!");
                } else if (strPinV.equalsIgnoreCase("")) {
                    etPinVerify.setError("Cannot be empty!");
                } else if (strPin.length() != Globals.PIN_LENGHT) {
                    etPinLogin.setError("Invalid!");
                } else if (strPinV.length() != Globals.PIN_LENGHT) {
                    etPinVerify.setError("Invalid!");
                } else if (!strPin.equalsIgnoreCase(strPinV)) {
                    if (strPinV.equalsIgnoreCase(pref.getUserPin())) {
                        ++wrongPinCount;
                        etPinLogin.setError("Wrong PIN: " + wrongPinCount);
                        etPinLogin.setText("");
                        setWrongPinCount(wrongPinCount);
                        if (wrongPinCount > 4) {
                            checkForLockOut();
                        }
                        Log.d("WRONGCOUNT", String.valueOf(wrongPinCount));
                    } else {
                        etPinVerify.setError("Pins do not match!");
                        etPinLogin.setText("");
                        etPinVerify.setText("");
                    }
                } else {
                    if (pref.getUserPin().equalsIgnoreCase("")) {
                        pref.setUserPin(strPin);
                        Toast.makeText(getApplicationContext(), "PIN Saved, Welcome!", Toast.LENGTH_SHORT).show();
                        if (pref.getLoginType().equalsIgnoreCase("parent")) {
                            Intent home = new Intent(PinActivity.this, DeviceHomeList.class);
                            startActivity(home);
                            pref.setLoggedin(true);
                            endActivity();
                        } else if (pref.getLoginType().equalsIgnoreCase("child")) {
                            Intent home = new Intent(PinActivity.this, ChildDeviceListActivity.class);
                            home.putExtra("sessionMins", sessionMins);
                            startActivity(home);
                            pref.setLoggedin(true);
                            endActivity();
                        }
                    } else if (strPin.equalsIgnoreCase(pref.getUserPin())) {
                        if (pref.getLoginType().equalsIgnoreCase("parent")) {
                            Intent home = new Intent(PinActivity.this, DeviceHomeList.class);
                            startActivity(home);
                            endActivity();
                        } else if (pref.getLoginType().equalsIgnoreCase("child")) {
                            Intent home = new Intent(PinActivity.this, ChildDeviceListActivity.class);
                            home.putExtra("sessionMins", sessionMins);
                            startActivity(home);
                            endActivity();
                        }
                        Log.d("SYSINFO", "PINS MATCH");
                    } else {
                        etPinLogin.setError("Wrong PIN!");
                        etPinLogin.setText("");
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_expired, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_expired:
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

    private void checkForSession() {
        Log.i(Globals.LOGTAG, "Checking session info....");
        String serverTime = childDatabase.getCurrentServerTime();
        String timer = childDatabase.getTimer();
        String savedInitTime = childDatabase.getSavedInitTime();

        String str[] = timer.split(",");
        List<String> timerList;
        timerList = Arrays.asList(str);

        Log.i(Globals.LOGTAG, "Saved Init Login Time: " + savedInitTime);
        Log.i(Globals.LOGTAG, "Current Time: " + serverTime);

//        String savedMonth = savedInitTime.split(" ")[1];
//        int savedDay = Integer.parseInt(savedInitTime.split(" ")[2]);
//        int savedHour = Integer.parseInt(savedInitTime.split(" ")[3].split(":")[0]);
//        int savedMin = Integer.parseInt(savedInitTime.split(" ")[3].split(":")[1]);
//
//        String gotMonth = serverTime.split(" ")[1];
//        int gotDay = Integer.parseInt(serverTime.split(" ")[2]);
//        int gotHour = Integer.parseInt(serverTime.split(" ")[3].split(":")[0]);
//        int gotMin = Integer.parseInt(serverTime.split(" ")[3].split(":")[1]);

        String savedMonth = savedInitTime.split("-")[1];
        int savedDay = Integer.parseInt(savedInitTime.split("-")[2]);
        int savedHour = Integer.parseInt(savedInitTime.split("-")[3]);
        int savedMin = Integer.parseInt(savedInitTime.split("-")[4]);

        String gotMonth = serverTime.split("-")[1];
        int gotDay = Integer.parseInt(serverTime.split("-")[2]);
        int gotHour = Integer.parseInt(serverTime.split("-")[3]);
        int gotMin = Integer.parseInt(serverTime.split("-")[4]);

        if (savedMonth.equalsIgnoreCase(gotMonth)) {
            //Calculating Difference
            int initMins = 1440 - ((savedHour * 60) + savedMin);
            int additionalMins = (gotHour * 60) + gotMin;
            int finalAdditionalMins = initMins + additionalMins;
            int dayDiffMins = (gotDay - (savedDay + 1)) * 1440;
            int currentDifferenceTimer = dayDiffMins + finalAdditionalMins;

            int savedDifferenceTimer = (Integer.parseInt(timerList.get(0)) * 1440) + (Integer.parseInt(timerList.get(1)) * 60) + Integer.parseInt(timerList.get(2));

            if (savedDifferenceTimer <= currentDifferenceTimer) {
                Log.i(Globals.LOGTAG, "You're session has expired!");
                setTitle("You're session has expired!");
                imageExpired.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "You're session has expired!", Toast.LENGTH_LONG).show();
                etPinLogin.setVisibility(GONE);
                etPinVerify.setVisibility(GONE);
                bDigLogin.setVisibility(GONE);
            } else {
                imageExpired.setVisibility(GONE);
                Log.i(Globals.LOGTAG, "You're session is on! " + (savedDifferenceTimer - currentDifferenceTimer) + " mins to go");
                Toast.makeText(getApplicationContext(), "You're session is on! " +
                        (savedDifferenceTimer - currentDifferenceTimer) + " mins to go", Toast.LENGTH_SHORT).show();
                sessionMins = (savedDifferenceTimer - currentDifferenceTimer);
            }
        } else {
            int daysInMonth = 0;
            String[] with31 = {"01", "03", "05", "07", "08", "10", "12"};
            String[] with30 = {"04", "06", "09", "11"};

            for (String x : with31) {
                if (savedMonth.equalsIgnoreCase(x)) {
                    daysInMonth = 31;
                }
            }

            for (String x : with30) {
                if (savedMonth.equalsIgnoreCase(x)) {
                    daysInMonth = 30;
                }
            }

            if (daysInMonth == 0) {
                daysInMonth = 28;
            }

            int initMins = 1440 - ((savedHour * 60) + savedMin);
            int additionalMins = (gotHour * 60) + gotMin;
            int finalAdditionalMins = initMins + additionalMins;
            int dayDiffMins = ((daysInMonth - savedDay) + gotDay) * 1440;
            int currentDifferenceTimer = dayDiffMins + finalAdditionalMins;

            int savedDifferenceTimer = (Integer.parseInt(timerList.get(0)) * 1440) + (Integer.parseInt(timerList.get(1)) * 60) + Integer.parseInt(timerList.get(2));

            if (savedDifferenceTimer <= currentDifferenceTimer) {
                Log.i(Globals.LOGTAG, "You're session has expired!");
                setTitle("You're session has expired!");
                imageExpired.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "You're session has expired!", Toast.LENGTH_LONG).show();
                etPinLogin.setVisibility(GONE);
                etPinVerify.setVisibility(GONE);
                bDigLogin.setVisibility(GONE);
            } else {
                imageExpired.setVisibility(GONE);
                Log.i(Globals.LOGTAG, "You're session is on! " + (savedDifferenceTimer - currentDifferenceTimer) + " mins to go");
                Toast.makeText(getApplicationContext(), "You're session is on! " +
                        (savedDifferenceTimer - currentDifferenceTimer) + " mins to go", Toast.LENGTH_SHORT).show();
                sessionMins = (savedDifferenceTimer - currentDifferenceTimer);
            }
        }
    }

    private void mailTo() {
        String subject = "Alteration Smart Gadget Credentials. Please keep this mail safe";
        String message = "Alteration ID: " + pref.getemailID() + "\nPassword: " + pref.getPassword();

        Toast.makeText(getApplicationContext(), "Opening mail, please click send to complete operation", Toast.LENGTH_LONG).show();

        Intent in = new Intent(Intent.ACTION_SEND);
        in.putExtra(Intent.EXTRA_EMAIL, new String[]{pref.getemailID()});
        in.putExtra(Intent.EXTRA_SUBJECT, subject);
        in.putExtra(Intent.EXTRA_TEXT, message);
        in.setType("message/rfc822");
        try {
            startActivity(Intent.createChooser(in, "Choose an Email client :"));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "No email client in device !!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setWrongPinCount(int wrongPinCount) {
        SharedPreferences pref = this.getSharedPreferences("wrongCountFile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("ATTEMPTs", wrongPinCount);
        editor.putLong("ATTEMPT_Time", System.currentTimeMillis());
        editor.apply();
    }

    private void checkForLockOut() {
        SharedPreferences pref = this.getSharedPreferences("wrongCountFile", Context.MODE_PRIVATE);
        wrongPinCount = pref.getInt("ATTEMPTs", 0);
        long time = pref.getLong("ATTEMPT_Time", 0);

        Log.d("SYSINFO PIN TIMES", "Time Saved: " + time + ", Current: " + (System.currentTimeMillis() - 300000));

        if (wrongPinCount > 4 && time > (System.currentTimeMillis() - 600000)) {
            bDigLogin.setVisibility(View.INVISIBLE);
            etPinLogin.setVisibility(View.INVISIBLE);

            new CountDownTimer(time - (System.currentTimeMillis() - 600000), 1000) {
                public void onTick(long millisUntilFinished) {
                    long mins = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                    long secs = (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                    txtSetup.setVisibility(View.VISIBLE);
                    txtSetup.setText("You're locked out for entering " + wrongPinCount + " invalid pins.\n\nPlease come after 10 mins to login again!\n\n" + mins + ":" + secs);
                }

                public void onFinish() {
                    setWrongPinCount(0);
                    PinActivity.this.recreate();
                }

            }.start();

        } else {
            bDigLogin.setVisibility(View.VISIBLE);
            setWrongPinCount(0);
        }
    }

    private boolean checkInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    private void logoutEverything() {
        Intent login = new Intent(PinActivity.this, PCSelectActivity.class);
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
