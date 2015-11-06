package com.safering.safebike.navigation;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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
import com.safering.safebike.R;

import java.util.HashMap;
import java.util.Map;


public class NavigationFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraChangeListener {
    private static final String DEBUG_TAG = "safebike";

    private static final int REQUEST_SEARCH_POI = 1002;
    private static final String KEY_POI_OBJECT = "poiobject";
//    private static final String KEY_POI_NAME = "poiName";
//    private static final String KEY_POI_LATITUDE = "poiLatitude";
//    private static final String KEY_POI_LONGITUDE = "poiLongitude";
//    private static final String KEY_POI_ADDRESS = "poiAddress";

    private GoogleMap mMap;
    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private static final String MOVE_CAMERA = "movecamera";
    private static final String ANIMATE_CAMERA = "animatecamera";
    private static String LOCATION_CHANGE_FLAG = "on";
    private static final String ON = "on";
    private static final String OFF = "off";

    GoogleApiClient mGoogleApiClient;
    Location mLocation, mCacheLocation;

    LocationRequest mLocationRequest;

    final Map<POI, Marker> mMarkerResolver = new HashMap<POI, Marker>();
    final Map<Marker, POI> mPOIResolver = new HashMap<Marker, POI>();

    View view;
    LinearLayout addressLayout;
    FloatingActionButton fabFindRoute;
    TextView tvPoiAddress;

    ArrayAdapter<POI> mListAdapter;

    public NavigationFragment() {
        // Required empty public constructor
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(DEBUG_TAG, "NavigationFragment.onCreate");

        if (mGoogleApiClient == null) {
            Log.d(DEBUG_TAG, "NavigationFragment.onCreate.new mGoogleApiClient");
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            createLocationRequest();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "NavigationFragment.onCreateView");

        // Inflate the layout for this fragment
        try {
            view = inflater.inflate(R.layout.fragment_navigation, container, false);

            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map);
            mapFragment.getMapAsync(this);

            addressLayout = (LinearLayout) view.findViewById(R.id.layout_address);
            addressLayout.setVisibility(View.INVISIBLE);

            tvPoiAddress = (TextView) view.findViewById(R.id.text_poi_address);

            fabFindRoute = (FloatingActionButton) view.findViewById(R.id.btn_find_route);
            fabFindRoute.setVisibility(View.GONE);

            if (View.GONE == fabFindRoute.getVisibility()) {
                LOCATION_CHANGE_FLAG = ON;
                Log.d(DEBUG_TAG, "NavigationFragment.LOCATION_CHANGE_FLAG.ON");
            }

            FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.btn_crt_location);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*
                     * 현재 위치
                     */
                }
            });
        } catch (InflateException e) {            /*
             * 구글맵 View가 이미 inflate되어 있는 상태이므로, 에러를 무시합니다.
             */
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        Toast.makeText(getContext(), "NavigationFragment.onStart", Toast.LENGTH_SHORT).show();
        Log.d(DEBUG_TAG, "NavigationFragment.onStart");
        if (!mResolvingError) {  // more about this later
            if (mGoogleApiClient != null) {
                Log.d(DEBUG_TAG, "NavigationFragment.onStart.mGoogleApiClient.connect -> mGoogleApiClient != null");
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Toast.makeText(getContext(), "NavigationFragment.onStop", Toast.LENGTH_SHORT).show();
        Log.d(DEBUG_TAG, "NavigationFragment.onStop");

        if (mGoogleApiClient != null) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
            Toast.makeText(getContext(), "NavigationFragment.onStop.mGoogleApiClient.disconnect", Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_TAG, "NavigationFragment.onStop.mGoogleApiClient.disconnect -> mGoogleApiClient == null");
        }
    }

    /*
         * 프래그먼트가 화면에서 사라질 때 프래그먼트의 뷰를 컨테이너 뷰에서 제거
         */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(DEBUG_TAG, "NavigationFragment.onDestroyView");
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();

            if(parent != null) {
                parent.removeView(view);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(DEBUG_TAG, "NavigationFragment.onDestroy");

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_navigation, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_fwd_search) {
            /*
             * 최근이용, 즐겨찾기 탭 활성화
             */
            Log.d(DEBUG_TAG, "NavigationFragment.onOptionsItemSelected.menu_fwd_search");
            Intent intent = new Intent(getContext(), ParentRctFvActivity.class);
            startActivityForResult(intent, REQUEST_SEARCH_POI);

            LOCATION_CHANGE_FLAG = OFF;
            Log.d(DEBUG_TAG, "NavigationFragment.LOCATION_CHANGE_FLAG.OFF");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(DEBUG_TAG, "NavigationFragment.onActivityResult");
        if (requestCode == REQUEST_SEARCH_POI) {
            if (resultCode == Activity.RESULT_OK) {
                POI poi = (POI) data.getSerializableExtra(KEY_POI_OBJECT);

//                double poiLatitude = data.getDoubleExtra(KEY_POI_LATITUDE, 0);
//                double poiLongitude = data.getDoubleExtra(KEY_POI_LONGITUDE, 0);
//                String poiName = data.getStringExtra(KEY_POI_NAME);
//                String poiAddress = data.getStringExtra(KEY_POI_ADDRESS);

//                Toast.makeText(getContext(), "NavigationFragment.onActivityResult.poiName : " + poi.name, Toast.LENGTH_SHORT).show();
                Log.d("safebike", "poiLatitude : " + Double.toString(poi.getLatitude()) + " poiLongitude : " + Double.toString(poi.getLongitude()));
                Log.d("safebike", "poiName : " + poi.name + " poiAddress : " + poi.getAddress());
//            activateDestination();

                if (poi != null) {
                    tvPoiAddress.setText(getDefineAddress(poi));

                    /*
                     * 맵 이동하면서 poi 마커 찍기
                     */
                    Log.d(DEBUG_TAG, "NavigationFragment.onActivityResult.poi.moveMap");

//                    stopLocationUpdates();

                    moveMap(poi.getLatitude(), poi.getLongitude(), ANIMATE_CAMERA);
                    addMarker(poi);


                    addressLayout.setVisibility(View.VISIBLE);
                    fabFindRoute.setVisibility(View.VISIBLE);

                    if (View.VISIBLE == fabFindRoute.getVisibility()) {
                        LOCATION_CHANGE_FLAG = OFF;
                        Log.d(DEBUG_TAG, "NavigationFragment.LOCATION_CHANGE_FLAG.OFF");
                    }

                    fabFindRoute.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getContext(), SelectRouteActivity.class);
                    /*
                     * 위에서 받은 데이터 전달 출발지, 목적지 다 보내야함
                     */
                            startActivity(intent);
                        }
                    });
                }
            }
        }
    }

        /*
         * 실패 등 예외상황 처리
         */


