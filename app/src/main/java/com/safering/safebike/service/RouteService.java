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
import android.media.AudioManager.OnAudioFocusChangeListener;
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
import com.safering.safebike.navigation.BicycleProperties;
import com.safering.safebike.navigation.BicycleRouteInfo;
import com.safering.safebike.navigation.NavigationNetworkManager;
import com.safering.safebike.navigation.StartNavigationActivity;
import com.safering.safebike.property.PropertyManager;
import com.safering.safebike.property.SpeakVoice;
import com.safering.safebike.setting.BluetoothConnection;

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

    Handler mHandler, mSimulationHandler, mMediaHandler;

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

    enum PlayState {
        IDLE,
        STARTED
    }

    PlayState mState = PlayState.IDLE;

    public RouteService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(DEBUG_TAG, "RouteService.onCreate");

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
            Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.new mGoogleApiClient");

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            createLocationRequest();
        }

        //        mSimulationHandler = new Handler(Looper.getMainLooper());
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
//                    case MESSAGE_INITIAL_LOCATION_TIMEOUT:
//                        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.handleMessage.MESSAGE_INITIAL_LOCATION_TIMEOUT");
//                        Toast.makeText(RouteService.this, "MESSAGE_INITIAL_LOCATION_TIMEOUT", Toast.LENGTH_SHORT).show();
//
//                        break;

                    case MESSAGE_ITERATIVE_LOCATION_TIMEOUT:
                        Log.d(DEBUG_TAG, "RouteService.onCreate.handleMessage.MESSAGE_ITERATIVE_LOCATION_TIMEOUT");
                        Toast.makeText(RouteService.this, "MESSAGE_ITERATIVE_LOCATION_TIMEOUT", Toast.LENGTH_SHORT).show();

                        break;

                    case MESSAGE_REROUTE_NAVIGATION:
                        Log.d(DEBUG_TAG, "RouteService.onCreate.handleMessage.MESSAGE_REROUTE_NAVIGATION");
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
        Log.d(DEBUG_TAG, "RouteService.onBind");

        return mBinder;
    }

    RemoteCallbackList<IRouteCallback> mCallbacks = new RemoteCallbackList<IRouteCallback>();

    IRouteService.Stub mBinder = new IRouteService.Stub() {
        @Override
        public boolean startRouting() throws RemoteException {
            return RouteService.this.startRouting();
        }

        /*@Override
        public boolean simulationStartRouting() throws RemoteException {
            return RouteService.this.simulationStartRouting();
        }*/

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
        Log.d(DEBUG_TAG, "RouteService.onStartCommand");

        if (intent == null) {
            Log.d(DEBUG_TAG, "RouteService.onStartCommand.intent == null");
            if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_RUNNING)) {
                Log.d(DEBUG_TAG, "RouteService.onStartCommand.intent == null.SERVICE_RUNNING");

                restartRouting();
            }
        } else {
            Log.d(DEBUG_TAG, "RouteService.onStartCommand.intent != null");
        }

        if (!mResolvingError) {  // more about this later
            if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
                Log.d(DEBUG_TAG, "StartNavigationActivity.onStart.mGoogleApiClient.connect");

                mGoogleApiClient.connect();

//                mHandler.sendEmptyMessageDelayed(MESSAGE_INITIAL_LOCATION_TIMEOUT, LOCATION_TIMEOUT_INTERVAL);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG, "RouteService.onDestroy");

        /*if (mLM != null) {
            if (Build.VERSION.SDK_INT > 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mLM.removeUpdates(mIterativeListener);

            mHandler.removeMessages(MESSAGE_ITERATIVE_LOCATION_TIMEOUT);
            mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);
        }*/

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();

            mGoogleApiClient.disconnect();

//            Toast.makeText(getContext(), "NavigationFragment.onStop.mGoogleApiClient.disconnect", Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_TAG, "StartNavigationActivity.onStop.mGoogleApiClient.disconnect");
        }

        sendExerciseReport(mSpeedList, mDistanceList);
         /*
             * 시연 후 삭제
             */
//        mSimulationHandler.removeCallbacks(mRouting);

        tts.close();

        MapInfoManager.getInstance().clearAllMapInfoData();
        MapInfoManager.getInstance().removeMapInfoGoogleMap();
        MapInfoManager.getInstance().removeMapInfoInitialGoogleMap();

//        MapInfoManager.getInstance().removeMapInfoMarkerAndPolyline();

        if (wakeLock.isHeld()) {
            Log.d(DEBUG_TAG, "RouteService.onDestroy.wakeLock != null && wakeLock.isHeld().wakeLock.release()");

            wakeLock.release();
        }

        mTelephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);

        mNm.cancel(NOTIFICATION_ID);
//        mNm.cancelAll();
    }

    protected void createLocationRequest() {
        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.createLocationRequest");

        if (mIterativeLocReq == null) {
            mIterativeLocReq = new LocationRequest();
            mIterativeLocReq.setInterval(REQUEST_LOCATION_REQUEST_ITERATIVE_INTERVAL);
            mIterativeLocReq.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
    }

    protected void starIterativeLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mIterativeLocReq, mIterativeListener);
        Log.d(DEBUG_TAG, "StartNavigationActivity.starIterativeLocationUpdates");

        mHandler.sendEmptyMessageDelayed(MESSAGE_ITERATIVE_LOCATION_TIMEOUT, LOCATION_TIMEOUT_INTERVAL);
    }
    /*
     * exception 처리
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mIterativeListener);
        Log.d(DEBUG_TAG, "StartNavigationActivity.stopLocationUpdates");

        mHandler.removeMessages(MESSAGE_ITERATIVE_LOCATION_TIMEOUT);
        mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected");
        starIterativeLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation != null) {
            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.mLocation" + " : " + Double.toString(mLocation.getLatitude()) + ", " + Double.toString(mLocation.getLongitude()));
//            Toast.makeText(this, "StartNavigationActivity.onConnected.mLocation" + " : " + mLocation.getLatitude() + ", " + mLocation.getLongitude(), Toast.LENGTH_SHORT).show();

            PropertyManager.getInstance().setRecentLatitude(Double.toString(mLocation.getLatitude()));
            PropertyManager.getInstance().setRecentLongitude(Double.toString(mLocation.getLongitude()));
            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.setRecentLocation");
        } else {
            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.mLocation null");
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
////        Toast.makeText(getContext(), "NavigationFragment.onConnectionSuspended", Toast.LENGTH_SHORT).show();
        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        }

//        Toast.makeText(getContext(), "NavigationFragment.onConnectionFailed", Toast.LENGTH_SHORT).show();
        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnectionFailed");
    }

    boolean startRouting() {
        if (isInitialServiceRunning) {
            Log.d(DEBUG_TAG, "RouteService.startRouting.isInitialServiceRunning");
            if (mLM != null && mLM.isProviderEnabled(mProvider) && isStartNavigation == true) {
                if (!isCalling) {
                    tts.translate("안내를 시작합니다.");
                }

                sendNotification();

                if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(RouteService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(RouteService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                return;
                }

                Log.d(DEBUG_TAG, "RouteService.startRouting.requestLocationUpdates");

//                mLM.requestLocationUpdates(mProvider, REQUEST_LOCATION_UPDATES_ITERATIVE_INTERVAL, 0, mIterativeListener);
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    starIterativeLocationUpdates();
                }

                if (!wakeLock.isHeld()) {
                    if (wakeLock != null) {
                        Log.d(DEBUG_TAG, "RouteService.startRouting.wakeLock != null && !wakeLock.isHeld().wakeLock.wakeLock.acquire()");

                        wakeLock.acquire();
                    } else if (wakeLock == null) {
                        Log.d(DEBUG_TAG, "RouteService.startRouting.wakeLock == null && !wakeLock.isHeld().wakeLock.wakeLock.acquire()");

//                        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
                        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, WAKE_LOCK_TAG);
                        wakeLock.acquire();
                    }
                }

                startTime = System.currentTimeMillis();
                Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.startTime : " + Long.toString(startTime));

                findRoute();

                isInitialServiceRunning = false;

//                int result = mAudioManager.requestAudioFocus(mFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);


            }

            return true;
        } else {
            return false;
        }
    }

    /*boolean simulationStartRouting() {
        if (isInitialServiceRunning) {
            Log.d(DEBUG_TAG, "RouteService.simulationStartRouting.isInitialServiceRunning");

            tts.translate("안내를 시작합니다.");

            if (!wakeLock.isHeld()) {
                if (wakeLock != null) {
                    Log.d(DEBUG_TAG, "RouteService.startRouting.wakeLock != null && !wakeLock.isHeld().wakeLock.wakeLock.acquire()");

                    wakeLock.acquire();
                } else if (wakeLock == null) {
                    Log.d(DEBUG_TAG, "RouteService.startRouting.wakeLock == null && !wakeLock.isHeld().wakeLock.wakeLock.acquire()");

                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
                    wakeLock.acquire();
                }
            }

            startTime = System.currentTimeMillis();
            Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.startTime : " + Long.toString(startTime));

            findRoute();

            isInitialServiceRunning = false;

            mSimulationHandler.post(mRouting);

            return true;
        } else {

            return false;
        }
    }
*/
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
        Log.d(DEBUG_TAG, "RouteService.restartRouting");

