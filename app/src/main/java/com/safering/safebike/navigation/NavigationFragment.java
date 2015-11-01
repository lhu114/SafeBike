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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.safering.safebike.R;


public class NavigationFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final String DEBUG_TAG = "safebike";

    private static final int REQUEST_SEARCH_POI = 1;
    private static final String KEY_POI_NAME = "poiName";

    private GoogleMap mMap;

    View view;
    LinearLayout addressLayout;
    FloatingActionButton fabFindRoute;
    TextView textView;

    ArrayAdapter<POI> mListAdapter;

    public NavigationFragment() {
        // Required empty public constructor
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(DEBUG_TAG, "NavigationFragment.onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "NavigationFragment.onCreateView");
        // Inflate the layout for this fragment
        try {
            view = inflater.inflate(R.layout.fragment_navigation, container, false);

//            SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
//                    .findFragmentById(R.id.map);
//            mapFragment.getMapAsync(this);

            addressLayout = (LinearLayout) view.findViewById(R.id.layout_address);
            addressLayout.setVisibility(View.INVISIBLE);

            textView = (TextView) view.findViewById(R.id.text_poi_name);

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

        if (requestCode == REQUEST_SEARCH_POI && resultCode == Activity.RESULT_OK) {
            String poiName = data.getStringExtra(KEY_POI_NAME);

            Toast.makeText(getContext(), "NavigationFragment.onActivityResult / " + poiName, Toast.LENGTH_SHORT).show();
            textView.setText(poiName);

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

        /*
         * 실패 등 예외상황 처리
         */
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }
}