//    public void activateDestination() {
//        addressLayout.setVisibility(View.VISIBLE);
//        fabFindRoute.setVisibility(View.VISIBLE);
//
//        fabFindRoute.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getContext(), SelectRouteActivity.class);
//                startActivity(intent);
//            }
//        });
//    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getContext(), "NavigationFragment.onMapReady", Toast.LENGTH_SHORT).show();
        Log.d(DEBUG_TAG, "NavigationFragment.onMapReady");
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);

        if (mCacheLocation != null) {
            Toast.makeText(getContext(), "NavigationFragment.onMapReady.mCacheLocation.moveMap", Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_TAG, "NavigationFragment.onMapReady.mCacheLocation.moveMap");
            moveMap(mLocation.getLatitude(), mLocation.getLongitude(), MOVE_CAMERA);
            mCacheLocation = null;
        }


//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    protected void createLocationRequest() {
        Log.d(DEBUG_TAG, "NavigationFragment.onCreate.createLocationRequest");

        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setNumUpdates(1);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    protected  void startLocationUpdates() {
        Log.d(DEBUG_TAG, "NavigationFragment.startLocationUpdates");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mListener);
    }

    protected void stopLocationUpdates() {
        Log.d(DEBUG_TAG, "NavigationFragment.stopLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mListener);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(DEBUG_TAG, "NavigationFragment.onConnected");
        startLocationUpdates();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        Toast.makeText(getContext(), "NavigationFragment.onConnected.mLocation" + " : " + mLocation.getLatitude() + ", " + mLocation.getLongitude(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getContext(), "NavigationFragment.onConnectionSuspended", Toast.LENGTH_SHORT).show();
        Log.d(DEBUG_TAG, "NavigationFragment.onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        }

        Toast.makeText(getContext(), "NavigationFragment.onConnectionFailed", Toast.LENGTH_SHORT).show();
        Log.d(DEBUG_TAG, "NavigationFragment.onConnectionFailed");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getContext(), "NavigationFragment.onLocationChanged", Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_TAG, "NavigationFragment.onLocationChanged");
            Log.d(DEBUG_TAG, "NavigationFragment.onLocationChanged.flag : " + LOCATION_CHANGE_FLAG);
            if (mMap != null) {
                Log.d(DEBUG_TAG, "NavigationFragment.onLocationChanged.mMap != null");
                if (LOCATION_CHANGE_FLAG.equals(ON)) {
                    moveMap(location.getLatitude(), location.getLongitude(), MOVE_CAMERA);
                }
            } else {
                Log.d(DEBUG_TAG, "NavigationFragment.onLocationChanged.mMap == null");
                mCacheLocation = location;
            }
        }
    };

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
//        activateDestination();

//        Toast.makeText(getContext(), "onMapLongClick", Toast.LENGTH_SHORT).show();

        /*
         *  클릭 좌표 가져와서 네트워크 요청하고 주소 받아와서 View 에 던지기
         */
        addressLayout.setVisibility(View.VISIBLE);
        fabFindRoute.setVisibility(View.VISIBLE);

        fabFindRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SelectRouteActivity.class);
                startActivity(intent);
            }
        });
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
            } else if(moveAction.equals(MOVE_CAMERA)) {
                mMap.moveCamera(update);
            }
        }
    }

    private void addMarker(POI poi) {
        MarkerOptions options  = new MarkerOptions();
        /*
         * 어떤 값으로 위도 경도 넘길지는 고민
         */
//        options.position(new LatLng(poi.getLatitude(), poi.getLongitude()));
        options.position(new LatLng(poi.noorLat, poi.noorLon));
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        options.anchor(0.5f, 1.0f);
        options.title(poi.name);
        options.draggable(false);

        Marker m = mMap.addMarker(options);

        mMarkerResolver.put(poi, m);
        mPOIResolver.put(m, poi);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        mMap.getProjection();
    }

    private String getDefineAddress(POI poi) {
        String defineAddress = null;

        if (!poi.detailAddrName.equals("") && !poi.firstNo.equals("") && !poi.secondNo.equals("")) {
            defineAddress = poi.getAddress() + " "+ poi.getDetailAddress();

            Log.d("safebike", "defineAddress 1");
        } else if (!poi.detailAddrName.equals("") && !poi.firstNo.equals("") && poi.secondNo.equals("")) {
            defineAddress = poi.getAddress() + " " + poi.firstNo;

            Log.d("safebike", "defineAddress 2");
        } else if (!poi.detailAddrName.equals("") && poi.firstNo.equals("") && poi.secondNo.equals("")) {
            defineAddress = poi.getAddress();

            Log.d("safebike", "defineAddress 3");
        } else if (poi.detailAddrName.equals("") && !poi.firstNo.equals("") && !poi.secondNo.equals("")) {
            defineAddress = poi.middleAddrName + " " + poi.lowerAddrName + " " + poi.getDetailAddress();

            Log.d("safebike", "defineAddress 4");
        } else if (poi.detailAddrName.equals("") && !poi.firstNo.equals("") && poi.secondNo.equals("")) {
            defineAddress = poi.getAddress() + " " + poi.firstNo;

            Log.d("safebike", "defineAddress 5");
        } else if (poi.detailAddrName.equals("") && poi.firstNo.equals("") && poi.secondNo.equals("")) {
            defineAddress = poi.middleAddrName + " " + poi.lowerAddrName;

            Log.d("safebike", "defineAddress 6");
        } else {
            defineAddress = poi.getAddress() + " " + poi.getDetailAddress();

            Log.d("safebike", "defineAddress 7");
        }

        return defineAddress;
    }
}