//        Log.d(DEBUG_TAG, "RouteService.restartRouting.isInitialServiceRunning");
        if (mLM != null && mLM.isProviderEnabled(mProvider) && isStartNavigation == true) {
            if (!isCalling) {
                tts.translate("안내를 시작합니다.");
            }

            sendNotification();

            if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(RouteService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(RouteService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                return;
            }

            Log.d(DEBUG_TAG, "RouteService.restartRouting.requestLocationUpdates");

//                mLM.requestLocationUpdates(mProvider, REQUEST_LOCATION_UPDATES_ITERATIVE_INTERVAL, 0, mIterativeListener);
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                starIterativeLocationUpdates();
            }

            if (!wakeLock.isHeld()) {
                if (wakeLock != null) {
                    Log.d(DEBUG_TAG, "RouteService.restartRouting.wakeLock != null && !wakeLock.isHeld().wakeLock.wakeLock.acquire()");

                    wakeLock.acquire();
                } else if (wakeLock == null) {
                    Log.d(DEBUG_TAG, "RouteService.restartRouting.wakeLock == null && !wakeLock.isHeld().wakeLock.wakeLock.acquire()");

//                        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
                    wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, WAKE_LOCK_TAG);
                    wakeLock.acquire();
                }
            }

            startTime = System.currentTimeMillis();
            Log.d(DEBUG_TAG, "RouteService.restartRouting.startTime : " + Long.toString(startTime));

            findRoute();

            isInitialServiceRunning = false;

