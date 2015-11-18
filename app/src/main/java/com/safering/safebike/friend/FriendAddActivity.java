package com.safering.safebike.friend;

import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.safering.safebike.R;
import com.safering.safebike.exercisereport.CalorieFragment;
import com.safering.safebike.exercisereport.DistanceFragment;
import com.safering.safebike.exercisereport.SpeedFragment;
import com.safering.safebike.manager.FontManager;

public class FriendAddActivity extends AppCompatActivity {
    FragmentTabHost tabHost;
    TextView textTitle;
    ImageView imageSearch;
    ImageView imageBack;
    View friendAddress;
    View friendDirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar_friend);


        friendAddress = getLayoutInflater().inflate(R.layout.friend_address_view, null);
        friendDirect = getLayoutInflater().inflate(R.layout.friend_direct_view, null);

        textTitle = (TextView)findViewById(R.id.text_custom_title_friend);
        imageBack = (ImageView)findViewById(R.id.image_backkey_friend);
        imageSearch = (ImageView)findViewById(R.id.image_search_friend);
        tabHost = (FragmentTabHost)findViewById(R.id.friend_tabHost);

        setFont();
        tabHost.setup(this, getSupportFragmentManager(), R.id.friend_realtabcontent);
        tabHost.addTab(tabHost.newTabSpec("adress").setIndicator(friendAddress), FriendAddressFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("direct").setIndicator(friendDirect), FriendDirectFragment.class, null);
        tabHost.getTabWidget().setDividerDrawable(null);

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("search","click");
            }
        });



    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    public void setFont(){
        textTitle.setText(R.string.text_add_friend);
        textTitle.setTypeface(FontManager.getInstance().getTypeface(FriendAddActivity.this,FontManager.NOTOSANS_M));
    }
}
