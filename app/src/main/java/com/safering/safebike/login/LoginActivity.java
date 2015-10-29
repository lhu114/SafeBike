package com.safering.safebike.login;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.safering.safebike.R;

public class LoginActivity extends AppCompatActivity {
    //private static final String FRAGMENT_LOGIN_TAG = "login_fragment";
    private Fragment loginFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginFragment = new LoginFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.login_container,loginFragment,"loginss");
        ft.commit();



    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(LoginActivity.this,"LoginActivityResume",Toast.LENGTH_SHORT).show();

    }

}