//                int result = mAudioManager.requestAudioFocus(mFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }
    }

    /*int simulationLatLngIndex = 0;

    Runnable mRouting = new Runnable() {
        @Override
        public void run() {
            *//*
             *  거리에 따라
             *//*
            Log.d(DEBUG_TAG, "RouteService.simulationStartRouting.run");

            mSimulationHandler.postDelayed(mRouting, 1000);

            LatLng simulationLatLng;

            Log.d(DEBUG_TAG, "RouteService.simulationStartRouting.run.mBicycleNaviInfoList.size() - 1: " + Integer.toString(mBicycleNaviInfoList.size() - 1));
            
            if (simulationLatLngIndex < mBicycleNaviInfoList.size()) {
                Log.d(DEBUG_TAG, "RouteService.simulationStartRouting.run.simulationLatLngIndex : " + Integer.toString(simulationLatLngIndex));

                simulationLatLng = mBicycleNaviInfoList.get(simulationLatLngIndex).latLng;

                simulationRouting(simulationLatLng);

                simulationLatLngIndex++;
            } else if (simulationLatLngIndex == mBicycleNaviInfoList.size() && mBicycleNaviInfoList.size() != 0) {
                Log.d(DEBUG_TAG, "RouteService.simulationStartRouting.run.simulationLatLngIndex == mBicycleNaviInfoList.size()");

                autoFinishNavigationDialog();
            }
        }
    };*/

    /*private void simulationRouting(LatLng simulationLatLng) {
        simulationLoc.setLatitude(simulationLatLng.latitude);
        simulationLoc.setLongitude(simulationLatLng.longitude);

        lastLocation.setLatitude(Double.parseDouble(PropertyManager.getInstance().getRecentLatitude()));
        lastLocation.setLongitude(Double.parseDouble(PropertyManager.getInstance().getRecentLongitude()));

        mDistanceList.add(lastLocation.distanceTo(simulationLoc));
        *//*
         * 시연 후 원래대로 복구!!!
         *//*
//        mSpeedList.add(simulationLoc.getSpeed());
        float simulationSpeed = 8;
        mSpeedList.add(simulationSpeed);

        PropertyManager.getInstance().setRecentLatitude(Double.toString(simulationLoc.getLatitude()));
        PropertyManager.getInstance().setRecentLongitude(Double.toHexString(simulationLoc.getLongitude()));

        moveMap(simulationLoc.getLatitude(), simulationLoc.getLongitude(), simulationLoc.getBearing(), ANIMATE_CAMERA);
        Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.moveMap");

        if (mBicycleNaviInfoList.size() > 0) {
            int maxNaviLatLngIndex = mBicycleNaviInfoList.size() - 1;
            Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.maxNaviLatLngIndex : " + maxNaviLatLngIndex);

            clearAllOrthogonalDistanceList();

            Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : " + naviLatLngIndex + "(비교 전 인덱스)");
            if (naviLatLngIndex + 3 <= maxNaviLatLngIndex) {
                Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : 3개 index 비교");
                getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, simulationLoc, naviLatLngIndex);
                getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, simulationLoc, naviLatLngIndex + 1);
                getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 3).latLng, simulationLoc, naviLatLngIndex + 2);

            } else if (naviLatLngIndex + 2 <= maxNaviLatLngIndex && naviLatLngIndex + 3 > maxNaviLatLngIndex) {
                Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : 2개 index 비교");
                getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, simulationLoc, naviLatLngIndex);
                getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, simulationLoc, naviLatLngIndex + 1);

            } else if (naviLatLngIndex + 1 <= maxNaviLatLngIndex && naviLatLngIndex + 2 > maxNaviLatLngIndex) {
                Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : 1개 index 비교");
                getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, simulationLoc, naviLatLngIndex);
            }

            if (mOrthogonalDistanceList.size() > 0) {
                Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.mOrthogonalDistanceList(distance 비교 개수) : " + Integer.toString(mOrthogonalDistanceList.size()));

                mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);

                float minDistance = mOrthogonalDistanceList.get(0);

                for (int i = 0; i < mOrthogonalDistanceList.size(); i++) {
                    if (mOrthogonalDistanceList.get(i) <= minDistance) {
                        minDistance = mOrthogonalDistanceList.get(i);

                                    *//*
                                     *  여기서 거치지 않은 index description 보여주기
                                     *//*
                        int newNaviLatLngIndex = mOrthogonalDistanceResolver.get(minDistance);

                        if (naviLatLngIndex != newNaviLatLngIndex) {
                            for (int j = naviLatLngIndex; j < newNaviLatLngIndex; j++) {
                                Log.d(DEBUG_TAG, "(수선의 발 있는 경우 지나친 인덱스 검색) naviLatLngIndex : " + j + " | newNaviLatLngIndex 까지 : " + newNaviLatLngIndex);

                                getPointInfoNotifications(j);
                                            *//*
                                             *  종료 처리
                                             *//*
                                checkFinishNavigation(j, maxNaviLatLngIndex, simulationLoc);
                            }
                        }

                        naviLatLngIndex = newNaviLatLngIndex;

                    }
                }

                if (minDistance >= LIMIT_DISTANCE) {
                    Toast.makeText(RouteService.this, "경로에서 벗어났습니다. 경로를 재탐색합니다.(Limit Distance)", Toast.LENGTH_SHORT).show();
                    Log.d(DEBUG_TAG, "minDistance >= " + LIMIT_DISTANCE + ": 경로 재탐색");

                    if (!isActivateRouteWithinLimitDistanceNoti) {
                        tts.translate("경로에서 벗어났습니다. 경로를 재탐색합니다.");

                        findRoute();
                    }


//                                Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(경로 재탐색 직전 인덱스) : " + Integer.toString(naviLatLngIndex));
                } else {
                                *//*
                                 *  TextView에 정보 보여주기 처리
                                *//*
                    BicycleNavigationInfo info = mBicycleNaviInfoList.get(naviLatLngIndex);

                    getPointInfoNotifications(naviLatLngIndex);

                    if (info.properties != null) {
//                        Toast.makeText(RouteService.this, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(비교 후 현재 인덱스) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance) + " | description : " + info.properties.description, Toast.LENGTH_SHORT).show();
                        Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(비교 후 현재 인덱스) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance) + " | description : " + info.properties.description);
                    } else {
//                        Toast.makeText(RouteService.this, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(비교 후 현재 인덱스) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance), Toast.LENGTH_SHORT).show();
                        Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(비교 후 현재 인덱스) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance));
                    }
                                *//*
                                 *  종료 처리
                                 *//*
                    checkFinishNavigation(naviLatLngIndex, maxNaviLatLngIndex, simulationLoc);
                }
            } else {
                mHandler.sendEmptyMessageDelayed(MESSAGE_REROUTE_NAVIGATION, REROUTE_NAVIGATION_TIMEOUT_INTERVAL);
                Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.수선의 발 없는 경우");

                clearAllPointDistanceList();
                if (naviLatLngIndex + 3 <= maxNaviLatLngIndex) {
                    Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : 3개 index 비교");
                    getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, simulationLoc, naviLatLngIndex);
                    getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, simulationLoc, naviLatLngIndex + 1);
                    getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 3).latLng, simulationLoc, naviLatLngIndex + 2);

                } else if (naviLatLngIndex + 2 <= maxNaviLatLngIndex && naviLatLngIndex + 3 > maxNaviLatLngIndex) {
                    Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : 2개 index 비교");
                    getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, simulationLoc, naviLatLngIndex);
                    getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, simulationLoc, naviLatLngIndex + 1);

                } else if (naviLatLngIndex + 1 <= maxNaviLatLngIndex && naviLatLngIndex + 2 > maxNaviLatLngIndex) {
                    Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : 1개 index 비교");
                    getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, simulationLoc, naviLatLngIndex);
                }

                if (naviLatLngIndex + 2 <= maxNaviLatLngIndex) {
                    Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex(수선의 발 없는 경우) : 3개 index 비교");

                    getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, simulationLoc, naviLatLngIndex);
                    getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, simulationLoc, naviLatLngIndex + 1);
                    getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, simulationLoc, naviLatLngIndex + 2);
                } else if (naviLatLngIndex + 1 <= maxNaviLatLngIndex && naviLatLngIndex + 2 > maxNaviLatLngIndex) {
                    Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex(수선의 발 없는 경우) : 2개 index 비교");

                    getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, simulationLoc, naviLatLngIndex);
                    getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, simulationLoc, naviLatLngIndex + 1);
                } else if (naviLatLngIndex <= maxNaviLatLngIndex && naviLatLngIndex + 1 > maxNaviLatLngIndex) {
                    Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex(수선의 발 없는 경우) : 1개 index 비교");

                    getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, simulationLoc, naviLatLngIndex);
                }

                if (mPointDistanceList.size() > 0) {
                    mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);

                    float minDistance = mPointDistanceList.get(0);

                    for (int i = 0; i < mPointDistanceList.size(); i++) {
                        if (mPointDistanceList.get(i) <= minDistance) {
                            minDistance = mPointDistanceList.get(i);
                                        *//*
                                         *  여기서 거치지 않은 index description 보여주기
                                         *//*
                            int newNaviLatLngIndex = mPointDistanceResolver.get(minDistance);

                            if (naviLatLngIndex != newNaviLatLngIndex) {
                                for (int j = naviLatLngIndex; j < newNaviLatLngIndex; j++) {
                                    Log.d(DEBUG_TAG, "(수선의 발 없는 경우 지나친 인덱스 검색) naviLatLngIndex : " + j + " | newNaviLatLngIndex 까지 : " + newNaviLatLngIndex);

                                    getPointInfoNotifications(j);
                                                *//*
                                                 *  종료 처리
                                                 *//*
                                    checkFinishNavigation(j, maxNaviLatLngIndex, simulationLoc);
                                }
                            }

                            naviLatLngIndex = newNaviLatLngIndex;
                        }
                    }

                    if (minDistance >= LIMIT_DISTANCE) {
                        Toast.makeText(RouteService.this, "경로에서 벗어났습니다. 경로를 재탐색합니다.(Limit Distance)", Toast.LENGTH_SHORT).show();
                        Log.d(DEBUG_TAG, "minDistance >= " + LIMIT_DISTANCE + ": 경로 재탐색");

                        if (!isActivateRouteWithinLimitDistanceNoti) {
                            tts.translate("경로에서 벗어났습니다. 경로를 재탐색합니다.");

                            findRoute();
                        }

//                                    Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우) : " + Integer.toString(naviLatLngIndex));
                    } else {
                                    *//*
                                     *  TextView에 정보 보여주기 처리
                                     *//*
                        BicycleNavigationInfo info = mBicycleNaviInfoList.get(naviLatLngIndex);

                        getPointInfoNotifications(naviLatLngIndex);

                        if (info.properties != null) {
//                            Toast.makeText(RouteService.this, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우|(비교 후 현재 인덱스)) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance) + " | description : " + info.properties.description, Toast.LENGTH_SHORT).show();
                            Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우|(비교 후 현재 인덱스)) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance) + " | description : " + info.properties.description);
                        } else {
//                            Toast.makeText(RouteService.this, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우|(비교 후 현재 인덱스)) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance), Toast.LENGTH_SHORT).show();
                            Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우|(비교 후 현재 인덱스)) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance));
                        }

                                    *//*
                                     *  종료 처리
                                     *//*
                        checkFinishNavigation(naviLatLngIndex, maxNaviLatLngIndex, simulationLoc);

                    }
                } else {

                    Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex(둘 다 없는 경우)");
                }

//                            Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우) : " + Integer.toString(naviLatLngIndex));
            }
        }
    }*/

    LocationListener mIterativeListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged");

            mHandler.removeMessages(MESSAGE_ITERATIVE_LOCATION_TIMEOUT);

            if (location != null) {
//                Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.location != null");
//                    Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.mIterativeListener.onLocationChanged : " + location.getLatitude() + ", " + location.getLongitude() + " | " + Float.toString(location.getSpeed()), Toast.LENGTH_SHORT).show();
//                Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.location.getLatitude, location.getLongitude : " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));
//                Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.bearing,speed +: " + Float.toString(location.getBearing()) + ", " + Float.toString(location.getSpeed()));

                lastLocation.setLatitude(Double.parseDouble(PropertyManager.getInstance().getRecentLatitude()));
                lastLocation.setLongitude(Double.parseDouble(PropertyManager.getInstance().getRecentLongitude()));

                mDistanceList.add(lastLocation.distanceTo(location));
                mSpeedList.add((lastLocation.distanceTo(location) / (REQUEST_LOCATION_REQUEST_ITERATIVE_INTERVAL / 1000)));

                PropertyManager.getInstance().setRecentLatitude(Double.toString(location.getLatitude()));
                PropertyManager.getInstance().setRecentLongitude(Double.toHexString(location.getLongitude()));

                moveMap(location.getLatitude(), location.getLongitude(), ANIMATE_CAMERA);
//                Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.moveMap");

                if (mBicycleNaviInfoList.size() > 0) {
                    int maxNaviLatLngIndex = mBicycleNaviInfoList.size() - 1;
//                    Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.maxNaviLatLngIndex : " + maxNaviLatLngIndex);

                    clearAllOrthogonalDistanceList();

//                    Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : " + naviLatLngIndex + "(비교 전 인덱스)");
                    if (naviLatLngIndex + 3 <= maxNaviLatLngIndex) {
//                        Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : 3개 index 비교");
                        getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                        getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 1);
                        getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 3).latLng, location, naviLatLngIndex + 2);

                    } else if (naviLatLngIndex + 2 <= maxNaviLatLngIndex && naviLatLngIndex + 3 > maxNaviLatLngIndex) {
//                        Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : 2개 index 비교");
                        getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                        getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 1);

                    } else if (naviLatLngIndex + 1 <= maxNaviLatLngIndex && naviLatLngIndex + 2 > maxNaviLatLngIndex) {
//                        Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : 1개 index 비교");
                        getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                    }

                    if (mOrthogonalDistanceList.size() > 0) {
//                        Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.mOrthogonalDistanceList(distance 비교 개수) : " + Integer.toString(mOrthogonalDistanceList.size()));

                        mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);

                        float minDistance = mOrthogonalDistanceList.get(0);

                        for (int i = 0; i < mOrthogonalDistanceList.size(); i++) {
                            if (mOrthogonalDistanceList.get(i) <= minDistance) {
                                minDistance = mOrthogonalDistanceList.get(i);

                                    /*
                                     *  여기서 거치지 않은 index description 보여주기
                                     */
                                int newNaviLatLngIndex = mOrthogonalDistanceResolver.get(minDistance);

                                if (naviLatLngIndex != newNaviLatLngIndex) {
                                    for (int j = naviLatLngIndex; j < newNaviLatLngIndex; j++) {
//                                        Log.d(DEBUG_TAG, "(수선의 발 있는 경우 지나친 인덱스 검색) naviLatLngIndex : " + j + " | newNaviLatLngIndex 까지 : " + newNaviLatLngIndex);

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
//                            Toast.makeText(RouteService.this, "경로에서 벗어났습니다. 경로를 재탐색합니다.", Toast.LENGTH_SHORT).show();
//                            Log.d(DEBUG_TAG, "minDistance >= " + LIMIT_DISTANCE + ": 경로 재탐색");

                            if (!isActivateRouteWithinLimitDistanceNoti) {
                                Toast.makeText(RouteService.this, "경로에서 벗어났습니다. 경로를 재탐색합니다.", Toast.LENGTH_SHORT).show();

                                /*if (!isCalling) {
                                 tts.translate("경로에서 벗어났습니다. 경로를 재탐색합니다.");
                                }*/

                                findRoute();
                            }


//                                Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(경로 재탐색 직전 인덱스) : " + Integer.toString(naviLatLngIndex));
                        } else {
                                /*
                                 *  TextView에 정보 보여주기 처리
                                */
                            BicycleNavigationInfo info = mBicycleNaviInfoList.get(naviLatLngIndex);

                            getPointInfoNotifications(naviLatLngIndex);

                            if (info.properties != null) {
//                                Toast.makeText(RouteService.this, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(비교 후 현재 인덱스) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance) + " | description : " + info.properties.description, Toast.LENGTH_SHORT).show();
                                Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(비교 후 현재 인덱스) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance) + " | description : " + info.properties.description);
                            } else {
//                                Toast.makeText(RouteService.this, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(비교 후 현재 인덱스) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance), Toast.LENGTH_SHORT).show();
                                Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(비교 후 현재 인덱스) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance));
                            }
                                /*
                                 *  종료 처리
                                 */
                            checkFinishNavigation(naviLatLngIndex, maxNaviLatLngIndex, location);
                        }
                    } else {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_REROUTE_NAVIGATION, REROUTE_NAVIGATION_TIMEOUT_INTERVAL);
