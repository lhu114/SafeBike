package com.safering.safebike.friend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.safering.safebike.R;
import com.safering.safebike.adapter.FriendItem;

public class FriendProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getIntent()
        Intent intent = getIntent();
        FriendItem friend = (FriendItem)intent.getSerializableExtra("friendInform");
        Toast.makeText(FriendProfileActivity.this,"getValue : " + friend.friendId,Toast.LENGTH_SHORT).show();
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
