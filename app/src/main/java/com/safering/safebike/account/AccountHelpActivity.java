package com.safering.safebike.account;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.safering.safebike.R;
import com.safering.safebike.login.LoginFailDialogFragment;

public class AccountHelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button btn;

        btn = (Button)findViewById(R.id.btn_show_use);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UseDialogFragment useDialogFragment = new UseDialogFragment();
                useDialogFragment.show(getSupportFragmentManager(), "Use");
            }
        });
        btn = (Button)findViewById(R.id.btn_qna_email);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QnAEmailDialogFragment qnAEmailDialogFragment = new QnAEmailDialogFragment();
                qnAEmailDialogFragment.show(getSupportFragmentManager(),"QNA");

            }
        });
        btn = (Button)findViewById(R.id.btn_policy_profile);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PolicyProfileDialogFragment policyProfileDialogFragment = new PolicyProfileDialogFragment();
                policyProfileDialogFragment.show(getSupportFragmentManager(),"Policy");
            }
        });
        btn = (Button)findViewById(R.id.btn_license);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LicenseDialogFragment licenseDialogFragment = new LicenseDialogFragment();
                licenseDialogFragment.show(getSupportFragmentManager(),"License");
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
