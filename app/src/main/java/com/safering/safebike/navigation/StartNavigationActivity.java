package com.safering.safebike.navigation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.property.PropertyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class StartNavigationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String DEBUG_TAG = "safebike";

    private static final String SERVICE_FINISH = "finish";

    private static final String KEY_POP_NAVIGATION_FRAGMENT = "popNavigation";
    private static final String VALUE_POP_NAVIGATION_FRAGMENT = "popNavigation";
    private static final String KEY_REPLACE_MAIN_FRAGMENT = "replaceMainFragment";
    private static final String VALUE_REPLACE_MAIN_FRAGMENT = "replaceMainFragment";

    private static final String MOVE_CAMERA = "movecamera";
    private static final String ANIMATE_CAMERA = "animatecamera";

    private static final int BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION = 3;
    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_POINT = "Point";
    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING = "LineString";

    private static final String POINTTYPE_SP = "SP";
    private static final String POINTTYPE_EP = "EP";
    private static final String POINTTYPE_GP = "GP";
    private static final String POINTTYPE_ST = "ST";

    public static final int MESSAGE_INITIAL_LOCATION_TIMEOUT = 1;
    public static final int MESSAGE_ITERATIVE_LOCATION_TIMEOUT = 2;
    public static final int MESSAGE_REROUTE_NAVIGATION = 3;

    public static final int LOCATION_TIMEOUT_INTERVAL = 60000;
    public static final int REROUTE_NAVIGATION_TIMEOUT_INTERVAL = 15000;

    private static final float LIMIT_DISTANCE = 20;

    private GoogleMap mMap;
    LocationManager mLM;
    Location orthogonalLoc, pointLoc, locationA, locationB;

    String mProvider;
    Handler mHandler;

    Polyline polyline;
    PolylineOptions options;
    ArrayList<Polyline> polylineList;

    final Map<LatLng, Marker> mMarkerResolver = new HashMap<LatLng, Marker>();
    final Map<LatLng, String> mBitmapResolver = new HashMap<LatLng, String>();
    final Map<LatLng, BicycleProperties> mPropertiesResolver = new HashMap<LatLng, BicycleProperties>();
    final Map<Float, Integer> mOrthogonalDistanceResolver = new HashMap<Float, Integer>();
    final Map<Float, Integer> mPointDistanceResolver = new HashMap<Float, Integer>();

    ArrayList<LatLng> mPointLatLngList;
    ArrayList<Float> mOrthogonalDistanceList;
    ArrayList<Float> mPointDistanceList;
    ArrayList<BicycleNavigationInfo> mBicycleNaviInfoList;

    int gpIndex = 0;
    int naviLatLngIndex = 0;

    TextView tvNaviDescription;

    boolean isStartNavigation = true;
    boolean isFirstFinishDialog = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate");
        setContentView(R.layout.activity_start_navigation);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvNaviDescription = (TextView) findViewById(R.id.text_navi_description);

        Button btnFullScreen = (Button) findViewById(R.id.btn_full_screen);
        btnFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionBar actionBar = getSupportActionBar();

                if (actionBar.isShowing()) {
                    actionBar.hide();
                } else {
                    actionBar.show();
                }
            }
        });

        polylineList = new ArrayList<Polyline>();
        mPointLatLngList = new ArrayList<LatLng>();
        mOrthogonalDistanceList = new ArrayList<Float>();
        mPointDistanceList = new ArrayList<Float>();
        mBicycleNaviInfoList = new ArrayList<BicycleNavigationInfo>();

        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

//        Criteria criteria = new Criteria();
//        /*
//         * criteria 설정
//         */
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
////        criteria.setPowerRequirement(Criteria.POWER_HIGH);
//        criteria.setBearingRequired(true);
//        criteria.setSpeedRequired(true);
//        criteria.setCostAllowed(true);
//        mProvider = mLM.getProvider();

        if (mProvider == null) {
            mProvider = LocationManager.GPS_PROVIDER;
        }

        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.mProvider : " + mProvider);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MESSAGE_INITIAL_LOCATION_TIMEOUT :
                        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.handleMessage.MESSAGE_INITIAL_LOCATION_TIMEOUT");
                        Toast.makeText(StartNavigationActivity.this, "MESSAGE_INITIAL_LOCATION_TIMEOUT", Toast.LENGTH_SHORT).show();

                        break;

                    case MESSAGE_ITERATIVE_LOCATION_TIMEOUT :
                        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.handleMessage.MESSAGE_ITERATIVE_LOCATION_TIMEOUT");
                        Toast.makeText(StartNavigationActivity.this, "MESSAGE_INITIAL_LOCATION_TIMEOUT", Toast.LENGTH_SHORT).show();

                        break;

                    case MESSAGE_REROUTE_NAVIGATION :
                        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.handleMessage.MESSAGE_REROUTE_NAVIGATION");
                        Toast.makeText(StartNavigationActivity.this, "경로에서 벗어났습니다. 경로를 재탐색합니다.(Time Out)", Toast.LENGTH_SHORT).show();

                        findRoute();

                        break;
                }
            }
        };
//        mSM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        mRotationSensor = mSM.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

//        Button btn = (Button) findViewById(R.id.btn_finish_navigation);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                /*
//                 * 운동 기록 처리, 자동으로 안내를 종료할지에 대한 시나리오, 사용자 직접 종료 또는 자동 종료에 따른 운동 기록 값 전달
//                 */
//
//                /*
//                 * 다이얼로그로 종료시 처리
//                 */
//            }
//        });

        orthogonalLoc = new Location(mProvider);
        pointLoc = new Location(mProvider);

        locationA = new Location(mProvider);
        locationB = new Location(mProvider);

