package com.safering.safebike.navigation;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lhu on 2015-11-02.
 */
public class FavoriteItem {
    @SerializedName("favoritesname")
    public String fvPOIName;

    @SerializedName("favoriteslatitude")
    public String fvPOILatitude;

    @SerializedName("favoriteslongitude")
    public String fvPOILongitude;
}
