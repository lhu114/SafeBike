// IRouteCallback.aidl
package com.safering.safebike;

// Declare any non-default types here with import statements

interface IRouteCallback {
    void moveMap(double latitude, double longitude, float bearing, String moveAction);
    void setImageDescription(int direction);
    void setTextDescription(String description, int distance);
    void addPointMarker(double latitude, double longitude);
    void addPolyline();
    void withinRouteLimitDistanceDialog();
    void autoFinishNavigationDialog();
    void clearMarkerAndPolyline();
}
