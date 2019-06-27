package com.alteration.child;

import android.content.Context;
import android.content.SharedPreferences;

public class ChildDatabase {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context mCtx;
    private String devID, devName, accToken, fileName = "c_data";
    private int dataCount;
    private String timer, savedInitTime, currentServerTime;

    public ChildDatabase() {

    }

    public void removeAllWithLogout() {
        preferences = (mCtx).getSharedPreferences(fileName, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    public ChildDatabase(Context context) {
        this.mCtx = context;
    }

    /*
     *
     *   Setter & Getter with
     *   Data Count
     *
     */
    private String getDataWithCount(int count, String key) {
        String param;
        String tempFileName = fileName + String.valueOf(count);
        preferences = (mCtx).getSharedPreferences(tempFileName, Context.MODE_PRIVATE);
        param = preferences.getString(key, "NULL");
        return param;
    }

    private void setDataWithCount(int count, String key, String param) {
        String tempFileName = fileName + String.valueOf(count);
        preferences = (mCtx).getSharedPreferences(tempFileName, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString(key, param);
        editor.apply();
    }

    //Setters & Getters

    public String getCurrentServerTime() {
        preferences = (mCtx).getSharedPreferences(fileName, Context.MODE_PRIVATE);
        currentServerTime = preferences.getString("currentServerTime", "NULL");
        return currentServerTime;
    }

    public void setCurrentServerTime(String currentServerTime) {
        preferences = (mCtx).getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("currentServerTime", currentServerTime);
        editor.apply();
    }

    public String getSavedInitTime() {
        preferences = (mCtx).getSharedPreferences(fileName, Context.MODE_PRIVATE);
        savedInitTime = preferences.getString("savedInitTime", "NULL");
        return savedInitTime;
    }

    public void setSavedInitTime(String savedInitTime) {
        preferences = (mCtx).getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("savedInitTime", savedInitTime);
        editor.apply();
    }

    public String getTimer() {
        preferences = (mCtx).getSharedPreferences(fileName, Context.MODE_PRIVATE);
        timer = preferences.getString("timer", "NULL");
        return timer;
    }

    public void setTimer(String timer) {
        preferences = (mCtx).getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("timer", timer);
        editor.apply();
    }

    public String getDevID(int dataCount) {
        devID = getDataWithCount(dataCount, "devID");
        return devID;
    }

    public String getDevName(int dataCount) {
        devName = getDataWithCount(dataCount, "devName");
        return devName;
    }

    public void setDevID(String devID, int dataCount) {
        setDataWithCount(dataCount, "devID", devID);
    }

    public void setDevName(String devName, int dataCount) {
        setDataWithCount(dataCount, "devName", devName);
    }

    public int getDataCount() {
        preferences = (mCtx).getSharedPreferences(fileName, Context.MODE_PRIVATE);
        dataCount = preferences.getInt("count", 0);
        return dataCount;
    }

    public String getAccToken() {
        preferences = (mCtx).getSharedPreferences(fileName, Context.MODE_PRIVATE);
        accToken = preferences.getString("c_token", "NULL");
        return accToken;
    }

    public void setAccToken(String accToken) {
        preferences = (mCtx).getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("c_token", accToken);
        editor.apply();
    }

    public void setDataCount(int dataCount) {
        preferences = (mCtx).getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putInt("count", dataCount);
        editor.apply();
    }
}
