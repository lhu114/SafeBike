package com.safering.safebike.account;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
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
        userName = (TextView)findViewById(R.id.text_edit_profilename);
        userPass = (TextView)findViewById(R.id.text_edit_profilepass);
        userPassConfirm = (TextView)findViewById(R.id.text_edit_profilepasscon);

        imageBack = (ImageView)findViewById(R.id.image_backkey);
        imageBack.setVisibility(View.GONE);

        textTitle = (TextView)findViewById(R.id.text_custom_title);


        textEditPhoto = (TextView)findViewById(R.id.text_editphoto_profile);
        userProfileImage = (ImageView) findViewById(R.id.image_user_profile);

        userId = (TextView) findViewById(R.id.text_id_profile);
        userEmail = (TextView) findViewById(R.id.text_email_profile);
        userJoin = (TextView) findViewById(R.id.text_join_profile);



        changeId = (EditText)findViewById(R.id.edit_change_id);
        changePassword = (EditText)findViewById(R.id.edit_change_password);
        changePasswordConfirm = (EditText)findViewById(R.id.edit_change_password_confirm);

        textCompelete = (TextView) findViewById(R.id.btn_edit_compelete);

        userId.setText(PropertyManager.getInstance().getUserId());
        userEmail.setText(PropertyManager.getInstance().getUserEmail());
        userJoin.setText(PropertyManager.getInstance().getUserJoin());

        textEditPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GET_USER_IMAGE);
            }
        });




        textTitle.setText(R.string.edit_profile_title);

        setFont();
        textCompelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = PropertyManager.getInstance().getUserEmail();
                String id = changeId.getText().toString();
                String password = changePassword.getText().toString();
                String passwordConfirm = changePasswordConfirm.getText().toString();
                /*if(checkEditForm() == EDIT_FAIL){
                    //에디트 텍스트 밑에 텍스트 띄우기
                    return;
                }
                if(uri != null) {
                    if (PropertyManager.getInstance().getUserImagePath() != uri.getPath()) {
                        file = new File(uri.getPath());

                    }
                }
                NetworkManager.getInstance().saveUserProfile(ProfileEditActivity.this, email, id, password, file, new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object success) {
                        //내 프로퍼티 수정
                    }

                    @Override
                    public void onFail(int code) {

                    }
                });
*/
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
                        /*.showImageForEmptyUri(R.drawable.ic)
                        .showImageOnFail(R.drawable.ic_error)
                        */
                        .considerExifParams(true)
                        .displayer(new RoundedBitmapDisplayer(50))
                        .build();
                uri = data.getData();

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
    public int checkEditForm(){
        String id = changeId.getText().toString();
        String password = changePassword.getText().toString();
        String passwordConfirm = changePasswordConfirm.getText().toString();

        if(TextUtils.isEmpty(id) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordConfirm)){
            return EDIT_FAIL;
        }
        if(!password.equals(passwordConfirm)){
            return EDIT_FAIL;
        }

        return EDIT_SUCCESS;
    }

    public void setFont(){
        userId.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this,FontManager.NOTOSANS_M));
        userEmail.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this,FontManager.NOTOSANS));
        userJoin.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this, FontManager.NOTOSANS));
        textEditPhoto.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this,FontManager.NOTOSANS));

        userName.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this,FontManager.NOTOSANS_M));
        userPass.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this,FontManager.NOTOSANS_M));
        userPassConfirm.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this,FontManager.NOTOSANS_M));
        textEditPhoto.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this,FontManager.NOTOSANS));
        textCompelete.setTypeface(FontManager.getInstance().getTypeface(ProfileEditActivity.this,FontManager.NOTOSANS_M));
    }
}
