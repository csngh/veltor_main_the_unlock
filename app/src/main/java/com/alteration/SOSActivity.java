package com.alteration;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.alteration.preferences.UserPreferences;

import java.util.ArrayList;

public class SOSActivity extends AppCompatActivity {

    private EditText etNum1, etNum2, etNum3;
    private Button bSaveNumbers, bEditNumbers;
    private ListView lsNumbers;
    private ArrayAdapter<String> listAdapter;
    private LinearLayout saveLayout, viewLayout;
    private UserPreferences preferences;
    private ArrayList<String> phoneList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_sos);

        etNum1 = findViewById(R.id.etnum1);
        etNum2 = findViewById(R.id.etnum2);
        etNum3 = findViewById(R.id.etnum3);
        bSaveNumbers = findViewById(R.id.bSaveNumbers);
        bEditNumbers = findViewById(R.id.bEditNumbers);
        lsNumbers = findViewById(R.id.listViewCall);
        saveLayout = findViewById(R.id.saveLayout);
        viewLayout = findViewById(R.id.callLayout);
        preferences = new UserPreferences(this);

        if (preferences.getPhoneNumbers().isEmpty()) {
            viewLayout.setVisibility(View.GONE);
            saveLayout.setVisibility(View.VISIBLE);
        } else {
            phoneList = preferences.getPhoneNumbers();
            listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, phoneList);
            lsNumbers.setAdapter(listAdapter);
            saveLayout.setVisibility(View.GONE);
            viewLayout.setVisibility(View.VISIBLE);
        }

        ActivityCompat.requestPermissions(SOSActivity.this,
                new String[]{Manifest.permission.SEND_SMS},
                1);

        bSaveNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oneNum = etNum1.getText().toString();
                String twoNum = etNum2.getText().toString();
                String threeNum = etNum3.getText().toString();

                if (oneNum.equalsIgnoreCase("") && twoNum.equalsIgnoreCase("") && threeNum.equalsIgnoreCase("")) {
                    Toast.makeText(getApplicationContext(), "Please enter atleast one number", Toast.LENGTH_SHORT).show();
                } else {
                    if (!oneNum.equalsIgnoreCase("")) {
                        phoneList.add(oneNum);
                    }

                    if (!twoNum.equalsIgnoreCase("")) {
                        phoneList.add(twoNum);
                    }

                    if (!threeNum.equalsIgnoreCase("")) {
                        phoneList.add(threeNum);
                    }
                    preferences.setPhoneNumbers(phoneList);
                    Toast.makeText(getApplicationContext(), "Numbers saved successfully!", Toast.LENGTH_SHORT).show();
                    endActivity();
                }
            }
        });

        lsNumbers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialogBuilderex = new AlertDialog.Builder(SOSActivity.this);
                alertDialogBuilderex.setTitle("Select one action for " + parent.getItemAtPosition(position) + "!");
                alertDialogBuilderex
                        .setCancelable(false)
                        .setNegativeButton("TEXT", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                showSendSMSDialog((String) parent.getItemAtPosition(position));
                            }
                        });
                AlertDialog alertDialogex = alertDialogBuilderex.create();
                alertDialogex.show();
            }
        });

        bEditNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewLayout.setVisibility(View.GONE);
                saveLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showSendSMSDialog(final String phoneNo) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.al_custom_dialog_nickname, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = dialogView.findViewById(R.id.editChildName);

        dialogBuilder.setTitle("Enter your message");
        dialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String msg = edt.getText().toString();
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, msg, null, null);
                    Toast.makeText(getApplicationContext(), "Message Sent",
                            Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Error sending sms!",
                            Toast.LENGTH_SHORT).show();
                    ex.printStackTrace();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(SOSActivity.this, "Permission denied to Send SMS, please enable it from settings.", Toast.LENGTH_SHORT).show();
                }
                break;
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void endActivity() {
        this.finish();
    }
}
