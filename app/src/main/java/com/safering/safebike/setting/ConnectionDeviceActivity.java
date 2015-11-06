package com.safering.safebike.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.adapter.BluetoothDeviceAdapter;
import com.safering.safebike.adapter.BluetoothDeviceHeaderItem;
import com.safering.safebike.adapter.BluetoothDeviceItem;
import com.safering.safebike.adapter.BluetoothItemHeaderView;

import java.util.ArrayList;

public class ConnectionDeviceActivity extends AppCompatActivity {
    ListView listViewBand;
    ListView listViewBacklight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_connection_device);// 이렇게 해도 되는지 물어보기
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listViewBand = (ListView) findViewById(R.id.listview_band);
        listViewBacklight = (ListView) findViewById(R.id.listview_backlight);
        detectDevices();


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


    public void detectDevices() {
        //블루투스 디바이스들 감지해서 화면에 뿌려줌
        BluetoothDeviceAdapter bandAdapter = new BluetoothDeviceAdapter();
        BluetoothDeviceAdapter backligthAdapter = new BluetoothDeviceAdapter();
        for (int i = 0; i < 4; i++) {
            BluetoothDeviceItem data = new BluetoothDeviceItem();
            data.deviceName = "bluetoothBand " + i;
            data.deviceAddress = "A:B:C:D:E:F " + "/" + i;
            bandAdapter.add(data);
        }
        for (int i = 0; i < 4; i++) {
            BluetoothDeviceItem data = new BluetoothDeviceItem();
            data.deviceName = "bluetoothBackligth " + i;
            data.deviceAddress = "F:E:D:C:B:A " + "/" + i;
            backligthAdapter.add(data);

        }
        BluetoothDeviceHeaderItem Header = new BluetoothDeviceHeaderItem();
        Header.deviceType = "진동밴드 선택";
        BluetoothItemHeaderView bluetoothBandHeaderView = new BluetoothItemHeaderView(this);
        bluetoothBandHeaderView.setHeaderItem(Header);

        listViewBand.addHeaderView(bluetoothBandHeaderView, null, false);
        listViewBand.setAdapter(bandAdapter);

        Header = new BluetoothDeviceHeaderItem();
        Header.deviceType = "후미등 선택";
        bluetoothBandHeaderView = new BluetoothItemHeaderView(this);
        bluetoothBandHeaderView.setHeaderItem(Header);

        listViewBacklight.addHeaderView(bluetoothBandHeaderView, null, false);
        listViewBacklight.setAdapter(backligthAdapter);

        listViewBacklight.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("item","selected");
                //view.setSelected(true);
                view.setPressed(true);

                //view.setEnabled(false);
                //view.setActivated(true);
                //view.setHovered(true);
                //view.setFocusable(true);

            }
        });


    }
}