//        checkOrthogonalPoint(-10, 0, 0, 10, 9,-10);
    }

    /*
     * 안내종료 버튼 처리
     */
    public void onFinishNavigationBtn(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("내비게이션 안내종료");
        builder.setMessage("현재 내비게이션 안내 중입니다. 정말로 종료하시겠습니까");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 /*
                 *  목적지 위도, 경도, searchoption 날리기
                 */
                PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                PropertyManager.getInstance().setDestinationLatitude(null);
                PropertyManager.getInstance().setDestinationLongitude(null);
                PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

//                Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onCreate : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
                intent.putExtra(KEY_REPLACE_MAIN_FRAGMENT, VALUE_REPLACE_MAIN_FRAGMENT);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
//        builder.setCancelable(false);

        builder.create().show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onNewIntent", Toast.LENGTH_SHORT).show();
    }

    boolean isFirst = true;

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG, "StartNavigationActivity.onStart");

        if (!mLM.isProviderEnabled(mProvider)) {
            if (isFirst) {
                Log.d(DEBUG_TAG, "StartNavigationActivity.!mLM.isProviderEnabled(mProvider).isFirst");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

                isFirst = false;

                Toast.makeText(StartNavigationActivity.this, "GPS를 설정해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(DEBUG_TAG, "StartNavigationActivity.!mLM.isProviderEnabled(mProvider).!isFirst");
                /*
                 * 확인 후 처리
                 */
                Toast.makeText(StartNavigationActivity.this, "GPS 설정이 필요합니다.", Toast.LENGTH_SHORT).show();

                /*
                 *  sharedPreferences 값 다 날리는 처리
                 */
                PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);
//                PropertyManager.getInstance().setDestinationLatitude(null);
//                PropertyManager.getInstance().setDestinationLongitude(null);
//                PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

                finish();
            }

            return;
        } else if (mLM != null && mLM.isProviderEnabled(mProvider) && isStartNavigation == true) {
            if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(StartNavigationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(StartNavigationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            Log.d(DEBUG_TAG, "StartNavigationActivity.onStart.requestSingleUpdate");
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onStart.requestLocationUpdates");
            mLM.requestSingleUpdate(mProvider, mInitialListener, null);
//            mLM.requestLocationUpdates(mProvider, 2000, 0, mIterativeListener);

            mHandler.sendEmptyMessageDelayed(MESSAGE_INITIAL_LOCATION_TIMEOUT, LOCATION_TIMEOUT_INTERVAL);

            /*
             *  TextView 처음으로 set
             */

            tvNaviDescription.setText("길안내");
            /*
             * GPS_PROVIDER 에 해당하는 마지막 위치만 가져오기 때문에 이전 상태에서 GPS가 아닌 Network로 마지막 위치 읽었을 때 정보 가져오지 못하므로 사용하지 말아야 하나...
             */
//            mRecentLocation = mLM.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//            if (mRecentLocation != null) {
//                PropertyManager.getInstance().setRecentLongitude(Double.toString(mRecentLocation.getLatitude()));
//                PropertyManager.getInstance().setRecentLongitude(Double.toString(mRecentLocation.getLongitude()));
//
//                Log.d(DEBUG_TAG, "StartNavigationActivity.onStart.mRecentLocation" + " : " + Double.toString(mRecentLocation.getLatitude()) + ", " + Double.toString(mRecentLocation.getLongitude()));
//                Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onStart.mRecentLocation" + " : " + mRecentLocation.getLatitude() + ", " + mRecentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
//            }
        }
    }

    /*
     * exception 처리
     */
    @Override
    protected void onStop() {
        super.onStop();
//        Toast.makeText(getContext(), "NavigationFragment.onStop", Toast.LENGTH_SHORT).show();
        Log.d(DEBUG_TAG, "StartNavigationActivity.onStop");

        if (mLM != null) {
            if (Build.VERSION.SDK_INT > 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            Log.d(DEBUG_TAG, "StartNavigationActivity.onStart.removeUpdates.mInitialListener");
            Log.d(DEBUG_TAG, "StartNavigationActivity.onStart.removeUpdates.mIterativeListener");

            mLM.removeUpdates(mInitialListener);
            mLM.removeUpdates(mIterativeListener);

            mHandler.removeMessages(MESSAGE_INITIAL_LOCATION_TIMEOUT);
            mHandler.removeMessages(MESSAGE_ITERATIVE_LOCATION_TIMEOUT);
            mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "StartNavigationActivity.onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "StartNavigationActivity.onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG, "StartNavigationActivity.onDestroy");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
                startActivity(intent);

                finish();
                /*
                 * 왜 finish 안했었는데 종료될까...
                 */

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onBackPressed : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
        startActivity(intent);

        finish();
        /*
         * 플래그와 finish 관계 여쭤보기
         */
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(DEBUG_TAG, "StartNavigationActivity.onMapReady");
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        double recentLatitude = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
        double recentLongitude = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());

        Log.d(DEBUG_TAG, "StartNavigationActivity.onMapReady.recent.moveMap");
        moveMap(recentLatitude, recentLongitude, 0, MOVE_CAMERA);
    }

    private void moveMap(double latitude, double longitude, float bearing, String moveAction) {
        if (mMap != null) {
            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.target(new LatLng(latitude, longitude));
            builder.bearing(bearing);
            builder.zoom(19);
            builder.tilt(45);
//            Log.d(DEBUG_TAG, "StartNavigationActivity.moveMap.bearing.mAngle : " + bearing);

            CameraPosition position = builder.build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

            if (moveAction.equals(ANIMATE_CAMERA)) {
                mMap.animateCamera(update);
            } else if (moveAction.equals(MOVE_CAMERA)) {
                mMap.moveCamera(update);
            }
        }
    }
//
//    protected void createLocationRequest() {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.createLocationRequest");
//
//        if (mInitialLocReq == null) {
//            mInitialLocReq = new LocationRequest();
//            mInitialLocReq.setNumUpdates(1);
//            mInitialLocReq.setInterval(500);
//            mInitialLocReq.setMaxWaitTime(1000);
//            mInitialLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        }
//
//        if (mIterativeLocReq == null) {
//            mIterativeLocReq = new LocationRequest();
//            mIterativeLocReq.setInterval(2000);
//            mIterativeLocReq.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        }
//    }

//    protected void startInitialLocationUpdates() {
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mInitialLocReq, mInitialListener);
//        Log.d(DEBUG_TAG, "StartNavigationActivity.startInitialLocationUpdates");
//
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mIterativeLocReq, mIterativeListener);
//        Log.d(DEBUG_TAG, "StartNavigationActivity.starIterativeLocationUpdates");
//    }

    //    protected void starIterativeLocationUpdates() {
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mIterativeLocReq, mIterativeListener);
//
//        Log.d(DEBUG_TAG, "StartNavigationActivity.starIterativeLocationUpdates");
//    }
    /*
     * exception 처리
     */
//    protected void stopLocationUpdates() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mInitialListener);
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mIterativeListener);
//
//        Log.d(DEBUG_TAG, "StartNavigationActivity.stopLocationUpdates");
//    }
//
//    @Override
//    public void onConnected(Bundle bundle) {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected");
//        startInitialLocationUpdates();
//        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//        if (mLocation != null) {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.mLocation" + " : " + Double.toString(mLocation.getLatitude()) + ", " + Double.toString(mLocation.getLongitude()));
//            Toast.makeText(this, "StartNavigationActivity.onConnected.mLocation" + " : " + mLocation.getLatitude() + ", " + mLocation.getLongitude(), Toast.LENGTH_SHORT).show();
//
//            PropertyManager.getInstance().setRecentLatitude(Double.toString(mLocation.getLatitude()));
//            PropertyManager.getInstance().setRecentLongitude(Double.toString(mLocation.getLongitude()));
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.setRecentLocation");
//        } else {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.mLocation null");
//        }
//
//
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
////        Toast.makeText(getContext(), "NavigationFragment.onConnectionSuspended", Toast.LENGTH_SHORT).show();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnectionSuspended");
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//        if (mResolvingError) {
//            // Already attempting to resolve an error.
//            return;
//        }
//
////        Toast.makeText(getContext(), "NavigationFragment.onConnectionFailed", Toast.LENGTH_SHORT).show();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnectionFailed");
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
//    }

    LocationListener mInitialListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
