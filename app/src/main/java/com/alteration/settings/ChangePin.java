package com.alteration.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.preferences.Globals;
import com.alteration.preferences.UserPreferences;
import com.chaos.view.PinView;

public class ChangePin extends AppCompatActivity {

    private PinView etOldPin;
    private PinView etNewPin;
    private UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.al_activity_change_pin);
        setTitle("Change PIN");

        etOldPin = findViewById(R.id.editPinOld);
        etNewPin = findViewById(R.id.editPinNew);
        userPreferences = new UserPreferences(this);
        Button bConfirmChange = findViewById(R.id.bConfPinChange);

        bConfirmChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPin = etOldPin.getText().toString();
                String newPin = etNewPin.getText().toString();
                String userPin = userPreferences.getUserPin();

                if (oldPin.equalsIgnoreCase("")) {
                    etOldPin.setError("Cannot be empty!");
                } else if (newPin.equalsIgnoreCase("")) {
                    etNewPin.setError("Cannot be empty!");
                } else if (oldPin.length() != Globals.PIN_LENGHT) {
                    etOldPin.setError("Invalid!");
                } else if (newPin.length() != Globals.PIN_LENGHT) {
                    etNewPin.setError("Invalid!");
                } else if (!oldPin.equalsIgnoreCase(userPin)) {
                    etOldPin.setError("Wrong PIN!");
                } else {
                    userPreferences.setUserPin(newPin);
                    Toast.makeText(getApplicationContext(), "PIN Changed, please restart your app.", Toast.LENGTH_SHORT
                    ).show();
                    endActivity();
                }
            }
        });
    }

    private void endActivity() {
        this.finish();
    }
}
