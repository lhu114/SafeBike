package com.safering.safebike.setting;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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
import android.widget.Toast;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.adapter.BluetoothDeviceAdapter;
import com.safering.safebike.adapter.BluetoothDeviceHeaderItem;
import com.safering.safebike.adapter.BluetoothDeviceItem;
import com.safering.safebike.adapter.BluetoothItemHeaderView;
import com.safering.safebike.adapter.BluetoothItemView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ConnectionDeviceActivity extends AppCompatActivity {
    ListView listViewBand;
    ListView listViewBacklight;
    Button searchDevices;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDeviceAdapter bandAdapter;
    BluetoothDeviceAdapter backligthAdapter;
    BluetoothConnection bConnection;
    private ArrayList<UUID> mUuids;


    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_connection_device);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*mUuids = new ArrayList<UUID>();
        mUuids.add(UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
        mUuids.add(UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
        mUuids.add(UUID.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"));
        mUuids.add(UUID.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"));
        mUuids.add(UUID.fromString("aa91eab1-d8ad-448e-abdb-95ebba4a9b55"));
        mUuids.add(UUID.fromString("4d34da73-d0a4-4f40-ac38-917e0a9dee97"));
        mUuids.add(UUID.fromString("5e14d4df-9c8a-4db7-81e4-c937564c86e0"));
        */

        bConnection = new BluetoothConnection();
        bandAdapter = new BluetoothDeviceAdapter();
        backligthAdapter = new BluetoothDeviceAdapter();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listViewBand = (ListView) findViewById(R.id.listview_band);
        listViewBand.setFooterDividersEnabled(false);

        listViewBacklight = (ListView) findViewById(R.id.listview_backlight);
        listViewBacklight.setFooterDividersEnabled(false);
        listViewBand.setAdapter(bandAdapter);
        listViewBacklight.setAdapter(backligthAdapter);


        searchDevices = (Button) findViewById(R.id.btn_search_direct_device);
        searchDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDevices();//직접 검색
            }
        });
        resultDevices();//페어링된것만 보여주기


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

    public void searchDevices() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

    }

    public void resultDevices() {
        //블루투스 디바이스들 감지해서 화면에 뿌려줌
        //BluetoothDeviceAdapter bandAdapter = new BluetoothDeviceAdapter();
        // BluetoothDeviceAdapter backligthAdapter = new BluetoothDeviceAdapter();
        /*for (int i = 0; i < 4; i++) {
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

        }*/

       /* listViewBacklight.setAdapter(backligthAdapter);
*/
        listViewBacklight.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BluetoothSocket tmp = null;

                BluetoothItemView itemView = (BluetoothItemView) view;
                Toast.makeText(ConnectionDeviceActivity.this, "selectitem : " + itemView.deviceName.getText().toString(), Toast.LENGTH_SHORT).show();

                /*Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, itemView.deviceAddress.getText().toString());

                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
                */
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(itemView.deviceAddress.getText().toString());
                bConnection.connect(device, MY_UUID_SECURE);

            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(ConnectionDeviceActivity.this, "onPause", Toast.LENGTH_SHORT).show();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
        //this.unregisterReceiver(mReceiver);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);

    }

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    BluetoothDeviceItem data = new BluetoothDeviceItem();
                    data.deviceName = device.getName();
                    data.deviceAddress = device.getAddress();
                    backligthAdapter.add(data);
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
             /*   setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                Toast.makeText(DeviceListActivity.this,"ACTION_DISCOVERY_FINISED",Toast.LENGTH_SHORT).show();

                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }*/
            }
        }
    };

}
