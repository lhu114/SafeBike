package com.safering.safebike.property;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by Tacademy on 2015-10-29.
 */
public class PropertyManager {

    public static final String USER_ID = "userid";
    public static final String USER_EMAIL = "useremail";
    public static final String USER_JOIN = "userjoin";
    public static final String USER_PASSWORD = "userpassword";
    public static final String USER_IMAGE_PATH = "userimagepa";



    public static final String STARTING_LATITUDE = "startingLatitude";
    public static final String STARTING_LONGITUDE = "startingPointLongitude";
    public static final String DESTINATION_LATITUDE = "destinationLatitude";
    public static final String DESTINATION_LONGITUDE = "destinationLongitude";
    public static final String SERVICE_CONDITION = "SERVICE_CONDITION";

    public static final String RECENT_LATITUDE = "recentlatitude";
    public static final String RECENT_LONGITUDE = "recentlongitude";

    public static final String FIND_ROUTE_SEARCHOPTION = "findroutesearchoption";

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

    public void setUserImagePath(String path){
        mEditor.putString(USER_IMAGE_PATH,path);
        mEditor.commit();
    }

    public String getUserImagePath(){
        return mPrefs.getString(USER_IMAGE_PATH,"");
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
        mEditor.putString(SERVICE_CONDITION, serviceCondition);
        mEditor.commit();
    }

    public String getServiceCondition() {
        return  mPrefs.getString(SERVICE_CONDITION, "");
    }


    public void setRecentLatitude(String recentLatitude) {
        mEditor.putString(RECENT_LATITUDE, recentLatitude);
        mEditor.commit();
    }

    public String getRecentLatitude() {
        return mPrefs.getString(RECENT_LATITUDE, "");
    }

    public void setRecentLongitude(String recentLongitude) {
        mEditor.putString(RECENT_LONGITUDE, recentLongitude);
        mEditor.commit();
    }

    public String getRecentLongitude() {
        return mPrefs.getString(RECENT_LONGITUDE, "");
    }

    public void setFindRouteSearchOption(int searchOption) {
        mEditor.putInt(FIND_ROUTE_SEARCHOPTION, searchOption);
        mEditor.commit();
    }

    public int getFindRouteSearchOption() {
        return mPrefs.getInt(FIND_ROUTE_SEARCHOPTION, 0);
    }

    public String getUserPhoneNumber(){
        TelephonyManager telManager = (TelephonyManager)MyApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNum = telManager.getLine1Number();
        if(phoneNum != null) {
            if (phoneNum.startsWith("+82")) {
                phoneNum = phoneNum.replace("+82", "0");
            }
        }
        return phoneNum;
    }


}
