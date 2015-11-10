package com.safering.safebike.friend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.safering.safebike.R;
import com.safering.safebike.adapter.FriendItem;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

public class FriendProfileActivity extends AppCompatActivity {
    ImageView friendImage;
    TextView friendId;
    TextView friendEmail;
    TextView friendJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        friendImage = (ImageView)findViewById(R.id.image_friend);
        friendId = (TextView)findViewById(R.id.text_friendid_profile);
        friendEmail = (TextView)findViewById(R.id.text_friendemail_profile);
        friendJoin = (TextView)findViewById(R.id.text_friendjoin_profile);

        Intent intent = getIntent();
        FriendItem friend = (FriendItem)intent.getSerializableExtra("friendInform");
        String uEmail = PropertyManager.getInstance().getUserEmail();
        String fEmail = friend.pemail;

        /*NetworkManager.getInstance().getFriendProfile(FriendProfileActivity.this, uEmail, fEmail, new NetworkManager.OnResultListener() {
            @Override
            public void onSuccess(Object success) {


            }

            @Override
            public void onFail(int code) {

            }
        });
        */
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
