package com.safering.safebike.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;

import java.util.ArrayList;

public class ConnectionDeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_connection_device);// 이렇게 해도 되는지 물어보기
        //setContentView(R.layout.activity_connection_device);

        TextView header = new TextView(this);
        header.setText("헤더뷰");

        TextView header1 = new TextView(this);
        header1.setText("헤더뷰");

        ArrayList<String> bandList = new ArrayList<>();
        bandList.add("hand1");
        bandList.add("hand2");

        ArrayList<String> backlightList = new ArrayList<>();
        backlightList.add("backlight1");
        backlightList.add("bakcligth2");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,bandList);

        ListView listView = (ListView)findViewById(R.id.listview_band);
        listView.addHeaderView(header1);

        listView.setAdapter(arrayAdapter);

        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,backlightList);
        listView = (ListView)findViewById(R.id.listview_backlight);
        listView.addHeaderView(header);

        listView.setAdapter(arrayAdapter);

        Button btn = (Button)findViewById(R.id.btn_complete);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConnectionDeviceActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
