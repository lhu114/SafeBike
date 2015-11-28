package com.safering.safebike.friend;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.safering.safebike.R;
import com.safering.safebike.adapter.FriendAdapter;
import com.safering.safebike.adapter.FriendItem;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.MyApplication;
import com.safering.safebike.property.PropertyManager;

import java.util.StringTokenizer;

public class FriendProfileActivity extends AppCompatActivity {
    ImageView friendImage;
    TextView friendId;
    TextView friendEmail;
    TextView friendJoin;
    TextView friendDelete;
    TextView textTitle;
    ImageView imageBack;
    FriendItem friend;
    FriendAdapter adapter;
    int friendPosition;

    TextView textFriendDistanceResult;
    TextView textFriendDistance;
    TextView textFriendSpeedResult;
    TextView textFriendSpeed;
    TextView textFriendCalorieResult;
    TextView textFriendCalorie;
    TextView textFriendTotalResult;
    String getfriendEmail;
    String getfriendPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);
        textTitle = (TextView) findViewById(R.id.text_custom_title);
        imageBack = (ImageView) findViewById(R.id.image_backkey);
        textFriendTotalResult = (TextView) findViewById(R.id.text_friend_total_result);

        friendImage = (ImageView) findViewById(R.id.image_friend_profile);
        friendId = (TextView) findViewById(R.id.text_friendid_profile);
        friendEmail = (TextView) findViewById(R.id.text_friendemail_profile);
        friendJoin = (TextView) findViewById(R.id.text_friendjoin_profile);
        friendDelete = (TextView) findViewById(R.id.text_friend_delete_profile);
        textFriendDistanceResult = (TextView) findViewById(R.id.text_friend_distance_result);
        textFriendDistance = (TextView) findViewById(R.id.text_friend_distance);
        textFriendSpeedResult = (TextView) findViewById(R.id.text_friend_speed_result);
        textFriendSpeed = (TextView) findViewById(R.id.text_friend_speed);
        textFriendCalorieResult = (TextView) findViewById(R.id.text_friend_calorie_result);
        textFriendCalorie = (TextView) findViewById(R.id.text_friend_calorie);

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setFont();

        Intent intent = getIntent();
        friend = (FriendItem) intent.getSerializableExtra("friendInform");
        adapter = (FriendAdapter) intent.getSerializableExtra("friendAdapter");
        friendPosition = intent.getIntExtra("friendPosition", 0);


        getfriendEmail = friend.pemail;
        getfriendPhoto = friend.photo;

        friendDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uEmail = PropertyManager.getInstance().getUserEmail();
                //String fEmail = friendItem.pemail;

                NetworkManager.getInstance().removeUserFriend(FriendProfileActivity.this, uEmail, getfriendEmail, new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object success) {

                        adapter.remove(friendPosition);


                    }

                    @Override
                    public void onFail(int code) {

                    }
                });


                //adapter.remove(friendPosition);

            }
        });

        NetworkManager.getInstance().getFriendProfile(FriendProfileActivity.this, getfriendEmail, new NetworkManager.OnResultListener<FriendProfileResult>() {
            @Override
            public void onSuccess(FriendProfileResult result) {
                setFriendProfile(result);

            }

            @Override
            public void onFail(int code) {

            }
        });

    }

    public void setFont() {
        textTitle.setText(R.string.text_add_friend);
        textTitle.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this, FontManager.NOTOSANS_M));
        textFriendDistanceResult.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this, FontManager.NOTOSANS));
        textFriendDistance.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this, FontManager.NOTOSANS));
        textFriendSpeedResult.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this, FontManager.NOTOSANS));
        textFriendSpeed.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this, FontManager.NOTOSANS));
        textFriendCalorieResult.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this, FontManager.NOTOSANS));
        textFriendCalorie.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this, FontManager.NOTOSANS));
        textFriendTotalResult.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this, FontManager.NOTOSANS));
        friendId.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this, FontManager.NOTOSANS));
        friendEmail.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this, FontManager.NOTOSANS));
        friendJoin.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this, FontManager.NOTOSANS));
        friendDelete.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this,FontManager.NOTOSANS));
    }

    public void setFriendProfile(FriendProfileResult result) {
        FriendProfile profile = result.friendprofile;
        friendId.setText(profile.name);
        friendEmail.setText(profile.email);
        friendJoin.setText(getDateFormat(profile.join));
        textFriendDistance.setText(profile.road + "km");
        textFriendSpeed.setText(profile.speed + "km/h");
        textFriendCalorie.setText(profile.calorie + "kcal");

        if (!getfriendPhoto.equals("null")) {
            DisplayImageOptions options;
            options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .showImageOnLoading(R.mipmap.profile_img)
                    .showImageForEmptyUri(R.mipmap.profile_img)


                    .considerExifParams(true)
                    .displayer(new RoundedBitmapDisplayer(1000))
                    .build();

            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(MyApplication.getContext()));
            ImageLoader.getInstance().displayImage(getfriendPhoto, friendImage, options);
        }

    }

    public String getDateFormat(String date) {
        Log.i("date", date);
        String resultDate = "";
        StringTokenizer tokenizer = new StringTokenizer(date, "-");
        resultDate += tokenizer.nextToken() + "년 ";
        resultDate += tokenizer.nextToken() + "월 ";
        resultDate += tokenizer.nextToken() + "일 가입";
        return resultDate;
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
}