//            Toast.makeText(getContext(), "NavigationFragment.onLocationChanged", Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged");

            mHandler.removeMessages(MESSAGE_INITIAL_LOCATION_TIMEOUT);

            if (mMap != null) {
                Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.mMap != null");

                if (location != null) {
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.location != null");
//                    starIterativeLocationUpdates();
                    PropertyManager.getInstance().setRecentLatitude(Double.toString(location.getLatitude()));
                    PropertyManager.getInstance().setRecentLongitude(Double.toString(location.getLongitude()));

                    if (mLM != null && mLM.isProviderEnabled(mProvider)) {
                        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(StartNavigationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(StartNavigationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        Log.d(DEBUG_TAG, "StartNavigationActivity.onLocationChanged.requestLocationUpdates");
                        mLM.requestLocationUpdates(mProvider, 1500, 0, mIterativeListener);
                        mHandler.sendEmptyMessageDelayed(MESSAGE_ITERATIVE_LOCATION_TIMEOUT, LOCATION_TIMEOUT_INTERVAL);
//                        mLM.removeUpdates(mInitialListener);
                    }

                    Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.mInitialListener.onLocationChanged +: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged +: " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));

//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.출발지, 목적지 위도 경도 : " + recentLatitude + ", " + recentLongitude + " | " + destinationLatitude + ", " + destinationLongitude);

                    findRoute();

                    moveMap(location.getLatitude(), location.getLongitude(), location.getBearing(), ANIMATE_CAMERA);
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.moveMap");
                }
            } else {
                Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.mMap == null");
//                mCacheLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                switch (status) {
                    case LocationProvider.OUT_OF_SERVICE:
                        /*
                         * 메시지 처리
                         */
                        Toast.makeText(StartNavigationActivity.this, "OUT_OF_SERVICE", Toast.LENGTH_SHORT).show();

                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        /*
                         * 메시지 처리
                         */
//                        Toast.makeText(StartNavigationActivity.this, "TEMPORARILY_UNAVAILABLE", Toast.LENGTH_SHORT).show();

                        break;
                    case LocationProvider.AVAILABLE:
                        break;
                }
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener mIterativeListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged");

            mHandler.removeMessages(MESSAGE_ITERATIVE_LOCATION_TIMEOUT);

            if (mMap != null) {
                Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.mMap != null");

                if (location != null) {
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.location != null");
//                    Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.mIterativeListener.onLocationChanged : " + location.getLatitude() + ", " + location.getLongitude() + " | " + Float.toString(location.getSpeed()), Toast.LENGTH_SHORT).show();
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.location.getLatitude, location.getLongitude : " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.bearing,speed +: " + Float.toString(location.getBearing()) + ", " + Float.toString(location.getSpeed()));

                    PropertyManager.getInstance().setRecentLatitude(Double.toString(location.getLatitude()));
                    PropertyManager.getInstance().setRecentLongitude(Double.toHexString(location.getLongitude()));

                    moveMap(location.getLatitude(), location.getLongitude(), location.getBearing(), ANIMATE_CAMERA);
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.moveMap");

                    if (mBicycleNaviInfoList.size() > 0) {
                        int maxNaviLatLngIndex = mBicycleNaviInfoList.size() - 1;
                        Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.maxNaviLatLngIndex : " + maxNaviLatLngIndex);

                        clearAllOrthogonalDistanceList();

                        Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.naviLatLngIndex : " + naviLatLngIndex + "(비교 전 인덱스)");
                        if (naviLatLngIndex + 3 <= maxNaviLatLngIndex) {
                            Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.naviLatLngIndex : 3개 index 비교");
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 1);
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 3).latLng, location, naviLatLngIndex + 2);

                        } else if (naviLatLngIndex + 2 <= maxNaviLatLngIndex && naviLatLngIndex + 3 > maxNaviLatLngIndex) {
                            Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.naviLatLngIndex : 2개 index 비교");
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 1);

                        } else if (naviLatLngIndex + 1 <= maxNaviLatLngIndex && naviLatLngIndex + 2 > maxNaviLatLngIndex) {
                            Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.naviLatLngIndex : 1개 index 비교");
                            getOrthogonalDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex);
                        }

                        if (mOrthogonalDistanceList.size() > 0) {
                            Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.mOrthogonalDistanceList(distance 비교 개수) : " + Integer.toString(mOrthogonalDistanceList.size()));

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
                                            Log.d(DEBUG_TAG, "(수선의 발 있는 경우 지나친 인덱스 검색) naviLatLngIndex : " + j + " | newNaviLatLngIndex 까지 : " + newNaviLatLngIndex);
//                                            LatLng latLng = mBicycleNaviInfoList.get(j).latLng;
//                                            BicycleProperties properties = mAllPropertiesResolver.get(latLng);

                                            BicycleNavigationInfo info = mBicycleNaviInfoList.get(j);

                                            if (info.properties != null) {
                                                Log.d(DEBUG_TAG, Integer.toString(mBicycleNaviInfoList.size()));
                                                if (info.properties.description != null && !info.properties.description.equals("")) {
                                                    tvNaviDescription.setText(info.properties.description);
                                                    Log.d(DEBUG_TAG, "(수선의 발 있는 경우 지나친 인덱스 description != null) naviLatLngIndex : " + j + " | description : " + info.properties.description);
                                                } else {
                                                    Log.d(DEBUG_TAG, "(수선의 발 있는 경우 지나친 인덱스 description == null) naviLatLngIndex : " + j + " | description : " + info.properties.description);
                                                }
                                            }

                                            /*
                                             *  종료 처리
                                             */
                                            checkFinishNavigation(j, maxNaviLatLngIndex, location, info);
                                        }
                                    }

                                    naviLatLngIndex = newNaviLatLngIndex;

                                }
                            }

                            if (minDistance >= LIMIT_DISTANCE) {
                                Toast.makeText(StartNavigationActivity.this, "경로에서 벗어났습니다. 경로를 재탐색합니다.(Limit Distance|수선의 발 있는 경우)", Toast.LENGTH_SHORT).show();
                                Log.d(DEBUG_TAG, "minDistance >= " + LIMIT_DISTANCE + ": 경로 재탐색");

                                findRoute();

//                                Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(경로 재탐색 직전 인덱스) : " + Integer.toString(naviLatLngIndex));
                            } else {
//                            distanceList.clear();
//                            mOrthogonalDistanceResolver.clear();

                                /*
                                 *  TextView에 정보 보여주기 처리
                                */
                                BicycleNavigationInfo info = mBicycleNaviInfoList.get(naviLatLngIndex);

                                if (info.properties != null) {
                                    if (info.properties.description != null && !info.properties.description.equals("")) {
                                        tvNaviDescription.setText(info.properties.description);

                                        Log.d(DEBUG_TAG, info.properties.description);
                                    }
                                }

                                if (info.properties != null) {
                                    Toast.makeText(StartNavigationActivity.this, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(비교 후 현재 인덱스) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance) + " | description : " + info.properties.description, Toast.LENGTH_SHORT).show();
                                    Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(비교 후 현재 인덱스) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance) + " | description : " + info.properties.description);
                                } else {
                                    Toast.makeText(StartNavigationActivity.this, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(비교 후 현재 인덱스) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance), Toast.LENGTH_SHORT).show();
                                    Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(비교 후 현재 인덱스) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance));
                                }
                                /*
                                 *  종료 처리
                                 */
                                checkFinishNavigation(naviLatLngIndex, maxNaviLatLngIndex, location, info);
                            }
                        } else {
                            mHandler.sendEmptyMessageDelayed(MESSAGE_REROUTE_NAVIGATION, REROUTE_NAVIGATION_TIMEOUT_INTERVAL);
                            Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.수선의 발 없는 경우");

                            clearAllPointDistanceList();

                            if (naviLatLngIndex + 2 <= maxNaviLatLngIndex) {
                                Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.naviLatLngIndex(수선의 발 없는 경우) : 3개 index 비교");

                                getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, location, naviLatLngIndex);
                                getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex + 1);
                                getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 2).latLng, location, naviLatLngIndex + 2);
                            } else if (naviLatLngIndex + 1 <= maxNaviLatLngIndex && naviLatLngIndex + 2 > maxNaviLatLngIndex) {
                                Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.naviLatLngIndex(수선의 발 없는 경우) : 2개 index 비교");

                                getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex).latLng, location, naviLatLngIndex);
                                getPointDistance(mBicycleNaviInfoList.get(naviLatLngIndex + 1).latLng, location, naviLatLngIndex + 1);
                            } else if (naviLatLngIndex <= maxNaviLatLngIndex && naviLatLngIndex + 1 > maxNaviLatLngIndex) {
                                Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.naviLatLngIndex(수선의 발 없는 경우) : 1개 index 비교");

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
                                                Log.d(DEBUG_TAG, "(수선의 발 없는 경우 지나친 인덱스 검색) naviLatLngIndex : " + j + " | newNaviLatLngIndex 까지 : " + newNaviLatLngIndex);

                                                BicycleNavigationInfo info = mBicycleNaviInfoList.get(j);

                                                if (info.properties != null) {
                                                    if (info.properties.description != null && !info.properties.description.equals("")) {
                                                        tvNaviDescription.setText(mBicycleNaviInfoList.get(j).properties.description);
                                                        Log.d(DEBUG_TAG, "(수선의 발 없는 경우 지나친 인덱스 description) naviLatLngIndex : " + j + " | description : " + info.properties.description);
                                                    } else {
                                                        Log.d(DEBUG_TAG, "(수선의 발 없는 경우 지나친 인덱스 description == null) naviLatLngIndex : " + j + " | description : " + info.properties.description);
                                                    }
                                                }

                                                /*
                                                 *  종료 처리
                                                 */
                                                checkFinishNavigation(j, maxNaviLatLngIndex, location, info);
                                            }
                                        }

                                        naviLatLngIndex = newNaviLatLngIndex;
                                    }
                                }

                                if (minDistance >= LIMIT_DISTANCE) {
                                    Toast.makeText(StartNavigationActivity.this, "경로에서 벗어났습니다. 경로를 재탐색합니다.(Limit Distance|수선의 발 없는 경우)", Toast.LENGTH_SHORT).show();
                                    Log.d(DEBUG_TAG, "minDistance >= " + LIMIT_DISTANCE + ": 경로 재탐색");

                                    findRoute();

//                                    Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우) : " + Integer.toString(naviLatLngIndex));
                                } else {
//                            distanceList.clear();
//                            mOrthogonalDistanceResolver.clear();

                                    /*
                                     *  TextView에 정보 보여주기 처리
                                     */
                                    BicycleNavigationInfo info = mBicycleNaviInfoList.get(naviLatLngIndex);

                                    if (info.properties != null) {
                                        if (info.properties.description != null && !info.properties.description.equals("")) {
                                            tvNaviDescription.setText(info.properties.description);

                                            Log.d(DEBUG_TAG, info.properties.description);
                                        }
                                    }

                                    if (info.properties != null) {
                                        Toast.makeText(StartNavigationActivity.this, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우|(비교 후 현재 인덱스)) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance) + " | description : " + info.properties.description, Toast.LENGTH_SHORT).show();
                                        Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우|(비교 후 현재 인덱스)) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance) + " | description : " + info.properties.description);
                                    } else {
                                        Toast.makeText(StartNavigationActivity.this, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우|(비교 후 현재 인덱스)) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance), Toast.LENGTH_SHORT).show();
                                        Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우|(비교 후 현재 인덱스)) : " + Integer.toString(naviLatLngIndex) + " | minDistance : " + Float.toString(minDistance));
                                    }

                                    /*
                                     *  종료 처리
                                     */
                                    checkFinishNavigation(naviLatLngIndex, maxNaviLatLngIndex, location, info);

                                }
                            } else {
                                mHandler.sendEmptyMessageDelayed(MESSAGE_REROUTE_NAVIGATION, REROUTE_NAVIGATION_TIMEOUT_INTERVAL);

                                Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.naviLatLngIndex(둘 다 없는 경우)");
                            }

