package com.safering.safebike.account;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.InformDialogFragment;

public class AccountHelpActivity extends AppCompatActivity {
   // TextView textShow;
    //debug용
    TextView textQnA;
    TextView textPolicy;
    TextView textLicense;
    TextView textTitle;
    TextView textSafeTitle;
    TextView textSafeCopyright;
    TextView textSafeRight;
    ImageView imageBackkey;
    InformDialogFragment dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_help);


        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);

        textTitle = (TextView)findViewById(R.id.text_custom_title);
        textTitle.setText(R.string.account_help);

        textSafeTitle = (TextView)findViewById(R.id.text_safebike_title);
        textSafeCopyright = (TextView)findViewById(R.id.text_safebike_copyright);
        textSafeRight = (TextView)findViewById(R.id.text_safebike_right);

        //textShow = (TextView)findViewById(R.id.text_show_use);
        textQnA = (TextView)findViewById(R.id.text_qna_email);
        textPolicy = (TextView)findViewById(R.id.text_policy_profile);
        textLicense = (TextView)findViewById(R.id.text_license);
        imageBackkey = (ImageView)findViewById(R.id.image_backkey);
        dialog = new InformDialogFragment();

/*
        textShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setContent("사용법", "준비중입니다.");
                dialog.show(getSupportFragmentManager(), "use");
            }
        });*/
        textQnA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QnAEmailDialogFragment qnAEmailDialogFragment = new QnAEmailDialogFragment();
                qnAEmailDialogFragment.show(getSupportFragmentManager(),"QNA");

            }
        });
        textPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PolicyProfileDialogFragment policyProfileDialogFragment = new PolicyProfileDialogFragment();
                policyProfileDialogFragment.show(getSupportFragmentManager(),"Policy");
            }
        });
        textLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LicenseDialogFragment licenseDialogFragment = new LicenseDialogFragment();
                licenseDialogFragment.show(getSupportFragmentManager(),"License");
            }
        });

        imageBackkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setFont();




    }

    public void setFont(){
        //textShow.setTypeface(FontManager.getInstance().getTypeface(AccountHelpActivity.this,FontManager.NOTOSANS_M));
        textQnA.setTypeface(FontManager.getInstance().getTypeface(AccountHelpActivity.this,FontManager.NOTOSANS_M));
        textPolicy.setTypeface(FontManager.getInstance().getTypeface(AccountHelpActivity.this,FontManager.NOTOSANS_M));
        textLicense.setTypeface(FontManager.getInstance().getTypeface(AccountHelpActivity.this,FontManager.NOTOSANS_M));
        textSafeTitle.setTypeface(FontManager.getInstance().getTypeface(AccountHelpActivity.this,FontManager.NOTOSANS));
        textSafeCopyright.setTypeface(FontManager.getInstance().getTypeface(AccountHelpActivity.this,FontManager.NOTOSANS));
        textSafeRight.setTypeface(FontManager.getInstance().getTypeface(AccountHelpActivity.this,FontManager.NOTOSANS));
        textTitle.setTypeface(FontManager.getInstance().getTypeface(AccountHelpActivity.this,FontManager.NOTOSANS));

    }
}
