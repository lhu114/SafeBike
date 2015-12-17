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
import android.util.Log;
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

//    private static final String POINTTYPE_SP = "SP";
//    private static final String POINTTYPE_EP = "EP";
//    private static final String POINTTYPE_GP = "GP";

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

//    Polyline polyline;
//    PolylineOptions polylineOptions;
//    ArrayList<Polyline> polylineList;
//    ArrayList<MarkerOptions> markerOptionsList;
//    MarkerOptions markerOptions;

    Marker mapInfoMarker;
    Polyline mapInfoPolyline;

//    final Map<LatLng, Marker> mMarkerResolver = new HashMap<LatLng, Marker>();
//    final Map<LatLng, String> mBitmapResolver = new HashMap<LatLng, String>();
//
//    ArrayList<LatLng> mPointLatLngList;
//    ArrayList<Float> mOrthogonalDistanceList;
//    ArrayList<Float> mPointDistanceList;
//    ArrayList<BicycleNavigationInfo> mBicycleNaviInfoList;
//    ArrayList<Integer> mPointLatLngIndexList;

//    int gpIndex = 0;

    TextView tvNaviDescription, tvMainTitle;
    ImageButton btnFullScreen, btnBackKey, btnFinishNavi;
    ImageView imageDescription;

    boolean isFirst = true;
    boolean isFirstFinishDialog = true;

    IRouteService mRouteService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate");

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
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle("내비게이션 안내종료");
                builder.setMessage("현재 내비게이션 안내 중입니다. 정말로 종료하시겠습니까");
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
//        builder.setCancelable(false);

                builder.create().show();
            }
        });

        setFont();

