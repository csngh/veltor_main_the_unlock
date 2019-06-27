package com.alteration.parent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.child.ChildDatabase;
import com.alteration.preferences.Globals;
import com.alteration.preferences.MySingleton;
import com.alteration.preferences.Validate;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.apache.commons.net.time.TimeTCPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Splash extends AppCompatActivity {

    private ImageView imageView;
    private ChildDatabase childDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_splash);

        childDatabase = new ChildDatabase(this);
        imageView = findViewById(R.id.splashImage);
        final Animation animationShake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bobble);
        imageView.startAnimation(animationShake);

        runSplash();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void runSplash() {
        Thread timer = new Thread() {
            public void run() {
                try {
//                    TimeTCPClient client = new TimeTCPClient();
//                    try {
//                        client.setDefaultTimeout(60000);
//                        client.connect("129.6.15.28");
//                        childDatabase.setCurrentServerTime(client.getDate().toString());
//                    } finally {
//                        client.disconnect();
//                    }
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
                            childDatabase.setCurrentServerTime(dateTimeStamp);
                            Log.d("MYTAGS", dateTimeStamp);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("MYTAGS", "Error in JSON.");
                        }
                    }, error -> Log.d("MYTAGS", "Error in response."));
                    MySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                    sleep(Globals.SLEEP_TIME);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Intent login = new Intent(Splash.this, PCSelectActivity.class);
                    startActivity(login);
                }
            }
        };
        timer.start();
    }
}