//                            Log.d(DEBUG_TAG, "maxNaviLatLngIndex : " + maxNaviLatLngIndex + " | " + "naviLatLngIndex(수선의 발 없는 경우) : " + Integer.toString(naviLatLngIndex));
                        }
                    }
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                switch (status) {
                    case LocationProvider.OUT_OF_SERVICE:
                        /*
                         * 메시지 처리
                         */
                        Toast.makeText(StartNavigationActivity.this, "OUT_OF_SERVICE", Toast.LENGTH_SHORT).show();

                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        /*
                         * 메시지 처리
                         */
//                        Toast.makeText(StartNavigationActivity.this, "TEMPORARILY_UNAVAILABLE", Toast.LENGTH_SHORT).show();

                        break;
                    case LocationProvider.AVAILABLE:
                        break;
                }
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.hideInfoWindow();

        return true;
    }

    private void addPointMarker(LatLng latLng, BicycleProperties properties, String bitmapFlag) {
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(latLng.latitude, latLng.longitude));
        options.anchor(0.5f, 1.0f);
        options.draggable(false);

        if (bitmapFlag.equals(POINTTYPE_SP)) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        } else if (bitmapFlag.equals(POINTTYPE_EP)) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        } else if (bitmapFlag.equals(POINTTYPE_GP)) {
            gpIndex++;

            /*
             * index 순서대로 이미지 다르게 적용
             */

            Log.d(DEBUG_TAG, "StartNavigationActivity.addPointMarker.BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION.POINTTYPE_GP.index : " + Integer.toString(gpIndex));

            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        }

        Marker m = mMap.addMarker(options);
        mMarkerResolver.put(latLng, m);
        mPropertiesResolver.put(latLng, properties);
        mBitmapResolver.put(latLng, bitmapFlag);
    }

    private void clearAllMarker() {
        for (int i = 0; i < mPointLatLngList.size(); i++) {
            LatLng latLng = mPointLatLngList.get(i);

            if (mMarkerResolver.size() > 0) {
                Marker m = mMarkerResolver.get(latLng);
                BicycleProperties properties = mPropertiesResolver.get(latLng);
                String bitmapFlag = mBitmapResolver.get(latLng);

                mMarkerResolver.remove(m);
                mPropertiesResolver.remove(properties);
                mBitmapResolver.remove(bitmapFlag);

                m.remove();
            }
        }

        mPointLatLngList.clear();
        gpIndex = 0;
    }

    private void clearAllPolyline() {
        for (Polyline line : polylineList) {
            line.remove();
        }

        polylineList.clear();
    }

/*    private void clearAllNaviLatLngList() {
        if (mNaviLatLngList.size() > 0) {
            mNaviLatLngList.clear();
            mAllPropertiesResolver.clear();
            naviLatLngIndex = 0;
        }
    }*/

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

        if (checkIncludeLine(latLngA,latLngB, orthogonalLoc)) {
            distance = orthogonalLoc.distanceTo(currentLocC);

            mOrthogonalDistanceResolver.put(distance, index);

            if (distance != -1 && distance >= 0) {
                mOrthogonalDistanceList.add(distance);
            }
            Log.d(DEBUG_TAG, "StartNavigationActivity.getOrthogonalDistance.index : " + Integer.toString(index) + " | distance : " + Float.toString(distance));
        } else {
            distance = -1;
        }

//        Toast.makeText(StartNavigationActivity.this, "findLatitude, findLongitude " + findLatitude + ", " + findLongitude + " | " + distance + " | " + currentLocC.getBearing() + " | " + currentLocC.getSpeed(), Toast.LENGTH_SHORT).show();

