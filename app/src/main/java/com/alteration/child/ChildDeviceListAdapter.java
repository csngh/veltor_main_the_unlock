package com.alteration.child;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alteration.R;
import com.alteration.preferences.MySingleton;
import com.alteration.preferences.UserPreferences;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildDeviceListAdapter extends ArrayAdapter {

    private List<Object> list = new ArrayList<>();
    private int resultCode, statusCode = -1;

    public ChildDeviceListAdapter(@NonNull Context context, @LayoutRes int resource) {
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
        //ImageView bLock;
        Button bLock, bUnlock;
        ChildDatabase childDatabase;
        UserPreferences pref;
        String toggle = "";
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final DataHandler handler = new DataHandler();
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(R.layout.al_device_child_list_home_items, parent, false);
        handler.dName = convertView.findViewById(R.id.tvDevName);
        //handler.bLock = convertView.findViewById(R.id.bLock);
        handler.bLock = convertView.findViewById(R.id.bChildLock);
        handler.bUnlock = convertView.findViewById(R.id.bChildUnock);
        handler.childDatabase = new ChildDatabase(getContext());
        handler.pref = new UserPreferences(getContext());
        convertView.setTag(handler);

        final ChildDeviceListHandler childDeviceListHandler;
        childDeviceListHandler = (ChildDeviceListHandler) this.getItem(position);

        handler.dName.setText(childDeviceListHandler.getDevName());

        handler.bLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performAction(childDeviceListHandler, handler, getContext().getResources().getString(R.string.call_lock));
            }
        });

        handler.bUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performAction(childDeviceListHandler, handler, getContext().getResources().getString(R.string.call_unlock));
            }
        });

        //handler.bLock.setImageResource(R.drawable.fail);

        /*handler.bLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (handler.toggle.equalsIgnoreCase("")) {
                    handler.toggle = getContext().getResources().getString(R.string.call_lock);
                    performAction(childDeviceListHandler, handler, handler.toggle);
                } else if (handler.toggle.equalsIgnoreCase("LOCK")) {
                    handler.toggle = getContext().getResources().getString(R.string.call_unlock);
                    performAction(childDeviceListHandler, handler, handler.toggle);
                } else if (handler.toggle.equalsIgnoreCase("UNLOCK")) {
                    handler.toggle = getContext().getResources().getString(R.string.call_lock);
                    performAction(childDeviceListHandler, handler, handler.toggle);
                } else if (handler.toggle.contains("CK!")) {
                    handler.toggle = handler.toggle.substring(0, handler.toggle.indexOf('!'));
                    Log.i("MYTAGS", "CONCATTED: " + handler.toggle);
                    performAction(childDeviceListHandler, handler, handler.toggle);
                }
            }
        });*/

        return convertView;
    }

    private void performAction(ChildDeviceListHandler childDeviceListHandler, final DataHandler handler, final String param) {

        Toast.makeText(getContext(), param + "ING " + childDeviceListHandler.getDevName(), Toast.LENGTH_SHORT).show();

        String WEB_URL = "https://api.particle.io/v1/devices/" + childDeviceListHandler.getDevID() + "/" + getContext().getResources().getString(R.string.lockDoor) + "?access_token=" + handler.childDatabase.getAccToken();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, WEB_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                resultCode = 1;
                Toast.makeText(getContext(), "Done", Toast.LENGTH_SHORT).show();
                /*if (param.equalsIgnoreCase("LOCK")) {
                    handler.bLock.setImageResource(R.drawable.ic_lock);
                } else if (param.equalsIgnoreCase("UNLOCK")) {
                    handler.bLock.setImageResource(R.drawable.ic_unlock);
                }*/
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getContext(), "Error, Action Failed!", Toast.LENGTH_SHORT).show();
                //handler.toggle += '!';
                resultCode = 0;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("args", param + handler.pref.getNickName() + "$" + param + "ED");
                return params;
            }
        };
        MySingleton.getmInstance(getContext()).addToRequestQueue(stringRequest);
    }

    private void checkLockStatus(final ChildDeviceListHandler childDeviceListHandler, String accToken, final DataHandler handler) {
        String WEB_URL = "https://api.particle.io/v1/devices/" + childDeviceListHandler.getDevID() + "/" + getContext().getResources().getString(R.string.isLocked) + "?access_token=" + accToken;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, WEB_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                Log.i("MYTAGS", "RESPONSE: " + response);
                handler.dName.setText(childDeviceListHandler.getDevName());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        Log.d("GOTDATA", res);
                    } catch (UnsupportedEncodingException e1) {
                        Log.d("GOTDATA", "NOthing");
                        e1.printStackTrace();
                    }
                }
                statusCode = -1;
                Log.i("CHECK", childDeviceListHandler.getDevName() + " Failed");
                //handler.bLock.setImageResource(R.drawable.fail);
                handler.dName.setText("Device Offline");
            }
        });

        MySingleton.getmInstance(getContext()).addToRequestQueue(stringRequest);
    }
}
