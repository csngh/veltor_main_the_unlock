package com.alteration.parent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.preferences.Globals;
import com.alteration.preferences.UserPreferences;
import com.alteration.preferences.Validate;
import com.alteration.qrpreference.ScanQRCode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class
Register extends AppCompatActivity {

    //Widgets
    private EditText editText, editTextEmail, editTextPassword;
    //Variables
    private String name, emailID, password;
    //Functions
    private UserPreferences pref;
    private Validate validate;
    private ScanQRCode scanQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activityl_register);
        setTitle(R.string.qr_code_activity);

        //Initializing All Widgets
        Button bLogin = findViewById(R.id.bLogin);
        editText = findViewById(R.id.editNickName);
        editTextEmail = findViewById(R.id.editREmailID);
        editTextPassword = findViewById(R.id.editRPassword);
        pref = new UserPreferences(this);
        validate = new Validate();
        scanQRCode = new ScanQRCode(this);

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = editText.getText().toString();
                emailID = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();

                if (name.equalsIgnoreCase("") || name.length() < 3) {
                    editText.setError("Too small name");
                } else if (emailID.equalsIgnoreCase("") || !emailID.contains("@") || !emailID.contains(".com")) {
                    editTextEmail.setError("Invalid Email");
                } else if (password.equalsIgnoreCase("") || password.length() < Globals.PASSWORD_ALLOWED_MIN_LENGHT) {
                    editTextPassword.setError("No less than 8 characters allowed!");
                } else {
                    pref.setNickName(name);
                    scanQRCode.initiateQRCodeScan("Scan QRCode from product back.");
//                    Intent cloud = new Intent(Register.this, CloudLogin.class);
//                    cloud.putExtra("email", emailID);
//                    cloud.putExtra("password", password);
//                    pref.setOauth_id("alteration-innotech-811");
//                    pref.setOauth_secret("179526904d4019eb46b2572fbd7437ee23d03b65");
//                    pref.setProduct_id(5059);
//                    startActivity(cloud);
//                    endActivity();
                }
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
                    Log.i("MYDATA", result.getContents());
                    Log.i("MYDATA", validate.decrypt(result.getContents(), Globals.KEY));
                    String ID = validate.decrypt(result.getContents(), Globals.KEY);
                    if ((ID.charAt(0) != 'P')) {
                        Toast.makeText(Register.this, "Invalid QRCode", Toast.LENGTH_SHORT).show();
                    } else if (ID.substring(ID.indexOf("P") + 2, ID.indexOf("I")).length() != 24 || ID.substring(ID.indexOf("I") + 2, ID.indexOf("D")).length() != 4) {
                        Toast.makeText(Register.this, "QRCode not supported", Toast.LENGTH_SHORT).show();
                    } else {
                        pref.setProduct_id(Integer.parseInt(ID.substring(ID.indexOf("I") + 2, ID.indexOf("D"))));
                        pref.setOauth_id(ID.substring(ID.indexOf("D") + 2, ID.indexOf("S")));
                        pref.setOauth_secret(ID.substring(ID.indexOf("S") + 2, ID.indexOf("#")));
                        //Displaying all datas
//                        Log.i("MYDATAS:IDOAUTH", ID.substring(ID.indexOf("D") + 2, ID.indexOf("S")));
//                        Log.i("MYDATAS:SECRETOAUTH", ID.substring(ID.indexOf("S") + 2, ID.indexOf("#")));

                        Intent cloud = new Intent(Register.this, CloudLogin.class);
                        cloud.putExtra("email", emailID);
                        cloud.putExtra("password", password);
                        startActivity(cloud);
                        endActivity();
                    }
                } catch (Exception e) {
                    Toast.makeText(Register.this, "QRCode Error Occoured", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void endActivity() {
        this.finish();
    }
}
