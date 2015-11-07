package com.safering.safebike.navigation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING = "LineString";

    private GoogleMap mMap;

    final Map<POI, Marker> mPOIMarkerResolver = new HashMap<POI, Marker>();
    ArrayList<POI> mPOIMarkerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "SelectRouteActivity.onCreate");

        setContentView(R.layout.activity_select_route);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.route_map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        Intent intent = getIntent();

//        if (intent != null) {
//            final double startX = Double.parseDouble(intent.getStringExtra(KEY_BICYCLE_ROUTE_STARTX));
//            final double startY = Double.parseDouble(intent.getStringExtra(KEY_BICYCLE_ROUTE_STARTY));
//            final double endX = Double.parseDouble(intent.getStringExtra(KEY_BICYCLE_ROUTE_ENDX));
//            final double endY = Double.parseDouble(intent.getStringExtra(KEY_BICYCLE_ROUTE_ENDY));

        final double startX = Double.parseDouble(PropertyManager.getInstance().getRecentLongitude());
        final double startY = Double.parseDouble(PropertyManager.getInstance().getRecentLatitude());
        final double endX = Double.parseDouble(PropertyManager.getInstance().getDestinationLongitude());
        final double endY = Double.parseDouble(PropertyManager.getInstance().getDestinationLatitude());

        if (startX != 0 && startY != 0 && endX != 0 && endY != 0) {

            NavigationNetworkManager.getInstance().findRoute(SelectRouteActivity.this, startX, startY, endX, endY, BICYCLE_ROUTE_MINIMUMTIME_SEARCHOPTION,
                    new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
                @Override
                public void onSuccess(BicycleRouteInfo result) {
                    Log.d(DEBUG_TAG, "SelectRouteActivity.onCreate.onSuccess");
                    if (result.features != null && result.features.size() > 0) {
                        int totalDistance = result.features.get(0).properties.totalDistance;
                        int totalTime = result.features.get(0).properties.totalTime;

                        PolylineOptions options = new PolylineOptions();

                        for (BicycleFeature feature : result.features) {
                            if (feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_LINESTRING)) {
                                double[] coord = feature.geometry.coordinates;

                                for (int i = 0; i < coord.length; i += 2) {
                                    options.add(new LatLng(coord[i + 1], coord[i]));
                                }
                            }
                        }

                        options.color(Color.RED);
                        options.width(10);
                        mMap.addPolyline(options);
                    }
                }

                @Override
                public void onFail(int code) {

                }
            });

//            NavigationNetworkManager.getInstance().findRoute(SelectRouteActivity.this, startX, startY, endX, endY, BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION,
//                    new NavigationNetworkManager.OnResultListener<BicycleRouteInfo>() {
//                @Override
//                public void onSuccess(BicycleRouteInfo result) {
//
//                }
//
//                @Override
//                public void onFail(int code) {
//
//                }
//            });
        }

//        if (mGoogleApiClient == null) {
//            Log.d(DEBUG_TAG, "NavigationFragment.onCreate.new mGoogleApiClient");
//            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
//                    .addApi(LocationServices.API)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this).build();
//
//            createLocationRequest();
//        }


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
    public void onMapReady(GoogleMap googleMap) {
        Log.d(DEBUG_TAG, "SelectRouteActivity.onMapReady");
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        mMap.setMyLocationEnabled(true);
//        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setCompassEnabled(false);

        double centerLatitude = (Double.parseDouble(PropertyManager.getInstance().getRecentLatitude()) + Double.parseDouble(PropertyManager.getInstance().getDestinationLatitude())) / 2;
        double centerLongitude = (Double.parseDouble(PropertyManager.getInstance().getRecentLongitude()) + Double.parseDouble(PropertyManager.getInstance().getDestinationLongitude())) / 2;

        Log.d(DEBUG_TAG, "SelectRouteActivity.onMapReady.center.moveMap");
        moveMap(centerLatitude, centerLongitude, MOVE_CAMERA);
    }

//    protected void createLocationRequest() {
//        Log.d(DEBUG_TAG, "NavigationFragment.onCreate.createLocationRequest");
//
//        if (mLocationRequest == null) {
//            mLocationRequest = new LocationRequest();
//            mLocationRequest.setNumUpdates(1);
//            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        }
//    }

    private void moveMap(double latitude, double longitude, String moveAction) {
        if (mMap != null) {
            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.target(new LatLng(latitude, longitude));
            builder.zoom(15);

            CameraPosition position = builder.build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

            if (moveAction.equals(ANIMATE_CAMERA)) {
                mMap.animateCamera(update);
            } else if(moveAction.equals(MOVE_CAMERA)) {
                mMap.moveCamera(update);
            }
        }
    }

    private void addPOIMarker(POI poi) {
        MarkerOptions options  = new MarkerOptions();
        options.position(new LatLng(poi.noorLat, poi.noorLon));
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        options.anchor(0.5f, 1.0f);
        options.title(poi.name);
        options.draggable(false);

        Marker m = mMap.addMarker(options);

        mPOIMarkerResolver.put(poi, m);
//        mPOIResolver.put(m, poi);
    }

    private void clearALLMarker() {
        for (int i = 0; i < mPOIMarkerList.size(); i++) {
            POI poi = mPOIMarkerList.get(i);
            Marker m = mPOIMarkerResolver.get(poi);
            mPOIMarkerResolver.remove(m);
            m.remove();
        }

        mPOIMarkerList.clear();
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
