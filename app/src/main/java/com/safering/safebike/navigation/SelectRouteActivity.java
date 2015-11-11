package com.safering.safebike.navigation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.safering.safebike.property.PropertyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectRouteActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String DEBUG_TAG = "safebike";

    private static final int REQUEST_START_NAVIGATION = 1003;
    private static final String SERVICE_RUNNING = "running";

    private static final String TAG_MAIN = "main";
    private static final String TAG_NAVIGATION = "navigation";

    private static final String MOVE_CAMERA = "movecamera";
    private static final String ANIMATE_CAMERA = "animatecamera";

    //    private static final String KEY_BICYCLE_ROUTE_STARTX = "startX";
//    private static final String KEY_BICYCLE_ROUTE_STARTY = "startY";
//    private static final String KEY_BICYCLE_ROUTE_ENDX = "endX";
//    private static final String KEY_BICYCLE_ROUTE_ENDY = "endY";
    private static final int BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION = 0;
    private static final int BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION = 3;

    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_POINT = "Point";
    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING = "LineString";

//    private static final String START_MARKER_FLAG = "start";
//    private static final String END_MARKER_FLAG = "end";

    private static final String POINTTYPE_SP = "SP";
    private static final String POINTTYPE_EP = "EP";
    private static final String POINTTYPE_GP = "GP";

    private static final String ITERATIVE_FLAG = "iterative";
    private static final String INITIAL_FLAG = "initial;";

    private static final String KEY_FAVORITE_POI_NAME = "favoritepoiname";

    private GoogleMap mMap;

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

    ArrayList<LatLng> mLaneLatLngList;
    ArrayList<LatLng> mMinLatLngList;

//    Handler mHandler;

    LinearLayout layoutLane, layoutMin;
    TextView tvLane, tvLaneTotalTime, tvLaneArvTime, tvLaneTotalDistance, tvMin, tvMinTotalTime, tvMinArvTime, tvMinTotalDistance;
    Button btnFullScreen;

    String favoritePOIName = null;
    int laneGPIndex = 0;
    int minGPIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "SelectRouteActivity.onCreate");

        setContentView(R.layout.activity_select_route);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.route_map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        btnFullScreen = (Button) findViewById(R.id.btn_full_screen);
        Button favoriteBtn = (Button) findViewById(R.id.btn_favorite_onoff);

        polylineList = new ArrayList<Polyline>();
        mLaneLatLngList = new ArrayList<LatLng>();
        mMinLatLngList = new ArrayList<LatLng>();

