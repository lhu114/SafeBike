package com.safering.safebike.account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.annotation.ColorRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.property.MyApplication;
import com.safering.safebike.property.PropertyManager;

import java.io.File;
import java.util.StringTokenizer;

public class ProfileActivity extends AppCompatActivity {
    TextView userId;
    TextView userEmail;
    TextView userJoin;
    TextView textEditProfile;
    ImageView imageProfileUser;

    TextView textTitle;
    ImageView imageBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);



        textTitle = (TextView)findViewById(R.id.text_custom_title);
        imageBack = (ImageView)findViewById(R.id.image_backkey);

        userId = (TextView) findViewById(R.id.text_id_profile);
        userEmail = (TextView)findViewById(R.id.text_email_profile);
        userJoin = (TextView)findViewById(R.id.text_join_profile);
        imageProfileUser = (ImageView)findViewById(R.id.image_user_profile);
        textEditProfile = (TextView)findViewById(R.id.text_edit_profile);





        setProfile();
        setFont();

        textEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                startActivity(intent);
            }
        });

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
    }*/


    public void setProfile(){
        userId.setText(PropertyManager.getInstance().getUserId());
        userEmail.setText(PropertyManager.getInstance().getUserEmail());
        userJoin.setText(getDateFormat(PropertyManager.getInstance().getUserJoin()));
        if(!PropertyManager.getInstance().getUserImagePath().equals("")){
            DisplayImageOptions options;
            options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .showImageOnLoading(R.mipmap.profile_img)
                    .showImageForEmptyUri(R.mipmap.profile_img)


                    .considerExifParams(true)
                    .displayer(new RoundedBitmapDisplayer(50))
                    .build();

            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(MyApplication.getContext()));
            ImageLoader.getInstance().displayImage(Uri.fromFile(new File(PropertyManager.getInstance().getUserImagePath())).toString(),imageProfileUser, options);
        }
    }

    public void setFont(){
        userJoin.setTypeface(FontManager.getInstance().getTypeface(ProfileActivity.this, FontManager.NOTOSANS));
        userEmail.setTypeface(FontManager.getInstance().getTypeface(ProfileActivity.this, FontManager.NOTOSANS));
        userId.setTypeface(FontManager.getInstance().getTypeface(ProfileActivity.this, FontManager.NOTOSANS));
        textEditProfile.setTypeface(FontManager.getInstance().getTypeface(ProfileActivity.this,FontManager.NOTOSANS_M));

    }
    public String getDateFormat(String date){
        String resultDate = "";
        StringTokenizer tokenizer = new StringTokenizer(date,"-");
        resultDate += tokenizer.nextToken() + "년 ";
        resultDate += tokenizer.nextToken() + "월 ";
        resultDate += tokenizer.nextToken() + "일 가입";
        return resultDate;
    }
}
