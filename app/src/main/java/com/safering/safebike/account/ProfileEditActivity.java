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
import com.safering.safebike.manager.InformDialogFragment;
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
    ImageView imageProfileUser;
    File file;
    ImageView userProfileImage;
    DisplayImageOptions options;
    ImageView imageBack;
    InformDialogFragment dialog;

    Uri uri = null;
    public static final int GET_USER_IMAGE = 11;
    public static final int EDIT_SUCCESS = 1;
    public static final int EDIT_FAIL = -1;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);
        //  ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(MyApplication.getContext()));
        dialog = new InformDialogFragment();
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
        //imageProfileUser = (ImageView)findViewById(R.id.image_edit_user_profile);

        changeId = (EditText) findViewById(R.id.edit_change_id);
        changePassword = (EditText) findViewById(R.id.edit_change_password);
        changePasswordConfirm = (EditText) findViewById(R.id.edit_change_password_confirm);

        textCompelete = (TextView) findViewById(R.id.btn_edit_compelete);





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
                startActivityForResult(photoPickerIntent, GET_USER_IMAGE);
            }
        });
        textCompelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = PropertyManager.getInstance().getUserEmail();
                final String id = changeId.getText().toString();
                final String password = changePassword.getText().toString();
                String passwordConfirm = changePasswordConfirm.getText().toString();
                if (checkEditForm() == EDIT_FAIL) {
                    //에디트 텍스트 밑에 텍스트 띄우기
                    dialog.setContent("프로필 편집","프로필 정보를 확인해주세요.");
                    dialog.show(getSupportFragmentManager(), "profile");
                    return;
                }
                NetworkManager.getInstance().saveUserProfile(ProfileEditActivity.this, email, id, password, file, new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object success) {

                        PropertyManager.getInstance().setUserId(id);
                        PropertyManager.getInstance().setUserPassword(password);

                        if (success.toString().contains("https")) {
                            //PropertyManager.getInstance().setUserImagePath("");
                            Toast.makeText(ProfileEditActivity.this, "url : " + success.toString(), Toast.LENGTH_SHORT).show();


                            PropertyManager.getInstance().setUserImagePath(success.toString().substring(1,success.toString().length()-1));


                            Log.i("return:", PropertyManager.getInstance().getUserImagePath());

                        } else {
                            Toast.makeText(ProfileEditActivity.this, "nonurl : " + success, Toast.LENGTH_SHORT).show();
                        }

                        Intent intent = new Intent(ProfileEditActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivityForResult(intent, GET_USER_IMAGE);

                    }

                    @Override
                    public void onFail(int code) {

                    }
                });


            }
        });

        setProfile();
        setFont();


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
                        .displayer(new RoundedBitmapDisplayer(1000))
                        .build();
                ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(MyApplication.getContext()));

                ImageLoader.getInstance().displayImage(Uri.fromFile(new File(file.getAbsolutePath())).toString(), userProfileImage, options);
                Toast.makeText(ProfileEditActivity.this, "file path : " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //  setProfile();
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

    public void setProfile() {
        userId.setText(PropertyManager.getInstance().getUserId());
        userEmail.setText(PropertyManager.getInstance().getUserEmail());
        userJoin.setText(getDateFormat(PropertyManager.getInstance().getUserJoin()));
        if (!PropertyManager.getInstance().getUserImagePath().equals("")) {
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
            ImageLoader.getInstance().displayImage(PropertyManager.getInstance().getUserImagePath(), userProfileImage, options);
        }


    }

    public void setFont() {

        userId.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS));
        userEmail.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS));
        userJoin.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS));
        textEditPhoto.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS));

        userName.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS_M));
        userPass.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS_M));
        userPassConfirm.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS_M));
        textTitle.setText(R.string.edit_profile_title);
        textTitle.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this,FontManager.NOTOSANS_M));
        textEditPhoto.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS));
        textCompelete.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS_M));
    }

    public String getDateFormat(String date) {
        String resultDate = "";
        StringTokenizer tokenizer = new StringTokenizer(date, "-");
        resultDate += tokenizer.nextToken() + "년 ";
        resultDate += tokenizer.nextToken() + "월 ";
        resultDate += tokenizer.nextToken() + "일 가입";
        return resultDate;
    }

}
