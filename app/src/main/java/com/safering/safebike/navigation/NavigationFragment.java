package com.safering.safebike.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.TypedValue;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.property.PropertyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class NavigationFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener {
    private static final String DEBUG_TAG = "safebike";

    private static final int REQUEST_SEARCH_POI = 1002;
    private static final String KEY_POI_OBJECT = "poiobject";

    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private static final String MOVE_CAMERA = "movecamera";
    private static final String ANIMATE_CAMERA = "animatecamera";
    private static String LOCATION_CHANGE_FLAG = "on";

    private static final String ON = "on";
    private static final String OFF = "off";

    private static final String KEY_DESTINATION_POI_NAME = "destinationpoiname";

    private GoogleMap mMap;
    String mProvider;

    GoogleApiClient mGoogleApiClient;
    Location mLocation, mCacheLocation;
    LocationRequest mLocationRequest;
    LocationManager mLM;

    Sensor mRotationSensor;
    SensorManager mSM;

    float[] orientation = new float[3];
    float[] mRotationMatrix = new float[9];
    float mAngle;

    final Map<POI, Marker> mPOIMarkerResolver = new HashMap<POI, Marker>();
    final Map<LatLng, Marker> mLcMarkerResolver = new HashMap<LatLng, Marker>();

    ArrayList<POI> mPOIMarkerList;
    ArrayList<LatLng> mLcMarkerList;

    View view;
    RelativeLayout relativeLayout;
    LinearLayout addressLayout;
    FrameLayout frameLayout;

    TextView textSafeBikeMainTitle;
    TextView tvPOIAddress;
    TextView tvPOIName;
    ImageButton btnFullScreen, btnFindRoute, btnFwdSearch, btnCurrentLoc;

    String definePOIName = null;

    boolean isBasicLocBtnOn = true;
    boolean isCurrentLocBtnOn = false;
    boolean isChangeLocBtnOn = false;
    boolean isActivateRotation = false;
    boolean isActivateCurrentLocBtn = false;
    boolean isCheckGetRecentLoc = false;
    boolean isFirst = true;

    private static final int BASIC_BUTTON_CURRENTLOC_MARGIN_RIGHT = 10;
    private static final int BASIC_BUTTON_CURRENTLOC_MARGIN_BOTTOM = 12;
    private static final int CHANGE_BUTTON_CURRENTLOC_MARGIN_BOTTOM = 147;

    int marginRight, marginBottom;

    public NavigationFragment() {
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            createLocationRequest();
        }

        mSM = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mRotationSensor = mSM.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mPOIMarkerList = new ArrayList<POI>();
        mLcMarkerList = new ArrayList<LatLng>();

        if (mProvider == null) {

            mProvider = LocationManager.GPS_PROVIDER;

        }

        mLM = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            view = inflater.inflate(R.layout.fragment_navigation, container, false);

            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map);
            mapFragment.getMapAsync(this);

            relativeLayout = (RelativeLayout) view.findViewById(R.id.layout_navigationfragment);
            frameLayout = (FrameLayout) ((MainActivity) getActivity()).findViewById(R.id.framelayout_toolbar);

            textSafeBikeMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_safebike_main_title);
            btnFwdSearch = (ImageButton) ((MainActivity) getActivity()).findViewById(R.id.btn_fwd_search);

            addressLayout = (LinearLayout) view.findViewById(R.id.layout_address);
            addressLayout.setVisibility(View.GONE);

            tvPOIAddress = (TextView) view.findViewById(R.id.text_poi_address);
            tvPOIName = (TextView) view.findViewById(R.id.text_poi_name);

            btnFullScreen = (ImageButton) view.findViewById(R.id.btn_full_screen);
            btnFindRoute = (ImageButton) view.findViewById(R.id.btn_find_route);
            btnFindRoute.setVisibility(View.GONE);
            btnFindRoute.setEnabled(false);

            if (View.GONE == btnFindRoute.getVisibility()) {
                LOCATION_CHANGE_FLAG = ON;

                MainActivity.FABFINDROUTE_ONOFF_FLAG = OFF;
            } else if (View.VISIBLE == btnFindRoute.getVisibility()) {
                MainActivity.FABFINDROUTE_ONOFF_FLAG = ON;
            }

            btnFwdSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ParentRctFvActivity.class);
                    startActivityForResult(intent, REQUEST_SEARCH_POI);

                    LOCATION_CHANGE_FLAG = OFF;
                }
            });

            btnFullScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();

                    if (actionBar.isShowing()) {
                        actionBar.hide();
                        frameLayout.setVisibility(View.GONE);
                        textSafeBikeMainTitle.setVisibility(View.GONE);


                        btnFullScreen.setSelected(true);
                    } else {
                        actionBar.show();
                        frameLayout.setVisibility(View.VISIBLE);
                        textSafeBikeMainTitle.setVisibility(View.VISIBLE);

                        btnFullScreen.setSelected(false);
                    }
                }
            });

            btnCurrentLoc = (ImageButton) view.findViewById(R.id.btn_crt_location);
            btnCurrentLoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*
                     * 현재 위치
                     */
                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    if (isBasicLocBtnOn) {

                        if (mLocation != null) {

                            moveMap(mLocation.getLatitude(), mLocation.getLongitude(), ANIMATE_CAMERA);

                            PropertyManager.getInstance().setRecentLatitude(Double.toString(mLocation.getLatitude()));
                            PropertyManager.getInstance().setRecentLongitude(Double.toString(mLocation.getLongitude()));

                        }

                        isBasicLocBtnOn = false;
                        isCurrentLocBtnOn = true;
                        isActivateCurrentLocBtn = true;

                        btnCurrentLoc.setBackgroundResource(R.drawable.button_my_location_selector);
                    } else if (isCurrentLocBtnOn) {

                        if (mRotationSensor != null) {
                            mSM.registerListener(mSensorListener, mRotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
                        }

                        isCurrentLocBtnOn = false;
                        isActivateRotation = true;
                        isChangeLocBtnOn = true;

                        btnCurrentLoc.setBackgroundResource(R.drawable.button_change_direction_selector);
                    } else if (isChangeLocBtnOn) {

                        if (mRotationSensor != null) {
                            mSM.unregisterListener(mSensorListener);
                        }

                        isChangeLocBtnOn = false;
                        isActivateRotation = false;
                        isBasicLocBtnOn = true;

                        btnCurrentLoc.setBackgroundResource(R.drawable.button_basic_location_selector);

                        setBearingMoveMap(0);
                    }
                }
            });

