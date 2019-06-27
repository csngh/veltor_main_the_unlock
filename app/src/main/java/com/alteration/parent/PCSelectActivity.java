package com.alteration.parent;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.child.ChildDatabase;
import com.alteration.preferences.Globals;
import com.alteration.preferences.MySingleton;
import com.alteration.preferences.UserPreferences;
import com.alteration.qrpreference.ScanQRCode;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import io.particle.android.sdk.utils.Toaster;

public class PCSelectActivity extends AppCompatActivity {

    //private Validate validate;
    private UserPreferences pref;
    private ScanQRCode scanQRCode;
    private ChildDatabase childDatabase;
    //Data Types
    private String QRTimeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_pcselect);
        setTitle(R.string.pc_select);

        final Button bParent = findViewById(R.id.bParent);
        Button bChild = findViewById(R.id.bChild);
        pref = new UserPreferences(this);
        scanQRCode = new ScanQRCode(this);
        //validate = new Validate();
        childDatabase = new ChildDatabase(this);

        final Animation animationShake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bobble);
        bParent.startAnimation(animationShake);
        bChild.startAnimation(animationShake);

        if (!pref.isLoggedin() && pref.getIsCloudLogged()) {
            Intent i = new Intent(PCSelectActivity.this, PinActivity.class);
            startActivity(i);
            endActivity();
        } else if (pref.isLoggedin() && pref.getIsCloudLogged()) {
            Intent i = new Intent(PCSelectActivity.this, PinActivity.class);
            startActivity(i);
            endActivity();
        }

        bParent.setOnClickListener(view -> {
            Intent parent = new Intent(PCSelectActivity.this, Register.class);
            pref.setLoginType("parent");
            startActivity(parent);
            endActivity();
        });

        bChild.setOnClickListener(view -> {
            if (checkInternetConnection()) {
                showNickNameSetDialog();
            } else {
                Toast.makeText(getApplicationContext(), "Sorry! you're not connected to internet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    String ID = result.getContents();
                    //ID = result.getContents();
                    Log.i(Globals.LOGTAG, "Fetched QR from Parent: " + ID);
                    if ((ID.charAt(0) != 'T' || ID.charAt(43) != '#')) {
                        Toast.makeText(PCSelectActivity.this, "Invalid QRCode", Toast.LENGTH_SHORT).show();
                    } else if (ID.substring(ID.indexOf("T") + 2, ID.indexOf(":#")).length() != Globals.DEVICE_ID_LENGHT) {
                        Toast.makeText(PCSelectActivity.this, "QRCode not supported", Toast.LENGTH_SHORT).show();
                    } else {

                        try {
                            String token = ID.substring(ID.indexOf("T") + 2, ID.indexOf(":#"));
                            childDatabase.setAccToken(token);
                            Log.i("CHILDDATA", token);
                            int c = Integer.parseInt((ID.substring(ID.indexOf("$") + 2, ID.indexOf("^"))));
                            for (int i = 0; i < c; i++) {
                                if (c == 1 || i == (c - 1)) {
                                    String dataComp = ID.substring(ID.indexOf(String.valueOf(i) + "N") + 3, ID.indexOf("$"));
                                    String dName = dataComp.substring(0, dataComp.indexOf("I"));
                                    String devID = dataComp.substring(dataComp.indexOf("I") + 2);
                                    Log.i("CHILDDATA", "Index: " + i + ", Name: " + dName + ", ID: " + devID);

                                    childDatabase.setDevName(dName, i);
                                    childDatabase.setDevID(devID, i);
                                    childDatabase.setDataCount(i + 1);

                                } else {
                                    String dataComp = ID.substring(ID.indexOf(String.valueOf(i) + "N") + 3, ID.indexOf(String.valueOf(i + 1) + "N:"));
                                    String dName = dataComp.substring(0, dataComp.indexOf("I"));
                                    String devID = dataComp.substring(dataComp.indexOf("I") + 2);
                                    Log.i("CHILDDATA", "Index: " + i + ", Name: " + dName + ", ID: " + devID);

                                    childDatabase.setDevName(dName, i);
                                    childDatabase.setDevID(devID, i);
                                    childDatabase.setDataCount(i + 1);
                                }
                            }
                            QRTimeStamp = ID.substring(ID.indexOf("^") + 2, ID.indexOf("!"));
                            Log.i(Globals.LOGTAG, "QR TimeStamp from Parent: " + QRTimeStamp);
                            String timer = ID.substring(ID.indexOf("!") + 2);
                            childDatabase.setTimer(timer);
                            getServerTimeUpdate();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(PCSelectActivity.this, "QRCode Error Occoured", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showNickNameSetDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.al_custom_dialog_nickname, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = dialogView.findViewById(R.id.editChildName);

        dialogBuilder.setTitle("Enter your name");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = edt.getText().toString();
                if (name.equalsIgnoreCase("") || name.length() < 3) {
                    edt.setError("Too small name");
                } else {
                    pref.setNickName(name);
                    scanQRCode.initiateQRCodeScan("Scan QRCode from parent device");
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

//    private void getServerTime() {
//        final String[] serverTime = new String[1];
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    TimeTCPClient client = new TimeTCPClient();
//                    try {
//                        client.setDefaultTimeout(60000);
//                        client.connect("129.6.15.28");
//                        serverTime[0] = String.valueOf(client.getDate());
//                        childDatabase.setSavedInitTime(serverTime[0]);
//
//                        int serverDay = Integer.parseInt(serverTime[0].split(" ")[2]);
//                        int serverHour = Integer.parseInt(serverTime[0].split(" ")[3].split(":")[0]);
//                        int serverMin = Integer.parseInt(serverTime[0].split(" ")[3].split(":")[1]);
//                        int QRDay = Integer.parseInt(QRTimeStamp.split(" ")[2]);
//                        int QRHour = Integer.parseInt(QRTimeStamp.split(" ")[3].split(":")[0]);
//                        int QRMin = Integer.parseInt(QRTimeStamp.split(" ")[3].split(":")[1]);
//
//                        Log.i(Globals.LOGTAG + "PCSelect", serverDay + " " + serverHour + ":" + serverMin + ", " + QRDay + " " + QRHour + ":" + QRMin);
//
//                        //For Changed Day
//                        if (serverDay != QRDay) {
//                            Toaster.l(PCSelectActivity.this, "QRCode is expired!");
//                            childDatabase.removeAllWithLogout();
//                        }//For Changed Hour
//                        else if (serverHour != QRHour) {
//                            Toaster.l(PCSelectActivity.this, "QRCode is expired!");
//                            childDatabase.removeAllWithLogout();
//                        }//For Changed Minute
//                        else if (serverMin != QRMin) {
//                            if(serverMin < QRMin){
//                                if(((60 - serverMin) + QRMin) > 5){
//                                    Toaster.l(PCSelectActivity.this, "QRCode is expired!");
//                                    childDatabase.removeAllWithLogout();
//                                }else {
//                                    Intent childHome = new Intent(PCSelectActivity.this, PinActivity.class);
//                                    pref.setLoginType("child");
//                                    pref.setIsCloudLogged(true);
//                                    pref.setemailID("child");
//                                    pref.setPassword("child");
//                                    startActivity(childHome);
//                                    endActivity();
//                                }
//                            }else {
//                                if((serverMin - QRMin) > 5){
//                                    Toaster.l(PCSelectActivity.this, "QRCode is expired!");
//                                    childDatabase.removeAllWithLogout();
//                                }else {
//                                    Intent childHome = new Intent(PCSelectActivity.this, PinActivity.class);
//                                    pref.setLoginType("child");
//                                    pref.setIsCloudLogged(true);
//                                    pref.setemailID("child");
//                                    pref.setPassword("child");
//                                    startActivity(childHome);
//                                    endActivity();
//                                }
//                            }
//                        } else {
//                            Intent childHome = new Intent(PCSelectActivity.this, PinActivity.class);
//                            pref.setLoginType("child");
//                            pref.setIsCloudLogged(true);
//                            pref.setemailID("child");
//                            pref.setPassword("child");
//                            startActivity(childHome);
//                            endActivity();
//                        }
//
//                    } finally {
//                        client.disconnect();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
//    }

    private void getServerTimeUpdate(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://worldclockapi.com/api/json/utc/now",
                response -> {
                    try {
                        //Getting server time
                        JSONObject jsonObject = new JSONObject(response);
                        String dateTimeStamp = jsonObject.getString("currentDateTime");
                        dateTimeStamp = dateTimeStamp.substring(0, dateTimeStamp.length() - 1);
                        char[] dtCharArray = dateTimeStamp.toCharArray();
                        dtCharArray[10] = '-';
                        dtCharArray[13] = '-';
                        dateTimeStamp = String.valueOf(dtCharArray);

                        childDatabase.setSavedInitTime(dateTimeStamp);

                        String[] dtArrayString = dateTimeStamp.split("-");
                        int[] dtArray = new int[dtArrayString.length];
                        for (int i = 0; i < dtArrayString.length; i++) {
                            dtArray[i] = Integer.parseInt(dtArrayString[i]);
                        }

                        String[] QRdtArrayString = QRTimeStamp.split("-");
                        int[] QRdtArray = new int[QRdtArrayString.length];
                        for (int i = 0; i < QRdtArrayString.length; i++) {
                            QRdtArray[i] = Integer.parseInt(QRdtArrayString[i]);
                        }

                        int serverYear = dtArray[0];
                        int serverMonth = dtArray[1];
                        int serverDay = dtArray[2];
                        int serverHour = dtArray[3];
                        int serverMin = dtArray[4];

                        int QRYear = QRdtArray[0];
                        int QRMonth = QRdtArray[1];
                        int QRDay = QRdtArray[2];
                        int QRHour = QRdtArray[3];
                        int QRMin = QRdtArray[4];

                        Log.i(Globals.LOGTAG + "PCSelect", serverDay + " " + serverHour + ":" + serverMin + ", " + QRDay + " " + QRHour + ":" + QRMin);

                        //For Changed Year
                        if (serverYear != QRYear) {
                            Toaster.l(PCSelectActivity.this, "QRCode is expired!");
                            childDatabase.removeAllWithLogout();
                        }//For Changed Month
                        else if (serverMonth != QRMonth) {
                            Toaster.l(PCSelectActivity.this, "QRCode is expired!");
                            childDatabase.removeAllWithLogout();
                        }//For Changed Day
                        else if (serverDay != QRDay) {
                            Toaster.l(PCSelectActivity.this, "QRCode is expired!");
                            childDatabase.removeAllWithLogout();
                        }
                        //For Changed Hour
                        else if (serverHour != QRHour) {
                            Toaster.l(PCSelectActivity.this, "QRCode is expired!");
                            childDatabase.removeAllWithLogout();
                        }//For Changed Minute
                        else if (serverMin != QRMin) {
                            if(serverMin < QRMin){
                                if(((60 - serverMin) + QRMin) > 5){
                                    Toaster.l(PCSelectActivity.this, "QRCode is expired!");
                                    childDatabase.removeAllWithLogout();
                                }else {
                                    Intent childHome = new Intent(PCSelectActivity.this, PinActivity.class);
                                    pref.setLoginType("child");
                                    pref.setIsCloudLogged(true);
                                    pref.setemailID("child");
                                    pref.setPassword("child");
                                    startActivity(childHome);
                                    endActivity();
                                }
                            }else {
                                if((serverMin - QRMin) > 5){
                                    Toaster.l(PCSelectActivity.this, "QRCode is expired!");
                                    childDatabase.removeAllWithLogout();
                                }else {
                                    Intent childHome = new Intent(PCSelectActivity.this, PinActivity.class);
                                    pref.setLoginType("child");
                                    pref.setIsCloudLogged(true);
                                    pref.setemailID("child");
                                    pref.setPassword("child");
                                    startActivity(childHome);
                                    endActivity();
                                }
                            }
                        } else {
                            Intent childHome = new Intent(PCSelectActivity.this, PinActivity.class);
                            pref.setLoginType("child");
                            pref.setIsCloudLogged(true);
                            pref.setemailID("child");
                            pref.setPassword("child");
                            startActivity(childHome);
                            endActivity();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("MYTAGS", "Error in JSON.");
                    }
                }, error -> Log.d("MYTAGS", "Error in response."));
        MySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private boolean checkInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    private void endActivity() {
        this.finish();
    }
}
