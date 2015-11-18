package com.safering.safebike.account;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedVignetteBitmapDisplayer;
import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.MyApplication;
import com.safering.safebike.property.PropertyManager;

import java.io.File;
import java.util.StringTokenizer;

public class ProfileEditActivity extends AppCompatActivity {
    TextView userName;
    TextView userPass;
    TextView userPassConfirm;
    TextView textCompelete;
    TextView userId;
    TextView userEmail;
    TextView userJoin;
    TextView textTitle;
    TextView textEditPhoto;
    EditText changeId;
    EditText changePassword;
    EditText changePasswordConfirm;
    TextView textCompelte;
    File file;
    ImageView userProfileImage;
    DisplayImageOptions options;
    ImageView imageBack;

    Uri uri = null;
    public static final int GET_USER_IMAGE = 11;
    public static final int EDIT_SUCCESS = 1;
    public static final int EDIT_FAIL = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);
        userName = (TextView) findViewById(R.id.text_edit_profilename);
        userPass = (TextView) findViewById(R.id.text_edit_profilepass);
        userPassConfirm = (TextView) findViewById(R.id.text_edit_profilepasscon);

        imageBack = (ImageView) findViewById(R.id.image_backkey);
        imageBack.setVisibility(View.GONE);

        textTitle = (TextView) findViewById(R.id.text_custom_title);


        textEditPhoto = (TextView) findViewById(R.id.text_editphoto_profile);
        userProfileImage = (ImageView) findViewById(R.id.image_edit_user_profile);

        userId = (TextView) findViewById(R.id.text_id_profile);
        userEmail = (TextView) findViewById(R.id.text_email_profile);
        userJoin = (TextView) findViewById(R.id.text_join_profile);


        changeId = (EditText) findViewById(R.id.edit_change_id);
        changePassword = (EditText) findViewById(R.id.edit_change_password);
        changePasswordConfirm = (EditText) findViewById(R.id.edit_change_password_confirm);

        textCompelete = (TextView) findViewById(R.id.btn_edit_compelete);

        userId.setText(PropertyManager.getInstance().getUserId());
        userEmail.setText(PropertyManager.getInstance().getUserEmail());
        userJoin.setText(getDateFormat(PropertyManager.getInstance().getUserJoin()));

        textEditPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent photoPickerIntent = new Intent(
                        Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.putExtra("crop", "true");
                photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
                photoPickerIntent.putExtra("outputFormat",
                        Bitmap.CompressFormat.JPEG.toString());


                /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image*//*");
                */
                startActivityForResult(photoPickerIntent, GET_USER_IMAGE);
            }
        });


        textTitle.setText(R.string.edit_profile_title);

        setFont();
        textCompelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = PropertyManager.getInstance().getUserEmail();
                final String id = changeId.getText().toString();
                final String password = changePassword.getText().toString();
                String passwordConfirm = changePasswordConfirm.getText().toString();
                if (checkEditForm() == EDIT_FAIL) {
                    //에디트 텍스트 밑에 텍스트 띄우기
                    return;
                }
               /*if(file == null){
                    *//*Uri.parse("android.resource://com.androidbook.samplevideo/" + R.raw.myvideo);
                    *//*
                    Toast.makeText(ProfileEditActivity.this,"file is null",Toast.LENGTH_SHORT).show();
                    Uri otherPath = Uri.parse("android.resource://com.safering.safebike/" + R.mipmap.profile_img);
                    Toast.makeText(ProfileEditActivity.this,otherPath.getPath(),Toast.LENGTH_SHORT).show();

                    //file = new File();

                    return;
                }*/

                NetworkManager.getInstance().saveUserProfile(ProfileEditActivity.this, email, id, password, file, new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object success) {

                        PropertyManager.getInstance().setUserId(id);
                        PropertyManager.getInstance().setUserPassword(password);
                        PropertyManager.getInstance().setUserImagePath(file.getAbsolutePath());

                        Intent intent = new Intent(ProfileEditActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("edit", "e");
                        startActivityForResult(intent, GET_USER_IMAGE);
                    }

                    @Override
                    public void onFail(int code) {

                    }
                });


            }
        });


    }

    private Uri getTempUri() {
        file = new File(Environment.getExternalStorageDirectory(), "temp_" + System.currentTimeMillis() / 1000);
        Toast.makeText(this, "getTempUri", Toast.LENGTH_SHORT).show();
        Log.i("getTempUri", file.getAbsolutePath());
        return Uri.fromFile(file);
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
                        .showImageOnLoading(R.mipmap.profile_img)
                        .showImageForEmptyUri(R.mipmap.profile_img)


                        .considerExifParams(true)
                        .displayer(new RoundedBitmapDisplayer(50))
                        .build();
                /*uri = data.getData();

                Log.i("W", "uri: " + uri.toString());
                Log.i("W", "path: " + uri.getPath());
                */

                ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(MyApplication.getContext()));

                ImageLoader.getInstance().displayImage(Uri.fromFile(new File(file.getAbsolutePath())).toString(), userProfileImage, options);


            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    public int checkEditForm() {
        String id = changeId.getText().toString();
        String password = changePassword.getText().toString();
        String passwordConfirm = changePasswordConfirm.getText().toString();

        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordConfirm)) {
            return EDIT_FAIL;
        }
        if (!password.equals(passwordConfirm)) {
            return EDIT_FAIL;
        }

        return EDIT_SUCCESS;
    }

    public void setFont() {
        userId.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS));
        userEmail.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS));
        userJoin.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS));
        textEditPhoto.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS));

        userName.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS_M));
        userPass.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS_M));
        userPassConfirm.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS_M));
        textEditPhoto.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS));
        textCompelete.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS_M));
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
