package com.safering.safebike.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.safering.safebike.R;
import com.safering.safebike.property.PropertyManager;

public class LoginActivity extends AppCompatActivity {
    //private static final String FRAGMENT_LOGIN_TAG = "login_fragment";
    //2015/11/02
    private Fragment loginFragment;
    private static final String SERVICE_FINISH = "finish";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);

        loginFragment = new LoginFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.login_container, loginFragment, "loginss");
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(LoginActivity.this,"Stack Count : " + getSupportFragmentManager().getBackStackEntryCount(),Toast.LENGTH_SHORT).show();

    }



}
