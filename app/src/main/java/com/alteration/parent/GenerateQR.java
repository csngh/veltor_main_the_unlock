package com.alteration.parent;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.preferences.Globals;
import com.alteration.preferences.MySingleton;
import com.alteration.preferences.UserPreferences;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;

public class GenerateQR extends AppCompatActivity {

    private Bundle b;
    private ProgressDialog dialog;
    private ImageView imgQr;
    //private Validate validate;
    private Button bInvite;
    private Bitmap bitmap;
    private UserPreferences pref;
    //Data types
    private String devID, accToken, text2QR;
    private int day, hour, min;
    private String[] resultArr;
    private String currentServerTime, finalStr;
    private boolean isWithTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_generate_qr);
        setTitle(R.string.gen_qr);

        b = getIntent().getExtras();
        imgQr = findViewById(R.id.imgQR);
        bInvite = findViewById(R.id.bInvite);
        //validate = new Validate();
        pref = new UserPreferences(this);
        bInvite.setVisibility(View.GONE);

        if (b != null) {
            if (b.getStringArray("selectedItems") != null) {
                resultArr = b.getStringArray("selectedItems");
            }
            if (b.get("day") != null) {
                day = b.getInt("day");
                hour = b.getInt("hour");
                min = b.getInt("min");
                isWithTimer = true;
            } else {
                isWithTimer = false;
            }
            Toast.makeText(getApplicationContext(), "Sharing selected devices", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Sharing all devices", Toast.LENGTH_SHORT).show();
        }

        dialog = ProgressDialog.show(GenerateQR.this, "Please wait!", "Evaluating...", true);
        dialog.show();

        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                List<ParticleDevice> devices = ParticleCloudSDK.getCloud().getDevices();
                int count = 0;
                finalStr = "T:" + particleCloud.getAccessToken() + ":#";
                if (b == null) {
                    for (ParticleDevice device : devices) {
                        finalStr += count + "N:" + device.getName() + "I:" + device.getID();
                        count++;
                    }
                } else if (b != null && b.getStringArray("selectedItems") == null) {
                    for (ParticleDevice device : devices) {
                        finalStr += count + "N:" + device.getName() + "I:" + device.getID();
                        count++;
                    }
                } else {
                    for (String aResultArr : resultArr) {
                        for (ParticleDevice device : devices) {
                            if (device.getName().equalsIgnoreCase(aResultArr)) {
                                finalStr += count + "N:" + device.getName() + "I:" + device.getID();
                                count++;
                            }
                        }
                    }
                }

                if (isWithTimer) {
                    try {
                        int finalCount = count;
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://worldclockapi.com/api/json/utc/now",
                                response -> {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        String dateTimeStamp = jsonObject.getString("currentDateTime");
                                        dateTimeStamp = dateTimeStamp.substring(0, dateTimeStamp.length() - 1);
                                        char[] dtCharArray = dateTimeStamp.toCharArray();
                                        dtCharArray[10] = '-';
                                        dtCharArray[13] = '-';
                                        dateTimeStamp = String.valueOf(dtCharArray);
                                        Log.d("MYTAGS", dateTimeStamp);
                                        currentServerTime = dateTimeStamp;
                                        finalStr += "$:" + finalCount + "^:" + currentServerTime + "!:" + day + "," + hour + "," + min;
                                        text2QR = finalStr;

                                        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                                        try {
                                            BitMatrix bitMatrix = multiFormatWriter.encode(text2QR, BarcodeFormat.QR_CODE, 200, 200);
                                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                            bitmap = barcodeEncoder.createBitmap(bitMatrix);
                                            imgQr.setImageBitmap(bitmap);
                                        } catch (WriterException e) {
                                            e.printStackTrace();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Log.d("MYTAGS", "Error in JSON.");
                                    }
                                }, error -> Log.d("MYTAGS", "Error in response."));
                        MySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        int finalCount1 = count;
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://worldclockapi.com/api/json/utc/now",
                                response -> {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        String dateTimeStamp = jsonObject.getString("currentDateTime");
                                        dateTimeStamp = dateTimeStamp.substring(0, dateTimeStamp.length() - 1);
                                        char[] dtCharArray = dateTimeStamp.toCharArray();
                                        dtCharArray[10] = '-';
                                        dtCharArray[13] = '-';
                                        dateTimeStamp = String.valueOf(dtCharArray);
                                        Log.d("MYTAGS", dateTimeStamp);
                                        currentServerTime = dateTimeStamp;
                                        finalStr += "$:" + finalCount1 + "^:" + currentServerTime + "!:" + 365 + "," + 0 + "," + 0;
                                        text2QR = finalStr;
                                        Log.i(Globals.LOGTAG, "Generated QR String: " + text2QR);

                                        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                                        try {
                                            BitMatrix bitMatrix = multiFormatWriter.encode(text2QR, BarcodeFormat.QR_CODE, 200, 200);
                                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                            bitmap = barcodeEncoder.createBitmap(bitMatrix);
                                            imgQr.setImageBitmap(bitmap);
                                        } catch (WriterException e) {
                                            e.printStackTrace();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Log.d("MYTAGS", "Error in JSON.");
                                    }
                                }, error -> Log.d("MYTAGS", "Error in response."));
                        MySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            public void onSuccess(@NonNull Object o) {
                dialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull ParticleCloudException exception) {
                dialog.dismiss();
            }
        });

//        bInvite.setOnClickListener(view -> {
//            Bitmap bm = ((BitmapDrawable) imgQr.getDrawable()).getBitmap();
//            String result = MediaStore.Images.Media.insertImage(getContentResolver(), bm, "", "");
//            Uri imageUri = Uri.parse(result);
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType("image/*");
//            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
//            intent.putExtra(Intent.EXTRA_TEXT, "Hey this is " + pref.getNickName() + ", i am sharing my Key-U lock access with you.");
//            Intent chooser = Intent.createChooser(intent, "Send via");
//            try {
//                startActivity(chooser);
//            } catch (Exception e) {
//                Toast.makeText(getApplicationContext(), "Some error occoured", Toast.LENGTH_SHORT).show();
//            }
//        });

    }
}
