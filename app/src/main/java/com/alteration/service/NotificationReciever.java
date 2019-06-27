package com.alteration.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.alteration.R;
import com.alteration.parent.DeviceHomeList;

import java.io.IOException;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;

public class NotificationReciever extends BroadcastReceiver {

    private int stat = -1;

    @Override
    public void onReceive(final Context context, Intent intent) {

        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                List<ParticleDevice> devices = ParticleCloudSDK.getCloud().getDevices();
                for (ParticleDevice device : devices) {
                    try {
                        stat = device.getIntVariable("isLocked");
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            public void onSuccess(@NonNull Object o) {
                if (stat == 0) {
                    showNotification(context);
                }
            }

            @Override
            public void onFailure(@NonNull ParticleCloudException exception) {

            }
        });

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(1000);
        }
        SharedPreferences preferences = context.getSharedPreferences("alarmStat", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("alarm", false);
        editor.apply();
        Log.i("ALARM STATUS", "RANGGGG !!!");

    }

    private void showNotification(Context context) {
        String CHANNEL_ID = "my_channel_01";
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_unlock)
                        .setContentTitle("Lock Status")
                        .setContentText("Reminder: Your door is unlocked !");

        Intent resultIntent = new Intent(context, DeviceHomeList.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(DeviceHomeList.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager != null) {
            mNotificationManager.notify(1, mBuilder.build());
        }
    }

}
