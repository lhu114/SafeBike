package com.safering.safebike.navigation;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by lhu on 2015-11-02.
 */
public class RecentItem implements Serializable {
    long _id = -1;

    Drawable poiIcon;
    String rctPOIName;
    String searchDate;
    long timeStamp;

}
