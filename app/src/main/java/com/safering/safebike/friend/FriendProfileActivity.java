package com.safering.safebike.friend;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.safering.safebike.R;
import com.safering.safebike.adapter.FriendItem;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

public class FriendProfileActivity extends AppCompatActivity {
    ImageView friendImage;
    TextView friendId;
    TextView friendEmail;
    TextView friendJoin;
    TextView textTitle;
    ImageView imageBack;

    TextView textFriendDistanceResult;
    TextView textFriendDistance;
    TextView textFriendSpeedResult;
    TextView textFriendSpeed;
    TextView textFriendCalorieResult;
    TextView textFriendCalorie;
    TextView textFriendTotalResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);
        textTitle = (TextView)findViewById(R.id.text_custom_title);
        imageBack = (ImageView)findViewById(R.id.image_backkey);
        textFriendTotalResult = (TextView)findViewById(R.id.text_friend_total_result);

        friendImage = (ImageView)findViewById(R.id.image_friend);
        friendId = (TextView)findViewById(R.id.text_friendid_profile);
        friendEmail = (TextView)findViewById(R.id.text_friendemail_profile);
        friendJoin = (TextView)findViewById(R.id.text_friendjoin_profile);

        textFriendDistanceResult = (TextView)findViewById(R.id.text_friend_distance_result);
        textFriendDistance = (TextView)findViewById(R.id.text_friend_distance);
        textFriendSpeedResult = (TextView)findViewById(R.id.text_friend_speed_result);
        textFriendSpeed = (TextView)findViewById(R.id.text_friend_speed);
        textFriendCalorieResult = (TextView)findViewById(R.id.text_friend_calorie_result);
        textFriendCalorie = (TextView)findViewById(R.id.text_friend_calorie);

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setFont();

        Intent intent = getIntent();
        FriendItem friend = (FriendItem)intent.getSerializableExtra("friendInform");
        String uEmail = PropertyManager.getInstance().getUserEmail();
        String fEmail = friend.pemail;

        NetworkManager.getInstance().getFriendProfile(FriendProfileActivity.this, uEmail, fEmail, new NetworkManager.OnResultListener() {
            @Override
            public void onSuccess(Object success) {


            }

            @Override
            public void onFail(int code) {

            }
        });

    }

    public void setFont(){
        textTitle.setText(R.string.text_add_friend);
        textTitle.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this, FontManager.NOTOSANS_M));
        textFriendDistanceResult.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this,FontManager.NOTOSANS_M));
        textFriendDistance.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this,FontManager.NOTOSANS_M));
        textFriendSpeedResult.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this,FontManager.NOTOSANS_M));
        textFriendSpeed.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this,FontManager.NOTOSANS_M));
        textFriendCalorieResult.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this,FontManager.NOTOSANS_M));
        textFriendCalorie.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this,FontManager.NOTOSANS_M));
        textFriendTotalResult.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this,FontManager.NOTOSANS_M));
        friendId.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this,FontManager.NOTOSANS));
        friendEmail.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this,FontManager.NOTOSANS));
        friendJoin.setTypeface(FontManager.getInstance().getTypeface(FriendProfileActivity.this,FontManager.NOTOSANS));
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