//                        Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.수선의 발 없는 경우");

                        clearAllPointDistanceList();
                        if (naviLatLngIndex + 3 <= maxNaviLatLngIndex) {
//                            Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : 3개 index 비교");
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 1);
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 3).latLng, location, naviLatLngIndex + 2);

                        } else if (naviLatLngIndex + 2 <= maxNaviLatLngIndex && naviLatLngIndex + 3 > maxNaviLatLngIndex) {
//                            Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : 2개 index 비교");
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 1);

                        } else if (naviLatLngIndex + 1 <= maxNaviLatLngIndex && naviLatLngIndex + 2 > maxNaviLatLngIndex) {
//                            Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex : 1개 index 비교");
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                        }

                        if (naviLatLngIndex + 2 <= maxNaviLatLngIndex) {
//                            Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex(수선의 발 없는 경우) : 3개 index 비교");

                            getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, location, naviLatLngIndex);
                            getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex + 1);
                            getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 2);
                        } else if (naviLatLngIndex + 1 <= maxNaviLatLngIndex && naviLatLngIndex + 2 > maxNaviLatLngIndex) {
//                            Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex(수선의 발 없는 경우) : 2개 index 비교");

                            getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, location, naviLatLngIndex);
                            getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex + 1);
                        } else if (naviLatLngIndex <= maxNaviLatLngIndex && naviLatLngIndex + 1 > maxNaviLatLngIndex) {
//                            Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex(수선의 발 없는 경우) : 1개 index 비교");

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
//                                            Log.d(DEBUG_TAG, "(수선의 발 없는 경우 지나친 인덱스 검색) naviLatLngIndex : " + j + " | newNaviLatLngIndex 까지 : " + newNaviLatLngIndex);

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
//                                Toast.makeText(RouteService.this, "경로에서 벗어났습니다. 경로를 재탐색합니다.", Toast.LENGTH_SHORT).show();
//                                Log.d(DEBUG_TAG, "minDistance >= " + LIMIT_DISTANCE + ": 경로 재탐색");

                                if (!isActivateRouteWithinLimitDistanceNoti) {
                                    Toast.makeText(RouteService.this, "경로에서 벗어났습니다. 경로를 재탐색합니다.", Toast.LENGTH_SHORT).show();

                                    /*if (!isCalling) {
                                        tts.translate("경로에서 벗어났습니다. 경로를 재탐색합니다.");
                                    }*/

                                    findRoute();
                                }

//                                    Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우) : " + Integer.toString(naviLatLngIndex));
                            } else {
                                    /*
                                     *  TextView에 정보 보여주기 처리
                                     */
                                BicycleNavigationInfo info = mBicycleNaviInfoList.get(naviLatLngIndex);

                                getPointInfoNotifications(naviLatLngIndex);

                                if (info.properties != null) {
//                                    Toast.makeText(RouteService.this, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우|(비교 후 현재 인덱스)) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance) + " | description : " + info.properties.description, Toast.LENGTH_SHORT).show();
                                    Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우|(비교 후 현재 인덱스)) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance) + " | description : " + info.properties.description);
                                } else {
//                                    Toast.makeText(RouteService.this, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우|(비교 후 현재 인덱스)) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance), Toast.LENGTH_SHORT).show();
                                    Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우|(비교 후 현재 인덱스)) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance));
                                }

                                    /*
                                     *  종료 처리
                                     */
                                checkFinishNavigation(naviLatLngIndex, maxNaviLatLngIndex, location);

                            }
                        } else {
                            mHandler.sendEmptyMessageDelayed(MESSAGE_REROUTE_NAVIGATION, REROUTE_NAVIGATION_TIMEOUT_INTERVAL);

                            Log.d(DEBUG_TAG, "RouteService.mIterativeListener.onLocationChanged.naviLatLngIndex(둘 다 없는 경우)");
                        }

