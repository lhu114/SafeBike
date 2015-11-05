package com.safering.safebike.login;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.safering.safebike.R;
import com.safering.safebike.property.PropertyManager;

public class LoginActivity extends AppCompatActivity {
    //private static final String FRAGMENT_LOGIN_TAG = "login_fragment";
    //2015/11/02
    private Fragment loginFragment;
    private static final String SERVICE_FINISH = "finish";

    public static final int MESSAGE_BACK_KEY = 1;
    public static final int TIME_BACK_TIMEOUT = 2000;
    private boolean isBackPressed = false;

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

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_BACK_KEY :
                    isBackPressed = false;

                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_FINISH)) {
            if (isBackPressed) {
                mHandler.removeMessages(MESSAGE_BACK_KEY);
                super.onBackPressed();
            } else {
                isBackPressed = true;
                Toast.makeText(this, "뒤로 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                mHandler.sendEmptyMessageDelayed(MESSAGE_BACK_KEY, TIME_BACK_TIMEOUT);
            }
        }
    }

}
