package com.safering.safebike.friend;

import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.safering.safebike.R;
import com.safering.safebike.exercisereport.CalorieFragment;
import com.safering.safebike.exercisereport.DistanceFragment;
import com.safering.safebike.exercisereport.SpeedFragment;

public class FriendAddActivity extends AppCompatActivity {
    FragmentTabHost tabHost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tabHost = (FragmentTabHost)findViewById(R.id.friend_tabHost);
        tabHost.setup(this,getSupportFragmentManager(), R.id.friend_realtabcontent);
        tabHost.addTab(tabHost.newTabSpec("adress").setIndicator("연락처"), FriendAddressFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("direct").setIndicator("직접찾기"), FriendDirectFragment.class, null);




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
}
