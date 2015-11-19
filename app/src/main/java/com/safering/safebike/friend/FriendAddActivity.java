package com.safering.safebike.friend;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.safering.safebike.R;
import com.safering.safebike.exercisereport.CalorieFragment;
import com.safering.safebike.exercisereport.DistanceFragment;
import com.safering.safebike.exercisereport.SpeedFragment;
import com.safering.safebike.manager.FontManager;

import org.w3c.dom.Text;

public class FriendAddActivity extends AppCompatActivity {
    FragmentTabHost tabHost;
    TextView textTitle;
    ImageView imageSearch;
    ImageView imageBack;
    View friendAddress;
    View friendDirect;
    public static final String TAG_ADDRESS = "Address";
    public static final String TAG_DIRECT = "Direct";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);




        friendAddress = getLayoutInflater().inflate(R.layout.friend_address_view, null);
        friendDirect = getLayoutInflater().inflate(R.layout.friend_direct_view, null);
        tabHost = (FragmentTabHost) findViewById(R.id.friend_tabHost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.friend_realtabcontent);
        tabHost.addTab(tabHost.newTabSpec(TAG_ADDRESS).setIndicator(friendAddress), FriendAddressFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(TAG_DIRECT).setIndicator(friendDirect), FriendDirectFragment.class, null);
        tabHost.getTabWidget().setDividerDrawable(null);
        actionBarSetting();
        setFont();






    }


    public void setFont() {
        textTitle.setText(R.string.text_add_friend);
        textTitle.setTypeface(FontManager.getInstance().getTypeface(FriendAddActivity.this, FontManager.NOTOSANS_M));
    }

    public void actionBarSetting(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar_friend);

        textTitle = (TextView) findViewById(R.id.text_custom_title_friend);
        imageBack = (ImageView) findViewById(R.id.image_backkey_friend);
        imageSearch = (ImageView) findViewById(R.id.image_search_friend);

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("search", "click");

                Toast.makeText(FriendAddActivity.this, "current : " + tabHost.getCurrentTabTag(), Toast.LENGTH_SHORT).show();

                if (tabHost.getCurrentTabTag() == TAG_ADDRESS) {

                    FriendAddressFragment addrFragment = (FriendAddressFragment) getSupportFragmentManager().findFragmentByTag(TAG_ADDRESS);
                    addrFragment.setList();



                }
                if(tabHost.getCurrentTabTag() == TAG_DIRECT){
                    getSupportActionBar().setCustomView(R.layout.custom_actionbar_friend_direct);

                    ImageView imageBackDirect = (ImageView) findViewById(R.id.image_backkey_friend_direct);
                    ImageView imageSearchDirect = (ImageView) findViewById(R.id.image_search_friend_direct);

                    imageBackDirect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });

                    imageSearchDirect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("friendSearch","searchsearch");



                        }
                    });



                }


            }
        });
        setFont();

    }
}
