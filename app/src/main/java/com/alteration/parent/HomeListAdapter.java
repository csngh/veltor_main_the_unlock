package com.alteration.parent;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.preferences.UserPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;

import static io.particle.android.sdk.utils.Py.list;

class HomeListAdapter extends ArrayAdapter {

    private List<Object> list = new ArrayList<>();
    private int isLock = -1;
    private int resultCode;

    HomeListAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }

    @Override
    public void add(@Nullable Object object) {
        super.add(object);
        list.add(object);
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    private static class DataHandler {
        TextView dName;
        ImageView bLock;
        String toggle;
        Animation animationShake;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final DataHandler handler = new DataHandler();

        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(R.layout.al_device_list_home_items, parent, false);
        handler.dName = convertView.findViewById(R.id.tvDevName);
        handler.bLock = convertView.findViewById(R.id.bLock);
        convertView.setTag(handler);

        final UserPreferences pref = new UserPreferences(this.getContext());
        final HomeListHandler listHandler;

        listHandler = (HomeListHandler) this.getItem(position);

        //==========================================================================================

        checkStatus(handler, listHandler);

        handler.bLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Please wait", Toast.LENGTH_SHORT).show();
                perform(listHandler, handler, pref);
            }
        });

        return convertView;
    }

    private int checkStatus(final DataHandler handler, final HomeListHandler list) {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                ParticleDevice myDevice = particleCloud.getDevice(list.getDevID());
                try {
                    isLock = myDevice.getIntVariable("isLocked");
                } catch (ParticleDevice.VariableDoesNotExistException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onSuccess(@NonNull Object o) {
                Log.i("CHECKSTATUS:" + list.getDevID(), "SUCCESS");
                if (isLock == 1) {
                    String temp = list.getDevName() + ":\nLOCKED";
                    handler.bLock.setImageResource(R.drawable.ic_lock);
                    handler.dName.setText(temp);
                    handler.dName.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
                    handler.toggle = getContext().getResources().getString(R.string.call_unlock);
                } else if (isLock == 0) {
                    String temp = list.getDevName() + ":\nUNLOCKED";
                    handler.bLock.setImageResource(R.drawable.ic_unlock);
                    handler.dName.setText(temp);

                    handler.dName.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
                    handler.toggle = getContext().getResources().getString(R.string.call_lock);

                } else if (isLock == -1) {
                    handler.bLock.setImageResource(R.drawable.ic_action_failed);
                    handler.dName.setText("Device Offline");
                    handler.dName.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                }
                handler.animationShake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                handler.bLock.startAnimation(handler.animationShake);
                isLock = -1;
            }

            @Override
            public void onFailure(@NonNull ParticleCloudException exception) {
                Log.i("CHECKSTATUS:" + list.getDevID(), "FAILED");
            }
        });
        return isLock;
    }

    private int perform(final HomeListHandler listHandler, final DataHandler handler, final UserPreferences pref) {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                ParticleDevice myDevice = particleCloud.getDevice(listHandler.getDevID());
                try {
                    myDevice.callFunction(getContext().getResources().getString(R.string.lockDoor), list(handler.toggle + pref.getNickName() + "$" + handler.toggle + "ED"));
                    //myDevice.callFunction(getContext().getResources().getString(R.string.setName), list(pref.getNickName() + "$" + handler.toggle + "ED"));
                } catch (ParticleDevice.FunctionDoesNotExistException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onSuccess(@NonNull Object o) {
                Log.i("TOGGLE:" + handler.toggle, "SUCCESS");
                checkStatus(handler, listHandler);
                resultCode = 1;
            }

            @Override
            public void onFailure(@NonNull ParticleCloudException exception) {
                Log.i("TOGGLE:" + listHandler.getDevID(), "FAILED");
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                checkStatus(handler, listHandler);
                resultCode = 0;
            }
        });
        return resultCode;
    }
}
