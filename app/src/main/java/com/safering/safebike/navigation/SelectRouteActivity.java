package com.safering.safebike.navigation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.safering.safebike.R;
import com.safering.safebike.property.PropertyManager;

public class SelectRouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private static final int REQUEST_START_NAVIGATION = 2;
    private static final String SERVICE_RUNNING = "running";

    private static final String TAG_MAIN = "main";
    private static final String TAG_NAVIGATION = "navigation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_route);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.route_map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

//        Button btn = (Button) findViewById(R.id.btn_start_navigation);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                /*
//                 * 운행 하기 직전 메인 네비게이션 화면 pop
//                 */
//
//                Fragment old = getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION);
//
//                if (old != null) {
//                    Toast.makeText(SelectRouteActivity.this, "pop", Toast.LENGTH_SHORT);
//                    getSupportFragmentManager().popBackStack(TAG_NAVIGATION, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//                } else {
//                    Toast.makeText(SelectRouteActivity.this, "null", Toast.LENGTH_SHORT);
//                }
//
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
    public void onStartNavigationDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("자전거 길안내에 대한 한계 및 책임");
        builder.setMessage("내용");
        builder.setPositiveButton("동의", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*
                 * 운행 하기 직전 메인 네비게이션 화면 pop
                 */
                Fragment old = getSupportFragmentManager().findFragmentByTag(TAG_NAVIGATION);

                if (old != null) {
                    Toast.makeText(SelectRouteActivity.this, "pop", Toast.LENGTH_SHORT);
                    getSupportFragmentManager().popBackStack(TAG_NAVIGATION, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    Toast.makeText(SelectRouteActivity.this, "null", Toast.LENGTH_SHORT);
                }

                /*
                 * 출발지 목적지 좌표 저장
                 */

                PropertyManager.getInstance().setStartingLatitude("");
                PropertyManager.getInstance().setStartingLongitude("");
                PropertyManager.getInstance().setDestinationLatitude("");
                PropertyManager.getInstance().setDestinationLongitude("");
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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

    private void emptyBackStack() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
