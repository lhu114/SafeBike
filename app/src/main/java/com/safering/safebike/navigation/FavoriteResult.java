package com.safering.safebike.navigation;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Tacademy on 2015-11-17.
 */
public class FavoriteResult {
    @SerializedName("favoriteslist")
    ArrayList<FavoriteItem> favoriteItemList;
}
