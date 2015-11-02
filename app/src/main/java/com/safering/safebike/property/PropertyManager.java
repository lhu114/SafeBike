package com.safering.safebike.property;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Tacademy on 2015-10-29.
 */
public class PropertyManager {
    private static PropertyManager instance;

    public static final String USER_EMAIL = "yhms4432";
    public static final String USER_PASSWORD = "hj023285";

    public static final String STARTING_LATITUDE = "startingLatitude";
    public static final String STARTING_LONGITUDE = "startingPointLongitude";
    public static final String DESTINATION_LATITUDE = "destinationLatitude";
    public static final String DESTINATION_LONGITUDE = "destinationLongitude";
    public static final String SERVICE_CONDITION = "SERVICE_CONDITION";

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

    public void setStartingLatitude(String startingLatitude) {
        mEditor.putString(STARTING_LATITUDE, startingLatitude);
        mEditor.commit();
    }

    public String getStartingLatitude() {
         return mPrefs.getString(STARTING_LATITUDE, "");
    }

    public void setStartingLongitude(String startingLongitude) {
        mEditor.putString(STARTING_LONGITUDE, startingLongitude);
        mEditor.commit();
    }

    public String getStartingLongitude() {
        return mPrefs.getString(STARTING_LONGITUDE, "");
    }

    public void setDestinationLatitude(String destinationLatitude) {
        mEditor.putString(DESTINATION_LATITUDE, destinationLatitude);
        mEditor.commit();
    }

    public String getDestinationLatitude() {
        return mPrefs.getString(DESTINATION_LATITUDE, "");
    }

    public void setDestinationLongitude(String destinationLongitude) {
        mEditor.putString(DESTINATION_LONGITUDE, destinationLongitude);
        mEditor.commit();
    }

    public String getDestinationLongitude() {
        return mPrefs.getString(DESTINATION_LONGITUDE, "");
    }

    public void setServiceCondition(String serviceCondition) {
        mEditor.putString(SERVICE_CONDITION, "");
        mEditor.commit();
    }

    public String getServiceCondition() {
        return  mPrefs.getString(SERVICE_CONDITION, "");
    }
}
