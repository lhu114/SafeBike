package com.safering.safebike.account;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.safering.safebike.R;
import com.safering.safebike.property.PropertyManager;

public class ProfileActivity extends AppCompatActivity {
    TextView userId;
    TextView userEmail;
    TextView userJoin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userId = (TextView)findViewById(R.id.text_id_profile);
        userEmail = (TextView)findViewById(R.id.text_email_profile);
        userJoin = (TextView)findViewById(R.id.text_join_profile);

        userId.setText(PropertyManager.getInstance().getUserId());
        userEmail.setText(PropertyManager.getInstance().getUserEmail());
        userJoin.setText(PropertyManager.getInstance().getUserJoin());

        Button btn = (Button)findViewById(R.id.btn_edit_profile);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this,ProfileEditActivity.class);
                startActivity(intent);
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
