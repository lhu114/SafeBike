package com.safering.safebike.navigation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    private static final String START_MARKER_FLAG = "start";
    private static final String END_MARKER_FLAG = "end";

    private GoogleMap mMap;

    Polyline polyline;
    PolylineOptions laneOptions, minOptions;
    ArrayList<Polyline> polylineList;

    double recentLatitude = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
    double recentLongitude = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());
    double destinationLatitude = Double.parseDouble(PropertyManager.getInstance().getDestinationLatitude());
    double destinationLongitude = Double.parseDouble(PropertyManager.getInstance().getDestinationLongitude());
    double centerLatitude = (recentLatitude + destinationLatitude) / 2;
    double centerLongitude = (recentLongitude + destinationLongitude) / 2;

    final double startX = recentLongitude;
    final double startY = recentLatitude;
    final double endX = destinationLongitude;
    final double endY = destinationLatitude;

//    final Map<POI, Marker> mPOIMarkerResolver = new HashMap<POI, Marker>();
    final Map<LatLng, Marker> mPointMarkerResolver = new HashMap<LatLng, Marker>();

//    ArrayList<POI> mPOIMarkerList;
    ArrayList<LatLng> mPointMarkerList;

    Handler mHandler;

    LinearLayout layoutLane, layoutMin;
    TextView tvLane, tvLaneTotalTime, tvLaneArvTime, tvLaneTotalDistance, tvMin, tvMinTotalTime, tvMinArvTime, tvMinTotalDistance;
    Button btnFullScreen;

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

        polylineList = new ArrayList<Polyline>();
        mPointMarkerList = new ArrayList<LatLng>();

        mHandler = new Handler(Looper.getMainLooper());

        PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

        findViewById(R.id.title_bar);
        if (startX != 0 && startY != 0 && endX != 0 && endY != 0) {
            NavigationNetworkManager.getInstance().findRoute(SelectRouteActivity.this, startX, startY, endX, endY, BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION,
                    new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
                        @Override
                        public void onSuccess(BicycleRouteInfo result) {
                            Log.d(DEBUG_TAG, "SelectRouteActivity.onCreate.onSuccess");
                            if (result.features != null && result.features.size() > 0) {
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
                                    if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT)) {
                                        double[] coord = feature.geometry.coordinates;

                                        for (int i = 0; i < coord.length; i += 2) {

                                        }
                                    }
                                }

                                minOptions.color(Color.GRAY);
                                minOptions.width(10);
                                polyline = mMap.addPolyline(minOptions);

                                polylineList.add(polyline);
                            }
                        }

                        @Override
                        public void onFail(int code) {

                        }
                    });

            NavigationNetworkManager.getInstance().findRoute(SelectRouteActivity.this, startX, startY, endX, endY, BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION,
                    new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
                        @Override
                        public void onSuccess(BicycleRouteInfo result) {
                            if (result.features != null && result.features.size() > 0) {
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
                                    moveMap(centerLatitude, centerLongitude, 13, ANIMATE_CAMERA);
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

                                clearALLPointMarker();

                                for (BicycleFeature feature : result.features) {
                                    if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT)) {
                                        double[] coord = feature.geometry.coordinates;

                                        for (int i = 0; i < coord.length; i += 2) {
                                            LatLng latLng = new LatLng(coord[i + 1], coord[i]);
//                                            addPointMarker(latLng, "");

//                                            mPointMarkerList.add(latLng);
                                            /*
                                             * 다시 처리
                                             */
                                        }
                                    }
                                }
                                /*
                                 * 올바른 처리인가..
                                 */
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        laneOptions.color(Color.RED);
                                        laneOptions.width(10);
                                        polyline = mMap.addPolyline(laneOptions);

                                        polylineList.add(polyline);
                                    }
                                }, 100);
                            }
                        }

                        @Override
                        public void onFail(int code) {

                        }
                    });
        }

        tvLane.setPaintFlags(tvLane.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        tvLaneTotalTime.setPaintFlags(tvLaneTotalTime.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        tvLaneArvTime.setPaintFlags(tvLaneArvTime.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        tvLaneTotalDistance.setPaintFlags(tvLaneTotalDistance.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);

        layoutLane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllPolyline();
                clearALLPointMarker();

                /*
                 * marker 다시 그리기
                 */
                minOptions.color(Color.GRAY);
                polyline = mMap.addPolyline(minOptions);
                polylineList.add(polyline);

                laneOptions.color(Color.RED);
                polyline = mMap.addPolyline(laneOptions);
                polylineList.add(polyline);

                tvLane.setPaintFlags(tvLane.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                tvLaneTotalTime.setPaintFlags(tvLaneTotalTime.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                tvLaneArvTime.setPaintFlags(tvLaneArvTime.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                tvLaneTotalDistance.setPaintFlags(tvLaneTotalDistance.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);

                tvMin.setPaintFlags(tvMin.getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG);
                tvMinTotalTime.setPaintFlags(tvMinTotalTime.getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG);
                tvMinArvTime.setPaintFlags(tvMinArvTime.getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG);
                tvMinTotalDistance.setPaintFlags(tvMinTotalDistance.getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG);

                PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);
            }
        });

        layoutMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllPolyline();
                clearALLPointMarker();

                /*
                 * marker 다시 그리기
                 */
                laneOptions.color(Color.GRAY);
                polyline = mMap.addPolyline(laneOptions);
                polylineList.add(polyline);

                minOptions.color(Color.RED);
                polyline = mMap.addPolyline(minOptions);
                polylineList.add(polyline);

                tvMin.setPaintFlags(tvMin.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                tvMinTotalTime.setPaintFlags(tvMinTotalTime.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                tvMinArvTime.setPaintFlags(tvMinArvTime.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                tvMinTotalDistance.setPaintFlags(tvMinTotalDistance.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);

                tvLane.setPaintFlags(tvLane.getPaintFlags() &~ Paint.FAKE_BOLD_TEXT_FLAG);
                tvLaneTotalTime.setPaintFlags(tvLaneTotalTime.getPaintFlags() &~ Paint.FAKE_BOLD_TEXT_FLAG);
                tvLaneArvTime.setPaintFlags(tvLaneArvTime.getPaintFlags() &~ Paint.FAKE_BOLD_TEXT_FLAG);
                tvLaneTotalDistance.setPaintFlags(tvLaneTotalDistance.getPaintFlags() &~ Paint.FAKE_BOLD_TEXT_FLAG);

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

//        double recentLatitude = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
//        double recentLongitude = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());
//        double destinationLatitude = Double.parseDouble(PropertyManager.getInstance().getDestinationLatitude());
//        double destinationLongitude = Double.parseDouble(PropertyManager.getInstance().getDestinationLongitude());
//
//        double centerLatitude = (recentLatitude + destinationLatitude) / 2;
//        double centerLongitude = (recentLongitude + destinationLongitude) / 2;

        Log.d(DEBUG_TAG, "SelectRouteActivity.onMapReady.center.moveMap");

        /*
         * 토탈 디스턴스에 따른 맵 레벨 다르게 보여주기기
        */
        moveMap(centerLatitude, centerLongitude, 11, MOVE_CAMERA);

        addStartEndMarker(recentLatitude, recentLongitude, START_MARKER_FLAG);
        addStartEndMarker(destinationLatitude, destinationLongitude, END_MARKER_FLAG);
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

    private void addStartEndMarker(double latitude, double longitude, String flag) {
        MarkerOptions options  = new MarkerOptions();
        options.position(new LatLng(latitude, longitude));
        options.anchor(0.5f, 1.0f);
        options.draggable(false);

        if (flag.equals(START_MARKER_FLAG)) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        } else if(flag.equals(END_MARKER_FLAG)) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        Marker m = mMap.addMarker(options);

//        mPOIMarkerResolver.put(poi, m);
//        mPOIResolver.put(m, poi);
    }

    private void addPointMarker(LatLng latLng, String flag) {
        MarkerOptions options  = new MarkerOptions();
        options.position(new LatLng(latLng.latitude, latLng.longitude));
        options.anchor(0.5f, 1.0f);
        options.draggable(false);

        if (flag.equals(START_MARKER_FLAG)) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        } else if(flag.equals(END_MARKER_FLAG)) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));


        Marker m = mMap.addMarker(options);

//        mPOIMarkerResolver.put(poi, m);
//        mPOIResolver.put(m, poi);
    }

//    private void clearALLMarker() {
//        for (int i = 0; i < mPOIMarkerList.size(); i++) {
//            POI poi = mPOIMarkerList.get(i);
//            Marker m = mPOIMarkerResolver.get(poi);
//            mPOIMarkerResolver.remove(m);
//            m.remove();
//        }
//
//        mPOIMarkerList.clear();
//    }

    private void clearALLPointMarker() {
        for (int i = 0; i < mPointMarkerList.size(); i++) {
            LatLng latLng = mPointMarkerList.get(i);
            Marker m = mPointMarkerResolver.get(latLng);
            mPointMarkerResolver.remove(m);
            m.remove();
        }

        mPointMarkerList.clear();
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
