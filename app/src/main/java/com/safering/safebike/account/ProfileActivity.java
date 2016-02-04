package com.safering.safebike.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.property.PropertyManager;

import java.util.StringTokenizer;

public class ProfileActivity extends AppCompatActivity {
    TextView userId;
    TextView userEmail;
    TextView userJoin;
    TextView textEditProfile;
    ImageView imageProfileUser;
    DisplayImageOptions options;
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

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .showImageOnLoading(R.drawable.profile_img)
                .showImageForEmptyUri(R.drawable.profile_img)


                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(1000))
                .build();


        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(ProfileActivity.this));


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

        setProfile();
        setFont();
    }




    public void setProfile(){
        userId.setText(PropertyManager.getInstance().getUserId());
        userEmail.setText(PropertyManager.getInstance().getUserEmail());
        userJoin.setText(getDateFormat(PropertyManager.getInstance().getUserJoin()));
        if(!PropertyManager.getInstance().getUserImagePath().equals("")){
            String imagePath = PropertyManager.getInstance().getUserImagePath();
            ImageLoader.getInstance().displayImage(imagePath, imageProfileUser, options);


        }
    }

    public void setFont(){
        textTitle.setTypeface(FontManager.getInstance().getTypeface(ProfileActivity.this,FontManager.NOTOSANS));
        userJoin.setTypeface(FontManager.getInstance().getTypeface(ProfileActivity.this, FontManager.NOTOSANS));
        userEmail.setTypeface(FontManager.getInstance().getTypeface(ProfileActivity.this, FontManager.NOTOSANS));
        userId.setTypeface(FontManager.getInstance().getTypeface(ProfileActivity.this, FontManager.NOTOSANS));
        textEditProfile.setTypeface(FontManager.getInstance().getTypeface(ProfileActivity.this,FontManager.NOTOSANS_M));

    }
    public String getDateFormat(String date){
        String resultDate = "";
        if (!date.equals("")) {
            StringTokenizer tokenizer = new StringTokenizer(date, "-");
            resultDate += tokenizer.nextToken() + "년 ";
            resultDate += tokenizer.nextToken() + "월 ";
            resultDate += tokenizer.nextToken() + "일 가입";
        }
        return resultDate;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