//                            Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우) : " + Integer.toString(naviLatLngIndex));
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

    boolean isTemporaryPause = false;
    boolean isTemporaryMute = false;

    OnAudioFocusChangeListener mFocusListener = new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS :
                    Log.d("safebike", "AUDIOFOCUS_LOSS");
                    isTemporaryMute = false;

                    break;

                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK :
                    Log.d("safebike", "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                    mute(true);

                    isTemporaryMute = true;

                    break;

                case AudioManager.AUDIOFOCUS_GAIN :
                    Log.d("safebike", "AUDIOFOCUS_GAIN");
                    if (isTemporaryMute) {
                        mute(false);
                    }

                    break;
            }
        }
    };

    private void mute(boolean isMute) {
        if (isMute) {
            Log.d("safebike", "mute.true");
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
//            mMediaHandler.removeCallbacks(volumeUp);
//            mMediaHandler.post(volumeDown);
        } else {
            Log.d("safebike", "mute.false");
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
//            mMediaHandler.removeCallbacks(volumeDown);
//            mMediaHandler.post(volumeUp);
        }
    }

    float currentVolume = 1.0f;

    Runnable volumeUp = new Runnable() {
        @Override
        public void run() {
            if (currentVolume < 1) {
//                mAudioManager.setStreamVolume();
//                mPlayer.setVolume(currentVolume, currentVolume);
                currentVolume += 0.1f;
                mMediaHandler.postDelayed(this, 100);
            } else {
                currentVolume = 1;
//                mPlayer.setVolume(1, 1);
            }
        }
    };

    Runnable volumeDown = new Runnable() {
        @Override
        public void run() {
            if (currentVolume < 0) {
//                mPlayer.setVolume(currentVolume, currentVolume);
                currentVolume -= 0.1f;
                mMediaHandler.postDelayed(this, 100);
            } else {
                currentVolume = 0;
//                mPlayer.setVolume(0, 0);
            }
        }
    };

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
            Log.d(DEBUG_TAG, "RouteService.getOrthogonalDistance.index : " + Integer.toString(index) + " | distance : " + Float.toString(distance));
        } else {
            distance = -1;
        }

