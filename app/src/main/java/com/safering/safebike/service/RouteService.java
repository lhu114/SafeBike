package com.safering.safebike.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.safering.safebike.IRouteCallback;
import com.safering.safebike.IRouteService;
import com.safering.safebike.R;
import com.safering.safebike.exercisereport.CalculatorCalorie;
import com.safering.safebike.manager.MapInfoManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.navigation.BicycleFeature;
import com.safering.safebike.navigation.BicycleNavigationInfo;
import com.safering.safebike.navigation.BicycleRouteInfo;
import com.safering.safebike.navigation.NavigationNetworkManager;
import com.safering.safebike.navigation.StartNavigationActivity;
import com.safering.safebike.property.PropertyManager;
import com.safering.safebike.property.SpeakVoice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RouteService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String DEBUG_TAG = "safebike";
    private static final String WAKE_LOCK_TAG = "routeservice";

    private static final String SERVICE_RUNNING = "running";

    private static final String ANIMATE_CAMERA = "animatecamera";

    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_POINT = "Point";
    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING = "LineString";

    private static final String POINTTYPE_SP = "SP";
    private static final String POINTTYPE_EP = "EP";
    private static final String POINTTYPE_GP = "GP";

    private static final int LEFT_SIDE = 12;
    private static final int RIGHT_SIDE = 13;
    private static final int EIGHT_LEFT_SIDE = 16;
    private static final int TEN_LEFT_SIDE = 17;
    private static final int TWO_RIGHT_SIDE = 18;
    private static final int FOUR_RIGHT_SIDE = 19;

    public static final int MESSAGE_ITERATIVE_LOCATION_TIMEOUT = 2;
    public static final int MESSAGE_REROUTE_NAVIGATION = 3;

    public static final int LOCATION_TIMEOUT_INTERVAL = 60000;
    public static final int REROUTE_NAVIGATION_TIMEOUT_INTERVAL = 15000;
    private static final int REQUEST_LOCATION_REQUEST_ITERATIVE_INTERVAL = 1000;
    private static final float LIMIT_DISTANCE = 30;
    private static final int LIMIT_DISTANCE_NOTIFICATION = 200;

    private static final int ERROR_CODE_ACTIVATE_ROUTE_LIMIT_DISTANCE = 3209;

    private static final int SUCCESS = 200;

    private static final int NOTIFICATION_ID = 1000;

    public SpeakVoice tts;

    private boolean mResolvingError = false;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mIterativeLocReq;
    Location mLocation;
    LocationManager mLM;
    Location orthogonalLoc, pointLoc, locationA, locationB, lastLocation, simulationLoc;
    LatLng endPointLatLng;

    String mProvider;

    MarkerOptions markerOptions;
    PolylineOptions polylineOptions;
    ArrayList<Polyline> polylineList;

    Handler mHandler, mMediaHandler;

    final Map<Float, Integer> mOrthogonalDistanceResolver = new HashMap<Float, Integer>();
    final Map<Float, Integer> mPointDistanceResolver = new HashMap<Float, Integer>();

    int gpIndexSize = 0;
    int naviLatLngIndex = 0;
    int mPointLatLngIndex = 0;

    ArrayList<Float> mOrthogonalDistanceList;
    ArrayList<Float> mPointDistanceList;
    ArrayList<BicycleNavigationInfo> mBicycleNaviInfoList;
    ArrayList<Integer> mPointLatLngIndexList;
    ArrayList<Float> mSpeedList;
    ArrayList<Float> mDistanceList;

    boolean isStartNavigation = true;
    boolean isFirstFinishDialog = true;
    boolean isActivateRouteWithinLimitDistanceNoti = false;
    boolean isInitialServiceRunning = true;
    boolean isActivateWithinRouteLimitDistance = false;
    boolean isActivateAutoFinishNavigation = false;
    boolean isFirstRepeatPointInfoNoti = true;
    boolean isCalling = false;

    long startTime = 0;
    long endTime = 0;

    int gpIndex = 0;
    int pointInfoDistance = 0;

    PowerManager pm;
    PowerManager.WakeLock wakeLock;

    TelephonyManager mTelephonyManager;
    AudioManager mAudioManager;

    CallStateListener callStateListener;

    NotificationManager mNm;


    public RouteService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        polylineList = new ArrayList<Polyline>();
        mOrthogonalDistanceList = new ArrayList<Float>();
        mPointDistanceList = new ArrayList<Float>();
        mBicycleNaviInfoList = new ArrayList<BicycleNavigationInfo>();
        mPointLatLngIndexList = new ArrayList<Integer>();
        mSpeedList = new ArrayList<Float>();
        mDistanceList = new ArrayList<Float>();

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, WAKE_LOCK_TAG);

        tts = new SpeakVoice();

        if (mProvider == null) {
            mProvider = LocationManager.GPS_PROVIDER;
        }

        if (mGoogleApiClient == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            createLocationRequest();
        }

        mMediaHandler = new Handler(Looper.getMainLooper());

        orthogonalLoc = new Location(mProvider);
        pointLoc = new Location(mProvider);
        locationA = new Location(mProvider);
        locationB = new Location(mProvider);
        lastLocation = new Location(mProvider);
        simulationLoc = new Location(mProvider);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MESSAGE_ITERATIVE_LOCATION_TIMEOUT:
                        Toast.makeText(RouteService.this, "LOCATION TIMEOUT", Toast.LENGTH_SHORT).show();

                        break;

                    case MESSAGE_REROUTE_NAVIGATION:
                        Toast.makeText(RouteService.this, "경로에서 벗어났습니다. 경로를 재탐색합니다.(Time Out)", Toast.LENGTH_SHORT).show();

                        findRoute();

                        break;
                }
            }
        };

        callStateListener = new CallStateListener();

        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        mNm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    RemoteCallbackList<IRouteCallback> mCallbacks = new RemoteCallbackList<IRouteCallback>();

    IRouteService.Stub mBinder = new IRouteService.Stub() {
        @Override
        public boolean startRouting() throws RemoteException {
            return RouteService.this.startRouting();
        }

        @Override
        public boolean initialStartRouting() throws RemoteException {
            return RouteService.this.initialStartRouting();
        }

        @Override
        public boolean activateWithinRouteLimitDistance() throws RemoteException {
            return RouteService.this.activateWithinRouteLimitDistance();
        }

        @Override
        public boolean activateAutoFinishNavigation() throws RemoteException {
            return RouteService.this.activateAutoFinishNavigation();
        }

        @Override
        public boolean registerCallback(IRouteCallback callback) throws RemoteException {
            return mCallbacks.register(callback);
        }

        @Override
        public boolean unregisterCallback(IRouteCallback callback) throws RemoteException {
            return mCallbacks.unregister(callback);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_RUNNING)) {

                restartRouting();
            }
        }

        if (!mResolvingError) {  // more about this later
            if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {

                mGoogleApiClient.connect();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();

            mGoogleApiClient.disconnect();

        }

        sendExerciseReport(mSpeedList, mDistanceList);

        tts.close();

        MapInfoManager.getInstance().clearAllMapInfoData();
        MapInfoManager.getInstance().removeMapInfoGoogleMap();
        MapInfoManager.getInstance().removeMapInfoInitialGoogleMap();

        if (wakeLock.isHeld()) {

            wakeLock.release();
        }

        mTelephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);

        mNm.cancel(NOTIFICATION_ID);
    }

    protected void createLocationRequest() {

        if (mIterativeLocReq == null) {
            mIterativeLocReq = new LocationRequest();
            mIterativeLocReq.setInterval(REQUEST_LOCATION_REQUEST_ITERATIVE_INTERVAL);
            mIterativeLocReq.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
    }

    protected void starIterativeLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mIterativeLocReq, mIterativeListener);

        mHandler.sendEmptyMessageDelayed(MESSAGE_ITERATIVE_LOCATION_TIMEOUT, LOCATION_TIMEOUT_INTERVAL);
    }
    /*
     * exception 처리
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mIterativeListener);

        mHandler.removeMessages(MESSAGE_ITERATIVE_LOCATION_TIMEOUT);
        mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);
    }

    @Override
    public void onConnected(Bundle bundle) {
        starIterativeLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation != null) {

            PropertyManager.getInstance().setRecentLatitude(Double.toString(mLocation.getLatitude()));
            PropertyManager.getInstance().setRecentLongitude(Double.toString(mLocation.getLongitude()));
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingError) {
            return;
        }

    }

    boolean startRouting() {
        if (isInitialServiceRunning) {
            if (mLM != null && mLM.isProviderEnabled(mProvider) && isStartNavigation == true) {
                if (!isCalling) {
                    tts.translate("안내를 시작합니다.");
                }

                sendNotification();

                if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(RouteService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(RouteService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }


                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    starIterativeLocationUpdates();
                }

                if (!wakeLock.isHeld()) {
                    if (wakeLock != null) {

                        wakeLock.acquire();
                    } else if (wakeLock == null) {

                        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, WAKE_LOCK_TAG);
                        wakeLock.acquire();
                    }
                }

                startTime = System.currentTimeMillis();

                findRoute();

                isInitialServiceRunning = false;
            }

            return true;
        } else {
            return false;
        }
    }

    boolean initialStartRouting() {
        if (isInitialServiceRunning) {
            return true;
        } else {
            return false;
        }
    }

    boolean activateWithinRouteLimitDistance() {
        if (isActivateWithinRouteLimitDistance) {
            return true;
        } else {
            return false;
        }
    }

    boolean activateAutoFinishNavigation() {
        if (isActivateAutoFinishNavigation) {
            return true;
        } else {
            return false;
        }
    }

    private void restartRouting() {

        if (mLM != null && mLM.isProviderEnabled(mProvider) && isStartNavigation == true) {
            if (!isCalling) {
                tts.translate("안내를 시작합니다.");
            }

            sendNotification();

            if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(RouteService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(RouteService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }


            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                starIterativeLocationUpdates();
            }

            if (!wakeLock.isHeld()) {
                if (wakeLock != null) {

                    wakeLock.acquire();
                } else if (wakeLock == null) {

                    wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, WAKE_LOCK_TAG);
                    wakeLock.acquire();
                }
            }

            startTime = System.currentTimeMillis();

            findRoute();

            isInitialServiceRunning = false;
        }
    }

    LocationListener mIterativeListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mHandler.removeMessages(MESSAGE_ITERATIVE_LOCATION_TIMEOUT);

            if (location != null) {

                lastLocation.setLatitude(Double.parseDouble(PropertyManager.getInstance().getRecentLatitude()));
                lastLocation.setLongitude(Double.parseDouble(PropertyManager.getInstance().getRecentLongitude()));

                mDistanceList.add(lastLocation.distanceTo(location));
                mSpeedList.add((lastLocation.distanceTo(location) / (REQUEST_LOCATION_REQUEST_ITERATIVE_INTERVAL / 1000)));

                PropertyManager.getInstance().setRecentLatitude(Double.toString(location.getLatitude()));
                PropertyManager.getInstance().setRecentLongitude(Double.toHexString(location.getLongitude()));

                //구글 맵 이동
                moveMap(location.getLatitude(), location.getLongitude(), ANIMATE_CAMERA);

                if (mBicycleNaviInfoList.size() > 0) {
                    int maxNaviLatLngIndex = mBicycleNaviInfoList.size() - 1;
                    clearAllOrthogonalDistanceList();

                    if (naviLatLngIndex + 3 <= maxNaviLatLngIndex) {
                        getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                        getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 1);
                        getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 3).latLng, location, naviLatLngIndex + 2);

                    } else if (naviLatLngIndex + 2 <= maxNaviLatLngIndex && naviLatLngIndex + 3 > maxNaviLatLngIndex) {
                        getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                        getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 1);

                    } else if (naviLatLngIndex + 1 <= maxNaviLatLngIndex && naviLatLngIndex + 2 > maxNaviLatLngIndex) {
                        getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                    }

                    if (mOrthogonalDistanceList.size() > 0) {

                        mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);

                        float minDistance = mOrthogonalDistanceList.get(0);

                        for (int i = 0; i < mOrthogonalDistanceList.size(); i++) {
                            if (mOrthogonalDistanceList.get(i) <= minDistance) {
                                minDistance = mOrthogonalDistanceList.get(i);

                                    /*
                                     *  여기서 거치지 않은 index description 보여주기
                                     */
                                //수선의 발까지 거리가 최소인점을 새로운 인덱스로 구하기
                                int newNaviLatLngIndex = mOrthogonalDistanceResolver.get(minDistance);

                                if (naviLatLngIndex != newNaviLatLngIndex) {
                                    //새로운 인덱스와 이전 인덱스가 다를경우
                                    for (int j = naviLatLngIndex; j < newNaviLatLngIndex; j++) {

                                        getPointInfoNotifications(j);
                                            /*
                                             *  종료 처리
                                             */
                                        checkFinishNavigation(j, maxNaviLatLngIndex, location);
                                    }
                                }

                                naviLatLngIndex = newNaviLatLngIndex;

                            }
                        }

                        if (minDistance >= LIMIT_DISTANCE) {

                            if (!isActivateRouteWithinLimitDistanceNoti) {
                                Toast.makeText(RouteService.this, "경로에서 벗어났습니다. 경로를 재탐색합니다.", Toast.LENGTH_SHORT).show();
                                findRoute();
                            }
                        } else {
                                /*
                                 *  TextView에 정보 보여주기 처리
                                */
                            BicycleNavigationInfo info = mBicycleNaviInfoList.get(naviLatLngIndex);

                            getPointInfoNotifications(naviLatLngIndex);


                                /*
                                 *  종료 처리
                                 */
                            checkFinishNavigation(naviLatLngIndex, maxNaviLatLngIndex, location);
                        }
                    } else {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_REROUTE_NAVIGATION, REROUTE_NAVIGATION_TIMEOUT_INTERVAL);

                        clearAllPointDistanceList();
                        if (naviLatLngIndex + 3 <= maxNaviLatLngIndex) {
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 1);
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 3).latLng, location, naviLatLngIndex + 2);

                        } else if (naviLatLngIndex + 2 <= maxNaviLatLngIndex && naviLatLngIndex + 3 > maxNaviLatLngIndex) {
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 1);

                        } else if (naviLatLngIndex + 1 <= maxNaviLatLngIndex && naviLatLngIndex + 2 > maxNaviLatLngIndex) {
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                        }

                        if (naviLatLngIndex + 2 <= maxNaviLatLngIndex) {

                            getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, location, naviLatLngIndex);
                            getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex + 1);
                            getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 2);
                        } else if (naviLatLngIndex + 1 <= maxNaviLatLngIndex && naviLatLngIndex + 2 > maxNaviLatLngIndex) {

                            getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, location, naviLatLngIndex);
                            getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex + 1);
                        } else if (naviLatLngIndex <= maxNaviLatLngIndex && naviLatLngIndex + 1 > maxNaviLatLngIndex) {

                            getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, location, naviLatLngIndex);
                        }

                        if (mPointDistanceList.size() > 0) {
                            mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);

                            float minDistance = mPointDistanceList.get(0);

                            for (int i = 0; i < mPointDistanceList.size(); i++) {
                                if (mPointDistanceList.get(i) <= minDistance) {
                                    minDistance = mPointDistanceList.get(i);
                                        /*
                                         *  여기서 거치지 않은 index description 보여주기
                                         */
                                    int newNaviLatLngIndex = mPointDistanceResolver.get(minDistance);

                                    if (naviLatLngIndex != newNaviLatLngIndex) {
                                        for (int j = naviLatLngIndex; j < newNaviLatLngIndex; j++) {

                                            getPointInfoNotifications(j);
                                                /*
                                                 *  종료 처리
                                                 */
                                            checkFinishNavigation(j, maxNaviLatLngIndex, location);
                                        }
                                    }

                                    naviLatLngIndex = newNaviLatLngIndex;
                                }
                            }

                            if (minDistance >= LIMIT_DISTANCE) {

                                if (!isActivateRouteWithinLimitDistanceNoti) {
                                    Toast.makeText(RouteService.this, "경로에서 벗어났습니다. 경로를 재탐색합니다.", Toast.LENGTH_SHORT).show();

                                    findRoute();
                                }

                            } else {
                                    /*
                                     *  TextView에 정보 보여주기 처리
                                     */
                                BicycleNavigationInfo info = mBicycleNaviInfoList.get(naviLatLngIndex);

                                getPointInfoNotifications(naviLatLngIndex);

                                checkFinishNavigation(naviLatLngIndex, maxNaviLatLngIndex, location);

                            }
                        } else {
                            mHandler.sendEmptyMessageDelayed(MESSAGE_REROUTE_NAVIGATION, REROUTE_NAVIGATION_TIMEOUT_INTERVAL);

                        }

                    }
                }
            }

        }
    };

    private class CallStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                /*
                 * 통화 종료 구현
                 */
                case TelephonyManager.CALL_STATE_IDLE :
                    isCalling = false;

                    break;

                /*
                 * 통화 중 상태 구현
                 */
                case TelephonyManager.CALL_STATE_OFFHOOK :
                    isCalling = true;

                    break;

                /*
                 * 통화 벨 울릴 시 구현
                 */
                case TelephonyManager.CALL_STATE_RINGING :
                    isCalling = true;

                    break;

            }
        }
    }

    private void clearAllmBicycleNaviInfoList() {
        if (mBicycleNaviInfoList.size() > 0) {
            mBicycleNaviInfoList.clear();
            naviLatLngIndex = 0;
        }
    }

    private void clearAllOrthogonalDistanceList() {
        if (mOrthogonalDistanceList.size() > 0) {
            mOrthogonalDistanceList.clear();
            mOrthogonalDistanceResolver.clear();
        }
    }

    private void clearAllPointDistanceList() {
        if (mPointDistanceList.size() > 0) {
            mPointDistanceList.clear();
            mPointDistanceResolver.clear();
        }
    }

    private void clearAllPointLatLngIndexList() {
        if (mPointLatLngIndexList.size() > 0) {
            mPointLatLngIndexList.clear();
            mPointLatLngIndex = 0;
        }
    }

    private void getOrthogonalDistance(LatLng latLngA, LatLng latLngB, Location currentLocC, int index) {
        double findLatitude, findLongitude;
        float distance;

        if (latLngA.latitude == latLngB.latitude) {
            findLatitude = latLngA.latitude;
            findLongitude = currentLocC.getLongitude();
        } else if (latLngA.longitude == latLngB.longitude) {
            findLatitude = currentLocC.getLatitude();
            findLongitude = latLngA.longitude;
        } else {
            double a1 = (latLngB.longitude - latLngA.longitude) / (latLngB.latitude - latLngA.latitude);
            double b1 = (-a1 * latLngA.latitude) + latLngA.longitude;
            double a2 = (-1 / a1);
            double b2 = (-a2 * currentLocC.getLatitude()) + currentLocC.getLongitude();

            findLatitude = (b2 - b1) / (a1 - a2);
            findLongitude = (a1 * findLatitude) + b1;
        }

        orthogonalLoc.setLatitude(findLatitude);
        orthogonalLoc.setLongitude(findLongitude);

        if (checkIncludeLine(latLngA, latLngB, orthogonalLoc)) {
            distance = orthogonalLoc.distanceTo(currentLocC);

            mOrthogonalDistanceResolver.put(distance, index);

            if (distance != -1 && distance >= 0) {
                mOrthogonalDistanceList.add(distance);
            }
        } else {
            distance = -1;
        }
    }

    private void getPointDistance(LatLng latLngA, Location currentLocB, int index) {
        float distance = -1;

        pointLoc.setLatitude(latLngA.latitude);
        pointLoc.setLongitude(latLngA.longitude);

        distance = pointLoc.distanceTo(currentLocB);

        if (distance != -1 && distance >= 0) {
            mPointDistanceResolver.put(distance, index);
            mPointDistanceList.add(distance);

        }

    }

    private boolean checkIncludeLine(LatLng latLngA, LatLng latLngB, Location orthogonalLocC) {
        if (((latLngB.latitude - orthogonalLocC.getLatitude()) * (latLngA.latitude - orthogonalLocC.getLatitude()) <= 0) &&
                ((latLngB.longitude - orthogonalLocC.getLongitude()) * (latLngA.longitude - orthogonalLocC.getLongitude()) <= 0)) {
            return true;
        } else {
            return false;
        }
    }

    private float getBetweenLastLatLngDistance(Location location, BicycleNavigationInfo info) {
        float distance = 0;

        Location lastLocation = new Location(mProvider);
        lastLocation.setLatitude(info.latLng.latitude);
        lastLocation.setLongitude(info.latLng.longitude);

        distance = location.distanceTo(lastLocation);

        return distance;
    }

    private void checkFinishNavigation(int currentIndex, int lastIndex, Location currentLocation) {
        BicycleNavigationInfo info = mBicycleNaviInfoList.get( mBicycleNaviInfoList.size() - 1);

        if (info.latLng.equals(endPointLatLng)) {
            info = mBicycleNaviInfoList.get( mBicycleNaviInfoList.size() - 1);

        } else {
            for (int i = 0; i < mBicycleNaviInfoList.size(); i++) {
                if (mBicycleNaviInfoList.get(i).latLng.equals(endPointLatLng)) {
                    info = mBicycleNaviInfoList.get(i);

                }
            }
        }

        /*
         * 시연 후 원래대로 복구!!!
         */
        if (currentIndex == lastIndex - 2) {
            float distance = getBetweenLastLatLngDistance(currentLocation, info);

            if (distance <= 10) {
                /*
                 *  다이얼로그 보여주면서 종료
                 *  sharedpreferences 값 날리기
                 */
                autoFinishNavigationDialog();
            }
        } else if (currentIndex == lastIndex - 1) {
            float distance = getBetweenLastLatLngDistance(currentLocation, info);

            if (distance <= 10) {
                autoFinishNavigationDialog();
            }
        } else if (currentIndex == lastIndex) {
            autoFinishNavigationDialog();
        }
    }

    private void getPointInfoNotifications(int currentLatLngIndex) {

        if (mPointLatLngIndex + 1 < mPointLatLngIndexList.size()) {
            if (currentLatLngIndex == mPointLatLngIndexList.get(mPointLatLngIndex)) {
                BicycleNavigationInfo pointInfo;

                float tempDistance = 0;
                pointInfoDistance = 0;

                mPointLatLngIndex++;

                isFirstRepeatPointInfoNoti = true;

                for (int i = currentLatLngIndex; i <= mPointLatLngIndexList.get(mPointLatLngIndex); i++) {
                    pointInfo = mBicycleNaviInfoList.get(i);

                    tempDistance += pointInfo.distance;
                }

                pointInfo = mBicycleNaviInfoList.get(mPointLatLngIndexList.get(mPointLatLngIndex));
                pointInfoDistance = Math.round(tempDistance);

                if (!isCalling) {

                    tts.translate(Integer.toString(pointInfoDistance) + "m 이후 " + pointInfo.properties.description);
                }

                if (pointInfo.properties.turnType == LEFT_SIDE) {
                    setImageDescription(LEFT_SIDE);

                } else if (pointInfo.properties.turnType == RIGHT_SIDE) {
                    setImageDescription(RIGHT_SIDE);

                } else if (pointInfo.properties.turnType == EIGHT_LEFT_SIDE) {
                    setImageDescription(EIGHT_LEFT_SIDE);

                } else if (pointInfo.properties.turnType == TEN_LEFT_SIDE) {
                    setImageDescription(TEN_LEFT_SIDE);

                } else if (pointInfo.properties.turnType == TWO_RIGHT_SIDE) {
                    setImageDescription(TWO_RIGHT_SIDE);

                } else if (pointInfo.properties.turnType == FOUR_RIGHT_SIDE) {
                    setImageDescription(FOUR_RIGHT_SIDE);

                } else {
                    setImageDescription(0);

                }

                setTextDescription(Integer.toString(mPointLatLngIndex) + ". " + pointInfo.properties.description, pointInfoDistance);

            } else if (currentLatLngIndex == (mPointLatLngIndexList.get(mPointLatLngIndex) - 2) && mPointLatLngIndex > 0) {

                if (pointInfoDistance >= LIMIT_DISTANCE_NOTIFICATION) {

                    if (!isCalling && isFirstRepeatPointInfoNoti) {
                        BicycleNavigationInfo pointInfo;
                        pointInfo = mBicycleNaviInfoList.get(mPointLatLngIndexList.get(mPointLatLngIndex));

                        tts.translate("잠시후 " + pointInfo.properties.description);

                        isFirstRepeatPointInfoNoti = false;
                    }
                } else {
                }
            } else {
            }
        } else if (mPointLatLngIndex + 1 == mPointLatLngIndexList.size()) {

            BicycleNavigationInfo pointInfo = mBicycleNaviInfoList.get(mPointLatLngIndexList.get(mPointLatLngIndex));

        }
    }

    private void sendExerciseReport(ArrayList<Float> speedList, final ArrayList<Float> distanceList) {

        if (distanceList.size() > 0) {

            endTime = System.currentTimeMillis();
            int totalTime = 0;
            float totalSpeed = 0;
            float totalDistance = 0;
//            int second = (int) ((endTime - startTime / 1000) % 60);

            int second = (int) TimeUnit.MILLISECONDS.toSeconds(endTime - startTime);


            if (second >= 0 && second < 60) {
                totalTime = 1;
            } else if (second >= 60){
                totalTime = (int) TimeUnit.MILLISECONDS.toMinutes(endTime - startTime);
            }

            for (int i = 0; i < speedList.size(); i++) {
                totalSpeed += speedList.get(i);
            }

            for (int i = 0; i < distanceList.size(); i++) {
                totalDistance += distanceList.get(i);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();

            final String userEmail = PropertyManager.getInstance().getUserEmail();
            final String date = sdf.format(cal.getTime());
            final int calorie = CalculatorCalorie.getInstance().getCalorie(totalSpeed / speedList.size(), 65, totalTime);
            final int speed = Math.round(totalSpeed / speedList.size());
            final int distance = Math.round(totalDistance);
            Log.i("final speed",speed+"");
            if(speed > 0) {

                NetworkManager.getInstance().saveExercise(RouteService.this, userEmail, date, calorie, speed, distance, new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object result) {

                        if ((int) result == SUCCESS) {

                            mSpeedList.clear();
                            mDistanceList.clear();

                        }
                    /*
                     *  비정상 종료 처리 시에 기존 데이터(칼로리, 스피드, 거리 리스트 저장해 두었다가 onCreate 에서 저장) bundle 이용
                     *
                     *  또는 비정상 종료 시에 데이터 그냥 서버로 보내버림(이 방법이 좋을듯)
                     */
                    }

                    @Override
                    public void onFail(int code) {
                    }
                });
            }
     }

    }

    private void findRoute() {
        final double startX = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());
        final double startY = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
        final double endX = Double.parseDouble(PropertyManager.getInstance().getDestinationLongitude());
        final double endY = Double.parseDouble(PropertyManager.getInstance().getDestinationLatitude());
        final int searchOption = PropertyManager.getInstance().getFindRouteSearchOption();


        if (startX != 0 && startY != 0 && endX != 0 && endY != 0) {
            NavigationNetworkManager.getInstance().findRoute(RouteService.this, startX, startY, endX, endY, searchOption,
                    new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
                        @Override
                        public void onSuccess(BicycleRouteInfo result) {
                            if (result.features != null && result.features.size() > 0) {
                                clearMarkerAndPolyline();
                                clearAllmBicycleNaviInfoList();
                                clearAllPointLatLngIndexList();

                                for (BicycleFeature feature : result.features) {
                                    if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_EP)) {
                                        gpIndexSize = (feature.properties.index - 2) / 2;
                                    }
                                }

                                polylineOptions = new PolylineOptions();


                                for (int i = 0; i < result.features.size(); i++) {
                                    BicycleFeature feature = result.features.get(i);

                                    double[] coordForNavi = feature.geometry.coordinates;

                                    for (int j = 0; j < coordForNavi.length; j += 2) {
                                        float distance = 0;

                                        LatLng latLngA = new LatLng(coordForNavi[j + 1], coordForNavi[j]);
                                        LatLng latLngB;



                                        if (j <= coordForNavi.length - 4) {
                                            latLngB = new LatLng(coordForNavi[j + 3], coordForNavi[j + 2]);

                                            locationA.setLatitude(latLngA.latitude);
                                            locationA.setLongitude(latLngA.longitude);

                                            locationB.setLatitude(latLngB.latitude);
                                            locationB.setLongitude(latLngB.longitude);

                                            distance = locationA.distanceTo(locationB);

                                        } else {
                                            if (i + 1 < result.features.size()) {
                                                BicycleFeature tempFeature = result.features.get(i + 1);
                                                double[] tempCoordForNavi = tempFeature.geometry.coordinates;
                                                latLngB = new LatLng(tempCoordForNavi[1], tempCoordForNavi[0]);

                                                locationA.setLatitude(latLngA.latitude);
                                                locationA.setLongitude(latLngA.longitude);

                                                locationB.setLatitude(latLngB.latitude);
                                                locationB.setLongitude(latLngB.longitude);

                                                distance = locationA.distanceTo(locationB);

                                            } else {
                                                distance = 0;

                                            }
                                        }

                                        /*
                                         *  !feature.properties.pointType.equals(POINTTYPE_ST)
                                         *  목적지 도착 이후에 종종 pointType이 ST인 좌표가 하나 더 찍히기 때문에 제외시킴
                                         */
                                        if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT)) {
                                            BicycleNavigationInfo bicycleNaviInfo = new BicycleNavigationInfo();
                                            bicycleNaviInfo.latLng = latLngA;
                                            bicycleNaviInfo.properties = feature.properties;
                                            bicycleNaviInfo.distance = distance;

                                            mBicycleNaviInfoList.add(bicycleNaviInfo);


                                            if(mBicycleNaviInfoList.get(mBicycleNaviInfoList.size() - 1).properties.description != null)

                                                if (feature.properties.pointType.equals(POINTTYPE_SP)) {

                                                    addPointMarker(latLngA, POINTTYPE_SP, gpIndexSize);
                                                } else if ((feature.properties.pointType.equals(POINTTYPE_GP))) {

                                                    addPointMarker(latLngA, POINTTYPE_GP, gpIndexSize);
                                                } else if (feature.properties.pointType.equals(POINTTYPE_EP)) {

                                                    addPointMarker(latLngA, POINTTYPE_EP, gpIndexSize);

                                                    endPointLatLng = latLngA;
                                                }
                                        } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING)) {
                                            BicycleNavigationInfo bicycleNaviInfo = new BicycleNavigationInfo();
                                            bicycleNaviInfo.latLng = latLngA;
                                            bicycleNaviInfo.distance = distance;
                                            bicycleNaviInfo.properties = null;

                                            mBicycleNaviInfoList.add(bicycleNaviInfo);


                                            polylineOptions.add(latLngA);
                                        }

                                    }
                                }

                                for (int i = 0; i < mBicycleNaviInfoList.size(); i++) {
                                    BicycleNavigationInfo info = mBicycleNaviInfoList.get(i);

                                    if (info.properties != null && info.properties.description != null && !info.properties.description.equals("")) {
                                        mPointLatLngIndexList.add(i);
                                    }
                                }

                                addPolyline(polylineOptions);
                            }
                        }

                        @Override
                        public void onFail(int code) {
                            if (code == ERROR_CODE_ACTIVATE_ROUTE_LIMIT_DISTANCE) {
                                isActivateRouteWithinLimitDistanceNoti = true;

                                withinRouteLimitDistanceDialog();
                            }
                        }
                    });
        }
    }

    private void moveMap(double latitude, double longitude, String moveAction) {

        int count = mCallbacks.beginBroadcast();

        for (int i = 0; i < count; i++) {
            IRouteCallback callback = mCallbacks.getBroadcastItem(i);
            try {
                callback.moveMap(latitude, longitude, moveAction);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        mCallbacks.finishBroadcast();
    }

    private void setImageDescription(int direction) {

        int count = mCallbacks.beginBroadcast();

        for (int i = 0; i < count; i++) {
            IRouteCallback callback = mCallbacks.getBroadcastItem(i);
            try {
                callback.setImageDescription(direction);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        mCallbacks.finishBroadcast();

        MapInfoManager.getInstance().setUpdateImageDescription(direction);
    }

    private void setTextDescription(String description, int distance) {

        int count = mCallbacks.beginBroadcast();

        for (int i = 0; i < count; i++) {
            IRouteCallback callback = mCallbacks.getBroadcastItem(i);
            try {
                callback.setTextDescription(description, distance);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        mCallbacks.finishBroadcast();

        if (description != null && !description.equals("")) {
            MapInfoManager.getInstance().setUpdateTextDescription(description);
        }
    }

    private void addPointMarker(LatLng latLng, String bitmapFlag, int gpIndexSize) {


        if (bitmapFlag.equals(POINTTYPE_SP) || (bitmapFlag.equals(POINTTYPE_EP))) {
            markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.anchor(0.5f, 1.0f);
            markerOptions.draggable(false);

            if (bitmapFlag.equals(POINTTYPE_SP)) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.start));
            } else if (bitmapFlag.equals(POINTTYPE_EP)) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrival));
            }
        } else if (bitmapFlag.equals(POINTTYPE_GP)) {
            gpIndex++;


            if (gpIndexSize > 0 && gpIndexSize <= 20) {
                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.anchor(0.5f, 1.0f);
                markerOptions.draggable(false);

                addGPPointMarker(gpIndex);
            }
        }

        if (markerOptions != null) {
            MapInfoManager.getInstance().setMarkerOptionsInfo(markerOptions);
        }

        int count = mCallbacks.beginBroadcast();

        for (int i = 0; i < count; i++) {
            IRouteCallback callback = mCallbacks.getBroadcastItem(i);
            try {
                callback.addPointMarker(latLng.latitude, latLng.longitude);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        mCallbacks.finishBroadcast();
    }

    private void addPolyline(PolylineOptions polylineOptions) {

        if (polylineOptions != null) {
            polylineOptions.color(0xba3498db);
            polylineOptions.width(10);

            MapInfoManager.getInstance().setPolylineOptionsInfo(polylineOptions);
        }

        int count = mCallbacks.beginBroadcast();

        for (int i = 0; i < count; i++) {
            IRouteCallback callback = mCallbacks.getBroadcastItem(i);
            try {
                callback.addPolyline();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        mCallbacks.finishBroadcast();
    }

    private void addGPPointMarker(int index) {
        if (index == 1) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_1));
        } else if (index == 2) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_2));
        } else if (index == 3) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_3));
        } else if (index == 4) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_4));
        } else if (index == 5) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_5));
        } else if (index == 6) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_6));
        } else if (index == 7) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_7));
        } else if (index == 8) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_8));
        } else if (index == 9) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_9));
        } else if (index == 10) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_10));
        } else if (index == 11) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_11));
        } else if (index == 12) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_12));
        } else if (index == 13) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_13));
        } else if (index == 14) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_14));
        } else if (index == 15) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_15));
        } else if (index == 16) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_16));
        } else if (index == 17) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_17));
        } else if (index == 18) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_18));
        } else if (index == 19) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_19));
        } else if (index == 20) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_20));
        }
    }

    private void clearMarkerAndPolyline() {

        MapInfoManager.getInstance().clearAllMapInfoData();

        gpIndex = 0;

        int count = mCallbacks.beginBroadcast();

        for (int i = 0; i < count; i++) {
            IRouteCallback callback = mCallbacks.getBroadcastItem(i);
            try {
                callback.clearMarkerAndPolyline();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        mCallbacks.finishBroadcast();
    }

    private void withinRouteLimitDistanceDialog() {

        if (isFirstFinishDialog) {
            if (!isCalling) {
                tts.translate("목적지에 도착했습니다. 내비게이션 안내를 종료합니다.");
            }

            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                stopLocationUpdates();

                mGoogleApiClient.disconnect();

            }

            int count = mCallbacks.beginBroadcast();

            for (int i = 0; i < count; i++) {
                IRouteCallback callback = mCallbacks.getBroadcastItem(i);
                try {
                    callback.withinRouteLimitDistanceDialog();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            isActivateWithinRouteLimitDistance = true;
            isStartNavigation = false;
            isFirstFinishDialog = false;
            isInitialServiceRunning = false;
        }
    }

    private void autoFinishNavigationDialog() {

        if (isFirstFinishDialog == true) {
            if (!isCalling) {
                tts.translate("목적지에 도착했습니다. 내비게이션 안내를 종료합니다.");
            }

            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                stopLocationUpdates();

                mGoogleApiClient.disconnect();

            }

            int count = mCallbacks.beginBroadcast();

            for (int i = 0; i < count; i++) {
                IRouteCallback callback = mCallbacks.getBroadcastItem(i);
                try {
                    callback.autoFinishNavigationDialog();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            isActivateAutoFinishNavigation = true;
            isStartNavigation = false;
            isFirstFinishDialog = false;
            isInitialServiceRunning = false;
        }
    }

    private void sendNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.drawable.noti);
        builder.setContentTitle("Safe Riding");
        builder.setContentText("자전거 내비게이션 동작 중...");
        builder.setAutoCancel(false);

        Intent intent = new Intent(RouteService.this, StartNavigationActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        builder.setContentIntent(pi);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        mNm.notify(NOTIFICATION_ID, notification);
    }
}
