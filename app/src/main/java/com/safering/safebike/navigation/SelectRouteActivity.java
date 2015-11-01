package com.safering.safebike.navigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.safering.safebike.R;

public class SelectRouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private static final int REQUEST_START_NAVIGATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_route);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.route_map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button btn = (Button) findViewById(R.id.btn_start_navigation);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * 운행 하기 직전 메인 네비게이션 화면 pop
                 */
                emptyBackStack();

                /*
                 * FLAG_ACTIVITY_NO_HISTORY 알아보기
                 */
                Intent intent = new Intent(SelectRouteActivity.this, StartNavigationActivity.class);
//                startActivityForResult(intent, REQUEST_START_NAVIGATION);
                startActivity(intent);
                finish();
            }
        });
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