//        mHandler = new Handler(Looper.getMainLooper());

        PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

        Intent intent = getIntent();

        if (intent != null) {
            favoritePOIName = intent.getStringExtra(KEY_FAVORITE_POI_NAME);

            Log.d(DEBUG_TAG, "SelectRouteActivity.onCreate.getIntent.favoritePOIName : " + favoritePOIName);
        }

        /*
         *  이메일 destination 위도, 경도 값 서버에 보내서 서버에 있으면 즐겨찾기 버튼 On 상태 표시, flag 도 On으로 변경
         */

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

        Log.d(DEBUG_TAG, "SelectRouteActivity.onCreate.출발지, 목적지 위도 경도 : " + recentLatitude + ", " + recentLongitude + " | " + destinationLatitude + ", " + destinationLongitude);

        if (startX != 0 && startY != 0 && endX != 0 && endY != 0) {
            NavigationNetworkManager.getInstance().findRoute(SelectRouteActivity.this, startX, startY, endX, endY, BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION,
                    new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
                        @Override
                        public void onSuccess(BicycleRouteInfo result) {
                            if (result.features != null && result.features.size() > 0) {
                                clearAllLaneMarker();

                                int totalTime = result.features.get(0).properties.totalTime;
                                int totalDistance = result.features.get(0).properties.totalDistance;

                                /*
                                 * 추후 조정
                                 */
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

                                tvLaneTotalTime.setText(getTotalTime(totalTime));
                                tvLaneTotalDistance.setText(Integer.toString(totalDistance) + "m");

                                laneOptions = new PolylineOptions();

                                for (BicycleFeature feature : result.features) {
                                    if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING)) {
                                        double[] coord = feature.geometry.coordinates;

                                        for (int i = 0; i < coord.length; i += 2) {
                                            laneOptions.add(new LatLng(coord[i + 1], coord[i]));
                                        }
                                    }
                                }

                                for (BicycleFeature feature : result.features) {
                                    if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_SP)) {
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
                                /*
                                 * 올바른 처리인가..
                                 */
//                                mHandler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        laneOptions.color(Color.RED);
//                                        laneOptions.width(10);
//                                        polyline = mMap.addPolyline(laneOptions);
//
//                                        polylineList.add(polyline);
//                                    }
//                                }, 100);

                                addPolyline(minOptions, laneOptions);
                            }
                        }

                        @Override
                        public void onFail(int code) {

                        }
                    });
        }

        NavigationNetworkManager.getInstance().findRoute(SelectRouteActivity.this, startX, startY, endX, endY, BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION,
                new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
                    @Override
                    public void onSuccess(BicycleRouteInfo result) {
                        Log.d(DEBUG_TAG, "SelectRouteActivity.onCreate.onSuccess");
                        if (result.features != null && result.features.size() > 0) {
                            clearAllMinMarker();

                            int totalTime = result.features.get(0).properties.totalTime;
                            int totalDistance = result.features.get(0).properties.totalDistance;

                            tvMinTotalTime.setText(getTotalTime(totalTime));
                            tvMinTotalDistance.setText(Integer.toString(totalDistance) + "m");

                            minOptions = new PolylineOptions();

                            for (BicycleFeature feature : result.features) {
                                if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING)) {
                                    double[] coord = feature.geometry.coordinates;

                                    for (int i = 0; i < coord.length; i += 2) {
                                        minOptions.add(new LatLng(coord[i + 1], coord[i]));
                                    }
                                }
                            }

                            for (BicycleFeature feature : result.features) {
                                if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_SP)) {
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

//                                if (minOptions != null && laneOptions != null) {
//                                    Log.d(DEBUG_TAG, "SelectRouteActivity.onCreate.BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION.onSuccess.addPolyline----------------------------------");
//                                    minOptions.color(Color.GRAY);
//                                    minOptions.width(10);
//                                    polyline = mMap.addPolyline(minOptions);
//
//                                    polylineList.add(polyline);
//
//                                    laneOptions.color(Color.RED);
//                                    laneOptions.width(10);
//                                    polyline = mMap.addPolyline(laneOptions);
//
//                                    polylineList.add(polyline);
//                                }
                            addPolyline(minOptions, laneOptions);
                        }
                    }

                    @Override
                    public void onFail(int code) {

                    }
                });

        tvLane.setPaintFlags(tvLane.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        tvLaneTotalTime.setPaintFlags(tvLaneTotalTime.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        tvLaneArvTime.setPaintFlags(tvLaneArvTime.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        tvLaneTotalDistance.setPaintFlags(tvLaneTotalDistance.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);

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

                setPolylineColorAndBoldText(minOptions, laneOptions, tvLane, tvLaneTotalTime, tvLaneArvTime, tvLaneTotalDistance, tvMin, tvMinTotalTime, tvMinArvTime, tvMinTotalDistance);

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

                setPolylineColorAndBoldText(laneOptions, minOptions, tvMin, tvMinTotalTime, tvMinArvTime, tvMinTotalDistance, tvLane, tvLaneTotalTime, tvLaneArvTime, tvLaneTotalDistance);

                PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION);
            }
        });

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

        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 *    이메일, favoritePOIName, destination latitude, longitude 서버로 보냄
                 *    flag 바탕으로 한번 누르면 update 다시 한번 누르면 delete
                 */
            }
        });
    }

//        Button btn = (Button) findViewById(R.id.btn_start_navigation);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                /*
//                 * 운행 하기 직전 메인 네비게이션 화면 pop
//                 */

