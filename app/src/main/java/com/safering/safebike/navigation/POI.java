package com.safering.safebike.navigation;

import java.io.Serializable;

/**
 * Created by lhu on 2015-10-30.
 */
public class POI implements Serializable {
    String id = null;
    String name = null;
    String upperAddrName = null;
    String middleAddrName = null;
    String lowerAddrName = null;
    String detailAddrName = null;
    String firstNo = null;
    String secondNo = null;
    double frontLat;
    double frontLon;
    double noorLat;
    double noorLon;

    @Override
    public String toString() {
        return name;
    }

    public double getLatitude() {
        return (frontLat + noorLat) / 2;
    }

    public double getLongitude() {
        return (frontLon + noorLon) / 2;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return upperAddrName + " " + middleAddrName + " " + lowerAddrName;
    }

    public String getDetailAddress() {
        return firstNo + "-" + secondNo;
    }
}
