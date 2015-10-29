package com.safering.safebike.property;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Tacademy on 2015-10-29.
 */
public class PropertyManager {
    public static final String USER_EMAIL = "yhms4432";
    public static final String USER_PASSWORD = "hj023285";
    private static PropertyManager instance;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;

    public static PropertyManager getInstance(){
        if(instance == null){
            instance = new PropertyManager();
        }
        return instance;
    }
    private PropertyManager() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        mEditor = mPrefs.edit();
    }

    public void setUserEmail(String id) {
        mEditor.putString(USER_EMAIL, id);
        mEditor.commit();
    }

    public String getUserEmail() {
        return mPrefs.getString(USER_EMAIL,"");
    }

    public void setUserPassword(String password) {
        mEditor.putString(USER_PASSWORD, password);
        mEditor.commit();
    }

    public String getUserPassword() {
        return mPrefs.getString(USER_PASSWORD, "");
    }





}
