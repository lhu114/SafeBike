package com.safering.safebike.navigation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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


public class StartNavigationActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener {
    private static final String DEBUG_TAG = "safebike";

    private static final String SERVICE_FINISH = "finish";

    private static final String KEY_POP_NAVIGATION_FRAGMENT = "popNavigation";
    private static final String VALUE_POP_NAVIGATION_FRAGMENT = "popNavigation";
    private static final String KEY_REPLACE_MAIN_FRAGMENT = "replaceMainFragment";
    private static final String VALUE_REPLACE_MAIN_FRAGMENT = "replaceMainFragment";

    private static final String MOVE_CAMERA = "movecamera";
    private static final String ANIMATE_CAMERA = "animatecamera";

    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private static final int BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION = 3;
    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_POINT = "Point";
    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING = "LineString";

    private static final String POINTTYPE_SP = "SP";
    private static final String POINTTYPE_EP = "EP";
    private static final String POINTTYPE_GP = "GP";

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLocation, mCacheLocation;
    LocationRequest mInitialLocReq, mIterativeLocReq;

    Polyline polyline;
    PolylineOptions options;
    ArrayList<Polyline> polylineList;

    final Map<LatLng, Marker> mMarkerResolver = new HashMap<LatLng, Marker>();
    final Map<LatLng, String> mBitmapResolver = new HashMap<LatLng, String>();
    final Map<LatLng, BicycleProperties> mPropertiesResolver = new HashMap<LatLng, BicycleProperties>();

    ArrayList<LatLng> mLatLngList;

    double recentLatitude;
    double recentLongitude;
    double destinationLatitude;
    double destinationLongitude;
    double centerLatitude;
    double centerLongitude;

    int gpIndex = 0;

    Sensor mRotationSensor;
    SensorManager mSM;

    float[] orientation = new float[3];
    float[] mRotationMatrix = new float[9];
    float mAngle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate");
        setContentView(R.layout.activity_start_navigation);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (mGoogleApiClient == null) {
            Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.new mGoogleApiClient");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            createLocationRequest();
        }

        polylineList = new ArrayList<Polyline>();
        mLatLngList = new ArrayList<LatLng>();

        mSM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotationSensor = mSM.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

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
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
        Toast.makeText(this, "StartNavigationActivity.onNewIntent", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Toast.makeText(getContext(), "NavigationFragment.onStart", Toast.LENGTH_SHORT).show();
        Log.d(DEBUG_TAG, "StartNavigationActivity.onStart");
        if (!mResolvingError) {  // more about this later
            if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
                Log.d(DEBUG_TAG, "StartNavigationActivity.onStart.mGoogleApiClient.connect");
                mGoogleApiClient.connect();
            }
        }

