package com.safering.safebike.friend;

import android.media.Image;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.safering.safebike.adapter.FriendItem;
import com.safering.safebike.R;
import com.safering.safebike.adapter.*;
import com.safering.safebike.exercisereport.CalorieFragment;
import com.safering.safebike.exercisereport.DistanceFragment;
import com.safering.safebike.exercisereport.SpeedFragment;
import com.safering.safebike.manager.FontManager;

import org.w3c.dom.Text;

public class FriendAddActivity extends AppCompatActivity {
    FragmentTabHost tabHost;
    TextView textTitle;
    TextView textAddress;
    TextView textDirect;
    ImageView imageSearch;
    ImageView imageBack;
    View friendAddress;
    View friendDirect;
    FriendDirectFragment directFragment;
    FriendAddressFragment addressFragment;
    public static final String TAG_ADDRESS = "Address";
    public static final String TAG_DIRECT = "Direct";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);




        friendAddress = getLayoutInflater().inflate(R.layout.friend_address_view, null);
        friendDirect = getLayoutInflater().inflate(R.layout.friend_direct_view, null);
        textAddress = (TextView)friendAddress.findViewById(R.id.text_tab_address);
        textDirect = (TextView)friendDirect.findViewById(R.id.text_tab_direct);
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
        textAddress.setTypeface(FontManager.getInstance().getTypeface(FriendAddActivity.this,FontManager.NOTOSANS));
        textDirect.setTypeface(FontManager.getInstance().getTypeface(FriendAddActivity.this,FontManager.NOTOSANS));
    }

    public void actionBarSetting(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar_friend);
        getSupportActionBar().setElevation(0);
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
                //Log.i("search", "click");

                Toast.makeText(FriendAddActivity.this, "current : " + tabHost.getCurrentTabTag(), Toast.LENGTH_SHORT).show();

                if (tabHost.getCurrentTabTag() == TAG_ADDRESS) {


                    addressFragment = (FriendAddressFragment) getSupportFragmentManager().findFragmentByTag(TAG_ADDRESS);
                    //addrFragment.setList();


                    getSupportActionBar().setCustomView(R.layout.custom_actionbar_friend_address);
                    ImageView imageBackAddress = (ImageView)findViewById(R.id.image_backkey_friend_address);


                    EditText editTextAddress = (EditText)findViewById(R.id.edit_friend_search_address);
                    imageBackAddress.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                    editTextAddress.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            Log.i("textChange",s.toString() + "");
                            if(TextUtils.isEmpty(s.toString())){
                                addressFragment.fAdapter.clear();
                                addressFragment.setList();
                            }
                            else{
                                addressFragment.fAdapter.clear();
                                for(int i = 0; i < addressFragment.addressFriendList.size(); i++){
                                 //   Log.i("UserFriendList",UserFriendList.getInstance().items.get(i).pemail);

                                    if(addressFragment.addressFriendList.get(i).pname.contains(s.toString())){
                                       // FriendItem friend = UserFriendList.getInstance().items.get(i);
                                        FriendItem friend = addressFragment.addressFriendList.get(i);
                                        addressFragment.fAdapter.add(friend);
                                        Log.i("textFriend",friend.pemail);
                                    }
                                }


                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });

                }
                if(tabHost.getCurrentTabTag() == TAG_DIRECT){
                    directFragment = (FriendDirectFragment) getSupportFragmentManager().findFragmentByTag(TAG_DIRECT);
                    if(directFragment.SEARCH_ONOFF == 1) {
                        Toast.makeText(FriendAddActivity.this,"SEARCH_ON = 1",Toast.LENGTH_SHORT).show();

                        //directFragment.SEARCH_ONOFF = 2;
                        getSupportActionBar().setCustomView(R.layout.custom_actionbar_friend_directoff);
//                        getSupportActionBar().setCustomView(R.layout.custom_actionbar_friend_directoff);


                        ImageView imageBackDirect = (ImageView) findViewById(R.id.image_backkey_friend_directOff);
                        ImageView imageSearch = (ImageView)findViewById(R.id.image_search_friend_directOff);
                        ImageView imageCancel = (ImageView)findViewById(R.id.image_cancel_friend_directOff);
                        final EditText editSearchEmail = (EditText)findViewById(R.id.edit_friend_search_directOff);
                        imageBackDirect.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });

                        imageSearch.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(FriendAddActivity.this,"searchING",Toast.LENGTH_SHORT).show();
                                if(!TextUtils.isEmpty(editSearchEmail.getText().toString())){
                                    String email = editSearchEmail.getText().toString();
                                    directFragment.searchDirect(email);
                                }

                            }
                        });

                        imageCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(FriendAddActivity.this,"cancelING",Toast.LENGTH_SHORT).show();
                                actionBarSetting();
                            }
                        });

                    }
                 /*   else if(directFragment.SEARCH_ONOFF == 2){
                        Toast.makeText(FriendAddActivity.this,"SEARCH_ON = 2",Toast.LENGTH_SHORT).show();
                        getSupportActionBar().setCustomView(R.layout.custom_actionbar_friend_direct);
                        directFragment.SEARCH_ONOFF = 1;

                        ImageView imageBackDirect = (ImageView) findViewById(R.id.image_backkey_friend_directOn);

                        imageBackDirect.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });

                    }*/
/*
                    imageSearchDirect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("friendSearch","searchsearch");



                        }
                    });*/



                }


            }
        });
        setFont();

    }
}
