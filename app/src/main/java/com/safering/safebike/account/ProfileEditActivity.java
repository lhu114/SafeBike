package com.safering.safebike.account;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedVignetteBitmapDisplayer;
import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.property.MyApplication;
import com.safering.safebike.property.PropertyManager;

public class ProfileEditActivity extends AppCompatActivity {
    TextView userId;
    TextView userEmail;
    TextView userJoin;
    ImageView userProfileImage;
    DisplayImageOptions options;
    public static final int GET_USER_IMAGE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        userProfileImage = (ImageView) findViewById(R.id.image_friend_profile_edit);
        userId = (TextView) findViewById(R.id.text_change_id);
        userEmail = (TextView) findViewById(R.id.text_change_email);
        userJoin = (TextView) findViewById(R.id.text_change_join);

        userId.setText(PropertyManager.getInstance().getUserId());
        userEmail.setText(PropertyManager.getInstance().getUserEmail());
        userJoin.setText(PropertyManager.getInstance().getUserJoin());

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GET_USER_IMAGE);
            }
        });




        Button btn = (Button) findViewById(R.id.btn_edit_compelete);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileEditActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("edit", "e");
                startActivityForResult(intent, GET_USER_IMAGE);

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_USER_IMAGE) {
            if (resultCode == RESULT_OK) {
                options = new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisc(true)
                        .considerExifParams(true)
                        .displayer(new RoundedBitmapDisplayer(50))
                        .build();
                Uri uri = data.getData();

                Log.i("W", "uri: " + uri.toString());
                Log.i("W", "path: " + uri.getPath());
                //userProfileImage.setImageURI(uri);

                //ImageLoader.getInstance().displayImage(item.image, userProfileImage, options);
               // ImageLoader.getInstance().displayImage(uri.getPath(), userProfileImage, options);
                ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(MyApplication.getContext()));

                ImageLoader.getInstance().displayImage(uri.toString(), userProfileImage, options);


            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
