package com.safering.safebike.navigation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class SelectRouteActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String DEBUG_TAG = "safebike";

    private static final int REQUEST_START_NAVIGATION = 1003;
    private static final String SERVICE_RUNNING = "running";

    private static final String MOVE_CAMERA = "movecamera";
    private static final String ANIMATE_CAMERA = "animatecamera";

    private static final int BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION = 0;
    private static final int BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION = 3;

    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_POINT = "Point";
    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING = "LineString";

    private static final String POINTTYPE_SP = "SP";
    private static final String POINTTYPE_EP = "EP";
    private static final String POINTTYPE_GP = "GP";

    private static final String ITERATIVE_FLAG = "iterative";
    private static final String INITIAL_FLAG = "initial;";

    private static final String KEY_DESTINATION_POI_NAME = "destinationpoiname";
    private static final int SUCCESS = 200;
    private static final int BAD_REQUEST = 400;
    private static final int ERROR_CODE_ACTIVATE_ROUTE_LIMIT_DISTANCE = 3209;

    private static final String EXIST = "200";
    private static final String NOT_EXIST = "201";

    private GoogleMap mMap;
    String mProvider;
    LocationManager mLM;

    Polyline polyline;
    PolylineOptions laneOptions, minOptions;
    ArrayList<Polyline> polylineList;

    double recentLatitude;
    double recentLongitude;
    double destinationLatitude;
    double destinationLongitude;
    double centerLatitude;
    double centerLongitude;

    final Map<LatLng, Marker> mLaneMarkerResolver = new HashMap<LatLng, Marker>();
    final Map<LatLng, Marker> mMinMarkerResolver = new HashMap<LatLng, Marker>();
    final Map<LatLng, String> mLaneBitmapResolver = new HashMap<LatLng, String>();
    final Map<LatLng, String> mMinBitmapResolver = new HashMap<LatLng, String>();

    MarkerOptions markerOptions;
    ArrayList<LatLng> mLaneLatLngList;
    ArrayList<LatLng> mMinLatLngList;

    LinearLayout layoutSelectOption, layoutLane, layoutMin;
    TextView tvLane, tvLaneTotalTime, tvLaneArvTime, tvLaneTotalDistance, tvMin, tvMinTotalTime, tvMinArvTime, tvMinTotalDistance, tvMainTitle;
    ImageButton btnBackKey, btnFullScreen, btnFavorite, btnStartNavi;

    String favoritePOIName = null;
    int laneGPIndex = 0;
    int minGPIndex = 0;
    int laneGPIndexSize = 0;
    int minGPIndexSize = 0;

    boolean isFavoriteBtnOn = false;
    boolean isActivateRouteWithinLimitDistanceNoti = false;
    boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_route);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.route_map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar_navigation);

        layoutSelectOption = (LinearLayout) findViewById(R.id.layout_select_option);
        layoutLane = (LinearLayout) findViewById(R.id.layout_bicyclelane);
        layoutMin = (LinearLayout) findViewById(R.id.layout_minimuntime);

        tvLane = (TextView) findViewById(R.id.text_bicyclelane);
        tvLaneTotalTime = (TextView) findViewById(R.id.text_bicyclelane_totaltime);
        tvLaneArvTime = (TextView) findViewById(R.id.text_bicyclelane_arrivetime);
        tvLaneTotalDistance = (TextView) findViewById(R.id.text_bicyclelane_totaldistance);

        tvMin = (TextView) findViewById(R.id.text_minimumtime);
        tvMinTotalTime = (TextView) findViewById(R.id.text_minimumtime_totaltime);
        tvMinArvTime = (TextView) findViewById(R.id.text_minimumtime_arrivetime);
        tvMinTotalDistance = (TextView) findViewById(R.id.text_minimumtime_totaldistance);

        tvMainTitle = (TextView) findViewById(R.id.text_main_title);

        btnFullScreen = (ImageButton) findViewById(R.id.btn_full_screen);
        btnFavorite = (ImageButton) findViewById(R.id.btn_favorite_onoff);
        btnBackKey = (ImageButton) findViewById(R.id.btn_back_key);
        btnStartNavi = (ImageButton) findViewById(R.id.btn_start_navigation);
        polylineList = new ArrayList<Polyline>();
        mLaneLatLngList = new ArrayList<LatLng>();
        mMinLatLngList = new ArrayList<LatLng>();

        btnStartNavi.setEnabled(false);

        if (mProvider == null) {
            mProvider = LocationManager.GPS_PROVIDER;
        }

        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        setFont();

        PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

        Intent intent = getIntent();

        if (intent != null) {
            favoritePOIName = intent.getStringExtra(KEY_DESTINATION_POI_NAME);
        }

        recentLatitude = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
        recentLongitude = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());
        destinationLatitude = Double.parseDouble(PropertyManager.getInstance().getDestinationLatitude());
        destinationLongitude = Double.parseDouble(PropertyManager.getInstance().getDestinationLongitude());

        centerLatitude = (recentLatitude + destinationLatitude) / 2;
        centerLongitude = (recentLongitude + destinationLongitude) / 2;


        final String userEmail = PropertyManager.getInstance().getUserEmail();

        if (favoritePOIName != null) {
            NetworkManager.getInstance().getMatchFavorite(this, userEmail, favoritePOIName, destinationLatitude, destinationLongitude, new NetworkManager.OnResultListener() {
                @Override
                public void onSuccess(Object result) {
                    if (result.equals(EXIST)) {
                        btnFavorite.setSelected(true);
                        isFavoriteBtnOn = true;
                        btnFavorite.setEnabled(true);
                    } else if (result.equals(NOT_EXIST)) {
                        btnFavorite.setSelected(false);
                        isFavoriteBtnOn = false;
                        btnFavorite.setEnabled(true);
                    }
                }

                @Override
                public void onFail(int code) {

                    if (code == BAD_REQUEST) {
                        btnFavorite.setEnabled(false);
                        isFavoriteBtnOn = false;
                        btnFavorite.setSelected(false);
                    } else {
                        btnFavorite.setEnabled(false);
                        isFavoriteBtnOn = false;
                        btnFavorite.setSelected(false);
                    }
                }
            });
        }

        final double startX = recentLongitude;
        final double startY = recentLatitude;
        final double endX = destinationLongitude;
        final double endY = destinationLatitude;


        if (startX != 0 && startY != 0 && endX != 0 && endY != 0) {
            NavigationNetworkManager.getInstance().findRoute(SelectRouteActivity.this, startX, startY, endX, endY, BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION,
                    new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
                        @Override
                        public void onSuccess(BicycleRouteInfo result) {
                            if (result.features != null && result.features.size() > 0) {
                                clearAllLaneMarker();

                                int totalTime = result.features.get(0).properties.totalTime;
                                int totalDistance = result.features.get(0).properties.totalDistance;
                                long expireMillis = 60000l; // 1 minute

                                Calendar expireDate= Calendar.getInstance();

                                expireDate.setTimeInMillis(System.currentTimeMillis() + expireMillis);


                                for (BicycleFeature feature : result.features) {
                                    if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_EP)) {
                                        laneGPIndexSize = (feature.properties.index - 2) / 2;
                                    }
                                }


                                if (totalDistance >= 20000) {
                                    moveMap(centerLatitude, centerLongitude, 10, ANIMATE_CAMERA);
                                } else if (totalDistance >= 10000 && totalDistance < 20000) {
                                    moveMap(centerLatitude, centerLongitude, 11, ANIMATE_CAMERA);
                                } else if (totalDistance >= 5000 && totalDistance < 10000) {
                                    moveMap(centerLatitude, centerLongitude, 12, ANIMATE_CAMERA);
                                } else if (totalDistance >= 1000 && totalDistance < 5000) {
                                    moveMap(centerLatitude, centerLongitude, 14, ANIMATE_CAMERA);
                                } else if (totalDistance >= 0 && totalDistance < 1000) {
                                    moveMap(centerLatitude, centerLongitude, 15, ANIMATE_CAMERA);
                                }

                                tvLaneTotalTime.setText(getTotalTime(totalTime, tvLaneArvTime));
                                tvLaneTotalDistance.setText(Integer.toString(totalDistance) + "m");

                                laneOptions = new PolylineOptions();

                                for (BicycleFeature feature : result.features) {
                                    if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING)) {
                                        double[] coord = feature.geometry.coordinates;

                                        for (int i = 0; i < coord.length; i += 2) {
                                            laneOptions.add(new LatLng(coord[i + 1], coord[i]));
                                        }
                                    } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_SP)) {
                                        double[] coord = feature.geometry.coordinates;

                                        for (int i = 0; i < coord.length; i += 2) {
                                            LatLng latLng = new LatLng(coord[i + 1], coord[i]);
                                            addPointMarker(latLng, POINTTYPE_SP, BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION, INITIAL_FLAG);

                                            mLaneLatLngList.add(latLng);
                                        }
                                    } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_EP)) {
                                        double[] coord = feature.geometry.coordinates;

                                        for (int i = 0; i < coord.length; i += 2) {
                                            LatLng latLng = new LatLng(coord[i + 1], coord[i]);
                                            addPointMarker(latLng, POINTTYPE_EP, BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION, INITIAL_FLAG);

                                            mLaneLatLngList.add(latLng);
                                        }
                                    } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_GP)) {
                                        double[] coord = feature.geometry.coordinates;

                                        for (int i = 0; i < coord.length; i += 2) {
                                            LatLng latLng = new LatLng(coord[i + 1], coord[i]);
                                            addPointMarker(latLng, POINTTYPE_GP, BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION, INITIAL_FLAG);

                                            mLaneLatLngList.add(latLng);
                                        }
                                    }
                                }

                                addPolyline(minOptions, laneOptions);
                            }
                        }

                        @Override
                        public void onFail(int code) {
                            if (code == ERROR_CODE_ACTIVATE_ROUTE_LIMIT_DISTANCE) {
                                withinRouteLimitDistanceDialog();
                            }
                        }
                    });
        }

        NavigationNetworkManager.getInstance().findRoute(SelectRouteActivity.this, startX, startY, endX, endY, BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION,
                new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
                    @Override
                    public void onSuccess(BicycleRouteInfo result) {
                        if (result.features != null && result.features.size() > 0) {
                            clearAllMinMarker();

                            int totalTime = result.features.get(0).properties.totalTime;
                            int totalDistance = result.features.get(0).properties.totalDistance;

                            for (BicycleFeature feature : result.features) {
                                if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_EP)) {
                                    minGPIndexSize = (feature.properties.index - 2) / 2;
                                }
                            }

                            tvMinTotalTime.setText(getTotalTime(totalTime, tvMinArvTime));
                            tvMinTotalDistance.setText(Integer.toString(totalDistance) + "m");

                            minOptions = new PolylineOptions();

                            for (BicycleFeature feature : result.features) {
                                if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING)) {
                                    double[] coord = feature.geometry.coordinates;

                                    for (int i = 0; i < coord.length; i += 2) {
                                        minOptions.add(new LatLng(coord[i + 1], coord[i]));
                                    }
                                } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_SP)) {
                                    double[] coord = feature.geometry.coordinates;

                                    for (int i = 0; i < coord.length; i += 2) {
                                        LatLng latLng = new LatLng(coord[i + 1], coord[i]);
                                        addPointMarker(latLng, POINTTYPE_SP, BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION, INITIAL_FLAG);

                                        mMinLatLngList.add(latLng);
                                    }
                                } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_EP)) {
                                    double[] coord = feature.geometry.coordinates;

                                    for (int i = 0; i < coord.length; i += 2) {
                                        LatLng latLng = new LatLng(coord[i + 1], coord[i]);
                                        addPointMarker(latLng, POINTTYPE_EP, BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION, INITIAL_FLAG);

                                        mMinLatLngList.add(latLng);
                                    }
                                } else if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_GP)) {
                                    double[] coord = feature.geometry.coordinates;

                                    for (int i = 0; i < coord.length; i += 2) {
                                        LatLng latLng = new LatLng(coord[i + 1], coord[i]);
                                        addPointMarker(latLng, POINTTYPE_GP, BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION, INITIAL_FLAG);

                                        mMinLatLngList.add(latLng);
                                    }
                                }
                            }

                            addPolyline(minOptions, laneOptions);
                        }
                    }

                    @Override
                    public void onFail(int code) {
                        if (code == ERROR_CODE_ACTIVATE_ROUTE_LIMIT_DISTANCE) {
                            withinRouteLimitDistanceDialog();
                        }
                    }
                });

        layoutLane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllPolyline();
                clearAllLaneMarker();
                clearAllMinMarker();

                for (int i = 0; i < mLaneLatLngList.size(); i++) {
                    String bitmapFlag = null;

                    LatLng latLng = mLaneLatLngList.get(i);
                    bitmapFlag = mLaneBitmapResolver.get(latLng);

                    addPointMarker(latLng, bitmapFlag, BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION, ITERATIVE_FLAG);
                }

                layoutSelectOption.setBackgroundResource(R.drawable.white_box_and_shadow_big_1);
                setPolylineColorAndBoldText(minOptions, laneOptions);

                PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);
            }
        });

        layoutMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllPolyline();
                clearAllLaneMarker();
                clearAllMinMarker();
                /*
                 * marker 다시 그리기
                 */
                for (int i = 0; i < mMinLatLngList.size(); i++) {
                    String bitmapFlag = null;

                    LatLng latLng = mMinLatLngList.get(i);
                    bitmapFlag = mMinBitmapResolver.get(latLng);

                    addPointMarker(latLng, bitmapFlag, BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION, ITERATIVE_FLAG);
                }

                layoutSelectOption.setBackgroundResource(R.drawable.white_box_and_shadow_big_2);
                setPolylineColorAndBoldText(laneOptions, minOptions);

                PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION);
            }
        });

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

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 *    이메일, favoritePOIName, destination latitude, longitude 서버로 보냄
                 *    flag 바탕으로 한번 누르면 update 다시 한번 누르면 delete
                 */

                final String fvName = favoritePOIName;
                final double fvLatitude = destinationLatitude;
                final double fvLongitude = destinationLongitude;

                if (fvName != null && !fvName.equals("")) {
                    if (!isFavoriteBtnOn) {

                        NetworkManager.getInstance().saveFavorite(SelectRouteActivity.this, userEmail, fvName, fvLatitude, fvLongitude, new NetworkManager.OnResultListener() {
                            @Override
                            public void onSuccess(Object result) {

                                if ((int) result == SUCCESS) {

                                    isFavoriteBtnOn = true;
                                    btnFavorite.setSelected(true);
                                }
                            }

                            @Override
                            public void onFail(int code) {

                                isFavoriteBtnOn = false;
                                btnFavorite.setSelected(false);
                            }
                        });
                    } else if (isFavoriteBtnOn) {

                        NetworkManager.getInstance().removeFavorite(SelectRouteActivity.this, userEmail, fvName, fvLatitude, fvLongitude, new NetworkManager.OnResultListener() {
                            @Override
                            public void onSuccess(Object result) {
                                isFavoriteBtnOn = false;
                                btnFavorite.setSelected(false);
                            }

                            @Override
                            public void onFail(int code) {

                                isFavoriteBtnOn = true;
                                btnFavorite.setSelected(true);
                            }
                        });
                    }
                }
            }
        });

        btnBackKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnStartNavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectRouteActivity.this);

                builder.setIcon(null);
                builder.setTitle("자전거 내비게이션 사용 시 주의사항");
                builder.setMessage("자전거 내비게이션 서비스는 T map의 자전거 경로안내 정보를 바탕으로 제공합니다.\n" +
                        "\n" +
                        "자전거 경로안내의 경우 자전거 전용도로를 최우선순위로 안내해드리며 전용도로가 존재하지 않을 경우 일반도로 정보를 제공해드리고 있습니다.\n" +
                        "자동차 전용도로(고속도로 등)는 포함되지 않으며 유턴구간의 경우 횡단보도로 안내해드리고 있으니 반드시 참고 자료로만 활용하시기 바랍니다.\n" +
                        "\n" +
                        "안전을 위해 주행 중 스마트폰 조작은 위험하오니 법규에 준수하여 안전한 라이딩을 하시기 바랍니다.\n" +
                        "\n" +
                        "Safe Riding의 자전거 내비게이션 서비스를 사용 중에 발생는 사고 등에 관하여 당사는 법적 책임을 지지 않습니다.");

                builder.setPositiveButton("동의", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PropertyManager.getInstance().setServiceCondition(SERVICE_RUNNING);


                        Intent intent = new Intent(SelectRouteActivity.this, StartNavigationActivity.class);
                        startActivity(intent);
                        finish();
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mLM.isProviderEnabled(mProvider)) {
            if (isFirst) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

                isFirst = false;

                Toast.makeText(SelectRouteActivity.this, "GPS를 설정해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SelectRouteActivity.this, "GPS 설정이 필요합니다.", Toast.LENGTH_SHORT).show();

                finish();
            }

            return;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLaneMarkerResolver.clear();
        mMinMarkerResolver.clear();
        mLaneBitmapResolver.clear();
        mMinBitmapResolver.clear();

        mLaneLatLngList.clear();
        mMinLatLngList.clear();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setCompassEnabled(false);


        moveMap(centerLatitude, centerLongitude, 10, MOVE_CAMERA);
    }

    private void moveMap(double latitude, double longitude, int zoomLevel, String moveAction) {
        if (mMap != null) {
            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.target(new LatLng(latitude, longitude));
            builder.zoom(zoomLevel);

            CameraPosition position = builder.build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

            if (moveAction.equals(ANIMATE_CAMERA)) {
                mMap.animateCamera(update);
            } else if(moveAction.equals(MOVE_CAMERA)) {
                mMap.moveCamera(update);
            }
        }
    }


    private void addPointMarker(LatLng latLng, String bitmapFlag, int searchOption,  String iterativeFlag) {
        if (bitmapFlag.equals(POINTTYPE_SP) || (bitmapFlag.equals(POINTTYPE_EP))) {
            markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(latLng.latitude, latLng.longitude));
            markerOptions.anchor(0.5f, 1.0f);
            markerOptions.draggable(false);

            if (bitmapFlag.equals(POINTTYPE_SP)) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.start));
            } else if (bitmapFlag.equals(POINTTYPE_EP)) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrival));
            }
        } else if (searchOption == BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION && bitmapFlag.equals(POINTTYPE_GP)) {
            laneGPIndex++;


            if (laneGPIndexSize > 0 && laneGPIndexSize <= 20) {
                markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(latLng.latitude, latLng.longitude));
                markerOptions.anchor(0.5f, 1.0f);
                markerOptions.draggable(false);

                addGPPointMarker(laneGPIndex);
            }
        } else if (searchOption == BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION && bitmapFlag.equals(POINTTYPE_GP)) {
            minGPIndex++;


            if (minGPIndexSize > 0 && minGPIndexSize <= 20) {
                markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(latLng.latitude, latLng.longitude));
                markerOptions.anchor(0.5f, 1.0f);
                markerOptions.draggable(false);

                addGPPointMarker(minGPIndex);
            }
        }

        if (searchOption == BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION) {
            Marker m = mMap.addMarker(markerOptions);
            mLaneMarkerResolver.put(latLng, m);

            if (iterativeFlag.equals(INITIAL_FLAG)) {
                mLaneBitmapResolver.put(latLng, bitmapFlag);
            }
        } else if (searchOption == BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION ) {
            if (iterativeFlag.equals(INITIAL_FLAG)) {
                mMinBitmapResolver.put(latLng, bitmapFlag);
            } else if (iterativeFlag.equals(ITERATIVE_FLAG)) {
                Marker m = mMap.addMarker(markerOptions);
                mMinMarkerResolver.put(latLng, m);
            }
        }
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

    private void addPolyline(PolylineOptions minOptions, PolylineOptions laneOptions) {
        if (minOptions != null && laneOptions != null) {
            btnStartNavi.setEnabled(true);

            minOptions.color(0x383498db);
            minOptions.width(10);

            polyline = mMap.addPolyline(minOptions);

            polylineList.add(polyline);

            laneOptions.color(0xba3498db);
            laneOptions.width(10);

            polyline = mMap.addPolyline(laneOptions);

            polylineList.add(polyline);
        }
    }

    private void clearAllLaneMarker() {
        for (int i = 0; i < mLaneLatLngList.size(); i++) {
            LatLng latLng = mLaneLatLngList.get(i);

            if (mLaneMarkerResolver.size() > 0) {
                Marker m = mLaneMarkerResolver.get(latLng);
                mLaneMarkerResolver.remove(m);
                m.remove();
            }
        }

        laneGPIndex = 0;
    }

    private void clearAllMinMarker() {
        for (int i = 0; i < mMinLatLngList.size(); i++) {
            LatLng latLng = mMinLatLngList.get(i);

            if (mMinMarkerResolver.size() > 0) {
                Marker m = mMinMarkerResolver.get(latLng);
                mMinMarkerResolver.remove(m);
                m.remove();
            }
        }

        minGPIndex = 0;
    }

    private void clearAllPolyline() {
        for (Polyline line : polylineList) {
            line.remove();
        }

        polylineList.clear();
    }

    private void setPolylineColorAndBoldText(PolylineOptions firstOptions, PolylineOptions secondOptions) {
        if (firstOptions != null && firstOptions.getPoints().size() > 0 && secondOptions != null && secondOptions.getPoints().size() > 0) {
            firstOptions.color(0x383498db);
            polyline = mMap.addPolyline(firstOptions);
            polylineList.add(polyline);

            secondOptions.color(0xba3498db);
            polyline = mMap.addPolyline(secondOptions);
            polylineList.add(polyline);
        }
    }

    private String getTotalTime(int time, TextView arvTime) {
        String totalTime = null;
        int hour, minute, second, remainTime;

        if (time >= 3600) {
            hour = time / 3600;
            remainTime = time % 3600;

            if (remainTime > 60) {
                minute = remainTime / 60;
                remainTime = remainTime % 60;

                if (remainTime > 0) {
                    second = remainTime;

                    addTime(hour, minute + 1, arvTime);
                    totalTime = Integer.toString(hour) + "시간 " + Integer.toString(minute) + "분 " + Integer.toString(second) + "초";
                } else if (remainTime == 0) {
                    addTime(hour, minute + 1, arvTime);
                    totalTime = Integer.toString(hour) + "시간 " + Integer.toString(minute) + "분";
                }
            } else if (remainTime == 0) {
                addTime(hour, 0, arvTime);
                totalTime = Integer.toString(hour) + "시간";
            } else if (remainTime < 60 && remainTime > 0) {
                second = remainTime;

                addTime(hour, 1, arvTime);
                totalTime = Integer.toString(hour) + "시간 " + Integer.toString(second) + "초";
            }
        } else if (time < 3600) {
            minute = time / 60;
            remainTime = time % 60;

            if (remainTime < 60 && remainTime > 0) {
                second = remainTime;

                addTime(0, minute + 1, arvTime);
                totalTime = Integer.toString(minute) + "분 " + Integer.toString(second) + "초";
            } else if (remainTime == 0) {
                addTime(0, minute, arvTime);
                totalTime = Integer.toString(minute) + "분";
            }
        } else if (time < 60 && time > 0) {
            addTime(0, 1, arvTime);
            totalTime = Integer.toString(time) + "초";
        } else {
            addTime(0, 0, arvTime);
            totalTime = "";
        }

        return totalTime;
    }

    private void addTime(int hour, int minute, TextView arvTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm");
        String strCurrentTime = sdf.format(new Date(System.currentTimeMillis()));

        Calendar cal = new GregorianCalendar();
        try {
            cal.setTime(sdf.parse(strCurrentTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cal.add(Calendar.HOUR, hour);
        cal.add(Calendar.MINUTE, minute);

        String strAddTime = sdf.format(cal.getTime()) + " 도착";

        arvTime.setText(strAddTime);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.hideInfoWindow();

        return true;
    }

    private void setFont() {
        tvMainTitle.setTypeface(FontManager.getInstance().getTypeface(SelectRouteActivity.this, FontManager.BMJUA));

//        tvLane.setTypeface(FontManager.getInstance().getTypeface(SelectRouteActivity.this, FontManager.NOTOSANS_M));
//        tvLaneTotalTime.setTypeface(FontManager.getInstance().getTypeface(SelectRouteActivity.this, FontManager.NOTOSANS));
//        tvLaneArvTime.setTypeface(FontManager.getInstance().getTypeface(SelectRouteActivity.this, FontManager.NOTOSANS));
//        tvLaneTotalDistance.setTypeface(FontManager.getInstance().getTypeface(SelectRouteActivity.this, FontManager.NOTOSANS));
//
//        tvMin.setTypeface(FontManager.getInstance().getTypeface(SelectRouteActivity.this, FontManager.NOTOSANS_M));
//        tvMinTotalTime.setTypeface(FontManager.getInstance().getTypeface(SelectRouteActivity.this, FontManager.NOTOSANS));
//        tvMinArvTime.setTypeface(FontManager.getInstance().getTypeface(SelectRouteActivity.this, FontManager.NOTOSANS));
//        tvMinTotalDistance.setTypeface(FontManager.getInstance().getTypeface(SelectRouteActivity.this, FontManager.NOTOSANS));
    }

    private void withinRouteLimitDistanceDialog() {
        if (!isActivateRouteWithinLimitDistanceNoti) {
            isActivateRouteWithinLimitDistanceNoti = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(null);
            builder.setTitle("알림");
            builder.setMessage("목적지가 출발지와 근접합니다. 경로를 다시 선택해주세요.");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        }
    }
}