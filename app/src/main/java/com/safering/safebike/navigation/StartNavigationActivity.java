package com.safering.safebike.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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

    private static final String RUNNING_NAVIGATION = "changeButton";
    private static final String SERVICE_FINISH = "finish";

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

        Button btn = (Button) findViewById(R.id.btn_finish_navigation);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * 운동 기록 처리, 자동으로 안내를 종료할지에 대한 시나리오, 사용자 직접 종료 또는 자동 종료에 따른 운동 기록 값 전달
                 */
                PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);

                Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

                finish();
            }
        });

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
                startActivity(intent);

                finish();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(StartNavigationActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
