package com.alteration.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class UserPreferences {

    //Functions
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context mCtx;
    //Data types
    private boolean isLoggedin, isCloudLogged;
    private String userPin, emailID, password, nickName, loginType;
    private int product_id;
    private String oauth_id;
    private String oauth_secret;
    private ArrayList<String> phoneNumbers = new ArrayList<>();

    public UserPreferences(Context context) {
        this.mCtx = context;
    }

    public UserPreferences() {
    }

    public void removeAllWithLogout() {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
        preferences = (mCtx).getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    public String getUserPin() {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        userPin = preferences.getString("pin", "");
        return userPin;
    }

    public void setUserPin(String userPin) {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("pin", userPin);
        editor.apply();
    }

    public String getOauth_id() {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        oauth_id = preferences.getString("o_id", "");
        return oauth_id;
    }

    public void setOauth_id(String oauth_id) {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("o_id", oauth_id);
        editor.apply();
    }

    public String getOauth_secret() {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        oauth_secret = preferences.getString("o_secret", "");
        return oauth_secret;
    }

    public void setOauth_secret(String oauth_secret) {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("o_secret", oauth_secret);
        editor.apply();
    }

    public ArrayList<String> getPhoneNumbers() {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        Set<String> temp = preferences.getStringSet("phone", new HashSet<String>());
        phoneNumbers = new ArrayList<>(temp);
        return phoneNumbers;
    }

    public void setPhoneNumbers(ArrayList<String> phoneNumbers) {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
        Set<String> temp = new HashSet<>(phoneNumbers);
        editor.putStringSet("phone", temp);
        editor.apply();
    }

    public int getProduct_id() {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        product_id = preferences.getInt("product_id", 0);
        return product_id;
    }

    public void setProduct_id(int product_id) {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putInt("product_id", product_id);
        editor.apply();
    }

    public boolean isLoggedin() {
        preferences = (mCtx).getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        isLoggedin = preferences.getBoolean("islogin", false);
        return isLoggedin;
    }

    public void setLoggedin(boolean loggedin) {
        preferences = (mCtx).getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putBoolean("islogin", loggedin);
        editor.apply();
    }

    public String getemailID() {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        emailID = preferences.getString("emailid", "");
        return emailID;
    }

    public void setemailID(String emailID) {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("emailid", emailID);
        editor.apply();
    }

    public String getPassword() {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        password = preferences.getString("passwd", "");
        return password;
    }

    public void setPassword(String pass) {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("passwd", pass);
        editor.apply();
    }

    public boolean getIsCloudLogged() {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        isCloudLogged = preferences.getBoolean("cloudlogin", false);
        return isCloudLogged;
    }

    public void setIsCloudLogged(boolean isCloudLogged) {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putBoolean("cloudlogin", isCloudLogged);
        editor.apply();
    }

    public String getNickName() {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        nickName = preferences.getString("nickName", "");
        return nickName;
    }

    public void setNickName(String nickName) {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("nickName", nickName);
        editor.apply();
    }

    public String getLoginType() {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        loginType = preferences.getString("logintype", "");
        return loginType;
    }

    public void setLoginType(String loginType) {
        preferences = (mCtx).getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("logintype", loginType);
        editor.apply();
    }
}
