package com.safering.safebike.property;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Tacademy on 2015-10-29.
 */
public class PropertyManager {
    public static final String USER_ID = "@ID";
    public static final String USER_EMAIL = "@EMAIL";
    public static final String USER_JOIN = "@JOIN";
    public static final String USER_PASSWORD = "@PASS";

    private static PropertyManager instance;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;

    public static PropertyManager getInstance() {
        if (instance == null) {
            instance = new PropertyManager();
        }
        return instance;
    }

    private PropertyManager() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        mEditor = mPrefs.edit();
    }

    public void setUserEmail(String userEmail) {
        mEditor.putString(USER_EMAIL, userEmail);
        mEditor.commit();
    }

    public String getUserEmail() {
        return mPrefs.getString(USER_EMAIL, "");
    }

    public void setUserPassword(String password) {
        mEditor.putString(USER_PASSWORD, password);
        mEditor.commit();
    }

    public String getUserPassword() {
        return mPrefs.getString(USER_PASSWORD, "");
    }

    public void setUserId(String userId) {
        mEditor.putString(USER_ID, userId);
        mEditor.commit();
    }

    public String getUserId() {
        return mPrefs.getString(USER_ID, "");

    }

    public void setUserJoin(String userJoin) {
        mEditor.putString(USER_JOIN, userJoin);
        mEditor.commit();
    }

    public String getUserJoin() {
        return mPrefs.getString(USER_JOIN, "");
    }

    /*public boolean isBackupSync() {
        return mPrefs.getBoolean("perf_sync", false);
    }*/


}
