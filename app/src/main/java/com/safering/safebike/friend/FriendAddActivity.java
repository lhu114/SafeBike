package com.safering.safebike.friend;

import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    ImageView imageBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);



        textTitle = (TextView)findViewById(R.id.text_custom_title);
        imageBack = (ImageView)findViewById(R.id.image_backkey);
        tabHost = (FragmentTabHost)findViewById(R.id.friend_tabHost);

        setFont();
        tabHost.setup(this, getSupportFragmentManager(), R.id.friend_realtabcontent);
        tabHost.addTab(tabHost.newTabSpec("adress").setIndicator("연락처"), FriendAddressFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("direct").setIndicator("직접찾기"), FriendDirectFragment.class, null);

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