//        return distance;
    }

    private void getPointDistance(LatLng latLngA, Location currentLocB, int index) {
        float distance = -1;

        pointLoc.setLatitude(latLngA.latitude);
        pointLoc.setLongitude(latLngA.longitude);

        distance = pointLoc.distanceTo(currentLocB);

        if (distance != -1 && distance >= 0) {
            mPointDistanceResolver.put(distance, index);
            mPointDistanceList.add(distance);

            Log.d(DEBUG_TAG, "StartNavigationActivity.getPointDistance.index : " + Integer.toString(index) + " | distance : " + Float.toString(distance));
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

    private void checkFinishNavigation(int currentIndex, int lastIndex, Location currentLocation, BicycleNavigationInfo info) {
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

    private void autoFinishNavigationDialog() {
        if (isFirstFinishDialog == true) {
            if (mLM != null) {
                if (Build.VERSION.SDK_INT > 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                Log.d(DEBUG_TAG, "StartNavigationActivity.autoFinishNavigationDialog.removeUpdates.mInitialListener");
                Log.d(DEBUG_TAG, "StartNavigationActivity.autoFinishNavigationDialog.removeUpdates.mIterativeListener");

                mLM.removeUpdates(mInitialListener);
                mLM.removeUpdates(mIterativeListener);

                mHandler.removeMessages(MESSAGE_INITIAL_LOCATION_TIMEOUT);
                mHandler.removeMessages(MESSAGE_ITERATIVE_LOCATION_TIMEOUT);
                mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_info);
            builder.setTitle("내비게이션 안내종료");
            builder.setMessage("목적지에 도착했습니다. 내비게이션 안내를 종료합니다.");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                 /*
                 *  목적지 위도, 경도, searchoption 날리기
                 */
                    PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                    PropertyManager.getInstance().setDestinationLatitude(null);
                    PropertyManager.getInstance().setDestinationLongitude(null);
                    PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

//                Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onCreate : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
                    intent.putExtra(KEY_REPLACE_MAIN_FRAGMENT, VALUE_REPLACE_MAIN_FRAGMENT);
                    startActivity(intent);
                }
            });

            builder.setCancelable(false);

            builder.create().show();

            isStartNavigation = false;
            isFirstFinishDialog = false;
        }
    }

    private void withinRouteLimitDistanceDialog() {
        if (isFirstFinishDialog) {
            if (mLM != null) {
                if (Build.VERSION.SDK_INT > 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                Log.d(DEBUG_TAG, "StartNavigationActivity.autoFinishNavigationDialog.removeUpdates.mInitialListener");
                Log.d(DEBUG_TAG, "StartNavigationActivity.autoFinishNavigationDialog.removeUpdates.mIterativeListener");

                mLM.removeUpdates(mInitialListener);
                mLM.removeUpdates(mIterativeListener);

                mHandler.removeMessages(MESSAGE_INITIAL_LOCATION_TIMEOUT);
                mHandler.removeMessages(MESSAGE_ITERATIVE_LOCATION_TIMEOUT);
                mHandler.removeMessages(MESSAGE_REROUTE_NAVIGATION);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_info);
            builder.setTitle("내비게이션 안내종료");
            builder.setMessage("목적지가 출발지와 근접합니다.(30m 이내) 내비게이션 안내를 종료합니다.");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                 /*
                 *  목적지 위도, 경도, searchoption 날리기
                 */
                    PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                    PropertyManager.getInstance().setDestinationLatitude(null);
                    PropertyManager.getInstance().setDestinationLongitude(null);
                    PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

//                Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onCreate : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
                    intent.putExtra(KEY_REPLACE_MAIN_FRAGMENT, VALUE_REPLACE_MAIN_FRAGMENT);
                    startActivity(intent);
                }
            });

            builder.setCancelable(false);

            builder.create().show();

            isStartNavigation = false;
            isFirstFinishDialog = false;
        }
    }

//    double startX, double startY, double endX, double endY, int searchOption
    private void findRoute() {
        final double startX = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());
        final double startY = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
        final double endX = Double.parseDouble(PropertyManager.getInstance().getDestinationLongitude());
        final double endY = Double.parseDouble(PropertyManager.getInstance().getDestinationLatitude());
        final int searchOption =  PropertyManager.getInstance().getFindRouteSearchOption();

        Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.출발지, 목적지 위도 경도 : " + startY + ", " + startX + " | " + endY + ", " + endX);

        if (startX != 0 && startY != 0 && endX != 0 && endY != 0) {
            NavigationNetworkManager.getInstance().findRoute(StartNavigationActivity.this, startX, startY, endX, endY, searchOption,
                    new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
                        @Override
                        public void onSuccess(BicycleRouteInfo result) {
                            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess");
                            if (result.features != null && result.features.size() > 0) {
                                clearAllMarker();
                                clearAllPolyline();
                                clearAllmBicycleNaviInfoList();
//                                clearAllNaviLatLngList();

                                int totalTime = result.features.get(0).properties.totalTime;
                                int totalDistance = result.features.get(0).properties.totalDistance;

                                options = new PolylineOptions();

                                Log.d(DEBUG_TAG, "result.features.size() : " + result.features.size());

                                for (int i = 0; i < result.features.size(); i++) {
                                    BicycleFeature feature = result.features.get(i);

//                                    if ((feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_SP))
//                                            || (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_GP))
//                                            || (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_EP))
//                                            || feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING)) {
                                        double[] coordForNavi = feature.geometry.coordinates;

                                        for (int j = 0; j < coordForNavi.length; j += 2) {
                                            float distance = 0;

                                            LatLng latLngA = new LatLng(coordForNavi[j + 1], coordForNavi[j]);
                                            LatLng latLngB;

                                            BicycleProperties properties = feature.properties;

                                            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.BicycleProperties.mAllPropertiesResolver : " + properties.description);

//                                        mNaviLatLngList.add(latLngA);
//                                        mAllPropertiesResolver.put(latLngA, properties);

                                            if (j <= coordForNavi.length - 4) {
//                                            latLngA = new LatLng(coordForNavi[i + 1], coordForNavi[i]);
                                                latLngB = new LatLng(coordForNavi[j + 3], coordForNavi[j + 2]);

                                                locationA.setLatitude(latLngA.latitude);
                                                locationA.setLongitude(latLngA.longitude);

                                                locationB.setLatitude(latLngB.latitude);
                                                locationB.setLongitude(latLngB.longitude);

                                                distance = locationA.distanceTo(locationB);

                                                Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.distance(coordForNavi 인덱스 마지막 아닐 때) : " + mBicycleNaviInfoList.size() + " | " + distance);
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

                                                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.distance(coordForNavi 인덱스 마지막 아닐 때 다음 좌표와 거리 계산) : " + mBicycleNaviInfoList.size() + " | " + distance);
                                                } else {
                                                    distance = 0;

                                                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.distance(coordForNavi 인덱스 마지막) : " + mBicycleNaviInfoList.size() + " | " + distance);
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

                                                Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.BICYCLE_ROUTE_GEOMETRY_TYPE_POINT | LataLng : " + bicycleNaviInfo.latLng.latitude +
                                                        ", " + bicycleNaviInfo.latLng.longitude + " | distance : " + bicycleNaviInfo.distance + " | description : " + bicycleNaviInfo.properties.description);

                                                if (mBicycleNaviInfoList.get(mBicycleNaviInfoList.size() - 1).properties.description != null)
                                                    Log.d(DEBUG_TAG, "mBicycleNaviInfoList.size : " + Integer.toString(mBicycleNaviInfoList.size() - 1) + ", description : " + mBicycleNaviInfoList.get(mBicycleNaviInfoList.size() - 1).properties.description);

                                                if (feature.properties.pointType.equals(POINTTYPE_SP)) {
                                                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.bicycleNaviInfo.properties : " + bicycleNaviInfo.properties.description);

                                                    addPointMarker(latLngA, feature.properties, POINTTYPE_SP);

                                                    mPointLatLngList.add(latLngA);
                                                } else if ((feature.properties.pointType.equals(POINTTYPE_GP))) {
                                                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.bicycleNaviInfo.properties : " + bicycleNaviInfo.properties.description);

                                                    addPointMarker(latLngA, feature.properties, POINTTYPE_GP);

                                                    mPointLatLngList.add(latLngA);
                                                } else if (feature.properties.pointType.equals(POINTTYPE_EP)) {
                                                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.bicycleNaviInfo.properties : " + bicycleNaviInfo.properties.description);

                                                    addPointMarker(latLngA, feature.properties, POINTTYPE_EP);

                                                    mPointLatLngList.add(latLngA);
                                                }
                                            } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING)) {
                                                BicycleNavigationInfo bicycleNaviInfo = new BicycleNavigationInfo();
                                                bicycleNaviInfo.latLng = latLngA;
                                                bicycleNaviInfo.distance = distance;
                                                bicycleNaviInfo.properties = null;

                                                mBicycleNaviInfoList.add(bicycleNaviInfo);

                                                Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING | LataLng : " + bicycleNaviInfo.latLng.latitude +
                                                        ", " + bicycleNaviInfo.latLng.longitude + " | distance : " + bicycleNaviInfo.distance);

                                                options.add(latLngA);
                                            }
//
                                            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.mBicycleNaviInfoList : " + mBicycleNaviInfoList.size());
                                        }
//                                    }
//                                    if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING)) {
//                                        double[] coord = feature.geometry.coordinates;
//
//                                        for (int k = 0; k < coord.length; k += 2) {
//                                            options.add(new LatLng(coord[k + 1], coord[k]));
//                                        }
//                                    } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_SP)) {
//                                        double[] coord = feature.geometry.coordinates;
//
//                                        for (int k = 0; k < coord.length; k += 2) {
//                                            LatLng latLng = new LatLng(coord[k + 1], coord[k]);
//                                            BicycleProperties properties = feature.properties;
//
//                                            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.BicycleProperties : " + properties.description);
//
//                                            addPointMarker(latLng, properties, POINTTYPE_SP);
//
//                                            mPointLatLngList.add(latLng);
//                                        }
//                                    } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_EP)) {
//                                        double[] coord = feature.geometry.coordinates;
//
//                                        for (int k = 0; k < coord.length; k += 2) {
//                                            LatLng latLng = new LatLng(coord[k + 1], coord[k]);
//                                            BicycleProperties properties = feature.properties;
//
//                                            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.BicycleProperties : " + properties.description);
//
//                                            addPointMarker(latLng, properties, POINTTYPE_EP);
//
//                                            mPointLatLngList.add(latLng);
//                                        }
//                                    } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_GP)) {
//                                        double[] coord = feature.geometry.coordinates;
//
//                                        for (int k = 0; k < coord.length; k += 2) {
//                                            LatLng latLng = new LatLng(coord[k + 1], coord[k]);
//                                            BicycleProperties properties = feature.properties;
//                                            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.BicycleProperties : " + properties.description);
//
//                                            addPointMarker(latLng, properties, POINTTYPE_GP);
//
//                                            mPointLatLngList.add(latLng);
//                                        }
//                                    }
                                }


                                for (int m = 0; m < mBicycleNaviInfoList.size(); m ++) {
                                    BicycleNavigationInfo tempInfo = mBicycleNaviInfoList.get(m);
                                    if (tempInfo.properties != null) {
                                        if (tempInfo.properties.description != null) {
                                            Log.d(DEBUG_TAG, "index : " + Integer.toString(m) + ", description : " + tempInfo.properties.description + ", distance : " + tempInfo.distance);
                                        } else {
                                            Log.d(DEBUG_TAG, "index : " + Integer.toString(m) + ", description : null" + ", distance : " + tempInfo.distance);
                                        }
                                    } else  {
                                        Log.d(DEBUG_TAG, "index : " + Integer.toString(m) + ", tempInfo.properties == null" + ", distance : " + tempInfo.distance);
                                    }
                                }

