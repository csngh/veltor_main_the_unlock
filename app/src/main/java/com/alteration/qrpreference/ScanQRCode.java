package com.alteration.qrpreference;

import android.app.Activity;
import android.content.Context;

import com.google.zxing.integration.android.IntentIntegrator;

public class
ScanQRCode {

    private Context activityContext;

    public ScanQRCode(Context context) {
        this.activityContext = context;
    }

    public void initiateQRCodeScan(String message) {
        IntentIntegrator intentIntegrator = new IntentIntegrator((Activity) activityContext);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        intentIntegrator.setPrompt(message);
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setBarcodeImageEnabled(false);
        intentIntegrator.initiateScan();
    }

}
