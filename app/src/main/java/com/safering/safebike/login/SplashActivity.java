package com.safering.safebike.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.property.PropertyManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        String userEmail = PropertyManager.getInstance().getUserEmail();
        String userPassword = PropertyManager.getInstance().getUserPassword();
        Log.i("Userid", PropertyManager.getInstance().getUserId());
        Log.i("UserPass",PropertyManager.getInstance().getUserPassword());

        if(TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPassword)){

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {goLogin();}
            },1000);
        }
        else{
            goMain();
        }
    }

    public void goMain(){
        Intent intent = new Intent(SplashActivity.this,MainActivity.class);
        startActivity(intent);
        finish();

    }

    public void goLogin(){
        Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    Handler mHandler = new Handler(Looper.getMainLooper());

}
