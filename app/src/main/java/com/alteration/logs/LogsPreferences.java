package com.alteration.logs;

import android.content.Context;
import android.content.SharedPreferences;

public class LogsPreferences {

    //Functions
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context mCtx;
    //Data types
    private String nickName, fileName = "log";
    private int logCount;

    public LogsPreferences() {

    }

    public LogsPreferences(Context context) {
        this.mCtx = context;
    }

    public void setNickName(String name, int logCount) {
        String tempFileName = fileName + String.valueOf(logCount);
        preferences = (mCtx).getSharedPreferences(tempFileName, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("nickName", name);
        editor.apply();
    }

    public void setLogCount(int logCount) {
        preferences = (mCtx).getSharedPreferences("logCount", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putInt("count", logCount);
        editor.apply();
    }

    public int getLogCount() {
        preferences = (mCtx).getSharedPreferences("logCount", Context.MODE_PRIVATE);
        logCount = preferences.getInt("count", 0);
        return logCount;
    }

    public String getNickName(int count) {
        String tempFileName = fileName + String.valueOf(count);
        preferences = (mCtx).getSharedPreferences(tempFileName, Context.MODE_PRIVATE);
        nickName = preferences.getString("nickName", "NULL");
        return nickName;
    }

}