//        polylineList = new ArrayList<Polyline>();
//        mPointLatLngList = new ArrayList<LatLng>();
//        mOrthogonalDistanceList = new ArrayList<Float>();
//        mPointDistanceList = new ArrayList<Float>();
//        mBicycleNaviInfoList = new ArrayList<BicycleNavigationInfo>();
//        mPointLatLngIndexList = new ArrayList<Integer>();
//        markerOptionsList = new ArrayList<MarkerOptions>();

        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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

        mSM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotationSensor = mSM.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.mProvider : " + mProvider);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MESSAGE_INITIAL_LOCATION_TIMEOUT:
                        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.handleMessage.MESSAGE_INITIAL_LOCATION_TIMEOUT");
                        Toast.makeText(StartNavigationActivity.this, "MESSAGE_INITIAL_LOCATION_TIMEOUT", Toast.LENGTH_SHORT).show();

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
            Log.d(DEBUG_TAG, "StartNavigationActivity.moveMap");

            onMoveMap(latitude, longitude, mAngle, moveAction);
        }

        @Override
        public void setImageDescription(int direction) throws RemoteException {
            Log.d(DEBUG_TAG, "StartNavigationActivity.setImageDescription.direction : " + Integer.toString(direction));

            onSetUpdateImageDescription(direction);
        }

        @Override
        public void setTextDescription(String description, int distance) throws RemoteException {
            Log.d(DEBUG_TAG, "StartNavigationActivity.setTextDescription.description : " + description + " | distance : " + Integer.toString(distance));

//            tvNaviDescription.setText(Integer.toString(distance) + "m 이후 " + description);
            tvNaviDescription.setText(description);
        }

        @Override
        public void addPointMarker(double latitude, double longitude) throws RemoteException {
            Log.d(DEBUG_TAG, "StartNavigationActivity.addPointMarker.latitude : " + Double.toString(latitude) + " | longitude : " + Double.toString(longitude));

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
            Log.d(DEBUG_TAG, "StartNavigationActivity.addPolyline");

            PolylineOptions addPolylineOptions = MapInfoManager.getInstance().getPolylineOptionsInfo();

            if (addPolylineOptions != null) {
                addMapInfoPolyline(addPolylineOptions);
            }
        }

        @Override
        public void clearMarkerAndPolyline() throws RemoteException {
            Log.d(DEBUG_TAG, "StartNavigationActivity.clearMarkerAndPolyline");

            mMap = MapInfoManager.getInstance().getMapInfoGoogleMap();

            if (mMap != null) {
                mMap.clear();
            }

            mMap = MapInfoManager.getInstance().getMapInfoInitialGoogleMap();
//            removeMapInfoMarkerAndPolyline();
//            MapInfoManager.getInstance().removeMapInfoMarkerAndPolyline();
//            MapInfoManager.getInstance().clearAllMapInfoData();
//
//            for (int i = 0; i < mPointLatLngList.size(); i++) {
//                LatLng latLng = mPointLatLngList.get(i);
//
//                if (mMarkerResolver.size() > 0) {
//                    Marker m = mMarkerResolver.get(latLng);
//                    String bitmapFlag = mBitmapResolver.get(latLng);
//
//                    mMarkerResolver.remove(m);
//                    mBitmapResolver.remove(bitmapFlag);
//
//                    m.remove();
//                }
//            }
//
//            mPointLatLngList.clear();
//            gpIndex = 0;

//            for (Polyline line : polylineList) {
//                line.remove();
//            }

//            polylineOptions = new PolylineOptions();
//            polylineList.clear();

//            if (markerOptionsList.size() > 0) {
//                markerOptionsList.clear();
//            }

            /*if (mapInfoMarker != null) {
                Log.d(DEBUG_TAG, "StartNavigationActivity.clearMarkerAndPolyline.mapInfoMarker.remove");


                mapInfoMarker.remove();
            }

            if (mapInfoPolyline != null) {
                Log.d(DEBUG_TAG, "StartNavigationActivity.clearMarkerAndPolyline.mapInfoPolyline.remove");

                mapInfoPolyline.remove();
            }*/
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
        Log.d(DEBUG_TAG, "StartNavigationActivity.addMapInfoMarker");

        if (mMap != null && markerOptions != null) {
            mapInfoMarker = mMap.addMarker(markerOptions);

            MapInfoManager.getInstance().setMapInfoGoogleMap(mMap);
//            MapInfoManager.getInstance().setMapInfoMarker(markerOptions.getPosition(), mapInfoMarker);
        }
    }

    private void addMapInfoPolyline(PolylineOptions polylineOptions) {
        Log.d(DEBUG_TAG, "StartNavigationActivity.addMapInfoPolyline");

        if (mMap != null && polylineOptions != null) {
            mapInfoPolyline = mMap.addPolyline(polylineOptions);

            MapInfoManager.getInstance().setMapInfoGoogleMap(mMap);
//            MapInfoManager.getInstance().setMapInfoPolyline(mapInfoPolyline);
        }
    }

    ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(DEBUG_TAG, "StartNavigationActivity.onServiceConnected");

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
                    Log.d(DEBUG_TAG, "StartNavigationActivity.onServiceConnected.mRouteService.registerCallback(callback)");
                    mRouteService.registerCallback(callback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                /*if (mRouteService != null) {
                    try {
                        boolean success = mRouteService.simulationStartRouting();
                        Log.d(DEBUG_TAG, "StartNavigationActivity.onServiceConnected.mRouteService != null");

                        if (!success) {
                            Log.d(DEBUG_TAG, "StartNavigationActivity.Override.another simulationStartRouting running");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }*/
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(DEBUG_TAG, "StartNavigationActivity.onServiceDisconnected");

            mRouteService = null;
        }
    };

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
        } else if (mLM != null && mLM.isProviderEnabled(mProvider)) {
            if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(StartNavigationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(StartNavigationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            Log.d(DEBUG_TAG, "StartNavigationActivity.onStart.requestSingleUpdate");

            /*
             *  시연 하고나서 원래대로 복구할것!!!
             */
//            mLM.requestSingleUpdate(mProvider, mInitialListener, null);

            if (!mResolvingError) {  // more about this later
                if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
                    Log.d(DEBUG_TAG, "StartNavigationActivity.onStart.mGoogleApiClient.connect");

                    mGoogleApiClient.connect();

//                    mHandler.sendEmptyMessageDelayed(MESSAGE_INITIAL_LOCATION_TIMEOUT, LOCATION_TIMEOUT_INTERVAL);
                }
            }

            if (mRotationSensor != null) {
                mSM.registerListener(mSensorListener, mRotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

            Intent intent = new Intent(StartNavigationActivity.this, RouteService.class);
            startService(intent);
            bindService(intent, mConn, Service.BIND_AUTO_CREATE);
            /*
             *  TextView 처음으로 set
             */

//            tvNaviDescription.setText("길안내");
//            tts.translate("경로를 탐색 중 입니다.");

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

        /*if (mLM != null) {
            if (Build.VERSION.SDK_INT > 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            Log.d(DEBUG_TAG, "StartNavigationActivity.onStop.removeUpdates.mInitialListener");

            mLM.removeUpdates(mInitialListener);

            mHandler.removeMessages(MESSAGE_INITIAL_LOCATION_TIMEOUT);
        }*/

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();

            mGoogleApiClient.disconnect();

//            Toast.makeText(getContext(), "NavigationFragment.onStop.mGoogleApiClient.disconnect", Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_TAG, "StartNavigationActivity.onStop.mGoogleApiClient.disconnect");
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
        Log.d(DEBUG_TAG, "StartNavigationActivity.onResume");

        if (mRouteService != null && isFirstFinishDialog) {
            try {
                boolean success = mRouteService.activateWithinRouteLimitDistance();

                if (success) {
                    Log.d(DEBUG_TAG, "StartNavigationActivity.onResume.activateWithinRouteLimitDistance.success");
                    onWithinRouteLimitDistanceDialog();

                    isFirstFinishDialog = false;
                } else {
                    Log.d(DEBUG_TAG, "StartNavigationActivity.onResume.activateWithinRouteLimitDistance.fail");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                boolean success = mRouteService.activateAutoFinishNavigation();

                if (success) {
                    Log.d(DEBUG_TAG, "StartNavigationActivity.onResume.activateAutoFinishNavigation.success");
                    onAutoFinishNavigationDialog();

                    isFirstFinishDialog = false;
                } else {
                    Log.d(DEBUG_TAG, "StartNavigationActivity.onResume.activateAutoFinishNavigation.fail");
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

//        ArrayList<MarkerOptions> mapInfoMarkerOptionsList = MapInfoManager.getInstance().getMarkerOptionsInfo();
//        PolylineOptions mapInfoPolylineOptions = MapInfoManager.getInstance().getPolylineOptionsInfo();
//
//        if (mapInfoMarkerOptionsList != null && mapInfoMarkerOptionsList.size() > 0 && mapInfoPolylineOptions != null) {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onResume.mapInfoMarkerOptions != null && mapInfoPolylineOptions != null");
//
//            for (int i = 0; i < mapInfoMarkerOptionsList.size(); i++) {
//                addMapInfoMarker(mapInfoMarkerOptionsList.get(i));
//            }
//
//            addMapInfoPolyline(mapInfoPolylineOptions);
//
//            MapInfoManager.getInstance().clearAllMapInfoData();
//        }
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

            tts.close();
        }

        @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Log.d(DEBUG_TAG, "StartNavigationActivity.onDestroy");

//        outState.putParcelableArrayList("markeroptions", markerOptionsList);

            super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onBackPressed() {
//        Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onBackPressed : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
        intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
        startActivity(intent);

//        finish();
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

        MapInfoManager.getInstance().setMapInfoInitialGoogleMap(mMap);

        double recentLatitude = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
        double recentLongitude = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());

        Log.d(DEBUG_TAG, "StartNavigationActivity.onMapReady.recent.onMoveMap");
        onMoveMap(recentLatitude, recentLongitude, 0, MOVE_CAMERA);

        setMapInfo();
//        setMapReadyMapInfo();
//        ArrayList<MarkerOptions> mapInfoMarkerOptionsList = MapInfoManager.getInstance().getMarkerOptionsInfo();
//        PolylineOptions mapInfoPolylineOptions = MapInfoManager.getInstance().getPolylineOptionsInfo();
//
//        if (mapInfoMarker != null) {
//            mapInfoMarker.remove();
//        }
//
//        if (mapInfoPolyline != null) {
//            mapInfoPolyline.remove();
//        }
//
//        if (mapInfoMarkerOptionsList != null && mapInfoMarkerOptionsList.size() > 0 && mapInfoPolylineOptions != null) {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onResume.mapInfoMarkerOptionsList != null && mapInfoMarkerOptionsList.size() > 0 && mapInfoPolylineOptions != null");
//
//            for (int i = 0; i < mapInfoMarkerOptionsList.size(); i++) {
//                addMapInfoMarker(mapInfoMarkerOptionsList.get(i));
//            }
//
//            addMapInfoPolyline(mapInfoPolylineOptions);
//        } else {
//            Log.d(DEBUG_TAG, "StartNavigationActivity.onResume.mapInfoMarkerOptionsList == null && mapInfoMarkerOptionsList.size() < 0 && mapInfoPolylineOptions == null");
//        }
    }

    private void setMapInfo() {
        /*if (mapInfoMarker != null) {
            mapInfoMarker.remove();
        }

        if (mapInfoPolyline != null) {
            mapInfoPolyline.remove();
        }*/

//        if (MapInfoManager.getInstance().getActivateFindRoute()) {
//            removeMapInfoMarkerAndPolyline();

        mMap = MapInfoManager.getInstance().getMapInfoGoogleMap();

        if (mMap != null) {
            mMap.clear();
        }

        mMap = MapInfoManager.getInstance().getMapInfoInitialGoogleMap();

        ArrayList<MarkerOptions> mapInfoMarkerOptionsList = MapInfoManager.getInstance().getMarkerOptionsInfo();
        PolylineOptions mapInfoPolylineOptions = MapInfoManager.getInstance().getPolylineOptionsInfo();

        if (mapInfoMarkerOptionsList != null && mapInfoMarkerOptionsList.size() > 0) {
            Log.d(DEBUG_TAG, "StartNavigationActivity.setMapInfo.mapInfoMarkerOptionsList != null && mapInfoMarkerOptionsList.size() > 0");

            for (int i = 0; i < mapInfoMarkerOptionsList.size(); i++) {
                addMapInfoMarker(mapInfoMarkerOptionsList.get(i));
            }
        } else {
            Log.d(DEBUG_TAG, "StartNavigationActivity.setMapInfo.mapInfoMarkerOptionsList == null && mapInfoMarkerOptionsList.size() < 0");
        }

        if (mapInfoPolylineOptions != null) {
            Log.d(DEBUG_TAG, "StartNavigationActivity.setMapInfo.mapInfoPolylineOptions != null");

            addMapInfoPolyline(mapInfoPolylineOptions);
        } else {
            Log.d(DEBUG_TAG, "StartNavigationActivity.setMapInfo.mapInfoPolylineOptions == null");
        }

//            MapInfoManager.getInstance().setActivateFindRoute(false);
//        }
    }

    /*private void setMapReadyMapInfo() {
        *//*if (mapInfoMarker != null) {
            mapInfoMarker.remove();
        }

        if (mapInfoPolyline != null) {
            mapInfoPolyline.remove();
        }*//*

        ArrayList<MarkerOptions> mapInfoMarkerOptionsList = MapInfoManager.getInstance().getMarkerOptionsInfo();
        PolylineOptions mapInfoPolylineOptions = MapInfoManager.getInstance().getPolylineOptionsInfo();

        if (mapInfoMarkerOptionsList != null && mapInfoMarkerOptionsList.size() > 0) {
            Log.d(DEBUG_TAG, "StartNavigationActivity.setMapInfo.mapInfoMarkerOptionsList != null && mapInfoMarkerOptionsList.size() > 0");

            for (int i = 0; i < mapInfoMarkerOptionsList.size(); i++) {
                addMapInfoMarker(mapInfoMarkerOptionsList.get(i));
            }
        } else {
            Log.d(DEBUG_TAG, "StartNavigationActivity.setMapInfo.mapInfoMarkerOptionsList == null && mapInfoMarkerOptionsList.size() < 0");
        }

        if (mapInfoPolylineOptions != null) {
            Log.d(DEBUG_TAG, "StartNavigationActivity.setMapInfo.mapInfoPolylineOptions != null");

            addMapInfoPolyline(mapInfoPolylineOptions);
        } else {
            Log.d(DEBUG_TAG, "StartNavigationActivity.setMapInfo.mapInfoPolylineOptions == null");
        }
    }*/

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
        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.createLocationRequest");

        if (mInitialLocReq == null) {
            mInitialLocReq = new LocationRequest();
            mInitialLocReq.setNumUpdates(1);
            mInitialLocReq.setInterval(500);
            mInitialLocReq.setMaxWaitTime(10000);
            mInitialLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

//        if (mIterativeLocReq == null) {
//            mIterativeLocReq = new LocationRequest();
//            mIterativeLocReq.setInterval(2000);
//            mIterativeLocReq.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        }
    }

    protected void startInitialLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mInitialLocReq, mInitialListener);
        Log.d(DEBUG_TAG, "StartNavigationActivity.startInitialLocationUpdates");

        mHandler.sendEmptyMessageDelayed(MESSAGE_INITIAL_LOCATION_TIMEOUT, LOCATION_TIMEOUT_INTERVAL);
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mIterativeLocReq, mIterativeListener);
//        Log.d(DEBUG_TAG, "StartNavigationActivity.starIterativeLocationUpdates");
    }

//    protected void starIterativeLocationUpdates() {
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mIterativeLocReq, mIterativeListener);
//
//        Log.d(DEBUG_TAG, "StartNavigationActivity.starIterativeLocationUpdates");
//    }
    /*
     * exception 처리
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mInitialListener);
        Log.d(DEBUG_TAG, "StartNavigationActivity.stopLocationUpdates");

        mHandler.removeMessages(MESSAGE_INITIAL_LOCATION_TIMEOUT);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected");
        startInitialLocationUpdates();

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

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

//                    tts.translate("안내를 시작합니다.");

//                    starIterativeLocationUpdates();
                    try {
                        boolean success = mRouteService.initialStartRouting();

                        if (success) {
                            PropertyManager.getInstance().setRecentLatitude(Double.toString(location.getLatitude()));
                            PropertyManager.getInstance().setRecentLongitude(Double.toString(location.getLongitude()));

//                            Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.mInitialListener.initialStartRouting : true", Toast.LENGTH_SHORT).show();
                            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.initialStartRouting : true");
                        } else {
                            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.initialStartRouting : false");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

//                    if (mLM != null && mLM.isProviderEnabled(mProvider)) {
//                        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(StartNavigationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                                ContextCompat.checkSelfPermission(StartNavigationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                            return;
//                        }
//
//                        Log.d(DEBUG_TAG, "StartNavigationActivity.onLocationChanged.requestLocationUpdates");
//                        mLM.requestLocationUpdates(mProvider, 1500, 0, mIterativeListener);
//                        mHandler.sendEmptyMessageDelayed(MESSAGE_ITERATIVE_LOCATION_TIMEOUT, LOCATION_TIMEOUT_INTERVAL);
////                        mLM.removeUpdates(mInitialListener);
//                    }

                    try {
                        boolean success = mRouteService.startRouting();

                        if (!success) {
                            Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.another routing running");

//                            Toast.makeText(StartNavigationActivity.this, "another routing running....", Toast.LENGTH_SHORT).show();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

//                    Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.mInitialListener.onLocationChanged +: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged +: " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));

//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.출발지, 목적지 위도 경도 : " + recentLatitude + ", " + recentLongitude + " | " + destinationLatitude + ", " + destinationLongitude);

//                    findRoute();

                    onMoveMap(location.getLatitude(), location.getLongitude(), mAngle, ANIMATE_CAMERA);
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.onMoveMap");
                }
            } else {
                Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.mMap == null");
//                mCacheLocation = location;
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
        Log.d(DEBUG_TAG, "StartNavigationActivity.onWithinRouteLimitDistanceDialog");

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
        Log.d(DEBUG_TAG, "StartNavigationActivity.onAutoFinishNavigationDialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("내비게이션 안내종료");
        builder.setMessage("목적지에 도착했습니다. 내비게이션 안내를 종료합니다.");
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

   /* private void removeMapInfoMarkerAndPolyline() {
        ArrayList<LatLng> mPointLatLngList = new ArrayList<LatLng>();
        mPointLatLngList = MapInfoManager.getInstance().getMapInfoPointLatLngList();

        if (mPointLatLngList != null && mPointLatLngList.size() > 0) {
            for (int i = 0; i < mPointLatLngList.size(); i++) {
                LatLng latLng = mPointLatLngList.get(i);

                Map<LatLng, Marker> mMarkerResolver = new HashMap<LatLng, Marker>();
                mMarkerResolver = MapInfoManager.getInstance().getMapInfoMarker();

                if (mMarkerResolver.size() > 0) {
                    Marker m = mMarkerResolver.get(latLng);

                    m.remove();
                    mMarkerResolver.remove(m);
                    mMarkerResolver.clear();
                }
            }

            mPointLatLngList.clear();
        }

        ArrayList<Polyline> polylineList = new ArrayList<Polyline>();
        polylineList = MapInfoManager.getInstance().getMapInfoPolyline();

        if (polylineList != null && polylineList.size() > 0) {
            for (int i = 0; i < polylineList.size(); i++) {
                Polyline line = polylineList.get(i);

                line.remove();
            }

            polylineList.clear();
        }

        MapInfoManager.getInstance().removeMapInfoMarkerAndPolyline();
    }*/
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
