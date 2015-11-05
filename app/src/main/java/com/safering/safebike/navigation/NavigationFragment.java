package com.safering.safebike.navigation;

import android.app.Activity;
import android.content.Intent;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.safering.safebike.R;


public class NavigationFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String DEBUG_TAG = "safebike";

    private static final int REQUEST_SEARCH_POI = 1002;
    private static final String KEY_POI_NAME = "poiName";
    private static final String KEY_POI_LATITUDE = "poiLatitude";
    private static final String KEY_POI_LONGITUDE = "poiLongitude";
    private static final String KEY_POI_ADDRESS = "poiAddress";

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;

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
        Toast.makeText(getContext(), "NavigationFragment.onCreate", Toast.LENGTH_SHORT).show();


        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "NavigationFragment.onCreateView");
        Toast.makeText(getContext(), "NavigationFragment.onCreateView", Toast.LENGTH_SHORT).show();
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

//    @Override
//    public void onResume() {
//        super.onResume();
//        setUpMapIfNeeded();
//    }

    /*
         * 프래그먼트가 화면에서 사라질 때 프래그먼트의 뷰를 컨테이너 뷰에서 제거
         */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Toast.makeText(getContext(), "NavigationFragment.onDestroyView", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getContext(), "NavigationFragment.onDestroy", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(getContext(), ParentRctFvActivity.class);
            startActivityForResult(intent, REQUEST_SEARCH_POI);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SEARCH_POI) {
            if (resultCode == Activity.RESULT_OK) {
                double poiLatitude = data.getDoubleExtra(KEY_POI_LATITUDE, 0);
                double poiLongitude = data.getDoubleExtra(KEY_POI_LONGITUDE, 0);
                String poiName = data.getStringExtra(KEY_POI_NAME);
                String poiAddress = data.getStringExtra(KEY_POI_ADDRESS);

                Toast.makeText(getContext(), "NavigationFragment.onActivityResult.poiName : " + poiName, Toast.LENGTH_SHORT).show();
                Log.d("safebike", "poiLatitude : " + Double.toString(poiLatitude) + " poiLongitude : " + Double.toString(poiLongitude));
                Log.d("safebike", "poiName : " + data.getStringExtra(KEY_POI_NAME) + " poiAddress : " + data.getStringExtra(KEY_POI_ADDRESS));
//            activateDestination();

                tvPoiAddress.setText(poiAddress);

            /*
             * 맵 이동하면서 poi 마커 찍기
             */
                addressLayout.setVisibility(View.VISIBLE);
                fabFindRoute.setVisibility(View.VISIBLE);

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
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.getProjection();
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
//        activateDestination();

        Toast.makeText(getContext(), "onMapLongClick", Toast.LENGTH_SHORT).show();

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


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