//                                if (mNaviLatLngList.size() > 0 && mAllPropertiesResolver.size() > 0) {
//                                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.mNaviLatLngList.size : " + mNaviLatLngList.size() + " | mAllPropertiesResolver.size : " + mAllPropertiesResolver.size() + " | mBicycleNaviInfoList.size : " + mBicycleNaviInfoList.size());
//                                }

                                if (mBicycleNaviInfoList.size() > 0) {
                                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.mBicycleNaviInfoList.size : " + mBicycleNaviInfoList.size());
                                }

                                if (options != null) {
                                    options.color(Color.BLUE);
                                    options.width(10);
                                    options.geodesic(true);

                                    polyline = mMap.addPolyline(options);

                                    polylineList.add(polyline);
                                }

                                            /*
                                             * 출발 description TextView 에 전달
                                             */
//                                tvNaviDescription.setText(result.features.get(0).properties.description);
                            }
                        }

                        @Override
                        public void onFail(int code) {
                            if (code == 3209) {
                                withinRouteLimitDistanceDialog();
                            }
                        }
                    });
        }
    }
/*    private void checkOrthogonalPoint(double x1, double y1, double x2, double y2, double x3, double y3) {
        double findX, findY;

        if (x1 == x2) {
            findX = x1;
            findY = y3;
        } else if (y1 == y2) {
            findX = x3;
            findY = y1;
        } else {

            double m1 = (y2 - y1) / (x2 - x1);

            double k1 = -m1 * x1 + y1;

            double m2 = -1 / m1;

            double k2 = -m2 * x3 + y3;

            findX = (k2 - k1) / (m1 - m2);
            findY = m1 * findX + k1;
        }

        Toast.makeText(StartNavigationActivity.this, "findX, findY " + findX + ", " + findY, Toast.LENGTH_SHORT).show();
    }*/

    /* private boolean checkIncludeLine(double x1, double y1, double x2, double y2, double xC, double yC) {
        if (((x2 - xC) * (x1 - xC) <= 0) && ((y2 - yC) * (y1 - yC) <= 0)) {
            return true;
        } else {
            return false;
        }
    }*/
}