        if (mRotationSensor != null) {
            mSM.registerListener(mSensorListener, mRotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
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

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();

            mGoogleApiClient.disconnect();
//            Toast.makeText(getContext(), "NavigationFragment.onStop.mGoogleApiClient.disconnect", Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_TAG, "StartNavigationActivity.onStop.mGoogleApiClient.disconnect");
        }

        if (mRotationSensor != null) {
            mSM.unregisterListener(mSensorListener);
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
        switch (item.getItemId()){
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
        moveMap(recentLatitude, recentLongitude, MOVE_CAMERA);
    }

    private void moveMap(double latitude, double longitude, String moveAction) {
        if (mMap != null) {
            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.target(new LatLng(latitude, longitude));
            builder.bearing(mAngle);
            builder.zoom(17);
            builder.tilt(45);
            Log.d(DEBUG_TAG, "StartNavigationActivity.moveMap.bearing.mAngle : " + mAngle);

            CameraPosition position = builder.build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

            if (moveAction.equals(ANIMATE_CAMERA)) {
                mMap.animateCamera(update);
            } else if(moveAction.equals(MOVE_CAMERA)) {
                mMap.moveCamera(update);
            }
        }
    }

    protected void createLocationRequest() {
        Log.d(DEBUG_TAG, "StartNavigationActivity.onCreate.createLocationRequest");

        if (mInitialLocReq == null) {
            mInitialLocReq = new LocationRequest();
            mInitialLocReq.setNumUpdates(1);
            mInitialLocReq.setInterval(500);
            mInitialLocReq.setMaxWaitTime(1000);
            mInitialLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        if (mIterativeLocReq == null) {
            mIterativeLocReq = new LocationRequest();
            mIterativeLocReq.setInterval(2000);
            mIterativeLocReq.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
    }

    protected void startInitialLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mInitialLocReq, mInitialListener);
        Log.d(DEBUG_TAG, "StartNavigationActivity.startInitialLocationUpdates");

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mIterativeLocReq, mIterativeListener);
        Log.d(DEBUG_TAG, "StartNavigationActivity.starIterativeLocationUpdates");
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
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mIterativeListener);

        Log.d(DEBUG_TAG, "StartNavigationActivity.stopLocationUpdates");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected");
        startInitialLocationUpdates();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation != null) {
            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.mLocation" + " : " + Double.toString(mLocation.getLatitude()) + ", " + Double.toString(mLocation.getLongitude()));
            Toast.makeText(this, "StartNavigationActivity.onConnected.mLocation" + " : " + mLocation.getLatitude() + ", " + mLocation.getLongitude(), Toast.LENGTH_SHORT).show();

            PropertyManager.getInstance().setRecentLatitude(Double.toString(mLocation.getLatitude()));
            PropertyManager.getInstance().setRecentLongitude(Double.toString(mLocation.getLongitude()));
            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.setRecentLocation");
        } else {
            Log.d(DEBUG_TAG, "StartNavigationActivity.onConnected.mLocation null");
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
//        Toast.makeText(getContext(), "NavigationFragment.onConnectionSuspended", Toast.LENGTH_SHORT).show();
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

            if (mMap != null) {
                Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.mMap != null");

                if (location != null) {
//                    starIterativeLocationUpdates();
                    Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.mInitialListener.onLocationChanged +: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged +: " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));

                    PropertyManager.getInstance().setRecentLatitude(Double.toString(location.getLatitude()));
                    PropertyManager.getInstance().setRecentLongitude(Double.toString(location.getLongitude()));

                    recentLatitude = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
                    recentLongitude = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());
                    destinationLatitude = Double.parseDouble(PropertyManager.getInstance().getDestinationLatitude());
                    destinationLongitude = Double.parseDouble(PropertyManager.getInstance().getDestinationLongitude());

                    centerLatitude = (recentLatitude + destinationLatitude) / 2;
                    centerLongitude = (recentLongitude + destinationLongitude) / 2;

                    final double startX = recentLongitude;
                    final double startY = recentLatitude;
                    final double endX = destinationLongitude;
                    final double endY = destinationLatitude;
                    final int searchOption = PropertyManager.getInstance().getFindRouteSearchOption();

                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.출발지, 목적지 위도 경도 : " + recentLatitude + ", " + recentLongitude + " | " + destinationLatitude + ", " + destinationLongitude);

                    if (startX != 0 && startY != 0 && endX != 0 && endY != 0) {
                        NavigationNetworkManager.getInstance().findRoute(StartNavigationActivity.this, startX, startY, endX, endY, searchOption,
                                new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
                                    @Override
                                    public void onSuccess(BicycleRouteInfo result) {
                                        Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess");
                                        if (result.features != null && result.features.size() > 0) {
                                            clearAllMarker();
                                            clearAllPolyline();

                                            int totalTime = result.features.get(0).properties.totalTime;
                                            int totalDistance = result.features.get(0).properties.totalDistance;

                                            options = new PolylineOptions();

                                            for (BicycleFeature feature : result.features) {
                                                if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING)) {
                                                    double[] coord = feature.geometry.coordinates;

                                                    for (int i = 0; i < coord.length; i += 2) {
                                                        options.add(new LatLng(coord[i + 1], coord[i]));
                                                    }
                                                }
                                            }

                                            for (BicycleFeature feature : result.features) {
                                                if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_SP)) {
                                                    double[] coord = feature.geometry.coordinates;

                                                    for (int i = 0; i < coord.length; i += 2) {
                                                        LatLng latLng = new LatLng(coord[i + 1], coord[i]);
                                                        BicycleProperties properties = feature.properties;
                                                        Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.BicycleProperties : " + properties.description);

                                                        addPointMarker(latLng, properties, POINTTYPE_SP);

                                                        mLatLngList.add(latLng);
                                                    }
                                                } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_EP)) {
                                                    double[] coord = feature.geometry.coordinates;

                                                    for (int i = 0; i < coord.length; i += 2) {
                                                        LatLng latLng = new LatLng(coord[i + 1], coord[i]);
                                                        BicycleProperties properties = feature.properties;
                                                        Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.BicycleProperties : " + properties.description);

                                                        addPointMarker(latLng, properties, POINTTYPE_EP);

                                                        mLatLngList.add(latLng);
                                                    }
                                                } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_GP)) {
                                                    double[] coord = feature.geometry.coordinates;

                                                    for (int i = 0; i < coord.length; i += 2) {
                                                        LatLng latLng = new LatLng(coord[i + 1], coord[i]);
                                                        BicycleProperties properties = feature.properties;
                                                        Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.findRoute.onSuccess.BicycleProperties :" + properties.description);

                                                        addPointMarker(latLng, properties, POINTTYPE_GP);

                                                        mLatLngList.add(latLng);
                                                    }
                                                }
                                            }

                                            if (options != null) {
                                                options.color(Color.BLUE);
                                                options.width(10);
                                                polyline = mMap.addPolyline(options);

                                                polylineList.add(polyline);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFail(int code) {

                                    }
                                });
                    }

                    moveMap(location.getLatitude(), location.getLongitude(), ANIMATE_CAMERA);
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.moveMap");
                }
            } else {
                Log.d(DEBUG_TAG, "StartNavigationActivity.mInitialListener.onLocationChanged.mMap == null");
                mCacheLocation = location;
            }
        }
    };

    LocationListener mIterativeListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged");

            if (mMap != null) {
                Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.mMap != null");

                if (location != null ) {
                    Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.mIterativeListener.onLocationChanged : " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged +: " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));

                    PropertyManager.getInstance().setRecentLatitude(Double.toString(location.getLatitude()));
                    PropertyManager.getInstance().setRecentLongitude(Double.toHexString(location.getLongitude()));

                    moveMap(location.getLatitude(), location.getLongitude(), ANIMATE_CAMERA);
                    Log.d(DEBUG_TAG, "StartNavigationActivity.mIterativeListener.onLocationChanged.moveMap");
                }
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

//                    Log.d(DEBUG_TAG, "StartNavigationActivity.mSensorListener.onSensorChanged.mAngle : " + mAngle);

                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.hideInfoWindow();

        return true;
    }

    private void addPointMarker(LatLng latLng, BicycleProperties properties,  String bitmapFlag) {
        MarkerOptions options  = new MarkerOptions();
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
        for (int i = 0; i < mLatLngList.size(); i++) {
            LatLng latLng = mLatLngList.get(i);

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

        gpIndex = 0;

        mLatLngList.clear();
    }

    private void clearAllPolyline() {
        for (Polyline line : polylineList) {
            line.remove();
        }

        polylineList.clear();
    }
}