//        Toast.makeText(StartNavigationActivity.this, "findLatitude, findLongitude " + findLatitude + ", " + findLongitude + " | " + distance + " | " + currentLocC.getBearing() + " | " + currentLocC.getSpeed(), Toast.LENGTH_SHORT).show();
    }

    private void getPointDistance(LatLng latLngA, Location currentLocB, int index) {
        float distance = -1;

        pointLoc.setLatitude(latLngA.latitude);
        pointLoc.setLongitude(latLngA.longitude);

        distance = pointLoc.distanceTo(currentLocB);

        if (distance != -1 && distance >= 0) {
            mPointDistanceResolver.put(distance, index);
            mPointDistanceList.add(distance);

            Log.d(DEBUG_TAG, "RouteService.getPointDistance.index : " + Integer.toString(index) + " | distance : " + Float.toString(distance));
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

            Log.d("safebike", "RouteService.checkFinishNavigation.endPointIndex(마지막 인덱스 LatLng 값이 endPoint LatLng 과 같을 때)");
        } else {
            for (int i = 0; i < mBicycleNaviInfoList.size(); i++) {
                if (mBicycleNaviInfoList.get(i).latLng.equals(endPointLatLng)) {
                    info = mBicycleNaviInfoList.get(i);

                    Log.d("safebike", "RouteService.checkFinishNavigation.endPointIndex((마지막 인덱스 LatLng 값이 endPoint LatLng 과 다를 때) : " + Integer.toString(i));
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

        /*if (currentIndex == lastIndex - 1) {
            Log.d(DEBUG_TAG, "RouteService.checkFinishNavigation.currentIndex == lastIndex - 1");
            autoFinishNavigationDialog();
        } else if (currentIndex == lastIndex) {
            Log.d(DEBUG_TAG, "RouteService.checkFinishNavigation.currentIndex == lastIndex");
            autoFinishNavigationDialog();
        } else {
            Log.d(DEBUG_TAG, "RouteService.checkFinishNavigation.else.currentIndex : " + Integer.toString(currentIndex) + " | lastIndex : " + Integer.toString(lastIndex));
        }*/
    }

    private void getPointInfoNotifications(int currentLatLngIndex) {
        Log.d(DEBUG_TAG, "RouteService.getPointInfoNotifications.currentLatLngIndex : " + currentLatLngIndex);

        if (mPointLatLngIndex + 1 < mPointLatLngIndexList.size()) {
            if (currentLatLngIndex == mPointLatLngIndexList.get(mPointLatLngIndex)) {
                Log.d(DEBUG_TAG, "RouteService.getPointInfoNotifications.currentLatLngIndex == mPointLatLngIndexList.get(mPointLatLngIndex)");
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
                    Log.d(DEBUG_TAG, "RouteService.getPointInfoNotifications.currentLatLngIndex == mPointLatLngIndexList.get(mPointLatLngIndex).!isCalling.tts");

                    tts.translate(Integer.toString(pointInfoDistance) + "m 이후 " + pointInfo.properties.description);
                }

                if (pointInfo.properties.turnType == LEFT_SIDE) {
                    setImageDescription(LEFT_SIDE);

                    BluetoothConnection.getInstance().writeLeftValue();
                } else if (pointInfo.properties.turnType == RIGHT_SIDE) {
                    setImageDescription(RIGHT_SIDE);

                    BluetoothConnection.getInstance().writeRightValue();
                } else if (pointInfo.properties.turnType == EIGHT_LEFT_SIDE) {
                    setImageDescription(EIGHT_LEFT_SIDE);

                    BluetoothConnection.getInstance().writeLeftValue();
                } else if (pointInfo.properties.turnType == TEN_LEFT_SIDE) {
                    setImageDescription(TEN_LEFT_SIDE);

                    BluetoothConnection.getInstance().writeLeftValue();
                } else if (pointInfo.properties.turnType == TWO_RIGHT_SIDE) {
                    setImageDescription(TWO_RIGHT_SIDE);

                    BluetoothConnection.getInstance().writeRightValue();
                } else if (pointInfo.properties.turnType == FOUR_RIGHT_SIDE) {
                    setImageDescription(FOUR_RIGHT_SIDE);

                    BluetoothConnection.getInstance().writeRightValue();
                } else {
                    setImageDescription(0);

                    BluetoothConnection.getInstance().writeOffValue();
                }

                setTextDescription(Integer.toString(mPointLatLngIndex) + ". " + pointInfo.properties.description, pointInfoDistance);

//                Log.d(DEBUG_TAG, Integer.toString(distance) + "m 이후 " + pointInfo.properties.description + " | pointLatLngIndex : " + currentLatLngIndex);
            } else if (currentLatLngIndex == (mPointLatLngIndexList.get(mPointLatLngIndex) - 2) && mPointLatLngIndex > 0) {
                Log.d(DEBUG_TAG, "RouteService.getPointInfoNotifications.currentLatLngIndex : " + currentLatLngIndex + " | pointIndex : " + Integer.toString((mPointLatLngIndexList.get(mPointLatLngIndex) - 2)));

                if (pointInfoDistance >= LIMIT_DISTANCE_NOTIFICATION) {
                    Log.d(DEBUG_TAG, "RouteService.getPointInfoNotifications.pointInfoDistance >= 300");

                    if (!isCalling && isFirstRepeatPointInfoNoti) {
                        BicycleNavigationInfo pointInfo;
                        pointInfo = mBicycleNaviInfoList.get(mPointLatLngIndexList.get(mPointLatLngIndex));

                        tts.translate("잠시후 " + pointInfo.properties.description);

                        isFirstRepeatPointInfoNoti = false;
                    }
                } else {
                    Log.d(DEBUG_TAG, "RouteService.getPointInfoNotifications.pointInfoDistance < 300");
                }
            } else {
                Log.d(DEBUG_TAG, "RouteService.getPointInfoNotifications.currentLatLngIndex != mPointLatLngIndexList.get(mPointLatLngIndex)");
            }
        } else if (mPointLatLngIndex + 1 == mPointLatLngIndexList.size()) {
            Log.d(DEBUG_TAG, "RouteService.getPointInfoNotifications.mPointLatLngIndex + 1 == mPointLatLngIndexList.size()");

            BicycleNavigationInfo pointInfo = mBicycleNaviInfoList.get(mPointLatLngIndexList.get(mPointLatLngIndex));

//            Log.d(DEBUG_TAG, pointInfo.properties.description + " | pointLatLngIndex : " + currentLatLngIndex);
        }
    }

    private void sendExerciseReport(ArrayList<Float> speedList, final ArrayList<Float> distanceList) {
        Log.d(DEBUG_TAG, "RouteService.sendExerciseReport");

        if (distanceList.size() > 0) {
            Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.distanceList > 0");

            endTime = System.currentTimeMillis();
            Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.endTime : " + Long.toString(endTime));
            int totalTime = 0;
            float totalSpeed = 0;
            float totalDistance = 0;
//            int second = (int) ((endTime - startTime / 1000) % 60);
            int second = (int) TimeUnit.MILLISECONDS.toSeconds(endTime - startTime);

            Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.second : " + Integer.toString(second));

            if (second >= 0 && second < 60) {
                Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.second.second >= 0 && second < 60 : ");
                totalTime = 1;
            } else if (second >= 60){
                Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.second.second >= 60 : ");
                totalTime = (int) TimeUnit.MILLISECONDS.toMinutes(endTime - startTime);
            }

            for (int i = 0; i < speedList.size(); i++) {
                Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.speedList : " + Float.toString(speedList.get(i)));
                totalSpeed += speedList.get(i);
            }

            for (int i = 0; i < distanceList.size(); i++) {
//                Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.distanceList : " + Float.toString(distanceList.get(i)));
                totalDistance += distanceList.get(i);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();

            final String userEmail = PropertyManager.getInstance().getUserEmail();
            final String date = sdf.format(cal.getTime());
            final int calorie = CalculatorCalorie.getInstance().getCalorie(totalSpeed / speedList.size(), 65, totalTime);
            final int speed = Math.round(totalSpeed / speedList.size());
            final int distance = Math.round(totalDistance);

            Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.userEmail : " + userEmail + " | date : " + date + " | calorie : " + calorie + " | speed : " + speed + " | distance : " + distance + " | time : " + totalTime);
//            Toast.makeText(RouteService.this, "RouteService.sendExerciseReport.userEmail : " + userEmail + " | date : " + date + " | calorie : " + Integer.toString(calorie) + " | speed : " + Integer.toString(speed) + " | distance : " + Integer.toString(distance) + " | time : " + totalTime, Toast.LENGTH_LONG).show();
            NetworkManager.getInstance().saveExercise(RouteService.this, userEmail, date, calorie, speed, distance, new NetworkManager.OnResultListener() {
                @Override
                public void onSuccess(Object result) {
                    Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.saveExercise.onSuccess.result : " + result);

                    if ((int) result == SUCCESS) {
                        Log.d("safebike", "RouteService.removeFavorite.onSuccess.200");
//                        Toast.makeText(RouteService.this, "saveExercise.SUCCESS.200", Toast.LENGTH_SHORT).show();

                        mSpeedList.clear();
                        mDistanceList.clear();

                    } else {
                        Log.d("safebike", "RouteService.removeFavorite.onSuccess.else");
//                        Toast.makeText(RouteService.this, "saveExercise.SUCCESS.200.else", Toast.LENGTH_SHORT).show();
                    }
                    /*
                     *  비정상 종료 처리 시에 기존 데이터(칼로리, 스피드, 거리 리스트 저장해 두었다가 onCreate 에서 저장) bundle 이용
                     *
                     *  또는 비정상 종료 시에 데이터 그냥 서버로 보내버림(이 방법이 좋을듯)
                     */
                }

                @Override
                public void onFail(int code) {
                    Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.saveExercise.onFail.result : " + Integer.toString(code));
//                    Toast.makeText(RouteService.this, "saveExercise.FAIL", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.d(DEBUG_TAG, "RouteService.sendExerciseReport.distanceList < 0");
//            Toast.makeText(RouteService.this, "RouteService.sendExerciseReport.distanceList < 0", Toast.LENGTH_SHORT).show();
        }

    }

    private void findRoute() {
        final double startX = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());
        final double startY = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
        final double endX = Double.parseDouble(PropertyManager.getInstance().getDestinationLongitude());
        final double endY = Double.parseDouble(PropertyManager.getInstance().getDestinationLatitude());
        final int searchOption = PropertyManager.getInstance().getFindRouteSearchOption();

        Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.출발지, 목적지 위도 경도 : " + startY + ", " + startX + " | " + endY + ", " + endX);

        if (startX != 0 && startY != 0 && endX != 0 && endY != 0) {
            NavigationNetworkManager.getInstance().findRoute(RouteService.this, startX, startY, endX, endY, searchOption,
                    new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
                        @Override
                        public void onSuccess(BicycleRouteInfo result) {
                            Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.findRoute.onSuccess");
                            if (result.features != null && result.features.size() > 0) {
//                                MapInfoManager.getInstance().setActivateFindRoute(true);

                                clearMarkerAndPolyline();
                                clearAllmBicycleNaviInfoList();
                                clearAllPointLatLngIndexList();

                                for (BicycleFeature feature : result.features) {
                                    if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_EP)) {
                                        gpIndexSize = (feature.properties.index - 2) / 2;
//                                        Log.d(DEBUG_TAG, "RouteService.findRoute.POINTTYPE_GP.minGPIndexSize : " + Integer.toString(gpIndexSize));
                                    }
                                }

                                polylineOptions = new PolylineOptions();

//                                Log.d(DEBUG_TAG, "result.features.size() : " + result.features.size());

                                for (int i = 0; i < result.features.size(); i++) {
                                    BicycleFeature feature = result.features.get(i);

                                    double[] coordForNavi = feature.geometry.coordinates;

                                    for (int j = 0; j < coordForNavi.length; j += 2) {
                                        float distance = 0;

                                        LatLng latLngA = new LatLng(coordForNavi[j + 1], coordForNavi[j]);
                                        LatLng latLngB;

                                        BicycleProperties properties = feature.properties;

//                                        Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.findRoute.onSuccess.BicycleProperties.mAllPropertiesResolver : " + properties.description);

                                        if (j <= coordForNavi.length - 4) {
//                                            latLngA = new LatLng(coordForNavi[i + 1], coordForNavi[i]);
                                            latLngB = new LatLng(coordForNavi[j + 3], coordForNavi[j + 2]);

                                            locationA.setLatitude(latLngA.latitude);
                                            locationA.setLongitude(latLngA.longitude);

                                            locationB.setLatitude(latLngB.latitude);
                                            locationB.setLongitude(latLngB.longitude);

                                            distance = locationA.distanceTo(locationB);

//                                            Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.findRoute.onSuccess.distance(coordForNavi 인덱스 마지막 아닐 때) : " + mBicycleNaviInfoList.size() + " | " + distance);
                                        } else {
                                            if (i + 1 < result.features.size()) {
//                                                Log.d(DEBUG_TAG, "인덱스 마지막 아닐 때 다음 좌표와 거리 계산");
                                                BicycleFeature tempFeature = result.features.get(i + 1);
                                                double[] tempCoordForNavi = tempFeature.geometry.coordinates;
                                                latLngB = new LatLng(tempCoordForNavi[1], tempCoordForNavi[0]);

                                                locationA.setLatitude(latLngA.latitude);
                                                locationA.setLongitude(latLngA.longitude);

                                                locationB.setLatitude(latLngB.latitude);
                                                locationB.setLongitude(latLngB.longitude);

                                                distance = locationA.distanceTo(locationB);

//                                                Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.findRoute.onSuccess.distance(coordForNavi 인덱스 마지막 아닐 때 다음 좌표와 거리 계산) : " + mBicycleNaviInfoList.size() + " | " + distance);
                                            } else {
                                                distance = 0;

//                                                Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.findRoute.onSuccess.distance(coordForNavi 인덱스 마지막) : " + mBicycleNaviInfoList.size() + " | " + distance);
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

//                                            Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.findRoute.onSuccess.BICYCLE_ROUTE_GEOMETRY_TYPE_POINT | LataLng : " + bicycleNaviInfo.latLng.latitude +
//                                                    ", " + bicycleNaviInfo.latLng.longitude + " | distance : " + bicycleNaviInfo.distance + " | description : " + bicycleNaviInfo.properties.description);

                                            if (mBicycleNaviInfoList.get(mBicycleNaviInfoList.size() - 1).properties.description != null)
//                                                Log.d(DEBUG_TAG, "mBicycleNaviInfoList.size : " + Integer.toString(mBicycleNaviInfoList.size() - 1) + ", description : " + mBicycleNaviInfoList.get(mBicycleNaviInfoList.size() - 1).properties.description);

                                                if (feature.properties.pointType.equals(POINTTYPE_SP)) {
//                                                Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.findRoute.onSuccess.bicycleNaviInfo.properties : " + bicycleNaviInfo.properties.description);

                                                    addPointMarker(latLngA, POINTTYPE_SP, gpIndexSize);
                                                } else if ((feature.properties.pointType.equals(POINTTYPE_GP))) {
//                                                Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.findRoute.onSuccess.bicycleNaviInfo.properties : " + bicycleNaviInfo.properties.description);

                                                    addPointMarker(latLngA, POINTTYPE_GP, gpIndexSize);
                                                } else if (feature.properties.pointType.equals(POINTTYPE_EP)) {
//                                                Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.findRoute.onSuccess.bicycleNaviInfo.properties : " + bicycleNaviInfo.properties.description);

                                                    addPointMarker(latLngA, POINTTYPE_EP, gpIndexSize);

                                                    endPointLatLng = latLngA;
                                                }
                                        } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING)) {
                                            BicycleNavigationInfo bicycleNaviInfo = new BicycleNavigationInfo();
                                            bicycleNaviInfo.latLng = latLngA;
                                            bicycleNaviInfo.distance = distance;
                                            bicycleNaviInfo.properties = null;

                                            mBicycleNaviInfoList.add(bicycleNaviInfo);

//                                            Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.findRoute.onSuccess.BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING | LataLng : " + bicycleNaviInfo.latLng.latitude +
//                                                    ", " + bicycleNaviInfo.latLng.longitude + " | distance : " + bicycleNaviInfo.distance);

//                                            addPolyline(latLngA);
                                            polylineOptions.add(latLngA);
                                        }

                                        Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.findRoute.onSuccess.mBicycleNaviInfoList : " + mBicycleNaviInfoList.size());
                                    }
                                }

                                /*for (int m = 0; m < mBicycleNaviInfoList.size(); m++) {
                                    BicycleNavigationInfo tempInfo = mBicycleNaviInfoList.get(m);
                                    if (tempInfo.properties != null) {
                                        if (tempInfo.properties.description != null) {
                                            Log.d(DEBUG_TAG, "index : " + Integer.toString(m) + ", description : " + tempInfo.properties.description + ", distance : " + tempInfo.distance);
                                        } else {
                                            Log.d(DEBUG_TAG, "index : " + Integer.toString(m) + ", description : null" + ", distance : " + tempInfo.distance);
                                        }
                                    } else {
                                        Log.d(DEBUG_TAG, "index : " + Integer.toString(m) + ", tempInfo.properties == null" + ", distance : " + tempInfo.distance);
                                    }
                                }*/

                                for (int i = 0; i < mBicycleNaviInfoList.size(); i++) {
                                    BicycleNavigationInfo info = mBicycleNaviInfoList.get(i);

                                    if (info.properties != null && info.properties.description != null && !info.properties.description.equals("")) {
                                        mPointLatLngIndexList.add(i);
                                    }
                                }

                                addPolyline(polylineOptions);
                                /*for (int i = 0; i < mPointLatLngIndexList.size(); i++) {
                                    Log.d(DEBUG_TAG, Integer.toString(mPointLatLngIndexList.get(i)));
                                }

                                Log.d(DEBUG_TAG, Integer.toString(mPointLatLngIndexList.size()));

                                if (mBicycleNaviInfoList.size() > 0) {
                                    Log.d(DEBUG_TAG, "RouteService.mInitialListener.onLocationChanged.findRoute.onSuccess.mBicycleNaviInfoList.size : " + mBicycleNaviInfoList.size());
                                }*/
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
        Log.d(DEBUG_TAG, "RouteService.moveMap");

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
        Log.d(DEBUG_TAG, "RouteService.setImageDescription");

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
        Log.d(DEBUG_TAG, "RouteService.setTextDescription");

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
        Log.d(DEBUG_TAG, "RouteService.addPointMarker");

        Log.d(DEBUG_TAG, "StartNavigationActivity.addPointMarker.latitude : " + Double.toString(latLng.latitude) + " | longitude : " + Double.toString(latLng.longitude) +
                " | bitmapFlag : " + bitmapFlag);

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

            Log.d(DEBUG_TAG, "StartNavigationActivity.addPointMarker.BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION.POINTTYPE_GP.index : " + Integer.toString(gpIndex));

            if (gpIndexSize > 0 && gpIndexSize <= 20) {
                Log.d(DEBUG_TAG, "StartNavigationActivity.addPointMarker.BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION.POINTTYPE_GP.index : (gpIndexSize > 0 && gpIndexSize <= 20)");
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

//        MapInfoManager.getInstance().setMapInfoPointLatLngList(latLng);

//        Marker m = mMap.addMarker(markerOptions);
//        mMarkerResolver.put(latLng, m);
//        mBitmapResolver.put(latLng, bitmapFlag);
//        mPointLatLngList.add(latLng);

//             markerOptionsList.add(markerOptions);

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
        Log.d(DEBUG_TAG, "RouteService.addPolyline");

        if (polylineOptions != null) {
            polylineOptions.color(0xba3498db);
            polylineOptions.width(10);
//            polylineOptions.geodesic(true);

//            polyline = mMap.addPolyline(polylineOptions);
//
//            polylineList.add(polyline);

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
        Log.d(DEBUG_TAG, "RouteService.clearMarkerAndPolyline");

        MapInfoManager.getInstance().clearAllMapInfoData();
//        MapInfoManager.getInstance().removeMapInfoMarkerAndPolyline();

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
        Log.d(DEBUG_TAG, "RouteService.withinRouteLimitDistanceDialog");

        if (isFirstFinishDialog) {
            if (!isCalling) {
                tts.translate("목적지에 도착했습니다. 내비게이션 안내를 종료합니다.");
            }

//            sendExerciseReport(mSpeedList, mDistanceList);

            /*if (mLM != null) {
                if (Build.VERSION.SDK_INT > 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

//                Log.d(DEBUG_TAG, "StartNavigationActivity.autoFinishNavigationDialog.removeUpdates.mInitialListener");
                Log.d(DEBUG_TAG, "RouteService.autoFinishNavigationDialog.removeUpdates.mIterativeListener");

                mLM.removeUpdates(mIterativeListener);

                mHandler.removeMessages(MESSAGE_ITERATIVE_LOCATION_TIMEOUT);
                mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);
            }*/

            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                stopLocationUpdates();

                mGoogleApiClient.disconnect();

//            Toast.makeText(getContext(), "NavigationFragment.onStop.mGoogleApiClient.disconnect", Toast.LENGTH_SHORT).show();
                Log.d(DEBUG_TAG, "StartNavigationActivity.onStop.mGoogleApiClient.disconnect");
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

//            stopSelf();
        }
    }

    private void autoFinishNavigationDialog() {
        Log.d(DEBUG_TAG, "RouteService.autoFinishNavigationDialog");

        if (isFirstFinishDialog == true) {
            if (!isCalling) {
                tts.translate("목적지에 도착했습니다. 내비게이션 안내를 종료합니다.");
            }

            /*
             * 시연 후 삭제
             */
//            mSimulationHandler.removeCallbacks(mRouting);

//            sendExerciseReport(mSpeedList, mDistanceList);

            /*if (mLM != null) {
                if (Build.VERSION.SDK_INT > 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

//                Log.d(DEBUG_TAG, "StartNavigationActivity.autoFinishNavigationDialog.removeUpdates.mInitialListener");
                Log.d(DEBUG_TAG, "RouteService.autoFinishNavigationDialog.removeUpdates.mIterativeListener");

                mLM.removeUpdates(mIterativeListener);

                mHandler.removeMessages(MESSAGE_ITERATIVE_LOCATION_TIMEOUT);
                mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);
            }*/

            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                stopLocationUpdates();

                mGoogleApiClient.disconnect();

//            Toast.makeText(getContext(), "NavigationFragment.onStop.mGoogleApiClient.disconnect", Toast.LENGTH_SHORT).show();
                Log.d(DEBUG_TAG, "StartNavigationActivity.onStop.mGoogleApiClient.disconnect");
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

//            stopSelf();
        }
    }

    private void sendNotification() {
        Log.d("safebike", "RouteService.sendNotification");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.drawable.safebike_logo_s);
        builder.setContentTitle("Safe Bike");
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