//            setFont();

        } catch (InflateException e) {            /*
             * 구글맵 View가 이미 inflate되어 있는 상태이므로, 에러를 무시합니다.
             */
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        btnFwdSearch.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mResolvingError) {  // more about this later
            if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }

        if (!mLM.isProviderEnabled(mProvider)) {
            if (isFirst) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

                isFirst = false;

                Toast.makeText(getContext(), "GPS를 설정해주세요.", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(getContext(), "GPS 설정이 필요합니다.", Toast.LENGTH_SHORT).show();

                getActivity().getSupportFragmentManager().popBackStack();
            }

            return;
        }

    }

    /*
     * exception 처리
     */
    @Override
    public void onStop() {
        super.onStop();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }


        btnFwdSearch.setVisibility(View.GONE);
    }

    /*
     * 프래그먼트가 화면에서 사라질 때 프래그먼트의 뷰를 컨테이너 뷰에서 제거
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();

            if (parent != null) {
                parent.removeView(view);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SEARCH_POI) {
            if (resultCode == Activity.RESULT_OK) {

                POI poi = (POI) data.getSerializableExtra(KEY_POI_OBJECT);


                if (poi != null) {
                    setBtnCurrentLocMarginChange(BASIC_BUTTON_CURRENTLOC_MARGIN_RIGHT, CHANGE_BUTTON_CURRENTLOC_MARGIN_BOTTOM);

                    tvPOIName.setText(poi.name);
                    tvPOIAddress.setText(getDefinePOIAddress(poi));

                    definePOIName = poi.name;
                    moveMap(poi.getLatitude(), poi.getLongitude(), ANIMATE_CAMERA);

                    clearALLMarker();

                    addPOIMarker(poi);
                    mPOIMarkerList.add(poi);

                    PropertyManager.getInstance().setDestinationLatitude(Double.toString(poi.getLatitude()));
                    PropertyManager.getInstance().setDestinationLongitude(Double.toString(poi.getLongitude()));

                    addressLayout.setVisibility(View.VISIBLE);
                    btnFindRoute.setVisibility(View.VISIBLE);

                    if (View.VISIBLE == btnFindRoute.getVisibility()) {
                        LOCATION_CHANGE_FLAG = OFF;

                        MainActivity.FABFINDROUTE_ONOFF_FLAG = ON;
                    }

                    if (!definePOIName.equals("") && definePOIName != null) {
                        btnFindRoute.setEnabled(true);
                    }

                    btnFindRoute.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                    /*
                     * 위에서 받은 데이터 전달 출발지, 목적지 다 보내야함
                     */
