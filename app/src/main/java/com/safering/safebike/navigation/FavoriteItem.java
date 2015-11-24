package com.safering.safebike.navigation;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lhu on 2015-11-02.
 */
public class FavoriteItem {
//    public Drawable poiIcon;

//    public String uemail;

    @SerializedName("favoritesname")
    public String fvPOIName;

    @SerializedName("favoriteslatitude")
    public String fvPOILatitude;

    @SerializedName("favoriteslongtude")
    public String fvPOILongitude;

//    public String favoritesname;
//    public String favoriteslatitude;
//    public String favoriteslongtude;
}
