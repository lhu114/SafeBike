package com.safering.safebike.property;

import android.app.Application;
import android.content.Context;

/**
 * Created by Tacademy on 2015-10-29.
 */
public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {

        return mContext;
    }
}