//package com.safering.safebike.navigation;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.graphics.Color;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.location.Location;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Toast;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.CameraUpdate;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.CameraPosition;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.Polyline;
//import com.google.android.gms.maps.model.PolylineOptions;
//import com.safering.safebike.MainActivity;
//import com.safering.safebike.R;
//import com.safering.safebike.property.PropertyManager;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//
//public class StartNavigationActivity extends AppCompatActivity implements OnMapReadyCallback,
//        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener {
//    private static final String DEBUG_TAG = "safebike";
//
//    private static final String SERVICE_FINISH = "finish";
//
//    private static final String KEY_POP_NAVIGATION_FRAGMENT = "popNavigation";
//    private static final String VALUE_POP_NAVIGATION_FRAGMENT = "popNavigation";
//    private static final String KEY_REPLACE_MAIN_FRAGMENT = "replaceMainFragment";
//    private static final String VALUE_REPLACE_MAIN_FRAGMENT = "replaceMainFragment";
//
//    private static final String MOVE_CAMERA = "movecamera";
//    private static final String ANIMATE_CAMERA = "animatecamera";
//
//    private boolean mResolvingError = false;
//    private static final String STATE_RESOLVING_ERROR = "resolving_error";
//
//    private static final int BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION = 3;
//    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_POINT = "Point";
//    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING = "LineString";
//
//    private static final String POINTTYPE_SP = "SP";
//    private static final String POINTTYPE_EP = "EP";
//    private static final String POINTTYPE_GP = "GP";
//
//    private GoogleMap mMap;
//    GoogleApiClient mGoogleApiClient;
//    Location mLocation, mCacheLocation;
//    LocationRequest mInitialLocReq, mIterativeLocReq;
//
//    Polyline polyline;
//    PolylineOptions options;
//    ArrayList<Polyline> polylineList;
//
//    final Map<LatLng, Marker> mMarkerResolver = new HashMap<LatLng, Marker>();
//    final Map<LatLng, String> mBitmapResolver = new HashMap<LatLng, String>();
//    final Map<LatLng, BicycleProperties> mPropertiesResolver = new HashMap<LatLng, BicycleProperties>();
//
//    ArrayList<LatLng> mPointLatLngList;
//
//    double recentLatitude;
//    double recentLongitude;
//    double destinationLatitude;
//    double destinationLongitude;
//    double centerLatitude;
//    double centerLongitude;
//
//    int gpIndex = 0;
//
//    Sensor mRotationSensor;
//    SensorManager mSM;
//
//    float[] orientation = new float[3];
//    float[] mRotationMatrix = new float[9];
//    float mAngle;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate");
//        setContentView(R.layout.activity_start_navigation);
//
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.navigation_map);
//        mapFragment.getMapAsync(this);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        if (mGoogleApiClient == null) {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.new mGoogleApiClient");
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addApi(LocationServices.API)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this).build();
//
//            createLocationRequest();
//        }
//
//        polylineList = new ArrayList<Polyline>();
//        mPointLatLngList = new ArrayList<LatLng>();
//
//        mSM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        mRotationSensor = mSM.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//
////        Button btn = (Button) findViewById(R.id.btn_finish_navigation);
////        btn.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                /*
////                 * 운동 기록 처리, 자동으로 안내를 종료할지에 대한 시나리오, 사용자 직접 종료 또는 자동 종료에 따른 운동 기록 값 전달
////                 */
////
////                /*
////                 * 다이얼로그로 종료시 처리
////                 */
////            }
////        });
//    }
//    /*
//     * 안내종료 버튼 처리
//     */
//    public void onFinishNavigationBtn(View view) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setIcon(android.R.drawable.ic_dialog_info);
//        builder.setTitle("내비게이션 안내종료");
//        builder.setMessage("현재 내비게이션 안내 중입니다. 정말로 종료하시겠습니까");
//        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                 /*
//                 *  목적지 위도, 경도, searchoption 날리기
//                 */
//                PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
//                PropertyManager.getInstance().setDestinationLatitude(null);
//                PropertyManager.getInstance().setDestinationLongitude(null);
//                PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);
//
////                Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onCreate : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();
//
//                Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
//                intent.putExtra(KEY_REPLACE_MAIN_FRAGMENT, VALUE_REPLACE_MAIN_FRAGMENT);
//                startActivity(intent);
//            }
//        });
//        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
////        builder.setCancelable(false);
//
//        builder.create().show();
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        Toast.makeText(this, "StartNavigationActivity.onNewIntent", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
////        Toast.makeText(getContext(), "NavigationFragment.onStart", Toast.LENGTH_SHORT).show();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onStart");
//        if (!mResolvingError) {  // more about this later
//            if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
//                Log.d(DEBUG_TAG, "StartNavigationActivity.onStart.mGoogleApiClient.connect");
//                mGoogleApiClient.connect();
//            }
//        }
//
//        if (mRotationSensor != null) {
//            mSM.registerListener(mSensorListener, mRotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        }
//    }
//
//    /*
//     * exception 처리
//     */
//    @Override
//    protected void onStop() {
//        super.onStop();
////        Toast.makeText(getContext(), "NavigationFragment.onStop", Toast.LENGTH_SHORT).show();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onStop");
//
//        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
//            stopLocationUpdates();
//
//            mGoogleApiClient.disconnect();
////            Toast.makeText(getContext(), "NavigationFragment.onStop.mGoogleApiClient.disconnect", Toast.LENGTH_SHORT).show();
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onStop.mGoogleApiClient.disconnect");
//        }
//
//        if (mRotationSensor != null) {
//            mSM.unregisterListener(mSensorListener);
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onResume");
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onPause");
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onDestroy");
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case android.R.id.home:
//                Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
////                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
//                startActivity(intent);
//
//                finish();
//                /*
//                 * 왜 finish 안했었는데 종료될까...
//                 */
//
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onBackPressed() {
////        Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onBackPressed : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();
//
//        Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
////        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
//        startActivity(intent);
//
//        finish();
//        /*
//         * 플래그와 finish 관계 여쭤보기
//         */
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onMapReady");
//        mMap = googleMap;
//
//        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        mMap.setMyLocationEnabled(true);
//        mMap.setOnMarkerClickListener(this);
//        mMap.getUiSettings().setMyLocationButtonEnabled(false);
//        mMap.getUiSettings().setCompassEnabled(false);
//
//        double recentLatitude = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
//        double recentLongitude = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());
//
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onMapReady.recent.moveMap");
//        moveMap(recentLatitude, recentLongitude, MOVE_CAMERA);
//    }
//
//    private void moveMap(double latitude, double longitude, String moveAction) {
//        if (mMap != null) {
//            CameraPosition.Builder builder = new CameraPosition.Builder();
//            builder.target(new LatLng(latitude, longitude));
//            builder.bearing(mAngle);
//            builder.zoom(17);
//            builder.tilt(45);
//            Log.d(DEBUG_TAG, "StartNavigationActivity.moveMap.bearing.mAngle : " + mAngle);
//
//            CameraPosition position = builder.build();
//            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
//
//            if (moveAction.equals(ANIMATE_CAMERA)) {
//                mMap.animateCamera(update);
//            } else if(moveAction.equals(MOVE_CAMERA)) {
//                mMap.moveCamera(update);
//            }
//        }
//    }
//
//    protected void createLocationRequest() {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.createLocationRequest");
//
//        if (mInitialLocReq == null) {
//            mInitialLocReq = new LocationRequest();
//            mInitialLocReq.setNumUpdates(1);
//            mInitialLocReq.setInterval(500);
//            mInitialLocReq.setMaxWaitTime(1000);
//            mInitialLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        }
//
//        if (mIterativeLocReq == null) {
//            mIterativeLocReq = new LocationRequest();
//            mIterativeLocReq.setInterval(2000);
//            mIterativeLocReq.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        }
//    }
//
//    protected void startInitialLocationUpdates() {
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mInitialLocReq, mInitialListener);
//        Log.d(DEBUG_TAG, "StartNavigationActivity.startInitialLocationUpdates");
//
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mIterativeLocReq, mIterativeListener);
//        Log.d(DEBUG_TAG, "StartNavigationActivity.starIterativeLocationUpdates");
//    }
//
////    protected void starIterativeLocationUpdates() {
////        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mIterativeLocReq, mIterativeListener);
////
////        Log.d(DEBUG_TAG, "StartNavigationActivity.starIterativeLocationUpdates");
////    }
//    /*
//     * exception 처리
//     */
//    protected void stopLocationUpdates() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mInitialListener);
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mIterativeListener);
//
//        Log.d(DEBUG_TAG, "StartNavigationActivity.stopLocationUpdates");
//    }
//
//    @Override
//    public void onConnected(Bundle bundle) {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected");
//        startInitialLocationUpdates();
//        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//        if (mLocation != null) {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.mLocation" + " : " + Double.toString(mLocation.getLatitude()) + ", " + Double.toString(mLocation.getLongitude()));
//            Toast.makeText(this, "StartNavigationActivity.onConnected.mLocation" + " : " + mLocation.getLatitude() + ", " + mLocation.getLongitude(), Toast.LENGTH_SHORT).show();
//
//            PropertyManager.getInstance().setRecentLatitude(Double.toString(mLocation.getLatitude()));
//            PropertyManager.getInstance().setRecentLongitude(Double.toString(mLocation.getLongitude()));
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.setRecentLocation");
//        } else {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.mLocation null");
//        }
//
//
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
////        Toast.makeText(getContext(), "NavigationFragment.onConnectionSuspended", Toast.LENGTH_SHORT).show();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnectionSuspended");
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//        if (mResolvingError) {
//            // Already attempting to resolve an error.
//            return;
//        }
//
////        Toast.makeText(getContext(), "NavigationFragment.onConnectionFailed", Toast.LENGTH_SHORT).show();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnectionFailed");
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
//    }
//
//    LocationListener mInitialListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(Location location) {
////            Toast.makeText(getContext(), "NavigationFragment.onLocationChanged", Toast.LENGTH_SHORT).show();
//            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged");
//
//            if (mMap != null) {
//                Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.mMap != null");
//
//                if (location != null) {
////                    starIterativeLocationUpdates();
//                    Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.mInitialListener.onLocationChanged +: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged +: " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));
//
//                    PropertyManager.getInstance().setRecentLatitude(Double.toString(location.getLatitude()));
//                    PropertyManager.getInstance().setRecentLongitude(Double.toString(location.getLongitude()));
//
//                    recentLatitude = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
//                    recentLongitude = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());
//                    destinationLatitude = Double.parseDouble(PropertyManager.getInstance().getDestinationLatitude());
//                    destinationLongitude = Double.parseDouble(PropertyManager.getInstance().getDestinationLongitude());
//
//                    centerLatitude = (recentLatitude + destinationLatitude) / 2;
//                    centerLongitude = (recentLongitude + destinationLongitude) / 2;
//
//                    final double startX = recentLongitude;
//                    final double startY = recentLatitude;
//                    final double endX = destinationLongitude;
//                    final double endY = destinationLatitude;
//                    final int searchOption = PropertyManager.getInstance().getFindRouteSearchOption();
//
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.출발지, 목적지 위도 경도 : " + recentLatitude + ", " + recentLongitude + " | " + destinationLatitude + ", " + destinationLongitude);
//
//                    if (startX != 0 && startY != 0 && endX != 0 && endY != 0) {
//                        NavigationNetworkManager.getInstance().findRoute(StartNavigationActivity.this, startX, startY, endX, endY, searchOption,
//                                new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
//                                    @Override
//                                    public void onSuccess(BicycleRouteInfo result) {
//                                        Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess");
//                                        if (result.features != null && result.features.size() > 0) {
//                                            clearAllMarker();
//                                            clearAllPolyline();
//
//                                            int totalTime = result.features.get(0).properties.totalTime;
//                                            int totalDistance = result.features.get(0).properties.totalDistance;
//
//                                            options = new PolylineOptions();
//
//                                            for (BicycleFeature feature : result.features) {
//                                                if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING)) {
//                                                    double[] coord = feature.geometry.coordinates;
//
//                                                    for (int i = 0; i < coord.length; i += 2) {
//                                                        options.add(new LatLng(coord[i + 1], coord[i]));
//                                                    }
//                                                }
//                                            }
//
//                                            for (BicycleFeature feature : result.features) {
//                                                if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_SP)) {
//                                                    double[] coord = feature.geometry.coordinates;
//
//                                                    for (int i = 0; i < coord.length; i += 2) {
//                                                        LatLng latLng = new LatLng(coord[i + 1], coord[i]);
//                                                        BicycleProperties properties = feature.properties;
//                                                        Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.BicycleProperties : " + properties.description);
//
//                                                        addPointMarker(latLng, properties, POINTTYPE_SP);
//
//                                                        mPointLatLngList.add(latLng);
//                                                    }
//                                                } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_EP)) {
//                                                    double[] coord = feature.geometry.coordinates;
//
//                                                    for (int i = 0; i < coord.length; i += 2) {
//                                                        LatLng latLng = new LatLng(coord[i + 1], coord[i]);
//                                                        BicycleProperties properties = feature.properties;
//                                                        Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.BicycleProperties : " + properties.description);
//
//                                                        addPointMarker(latLng, properties, POINTTYPE_EP);
//
//                                                        mPointLatLngList.add(latLng);
//                                                    }
//                                                } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_GP)) {
//                                                    double[] coord = feature.geometry.coordinates;
//
//                                                    for (int i = 0; i < coord.length; i += 2) {
//                                                        LatLng latLng = new LatLng(coord[i + 1], coord[i]);
//                                                        BicycleProperties properties = feature.properties;
//                                                        Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.BicycleProperties :" + properties.description);
//
//                                                        addPointMarker(latLng, properties, POINTTYPE_GP);
//
//                                                        mPointLatLngList.add(latLng);
//                                                    }
//                                                }
//                                            }
//
//                                            if (options != null) {
//                                                options.color(Color.BLUE);
//                                                options.width(10);
//                                                polyline = mMap.addPolyline(options);
//
//                                                polylineList.add(polyline);
//                                            }
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFail(int code) {
//
//                                    }
//                                });
//                    }
//
//                    moveMap(location.getLatitude(), location.getLongitude(), ANIMATE_CAMERA);
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.moveMap");
//                }
//            } else {
//                Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.mMap == null");
//                mCacheLocation = location;
//            }
//        }
//    };
//
//    LocationListener mIterativeListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(Location location) {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged");
//
//            if (mMap != null) {
//                Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.mMap != null");
//
//                if (location != null ) {
//                    Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.mIterativeListener.onLocationChanged : " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged : " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.bearing,speed +: " + location.getBearing() + ", " + location.getSpeed());
//
//                    PropertyManager.getInstance().setRecentLatitude(Double.toString(location.getLatitude()));
//                    PropertyManager.getInstance().setRecentLongitude(Double.toHexString(location.getLongitude()));
//
//                    moveMap(location.getLatitude(), location.getLongitude(), ANIMATE_CAMERA);
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.moveMap");
//                }
//            }
//        }
//    };
//
//    SensorEventListener mSensorListener = new SensorEventListener() {
//        @Override
//        public void onSensorChanged(SensorEvent event) {
//            switch (event.sensor.getType()) {
//                case Sensor.TYPE_ROTATION_VECTOR :
//                    SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
//                    SensorManager.getOrientation(mRotationMatrix, orientation);
//
//                    mAngle = (float) Math.toDegrees(orientation[0]);
//
//                    if (mAngle < 0) {
//                        mAngle += 360;
//                    }
//
////                    Log.d(DEBUG_TAG, "StartNavigationActivity.mSensorListener.onSensorChanged.mAngle : " + mAngle);
//
//                    break;
//            }
//        }
//
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//        }
//    };
//
//    @Override
//    public boolean onMarkerClick(Marker marker) {
//        marker.hideInfoWindow();
//
//        return true;
//    }
//
//    private void addPointMarker(LatLng latLng, BicycleProperties properties,  String bitmapFlag) {
//        MarkerOptions options  = new MarkerOptions();
//        options.position(new LatLng(latLng.latitude, latLng.longitude));
//        options.anchor(0.5f, 1.0f);
//        options.draggable(false);
//
//        if (bitmapFlag.equals(POINTTYPE_SP)) {
//            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//        } else if (bitmapFlag.equals(POINTTYPE_EP)) {
//            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//        } else if (bitmapFlag.equals(POINTTYPE_GP)) {
//            gpIndex++;
//
//            /*
//             * index 순서대로 이미지 다르게 적용
//             */
//
//            Log.d(DEBUG_TAG, "StartNavigationActivity.addPointMarker.BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION.POINTTYPE_GP.index : " + Integer.toString(gpIndex));
//
//            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
//        }
//
//
//        Marker m = mMap.addMarker(options);
//        mMarkerResolver.put(latLng, m);
//        mPropertiesResolver.put(latLng, properties);
//        mBitmapResolver.put(latLng, bitmapFlag);
//    }
//
//    private void clearAllMarker() {
//        for (int i = 0; i < mPointLatLngList.size(); i++) {
//            LatLng latLng = mPointLatLngList.get(i);
//
//            if (mMarkerResolver.size() > 0) {
//                Marker m = mMarkerResolver.get(latLng);
//                BicycleProperties properties = mPropertiesResolver.get(latLng);
//                String bitmapFlag = mBitmapResolver.get(latLng);
//
//                mMarkerResolver.remove(m);
//                mPropertiesResolver.remove(properties);
//                mBitmapResolver.remove(bitmapFlag);
//
//                m.remove();
//            }
//        }
//
//        gpIndex = 0;
//
//        mPointLatLngList.clear();
//    }
//
//    private void clearAllPolyline() {
//        for (Polyline line : polylineList) {
//            line.remove();
//        }
//
//        polylineList.clear();
//    }
//}
