package com.safering.safebike.manager;

import android.util.Log;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by lhu on 2015-11-30.
 */
public class MapInfoManager {
    private static MapInfoManager instance;

    ArrayList<MarkerOptions> markerOptionsList = new ArrayList<MarkerOptions>();
    ArrayList<PolylineOptions> polylineOptionsList = new ArrayList<PolylineOptions>();

    public static MapInfoManager getInstance() {
        Log.d("safebike", "MapInfoManager.getInstance");

        if (instance == null) {
            instance = new MapInfoManager();
        }

        return instance;
    }

    public void setMarkerOptionsInfo(MarkerOptions markerOptions) {
        Log.d("safebike", "MapInfoManager.setMarkerOptionsInfo");

//        if(markerOptionsList.size() > 0) {
//            Log.d("safebike", "MapInfoManager.setMarkerOptionsInfo.markerOptionsList.size() > 0");
//            markerOptionsList.clear();
//        }

        markerOptionsList.add(markerOptions);
    }

    public ArrayList<MarkerOptions> getMarkerOptionsInfo() {
        Log.d("safebike", "MapInfoManager.getMarkerOptionsInfo");

        if (markerOptionsList.size() > 0) {
            Log.d("safebike", "MapInfoManager.getMarkerOptionsInfo.markerOptionsList.size() > 0");

            return markerOptionsList;
        }

        return null;
    }

    public void setPolylineOptionsInfo(PolylineOptions polylineOptions) {
        Log.d("safebike", "MapInfoManager.setPolylineOptionsInfo");

//        if (polylineOptionsList.size() > 0) {
//            Log.d("safebike", "MapInfoManager.setPolylineOptionsInfo.polylineOptionsList.size() > 0");
//
//            polylineOptionsList.clear();
//        }

        polylineOptionsList.add(polylineOptions);
    }

    public PolylineOptions getPolylineOptionsInfo() {
        Log.d("safebike", "MapInfoManager.getPolylineOptionsInfo");

        if (polylineOptionsList.size() > 0) {
            Log.d("safebike", "MapInfoManager.getPolylineOptionsInfo.polylineOptionsList.size() > 0");

            return polylineOptionsList.get(0);
        }

        return null;
    }

    public void mapInfoClearMarkerAndPolyline() {
        Log.d("safebike", "MapInfoManager.mapInfoClearMarkerAndPolyline");
        if(markerOptionsList.size() > 0) {
            Log.d("safebike", "MapInfoManager.mapInfoClearMarkerAndPolyline.markerOptionsList.size() > 0");

            markerOptionsList.clear();
        }

        if (polylineOptionsList.size() > 0) {
            Log.d("safebike", "MapInfoManager.mapInfoClearMarkerAndPolyline.polylineOptionsList.size() > 0");

            polylineOptionsList.clear();
        }
    }
    /*public void clearMarkerAndPolyline() {
        Log.d("safebike", "StartNavigationActivity.clearMarkerAndPolyline");

        for (int i = 0; i < mPointLatLngList.size(); i++) {
            LatLng latLng = mPointLatLngList.get(i);

            if (mMarkerResolver.size() > 0) {
                Marker m = mMarkerResolver.get(latLng);
                String bitmapFlag = mBitmapResolver.get(latLng);

                mMarkerResolver.remove(m);
                mBitmapResolver.remove(bitmapFlag);

                m.remove();
            }
        }

        mPointLatLngList.clear();
        gpIndex = 0;

        for (Polyline line : polylineList) {
            line.remove();
        }

        polylineOptions = new PolylineOptions();
        polylineList.clear();

        if (tempM != null) {
            tempM.remove();
        }

        if (markerOptionsList.size() > 0) {
            markerOptionsList.clear();
        }
    }*/
}
