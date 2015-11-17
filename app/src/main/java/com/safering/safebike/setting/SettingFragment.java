package com.safering.safebike.setting;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.adapter.BluetoothDeviceAdapter;
import com.safering.safebike.adapter.BluetoothDeviceHeaderItem;
import com.safering.safebike.adapter.BluetoothDeviceItem;
import com.safering.safebike.manager.FontManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {
    boolean isEnableBluetooth = false;
    boolean isDiscovery = false;
    private static final int REQUEST_ENABLE_BT = 3;
    ListView deviceList;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDeviceAdapter deviceAdapter;

    TextView textConnectDevice;

    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        textConnectDevice = (TextView) view.findViewById(R.id.btn_connect_device);
        deviceList = (ListView)view.findViewById(R.id.connective_device_list);
        deviceAdapter = new BluetoothDeviceAdapter();
        deviceList.setAdapter(deviceAdapter);


        setFont();
        textConnectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    return;
                }
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else{
                    isEnableBluetooth = true;
                }
                if (isEnableBluetooth == true) {
                    /*Intent intent = new Intent(getContext(), ConnectionDeviceActivity.class);
                    startActivity(intent);
                    */
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    ((MainActivity)getActivity()).registerReceiver(mReceiver, filter);

                    // Register for broadcasts when discovery has finished
                    filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    ((MainActivity)getActivity()).registerReceiver(mReceiver, filter);
                    Log.i("bluetooth","search");

                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    mBluetoothAdapter.startDiscovery();
                    isDiscovery = true;


                }
                else{
                    //블루투스 장치 사용불가 다이얼로그 띄우기
                }

            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    isEnableBluetooth = true;
                }
                break;
        }

    }

    public void setFont(){
        textConnectDevice.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS_M));
    }

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //bandAdapter.removeAll();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    Log.i("bluetooth result","search result");

                    BluetoothDeviceItem deviceItem = new BluetoothDeviceItem();
                    deviceItem.deviceName = device.getName();
                    deviceItem.deviceAddress = device.getAddress();
                    deviceAdapter.add(deviceItem);
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
