package com.safering.safebike.navigation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.property.PropertyManager;

public class StartNavigationActivity extends AppCompatActivity  implements OnMapReadyCallback {
    private GoogleMap mMap;

    private static final String SERVICE_FINISH = "finish";

    private static final String KEY_POP_NAVIGATION_FRAGMENT = "popNavigation";
    private static final String VALUE_POP_NAVIGATION_FRAGMENT = "popNavigation";

    double DestinationLati;
    double DestinationLongi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_navigation);

        getDestinationSharedPreferences();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
    public void onFinishNavigationDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("내비게이션 안내종료");
        builder.setMessage("현재 내비게이션 안내 중입니다. 정말로 종료하시겠습니까");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);

//                Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onCreate : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
                startActivity(intent);

                finish();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
                startActivity(intent);

                finish();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        Toast.makeText(StartNavigationActivity.this, "StartNavigationActivity.onBackPressed : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KEY_POP_NAVIGATION_FRAGMENT, VALUE_POP_NAVIGATION_FRAGMENT);
        startActivity(intent);

        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void getDestinationSharedPreferences() {
        String tmpDestiLati, tmpDestiLongi;

        tmpDestiLati = PropertyManager.getInstance().getDestinationLatitude();
        tmpDestiLongi = PropertyManager.getInstance().getDestinationLongitude();

        /*
         *  String -> double 캐스팅 필요
         */
    }
}
