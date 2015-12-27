package com.safering.safebike.navigation;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.safering.safebike.IRouteCallback;
import com.safering.safebike.IRouteService;
import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.MapInfoManager;
import com.safering.safebike.property.PropertyManager;
import com.safering.safebike.property.SpeakVoice;
import com.safering.safebike.service.RouteService;

import java.util.ArrayList;


public class StartNavigationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String DEBUG_TAG = "safebike";

    private static final String SERVICE_FINISH = "finish";

    private static final String KEY_POP_NAVIGATION_FRAGMENT = "popNavigation";
    private static final String VALUE_POP_NAVIGATION_FRAGMENT = "popNavigation";
    private static final String KEY_REPLACE_MAIN_FRAGMENT = "replaceMainFragment";
    private static final String VALUE_REPLACE_MAIN_FRAGMENT = "replaceMainFragment";

    private static final String MOVE_CAMERA = "movecamera";
    private static final String ANIMATE_CAMERA = "animatecamera";

    private static final int BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION = 3;

    private static final int LEFT_SIDE = 12;
    private static final int RIGHT_SIDE = 13;
    private static final int EIGHT_LEFT_SIDE = 16;
    private static final int TEN_LEFT_SIDE = 17;
    private static final int TWO_RIGHT_SIDE = 18;
    private static final int FOUR_RIGHT_SIDE = 19;

    public static final int MESSAGE_INITIAL_LOCATION_TIMEOUT = 1;
    public static final int LOCATION_TIMEOUT_INTERVAL = 60000;

    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    public SpeakVoice tts;

    private GoogleMap mMap;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mInitialLocReq;
    Location mLocation;
    LocationManager mLM;

    Sensor mRotationSensor;
    SensorManager mSM;

    float[] orientation = new float[3];
    float[] mRotationMatrix = new float[9];
    float mAngle = 0;

    String mProvider;
    Handler mHandler;

    Marker mapInfoMarker;
    Polyline mapInfoPolyline;

    TextView tvNaviDescription, tvMainTitle;
    ImageButton btnFullScreen, btnBackKey, btnFinishNavi;
    ImageView imageDescription;

    boolean isFirst = true;
    boolean isFirstFinishDialog = true;

    IRouteService mRouteService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate");

        setContentView(R.layout.activity_start_navigation);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar_navigation);

        tts = new SpeakVoice();

        tvMainTitle = (TextView) findViewById(R.id.text_main_title);
        imageDescription = (ImageView) findViewById(R.id.image_description);
        tvNaviDescription = (TextView) findViewById(R.id.text_navi_description);

        /*btnBluetooth = (ImageButton) findViewById(R.id.btn_status_bluetooth);

        if (BluetoothConnection.getInstance().getIsConnect() == 1) {
            btnBluetooth.setSelected(true);
        } else {
            btnBluetooth.setSelected(false);
        }*/

        btnFullScreen = (ImageButton) findViewById(R.id.btn_full_screen);
        btnFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionBar actionBar = getSupportActionBar();

                if (actionBar.isShowing()) {
                    actionBar.hide();

                    btnFullScreen.setSelected(true);
                } else {
                    actionBar.show();

                    btnFullScreen.setSelected(false);
                }
            }
        });

        btnBackKey = (ImageButton) findViewById(R.id.btn_back_key);
        btnBackKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
                intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
                startActivity(intent);

//                finish();
            }
        });

        btnFinishNavi = (ImageButton) findViewById(R.id.btn_finish_navigation);
        btnFinishNavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(StartNavigationActivity.this);

                builder.setIcon(null);
                builder.setTitle("내비게이션 안내종료");
                builder.setMessage("현재 내비게이션 안내 중입니다." + "\n" + "정말로 종료하시겠습니까?");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                 /*
                 *  목적지 위도, 경도, searchoption 날리기
                 */
                        PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                        PropertyManager.getInstance().setDestinationLatitude("0");
                        PropertyManager.getInstance().setDestinationLongitude("0");
                        PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