//
                            Intent intent = new Intent(getContext(), SelectRouteActivity.class);
                            intent.putExtra(KEY_DESTINATION_POI_NAME, definePOIName);
                            startActivity(intent);
                        }
                    });
                } else {
                    definePOIName = null;
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        if (mCacheLocation != null) {
            moveMap(mCacheLocation.getLatitude(), mCacheLocation.getLongitude(), MOVE_CAMERA);

            mCacheLocation = null;
        } else {
            double recentLatitude = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
            double recentLongitude = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());

            moveMap(recentLatitude, recentLongitude, MOVE_CAMERA);
        }
    }

    protected void createLocationRequest() {

        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setNumUpdates(1);
            mLocationRequest.setInterval(5000);
            mLocationRequest.setMaxWaitTime(10000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mListener);

    }

    /*
     * exception 처리
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mListener);

    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation != null) {
            PropertyManager.getInstance().setRecentLatitude(Double.toString(mLocation.getLatitude()));
            PropertyManager.getInstance().setRecentLongitude(Double.toString(mLocation.getLongitude()));
        }
//        Toast.makeText(getContext(), "NavigationFragment.onConnected.mLocation" + " : " + mLocation.getLatitude() + ", " + mLocation.getLongitude(), Toast.LENGTH_SHORT).show();

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mMap != null) {

                if (location != null) {
                    if (LOCATION_CHANGE_FLAG.equals(ON)) {
                        moveMap(location.getLatitude(), location.getLongitude(), MOVE_CAMERA);

                        PropertyManager.getInstance().setRecentLatitude(Double.toString(location.getLatitude()));
                        PropertyManager.getInstance().setRecentLongitude(Double.toString(location.getLongitude()));

                        oldLocation.setLatitude(Double.parseDouble(PropertyManager.getInstance().getRecentLatitude()));
                        oldLocation.setLongitude(Double.parseDouble(PropertyManager.getInstance().getRecentLongitude()));

                        isCheckGetRecentLoc = true;

                    }
                }
            } else {
                mCacheLocation = location;
            }
        }
    };

    SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ROTATION_VECTOR:
                    SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                    SensorManager.getOrientation(mRotationMatrix, orientation);

                    mAngle = (float) Math.toDegrees(orientation[0]);

                    if (mAngle < 0) {
                        mAngle += 360;
                    }

                    if (isActivateRotation) {
                        setBearingMoveMap(mAngle);
                    }


                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public void onMapClick(LatLng latLng) {
        /*
         *  마커도 내리기
         */
        clearALLMarker();

        setBtnCurrentLocMarginChange(BASIC_BUTTON_CURRENTLOC_MARGIN_RIGHT, BASIC_BUTTON_CURRENTLOC_MARGIN_BOTTOM);

        addressLayout.setVisibility(View.GONE);
        btnFindRoute.setVisibility(View.GONE);
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        setBtnCurrentLocMarginChange(BASIC_BUTTON_CURRENTLOC_MARGIN_RIGHT, CHANGE_BUTTON_CURRENTLOC_MARGIN_BOTTOM);


        if (latLng != null) {
            PropertyManager.getInstance().setDestinationLatitude(Double.toString(latLng.latitude));
            PropertyManager.getInstance().setDestinationLongitude(Double.toString(latLng.longitude));

            clearALLMarker();

            addLongClickMarker(latLng);
            mLcMarkerList.add(latLng);

            NavigationNetworkManager.getInstance().searchReverseGeo(getContext(), latLng, new NavigationNetworkManager.OnResultListener<AddressInfo>() {
                @Override
                public void onSuccess(AddressInfo result) {
                    if (result != null) {

                        String defineAddress = getDefineRvsGeoAddress(result);


                        if (!result.buildingName.equals("")) {
                            tvPOIName.setText(result.buildingName);
                            tvPOIAddress.setText(defineAddress);

                            definePOIName = result.buildingName;
                        } else {
                            tvPOIName.setText(defineAddress);
                            tvPOIAddress.setText("");

                            definePOIName = defineAddress;
                        }

                        if (!definePOIName.equals("") && definePOIName != null) {
                            btnFindRoute.setEnabled(true);
                        }

                    }
                }

                @Override
                public void onFail(int code) {

                    tvPOIName.setText("");
                    tvPOIAddress.setText("");

                    definePOIName = null;

                    btnFindRoute.setEnabled(false);
                }
            });

            addressLayout.setVisibility(View.VISIBLE);
            btnFindRoute.setVisibility(View.VISIBLE);

            MainActivity.FABFINDROUTE_ONOFF_FLAG = ON;

            btnFindRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), SelectRouteActivity.class);
                    intent.putExtra(KEY_DESTINATION_POI_NAME, definePOIName);
                    startActivity(intent);
                }
            });
        }
    }

    private void moveMap(double latitude, double longitude, String moveAction) {
        if (mMap != null) {
            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.target(new LatLng(latitude, longitude));
            builder.zoom(16);

            CameraPosition position = builder.build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

            if (moveAction.equals(ANIMATE_CAMERA)) {
                mMap.animateCamera(update);
            } else if (moveAction.equals(MOVE_CAMERA)) {
                mMap.moveCamera(update);
            }
        }
    }

    private void setBearingMoveMap(float angle) {
        if (mMap != null) {
            CameraPosition oldPos = mMap.getCameraPosition();

            CameraPosition pos = CameraPosition.builder(oldPos).bearing(angle).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
        }
    }

    private void addLongClickMarker(LatLng latLng) {
        MarkerOptions options = new MarkerOptions();
        /*
         * 어떤 값으로 위도 경도 넘길지는 고민
         */
        options.position(new LatLng(latLng.latitude, latLng.longitude));
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrival));
        options.anchor(0.5f, 1.0f);
        options.draggable(false);
        Marker m = mMap.addMarker(options);
        m.hideInfoWindow();

        mLcMarkerResolver.put(latLng, m);
    }

    private void addPOIMarker(POI poi) {
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(poi.noorLat, poi.noorLon));
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrival));
        options.anchor(0.5f, 1.0f);
        options.title(poi.name);
        options.draggable(false);

        Marker m = mMap.addMarker(options);

        mPOIMarkerResolver.put(poi, m);
    }

    private void clearALLMarker() {
        for (int i = 0; i < mPOIMarkerList.size(); i++) {
            POI poi = mPOIMarkerList.get(i);
            Marker m = mPOIMarkerResolver.get(poi);
            mPOIMarkerResolver.remove(m);
            m.remove();
        }

        mPOIMarkerList.clear();

        for (int i = 0; i < mLcMarkerList.size(); i++) {
            LatLng latLng = mLcMarkerList.get(i);
            Marker m = mLcMarkerResolver.get(latLng);
            mLcMarkerResolver.remove(m);
            m.remove();
        }

        mLcMarkerList.clear();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.hideInfoWindow();

        return true;
    }

    private String getDefinePOIAddress(POI poi) {
        String defineAddress = null;

        if (!poi.firstNo.equals("") && !poi.secondNo.equals("")) {

            defineAddress = poi.getAddress() + " " + poi.getDetailAddress();
        } else if (!poi.firstNo.equals("") && poi.secondNo.equals("")) {

            defineAddress = poi.getAddress() + " " + poi.firstNo;
        } else {

            defineAddress = poi.getAddress();
        }

        return defineAddress;
    }

    private String getDefineRvsGeoAddress(AddressInfo addressInfo) {
        String defineAddress = null;

        if (!addressInfo.bunji.equals("") && !addressInfo.bunji.equals(null)) {
            defineAddress = addressInfo.city_do + " " + addressInfo.gu_gun + " " + addressInfo.legalDong + " " + addressInfo.bunji;
        } else {
            defineAddress = addressInfo.city_do + " " + addressInfo.gu_gun + " " + addressInfo.legalDong;
        }

        return defineAddress;
    }

    public void setBtnFindRouteChange() {
        clearALLMarker();

        setBtnCurrentLocMarginChange(BASIC_BUTTON_CURRENTLOC_MARGIN_RIGHT, BASIC_BUTTON_CURRENTLOC_MARGIN_BOTTOM);

        addressLayout.setVisibility(View.GONE);
        btnFindRoute.setVisibility(View.GONE);
    }

    private void setBtnCurrentLocMarginChange(int changeMarginRight, int changeMarginBottom) {
        marginRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, changeMarginRight, this.getResources().getDisplayMetrics());
        marginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, changeMarginBottom, this.getResources().getDisplayMetrics());

        MarginLayoutParams margin = new MarginLayoutParams(btnCurrentLoc.getLayoutParams());
        margin.setMargins(0, 0, marginRight, marginBottom);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(margin);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        btnCurrentLoc.setLayoutParams(params);
    }

    private void setFont() {
        tvPOIName.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS_M));
        tvPOIAddress.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
    }

    double oldLatitude = 0;
    double oldLongitude = 0;

    Location oldLocation = new Location(LocationManager.GPS_PROVIDER);

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (isCheckGetRecentLoc) {
            double latitude = cameraPosition.target.latitude;
            double longitude = cameraPosition.target.longitude;

            Location newLocation = new Location(LocationManager.GPS_PROVIDER);
            newLocation.setLatitude(latitude);
            newLocation.setLongitude(longitude);

            if (isActivateCurrentLocBtn) {
                oldLatitude = latitude;
                oldLongitude = longitude;

                oldLocation.setLatitude(oldLatitude);
                oldLocation.setLongitude(oldLongitude);

                isActivateCurrentLocBtn = false;
            }
        }
    }
}

