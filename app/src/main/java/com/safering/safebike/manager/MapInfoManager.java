package com.safering.safebike.manager;

import android.util.Log;

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

        if (instance == null) {
            instance = new MapInfoManager();
        }

        return instance;
    }

    public void setMarkerOptionsInfo(MarkerOptions markerOptions) {
        Log.d("safebike", "MapInfoManager.setMarkerOptionsInfo");

        markerOptionsList.add(markerOptions);
    }

    public ArrayList<MarkerOptions> getMarkerOptionsInfo() {
        Log.d("safebike", "MapInfoManager.getMarkerOptionsInfo");

        return markerOptionsList;
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
            Log.d("safebike", "MapInfoManager.polylineOptionsList.size() > 0");

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

   /* public void setMapInfoMarker(LatLng latLng, Marker marker) {
        Log.d("safebike", "MapInfoManager.setMapInfoMarker");

        mMarkerResolver.put(latLng, marker);
    }

    public Map<LatLng, Marker> getMapInfoMarker() {
        Log.d("safebike", "MapInfoManager.getMapInfoMarker");

        return mMarkerResolver;
    }

    public void setMapInfoPolyline(Polyline polyline) {
        Log.d("safebike", "MapInfoManager.setMapInfoPolyline");

        polylineList.add(polyline);
    }

    public ArrayList<Polyline> getMapInfoPolyline() {
        Log.d("safebike", "MapInfoManager.getMapInfoPolyline");

        return polylineList;
    }

    public void setMapInfoPointLatLngList(LatLng latLng) {
        Log.d("safebike", "MapInfoManager.setMapInfoPointLatLngList");

        mPointLatLngList.add(latLng);
    }

    public ArrayList<LatLng> getMapInfoPointLatLngList() {
        Log.d("safebike", "MapInfoManager.getMapInfoPointLatLngList");

        return mPointLatLngList;
    }*/

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

   /* public void setActivateFindRoute(boolean isActivate) {
        Log.d("safebike", "MapInfoManager.setActivateFindRoute");

        isActivateFindRoute = isActivate;
    }*/

/*    public boolean getActivateFindRoute() {
        Log.d("safebike", "MapInfoManager.getActivateFindRoute");

        return isActivateFindRoute;
    }*/

    public void clearAllMapInfoData() {
        Log.d("safebike", "MapInfoManager.clearAllMapInfoData");

        if(markerOptionsList.size() > 0) {
            Log.d("safebike", "MapInfoManager.clearAllMapInfoData.markerOptionsList.size() > 0");

            markerOptionsList.clear();
        }

        if (polylineOptionsList.size() > 0) {
            Log.d("safebike", "MapInfoManager.clearAllMapInfoData.polylineOptionsList.size() > 0");

            polylineOptionsList.clear();
        }

        updateTextDescription = null;
        updateImageDescription = -1;
    }

    /*public void removeMapInfoMarkerAndPolyline() {
        if (mMarkerResolver != null && mMarkerResolver.size() > 0) {
            Log.d("safebike", "MapInfoManager.removeMapInfoMarkerAndPolyline.mapInfoMarker.remove");

            mMarkerResolver.clear();
        }

        if (polylineList != null && polylineList.size() > 0) {
            Log.d("safebike", "MapInfoManager.removeMapInfoMarkerAndPolyline.mapInfoPolyline.remove");

            polylineList.clear();
        }

        if (mPointLatLngList != null && mPointLatLngList.size() > 0) {
            mPointLatLngList.clear();
        }
    }*/

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