//                Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onCreate : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

                        Intent serviceIntent = new Intent(StartNavigationActivity.this, RouteService.class);
                        stopService(serviceIntent);

                        Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
                        intent.putExtra(KEY_REPLACE_MAIN_FRAGMENT, VALUE_REPLACE_MAIN_FRAGMENT);
                        startActivity(intent);
                    }
                });


                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();
            }
        });

        setFont();

        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (mProvider == null) {
            mProvider = LocationManager.GPS_PROVIDER;
        }

        if (mGoogleApiClient == null) {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.new mGoogleApiClient");

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            createLocationRequest();
        }

        mSM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotationSensor = mSM.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

//        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.mProvider : " + mProvider);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MESSAGE_INITIAL_LOCATION_TIMEOUT:
//                        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.handleMessage.MESSAGE_INITIAL_LOCATION_TIMEOUT");
                        Toast.makeText(StartNavigationActivity.this, "MESSAGE LOCATION TIMEOUT", Toast.LENGTH_SHORT).show();

                        break;

                }
            }
        };
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onNewIntent", Toast.LENGTH_SHORT).show();
    }

    IRouteCallback.Stub callback = new IRouteCallback.Stub() {

        @Override
        public void moveMap(double latitude, double longitude, String moveAction) throws RemoteException {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.moveMap");

            onMoveMap(latitude, longitude, mAngle, moveAction);
        }

        @Override
        public void setImageDescription(int direction) throws RemoteException {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.setImageDescription.direction : " + Integer.toString(direction));

            onSetUpdateImageDescription(direction);
        }

        @Override
        public void setTextDescription(String description, int distance) throws RemoteException {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.setTextDescription.description : " + description + " | distance : " + Integer.toString(distance));

//            tvNaviDescription.setText(Integer.toString(distance) + "m 이후 " + description);
            tvNaviDescription.setText(description);
        }

        @Override
        public void addPointMarker(double latitude, double longitude) throws RemoteException {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.addPointMarker.latitude : " + Double.toString(latitude) + " | longitude : " + Double.toString(longitude));

            LatLng latLng = new LatLng(latitude, longitude);

            MarkerOptions markerOptions = null;
            ArrayList<MarkerOptions> markerOptionsList = new ArrayList<MarkerOptions>();

            markerOptionsList = MapInfoManager.getInstance().getMarkerOptionsInfo();

            if (markerOptionsList.size() > 0) {
                for (int i = 0; i < markerOptionsList.size(); i++) {
                    if (latLng.equals(markerOptionsList.get(i).getPosition())) {
                        markerOptions = markerOptionsList.get(i);
                    }
                }
            }

            if (markerOptions != null) {
                addMapInfoMarker(markerOptions);
            }
        }

        @Override
        public void addPolyline() throws RemoteException {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.addPolyline");

            PolylineOptions addPolylineOptions = MapInfoManager.getInstance().getPolylineOptionsInfo();

            if (addPolylineOptions != null) {
                addMapInfoPolyline(addPolylineOptions);
            }
        }

        @Override
        public void clearMarkerAndPolyline() throws RemoteException {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.clearMarkerAndPolyline");

            mMap = MapInfoManager.getInstance().getMapInfoGoogleMap();

            if (mMap != null) {
                mMap.clear();
            }

            mMap = MapInfoManager.getInstance().getMapInfoInitialGoogleMap();
        }

        @Override
        public void withinRouteLimitDistanceDialog() throws RemoteException {
            if (isFirstFinishDialog) {
                onWithinRouteLimitDistanceDialog();

                isFirstFinishDialog = false;
            }
        }

        @Override
        public void autoFinishNavigationDialog() throws RemoteException {
            if (isFirstFinishDialog) {
                onAutoFinishNavigationDialog();

                isFirstFinishDialog = false;
            }
        }
    };

    private void addMapInfoMarker(MarkerOptions markerOptions) {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.addMapInfoMarker");

        if (mMap != null && markerOptions != null) {
            mapInfoMarker = mMap.addMarker(markerOptions);

            MapInfoManager.getInstance().setMapInfoGoogleMap(mMap);
        }
    }

    private void addMapInfoPolyline(PolylineOptions polylineOptions) {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.addMapInfoPolyline");

        if (mMap != null && polylineOptions != null) {
            mapInfoPolyline = mMap.addPolyline(polylineOptions);

            MapInfoManager.getInstance().setMapInfoGoogleMap(mMap);
        }
    }

    ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onServiceConnected");

            if (mRouteService != null) {
                try {
                    mRouteService.unregisterCallback(callback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                mRouteService = null;
            }

            if (mRouteService == null) {
                mRouteService = IRouteService.Stub.asInterface(service);

                try {
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.onServiceConnected.mRouteService.registerCallback(callback)");
                    mRouteService.registerCallback(callback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onServiceDisconnected");

            mRouteService = null;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onStart");

        if (!mLM.isProviderEnabled(mProvider)) {
            if (isFirst) {
//                Log.d(DEBUG_TAG, "StartNavigationActivity.!mLM.isProviderEnabled(mProvider).isFirst");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

                isFirst = false;

                Toast.makeText(StartNavigationActivity.this, "GPS를 설정해주세요.", Toast.LENGTH_SHORT).show();
            } else {
//                Log.d(DEBUG_TAG, "StartNavigationActivity.!mLM.isProviderEnabled(mProvider).!isFirst");
                /*
                 * 확인 후 처리
                 */
                Toast.makeText(StartNavigationActivity.this, "GPS 설정이 필요합니다.", Toast.LENGTH_SHORT).show();

                /*
                 *  sharedPreferences 값 다 날리는 처리
                 */
                PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

                finish();
            }

            return;
        } else if (mLM != null && mLM.isProviderEnabled(mProvider)) {
            if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(StartNavigationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(StartNavigationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

//            Log.d(DEBUG_TAG, "StartNavigationActivity.onStart.requestSingleUpdate");

            if (!mResolvingError) {  // more about this later
                if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.onStart.mGoogleApiClient.connect");

                    mGoogleApiClient.connect();
                }
            }

            if (mRotationSensor != null) {
                mSM.registerListener(mSensorListener, mRotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

            Intent intent = new Intent(StartNavigationActivity.this, RouteService.class);
            startService(intent);
            bindService(intent, mConn, Service.BIND_AUTO_CREATE);
        }
    }

    /*
     * exception 처리
     */
    @Override
    protected void onStop() {
        super.onStop();
//        Toast.makeText(getContext(), "NavigationFragment.onStop", Toast.LENGTH_SHORT).show();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onStop");

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();

            mGoogleApiClient.disconnect();

//            Toast.makeText(getContext(), "NavigationFragment.onStop.mGoogleApiClient.disconnect", Toast.LENGTH_SHORT).show();
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onStop.mGoogleApiClient.disconnect");
        }

        if (mRotationSensor != null) {
            mSM.unregisterListener(mSensorListener);
        }

        if (mRouteService != null) {
            try {
                mRouteService.unregisterCallback(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            unbindService(mConn);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onResume");

        if (mRouteService != null && isFirstFinishDialog) {
            try {
                boolean success = mRouteService.activateWithinRouteLimitDistance();

                if (success) {
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.onResume.activateWithinRouteLimitDistance.success");
                    onWithinRouteLimitDistanceDialog();

                    isFirstFinishDialog = false;
                } else {
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.onResume.activateWithinRouteLimitDistance.fail");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                boolean success = mRouteService.activateAutoFinishNavigation();

                if (success) {
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.onResume.activateAutoFinishNavigation.success");
                    onAutoFinishNavigationDialog();

                    isFirstFinishDialog = false;
                } else {
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.onResume.activateAutoFinishNavigation.fail");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        setMapInfo();

        String updateTextDescription = MapInfoManager.getInstance().getUpdateTextDescription();

        if (updateTextDescription != null && !updateTextDescription.equals("")) {
            tvNaviDescription.setText(updateTextDescription);
        }

        int updateImageDescription = MapInfoManager.getInstance().getUpdateImageDescription();

        if (updateImageDescription != -1) {
            onSetUpdateImageDescription(updateImageDescription);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onPause");
    }

        @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onDestroy");

            tts.close();
        }

        @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onDestroy");

            super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onBackPressed() {
//        Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onBackPressed : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
        intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
        startActivity(intent);

//        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onMapReady");
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        MapInfoManager.getInstance().setMapInfoInitialGoogleMap(mMap);

        double recentLatitude = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
        double recentLongitude = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());

//        Log.d(DEBUG_TAG, "StartNavigationActivity.onMapReady.recent.onMoveMap");
        onMoveMap(recentLatitude, recentLongitude, 0, MOVE_CAMERA);

        setMapInfo();
    }

    private void setMapInfo() {
        mMap = MapInfoManager.getInstance().getMapInfoGoogleMap();

        if (mMap != null) {
            mMap.clear();
        }

        mMap = MapInfoManager.getInstance().getMapInfoInitialGoogleMap();

        ArrayList<MarkerOptions> mapInfoMarkerOptionsList = MapInfoManager.getInstance().getMarkerOptionsInfo();
        PolylineOptions mapInfoPolylineOptions = MapInfoManager.getInstance().getPolylineOptionsInfo();

        if (mapInfoMarkerOptionsList != null && mapInfoMarkerOptionsList.size() > 0) {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.setMapInfo.mapInfoMarkerOptionsList != null && mapInfoMarkerOptionsList.size() > 0");

            for (int i = 0; i < mapInfoMarkerOptionsList.size(); i++) {
                addMapInfoMarker(mapInfoMarkerOptionsList.get(i));
            }
        } else {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.setMapInfo.mapInfoMarkerOptionsList == null && mapInfoMarkerOptionsList.size() < 0");
        }

        if (mapInfoPolylineOptions != null) {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.setMapInfo.mapInfoPolylineOptions != null");

            addMapInfoPolyline(mapInfoPolylineOptions);
        } else {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.setMapInfo.mapInfoPolylineOptions == null");
        }
    }

    private void onMoveMap(double latitude, double longitude, float bearing, String moveAction) {
        if (mMap != null) {
            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.target(new LatLng(latitude, longitude));
            builder.bearing(bearing);
            builder.zoom(18);
            builder.tilt(45);

            CameraPosition position = builder.build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

            if (moveAction.equals(ANIMATE_CAMERA)) {
                mMap.animateCamera(update);
            } else if (moveAction.equals(MOVE_CAMERA)) {
                mMap.moveCamera(update);
            }
        }
    }

    private void onSetUpdateImageDescription(int direction) {
        if (direction == LEFT_SIDE) {
            imageDescription.setVisibility(View.VISIBLE);
            imageDescription.setImageResource(R.drawable.left_side);
        } else if (direction == RIGHT_SIDE) {
            imageDescription.setVisibility(View.VISIBLE);
            imageDescription.setImageResource(R.drawable.right_side);
        } else if (direction == EIGHT_LEFT_SIDE) {
            imageDescription.setVisibility(View.VISIBLE);
            imageDescription.setImageResource(R.drawable.left_side);
        } else if (direction == TEN_LEFT_SIDE) {
            imageDescription.setVisibility(View.VISIBLE);
            imageDescription.setImageResource(R.drawable.left_side);
        } else if (direction == TWO_RIGHT_SIDE) {
            imageDescription.setVisibility(View.VISIBLE);
            imageDescription.setImageResource(R.drawable.right_side);
        } else if (direction == FOUR_RIGHT_SIDE) {
            imageDescription.setVisibility(View.VISIBLE);
            imageDescription.setImageResource(R.drawable.right_side);
        } else {
            imageDescription.setVisibility(View.GONE);
        }
    }

    protected void createLocationRequest() {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.createLocationRequest");

        if (mInitialLocReq == null) {
            mInitialLocReq = new LocationRequest();
            mInitialLocReq.setNumUpdates(1);
            mInitialLocReq.setInterval(500);
            mInitialLocReq.setMaxWaitTime(10000);
            mInitialLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    protected void startInitialLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mInitialLocReq, mInitialListener);
//        Log.d(DEBUG_TAG, "StartNavigationActivity.startInitialLocationUpdates");

        mHandler.sendEmptyMessageDelayed(MESSAGE_INITIAL_LOCATION_TIMEOUT, LOCATION_TIMEOUT_INTERVAL);
    }

    /*
     * exception 처리
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mInitialListener);
//        Log.d(DEBUG_TAG, "StartNavigationActivity.stopLocationUpdates");

        mHandler.removeMessages(MESSAGE_INITIAL_LOCATION_TIMEOUT);
    }

    @Override
    public void onConnected(Bundle bundle) {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected");
        startInitialLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation != null) {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.mLocation" + " : " + Double.toString(mLocation.getLatitude()) + ", " + Double.toString(mLocation.getLongitude()));
//            Toast.makeText(this, "StartNavigationActivity.onConnected.mLocation" + " : " + mLocation.getLatitude() + ", " + mLocation.getLongitude(), Toast.LENGTH_SHORT).show();

            PropertyManager.getInstance().setRecentLatitude(Double.toString(mLocation.getLatitude()));
            PropertyManager.getInstance().setRecentLongitude(Double.toString(mLocation.getLongitude()));
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.setRecentLocation");
        } else {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.mLocation null");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
////        Toast.makeText(getContext(), "NavigationFragment.onConnectionSuspended", Toast.LENGTH_SHORT).show();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingError) {
            return;
        }

//        Toast.makeText(getContext(), "NavigationFragment.onConnectionFailed", Toast.LENGTH_SHORT).show();
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnectionFailed");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    LocationListener mInitialListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
//            Toast.makeText(getContext(), "NavigationFragment.onLocationChanged", Toast.LENGTH_SHORT).show();
//            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged");

            mHandler.removeMessages(MESSAGE_INITIAL_LOCATION_TIMEOUT);

            if (mMap != null) {
//                Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.mMap != null");

                if (location != null) {
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.location != null");

                    try {
                        boolean success = mRouteService.initialStartRouting();

                        if (success) {
                            PropertyManager.getInstance().setRecentLatitude(Double.toString(location.getLatitude()));
                            PropertyManager.getInstance().setRecentLongitude(Double.toString(location.getLongitude()));

//                            Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.mInitialListener.initialStartRouting : true", Toast.LENGTH_SHORT).show();
//                            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.initialStartRouting : true");
                        } else {
//                            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.initialStartRouting : false");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    try {
                        boolean success = mRouteService.startRouting();

                        if (!success) {
//                            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.another routing running");

//                            Toast.makeText(StartNavigationActivity.this, "another routing running....", Toast.LENGTH_SHORT).show();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

//                    Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.mInitialListener.onLocationChanged +: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged +: " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));

//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.출발지, 목적지 위도 경도 : " + recentLatitude + ", " + recentLongitude + " | " + destinationLatitude + ", " + destinationLongitude);

                    onMoveMap(location.getLatitude(), location.getLongitude(), mAngle, ANIMATE_CAMERA);
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.onMoveMap");
                }
            } else {
//                Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.mMap == null");
            }
        }
    };

    SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ROTATION_VECTOR :
                    SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                    SensorManager.getOrientation(mRotationMatrix, orientation);

                    mAngle = (float) Math.toDegrees(orientation[0]);

                    if (mAngle < 0) {
                        mAngle += 360;
                    }
//
//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mSensorListener.onSensorChanged.mAngle : " + mAngle);

                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void onWithinRouteLimitDistanceDialog() {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onWithinRouteLimitDistanceDialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("내비게이션 안내종료");
        builder.setMessage("목적지가 출발지와 근접합니다.(30m 이내) 내비게이션 안내를 종료합니다.");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                PropertyManager.getInstance().setDestinationLatitude("0");
                PropertyManager.getInstance().setDestinationLongitude("0");
                PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

                Intent serviceIntent = new Intent(StartNavigationActivity.this, RouteService.class);
                stopService(serviceIntent);
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
    }

    private void onAutoFinishNavigationDialog() {
//        Log.d(DEBUG_TAG, "StartNavigationActivity.onAutoFinishNavigationDialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(null);
        builder.setTitle("내비게이션 안내종료");
        builder.setMessage("목적지에 도착했습니다." + "\n" + "내비게이션 안내를 종료합니다.");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                PropertyManager.getInstance().setDestinationLatitude("0");
                PropertyManager.getInstance().setDestinationLongitude("0");
                PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

                Intent serviceIntent = new Intent(StartNavigationActivity.this, RouteService.class);
                stopService(serviceIntent);

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
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.hideInfoWindow();

        return true;
    }

    private void setFont() {
        tvMainTitle.setTypeface(FontManager.getInstance().getTypeface(StartNavigationActivity.this, FontManager.BMJUA));
//        tvNaviDescription.setTypeface(FontManager.getInstance().getTypeface(StartNavigationActivity.this, FontManager.NOTOSANS_M));
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