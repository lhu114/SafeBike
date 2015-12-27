package com.safering.safebike.manager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by lhu on 2015-11-30.
 */
public class MapInfoManager {
    private static MapInfoManager instance;

    GoogleMap mMap = null;
    GoogleMap mInitialMap = null;

    ArrayList<MarkerOptions> markerOptionsList = new ArrayList<MarkerOptions>();
    ArrayList<PolylineOptions> polylineOptionsList = new ArrayList<PolylineOptions>();

    String updateTextDescription = null;
    int updateImageDescription = -1;

    public static MapInfoManager getInstance() {
//        Log.d("safebike", "MapInfoManager.getInstance");

        if (instance == null) {
            instance = new MapInfoManager();
        }

        return instance;
    }

    public void setMarkerOptionsInfo(MarkerOptions markerOptions) {
//        Log.d("safebike", "MapInfoManager.setMarkerOptionsInfo");

        markerOptionsList.add(markerOptions);
    }

    public ArrayList<MarkerOptions> getMarkerOptionsInfo() {
//        Log.d("safebike", "MapInfoManager.getMarkerOptionsInfo");

        return markerOptionsList;
    }

    public void setPolylineOptionsInfo(PolylineOptions polylineOptions) {
//        Log.d("safebike", "MapInfoManager.setPolylineOptionsInfo");

        polylineOptionsList.add(polylineOptions);
    }

    public PolylineOptions getPolylineOptionsInfo() {
//        Log.d("safebike", "MapInfoManager.getPolylineOptionsInfo");

        if (polylineOptionsList.size() > 0) {
//            Log.d("safebike", "MapInfoManager.polylineOptionsList.size() > 0");

            return polylineOptionsList.get(0);
        }

        return null;
    }

    public void setUpdateTextDescription(String description) {
        updateTextDescription = description;
    }

    public String getUpdateTextDescription() {
        if (updateTextDescription != null) {
            return updateTextDescription;
        }

        return null;
    }

    public void setUpdateImageDescription(int direction) {
        updateImageDescription = direction;
    }

    public int getUpdateImageDescription() {
        return updateImageDescription;
    }

    public void setMapInfoGoogleMap(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public GoogleMap getMapInfoGoogleMap() {
        if (mMap != null) {
            return mMap;
        }

        return mMap;
    }

    public void setMapInfoInitialGoogleMap(GoogleMap googleMap) {
        mInitialMap = googleMap;
    }

    public GoogleMap getMapInfoInitialGoogleMap() {
        if (mInitialMap != null) {
            return mInitialMap;
        }

        return mInitialMap;
    }

    public void clearAllMapInfoData() {
//        Log.d("safebike", "MapInfoManager.clearAllMapInfoData");

        if(markerOptionsList.size() > 0) {
//            Log.d("safebike", "MapInfoManager.clearAllMapInfoData.markerOptionsList.size() > 0");

            markerOptionsList.clear();
        }

        if (polylineOptionsList.size() > 0) {
//            Log.d("safebike", "MapInfoManager.clearAllMapInfoData.polylineOptionsList.size() > 0");

            polylineOptionsList.clear();
        }

        updateTextDescription = null;
        updateImageDescription = -1;
    }

    public void removeMapInfoGoogleMap() {
        if (mMap != null) {
            mMap.clear();
        }
    }

    public void removeMapInfoInitialGoogleMap() {
        if (mInitialMap != null) {
            mInitialMap.clear();
        }
    }
}