//                /*
//                 * 출발지 목적지 좌표 저장
//                 */
//
//                PropertyManager.getInstance().setStartingLatitude("");
//                PropertyManager.getInstance().setStartingLongitude("");
//                PropertyManager.getInstance().setDestinationLatitude("");
//                PropertyManager.getInstance().setDestinationLongitude("");
//                PropertyManager.getInstance().setServiceCondition(SERVICE_RUNNING);
//
////                Toast.makeText(SelectRouteActivity.this, "SelectRouteActivity.onCreate : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();
//
//
//                /*
//                 * 자전거 한계 및 책임 다이얼로그 띄우고 확인하면 startActivity 아니면 그대로
//                 */
//                Intent intent = new Intent(SelectRouteActivity.this, StartNavigationActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
//    }

    /*
     * 안내시작 버튼 처리
     */
    public void onStartNavigationBtn(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("자전거 길안내에 대한 한계 및 책임");
        builder.setMessage("내용");
        builder.setPositiveButton("동의", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*
                 * 출발지 목적지 좌표 저장
                 */

//                PropertyManager.getInstance().setStartingLatitude("");
//                PropertyManager.getInstance().setStartingLongitude("");
//                PropertyManager.getInstance().setDestinationLatitude("");
//                PropertyManager.getInstance().setDestinationLongitude("");
                PropertyManager.getInstance().setServiceCondition(SERVICE_RUNNING);

//                Toast.makeText(SelectRouteActivity.this, "SelectRouteActivity.onCreate : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();


                /*
                 * 자전거 한계 및 책임 다이얼로그 띄우고 확인하면 startActivity 아니면 그대로
                 */
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
//        builder.setCancelable(false);

        builder.create().show();
    }

    @Override
    protected void onStart() {
        Log.d(DEBUG_TAG, "SelectRouteActivity.onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(DEBUG_TAG, "SelectRouteActivity.onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG, "SelectRouteActivity.onDestroy");

        mLaneMarkerResolver.clear();
        mMinMarkerResolver.clear();
        mLaneBitmapResolver.clear();
        mMinBitmapResolver.clear();

        mLaneLatLngList.clear();
        mMinLatLngList.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(SelectRouteActivity.this, "onActivityResult", Toast.LENGTH_SHORT);

        if (requestCode == REQUEST_START_NAVIGATION && resultCode == Activity.RESULT_OK) {
            Toast.makeText(SelectRouteActivity.this, "Finish Navigation", Toast.LENGTH_SHORT);

            finish();
        } else {
            Toast.makeText(SelectRouteActivity.this, "error Finish Navigation", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(DEBUG_TAG, "SelectRouteActivity.onMapReady");
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setCompassEnabled(false);

        Log.d(DEBUG_TAG, "SelectRouteActivity.onMapReady.center.moveMap");

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
        MarkerOptions options  = new MarkerOptions();
        options.position(new LatLng(latLng.latitude, latLng.longitude));
        options.anchor(0.5f, 1.0f);
        options.draggable(false);

        if (bitmapFlag.equals(POINTTYPE_SP)) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        } else if (bitmapFlag.equals(POINTTYPE_EP)) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        } else if (searchOption == BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION && bitmapFlag.equals(POINTTYPE_GP)) {
            laneGPIndex++;

            /*
             * index 순서대로 이미지 다르게 적용
             */

            Log.d(DEBUG_TAG, "SelectRouteActivity.addPointMarker.BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION.POINTTYPE_GP.index : " + Integer.toString(laneGPIndex));

            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        } else if (searchOption == BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION && bitmapFlag.equals(POINTTYPE_GP)) {
            minGPIndex++;

            /*
             * index 순서대로 이미지 다르게 적용
             */

            Log.d(DEBUG_TAG, "SelectRouteActivity.addPointMarker.BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION.POINTTYPE_GP.index : " + Integer.toString(minGPIndex));

            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        }

//        if (searchOption == BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION && iterativeFlag.equals(INITIAL_FLAG)) {
//            Marker m = mMap.addMarker(options);
//            mLaneMarkerResolver.put(latLng, m);
//            mLaneBitmapResolver.put(latLng, bitmapFlag);
//        } else if (searchOption == BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION && iterativeFlag.equals(ITERATIVE_FLAG)) {
//            Marker m = mMap.addMarker(options);
//            mLaneMarkerResolver.put(latLng, m);
//        } else if (searchOption == BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION && iterativeFlag.equals(INITIAL_FLAG)) {
//            mMinBitmapResolver.put(latLng, bitmapFlag);
//        } else if (searchOption == BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION && iterativeFlag.equals(ITERATIVE_FLAG)) {
//            Marker m = mMap.addMarker(options);
//            mMinMarkerResolver.put(latLng, m);
//        }

        if (searchOption == BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION) {
            Marker m = mMap.addMarker(options);
            mLaneMarkerResolver.put(latLng, m);

            if (iterativeFlag.equals(INITIAL_FLAG)) {
                mLaneBitmapResolver.put(latLng, bitmapFlag);
            }
        } else if (searchOption == BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION ) {
            if (iterativeFlag.equals(INITIAL_FLAG)) {
                mMinBitmapResolver.put(latLng, bitmapFlag);
            } else if (iterativeFlag.equals(ITERATIVE_FLAG)) {
                Marker m = mMap.addMarker(options);
                mMinMarkerResolver.put(latLng, m);
            }
        }
    }

    private void addPolyline(PolylineOptions minOptions, PolylineOptions laneOptions) {
        if (minOptions != null && laneOptions != null) {
            minOptions.color(Color.GRAY);
            minOptions.width(10);
            polyline = mMap.addPolyline(minOptions);

            polylineList.add(polyline);

            laneOptions.color(Color.RED);
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

//        mLaneLatLngList.clear();
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

//        mLaneLatLngList.clear();
    }

    private void clearAllPolyline() {
//        for (int i = 0; i < polylineList.size(); i++) {
//            Polyline tmpPolyline = polylineList.get(i);
//            tmpPolyline.remove();
//        }
//
//        polylineList.clear();

        for (Polyline line : polylineList) {
            line.remove();
        }

        polylineList.clear();
    }

    private void setPolylineColorAndBoldText(PolylineOptions firstOptions, PolylineOptions secondOptions, TextView boldText, TextView boldTotalTime, TextView boldArvTime,
                                             TextView boldTotalDistance, TextView text, TextView totalTime, TextView arvTime, TextView totalDistance) {
        firstOptions.color(Color.GRAY);
        polyline = mMap.addPolyline(firstOptions);
        polylineList.add(polyline);

        secondOptions.color(Color.RED);
        polyline = mMap.addPolyline(secondOptions);
        polylineList.add(polyline);

        boldText.setPaintFlags(boldText.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        boldTotalTime.setPaintFlags(boldTotalTime.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        boldArvTime.setPaintFlags(boldArvTime.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        boldTotalDistance.setPaintFlags(boldTotalDistance.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);

        text.setPaintFlags(text.getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG);
        totalTime.setPaintFlags(totalTime.getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG);
        arvTime.setPaintFlags(arvTime.getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG);
        totalDistance.setPaintFlags(totalDistance.getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG);
    }

    private String getTotalTime(int time) {
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

                    totalTime = Integer.toString(hour) + "시간 " + Integer.toString(minute) + "분 " + Integer.toString(second) + "초";
                } else if (remainTime == 0) {
                    totalTime = Integer.toString(hour) + "시간 " + Integer.toString(minute) + "분";
                }
            } else if (remainTime == 0) {
                totalTime = Integer.toString(hour) + "시간";
            } else if (remainTime < 60 && remainTime > 0) {
                second = remainTime;

                totalTime = Integer.toString(hour) + "시간 " + Integer.toString(second) + "초";
            }
        } else if (time < 3600) {
            minute = time / 60;
            remainTime = time % 60;

            if (remainTime < 60 && remainTime > 0) {
                second = remainTime;

                totalTime = Integer.toString(minute) + "분 " + Integer.toString(second) + "초";
            } else if (remainTime == 0) {
                totalTime = Integer.toString(minute) + "분";
            }
        } else if (time < 60 && time > 0) {
            totalTime = Integer.toString(time) + "초";
        } else {
            totalTime = "";
        }

        return totalTime;
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
}
