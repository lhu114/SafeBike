package com.safering.safebike.navigation;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by lhu on 2015-10-30.
 */
public class POIs {
    @SerializedName("poi")
    List<POI> poiList;
}
