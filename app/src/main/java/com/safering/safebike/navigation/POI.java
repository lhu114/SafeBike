package com.safering.safebike.navigation;

/**
 * Created by lhu on 2015-10-30.
 */
public class POI {
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
        return middleAddrName + " " + lowerAddrName + " " + detailAddrName;
    }

    public String getDetailAddress() {
        return " " + firstNo + " - " + secondNo;
    }


    //    @Override
//    public String toString() {
//        return name;
//    }
//
//    public double getLatitude() {
//        return (Double.parseDouble(frontLat) + Double.parseDouble(noorLat)) / 2;
//    }
//
//    public double getLongitude() {
//        return (Double.parseDouble(frontLon) + Double.parseDouble(noorLon)) / 2;
//    }
//
//    public String getAddress() {
//        return upperAddrName + " " + middleAddrName + " " + lowerAddrName + " " + detailAddrName;
//    }
//
//    public double getLatitudeL1() {
//        return 0;
//    }
}
