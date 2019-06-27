package com.alteration.parent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.preferences.Globals;
import com.alteration.preferences.UserPreferences;
import com.alteration.qrpreference.ScanQRCode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import io.particle.android.sdk.cloud.ApiFactory;
import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;

public class CloudLogin extends AppCompatActivity {

    //Widgets
    private EditText etEmail, etPass;
    private Button bCdLogin;
    private TextView tvFp;
    //Functions
    private UserPreferences pref;
    private ProgressDialog dialog;
    private ScanQRCode scanQRCode;
    //private Validate validate;
    private Bundle extras;
    //Data types
    private String emailID, password, ID;
    private int product_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_cloud_login);
        setTitle(R.string.cus_login);

        etPass = findViewById(R.id.editPassword);
        etEmail = findViewById(R.id.editEmail);
        bCdLogin = findViewById(R.id.bCldLogin);
        tvFp = findViewById(R.id.textForgotPassword);
        pref = new UserPreferences(this);
        scanQRCode = new ScanQRCode(this);
        //validate = new Validate();
        extras = getIntent().getExtras();

        product_ID = pref.getProduct_id();
        Log.i(Globals.LOGTAG, "Product ID: " + String.valueOf(product_ID));
        Log.i(Globals.LOGTAG, "Oauth: " + pref.getOauth_id());
        Log.i(Globals.LOGTAG, "OAUTH Sec: " + pref.getOauth_secret());

        //ParticleCloudSDK.init(CloudLogin.this);

        ParticleCloudSDK.initWithOauthCredentialsProvider(CloudLogin.this,
                new ApiFactory.OauthBasicAuthCredentialsProvider() {

                    public String getClientId() {
                        return pref.getOauth_id();
                    }

                    public String getClientSecret() {
                        return pref.getOauth_secret();
                    }
                });

        if (extras != null) {
            etEmail.setText(extras.getString("email"));
            etPass.setText(extras.getString("password"));
        }

        tvFp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQRCode.initiateQRCodeScan("Scan QRCode from product back.");
            }
        });

        bCdLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = ProgressDialog.show(CloudLogin.this, "", "Logging you in...", true);
                dialog.show();
                emailID = etEmail.getText().toString();
                password = etPass.getText().toString();

                if (emailID.equalsIgnoreCase("") || !emailID.contains("@") || !emailID.contains(".com")) {
                    etEmail.setError("Invalid Email");
                    dialog.dismiss();
                } else if (password.equalsIgnoreCase("") || password.length() < Globals.PASSWORD_ALLOWED_MIN_LENGHT) {
                    etPass.setError("No less than 8 characters allowed!");
                    dialog.dismiss();
                } else {
                    loginIntoCloud();
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
                    ID = result.getContents();
                    if ((ID.charAt(0) != 'P')) {
                        Toast.makeText(CloudLogin.this, "Invalid QRCode", Toast.LENGTH_SHORT).show();
                    } else if (ID.substring(ID.indexOf("P") + 2).length() != 24) {
                        Toast.makeText(CloudLogin.this, "QRCode not supported", Toast.LENGTH_SHORT).show();
                    } else {
                        initiatePassWordRecovery();
                    }
                } catch (Exception e) {
                    Toast.makeText(CloudLogin.this, "QRCode Error Occoured", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initiatePassWordRecovery() {
        Toast.makeText(getApplicationContext(), "You can find your password in your mail account.", Toast.LENGTH_LONG).show();
    }

    private void loginIntoCloud() {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException {
                ParticleCloudSDK.getCloud().signUpAndLogInWithCustomer(emailID, password, product_ID);
                //ParticleCloudSDK.getCloud().logIn(emailID, password);
                return null;
            }

            @Override
            public void onSuccess(@NonNull Object o) {
                pref.setIsCloudLogged(true);
                pref.setemailID(emailID);
                pref.setPassword(password);
                Intent setPin = new Intent(CloudLogin.this, PinActivity.class);
                startActivity(setPin);
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Successfully logged in.\nPlease take a note of your passowrd.", Toast.LENGTH_LONG).show();
                endActivity();
            }

            @Override
            public void onFailure(@NonNull ParticleCloudException exception) {
                Toast.makeText(getApplicationContext(), "Error occoured.\n" + emailID + "\n" + password, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }

    private void endActivity() {
        this.finish();
    }
}
