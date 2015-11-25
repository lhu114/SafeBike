package com.safering.safebike.setting;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.safering.safebike.MainActivity;
import com.safering.safebike.MainFragment;
import com.safering.safebike.R;
import com.safering.safebike.adapter.BluetoothDeviceAdapter;
import com.safering.safebike.adapter.BluetoothDeviceHeaderItem;
import com.safering.safebike.adapter.BluetoothDeviceItem;
import com.safering.safebike.adapter.BluetoothItemView;
import com.safering.safebike.manager.FontManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.android.gms.internal.zzid.runOnUiThread;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {
    public static UUID SERVICE_UUID = UUID.fromString("1706BBC0-88AB-4B8D-877E-2237916EE929");
    private static UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final int REQUEST_ENABLE_BT = 3;
    public BluetoothGatt mGatt;
    boolean isEnableBluetooth = false;
    boolean isButtonClick = false;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDeviceAdapter deviceAdapter;
    ProgressBar searchDevice;
    ListView deviceList;
    Button tmpLeft;
    Button tmpRight;
    TextView textConnectDevice;
    MainFragment mainFragment;

    public SettingFragment() {

        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        textConnectDevice = (TextView) view.findViewById(R.id.btn_connect_device);
        deviceList = (ListView) view.findViewById(R.id.connective_device_list);
        searchDevice = (ProgressBar) view.findViewById(R.id.progressBar_search);
        tmpLeft = (Button) view.findViewById(R.id.btn_tmp_left);
        tmpRight = (Button) view.findViewById(R.id.btn_tmp_right);
        isEnableBluetooth = false;
        isButtonClick = false;
        deviceAdapter = new BluetoothDeviceAdapter();
        deviceList.setAdapter(deviceAdapter);
        //       deviceList.addView(n);

        setFont();
        setDevice();

        tmpLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothConnection.getInstance().writeLeftValue();

                //writeLeftValue();
            }
        });

        tmpRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothConnection.getInstance().writeRightValue();

                //writeRightValue();
            }
        });

        deviceAdapter.setOnSwitchClickListener(new BluetoothItemView.OnSwitchClickListener() {
            @Override
            public void onSwitchClick(BluetoothItemView view, BluetoothDeviceItem item, boolean isChecked) {

                if (isChecked) {


                    if (BluetoothConnection.getInstance().getConnectedValue(item.deviceAddress) == false) {
                        // Log.i("device~!~address false", item.deviceAddress);

                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(item.deviceAddress);
                        connectToDevice(device);
                        //deviceAdapter.getItem(item.deviceAddress).isConnecting = true;
                        // Log.i("deviceCon", deviceAdapter.getItem(item.deviceAddress).isConnecting + "");
                    } else {
                        Log.i("device~!~address true", item.deviceAddress);

                    }


                } else {
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(item.deviceAddress);
                    disconnectToDevice(device);

                }

            }
        });

        textConnectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    //다이얼로그
                    return;
                }
                BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    searchDevice.setVisibility(View.VISIBLE);
                    scanLeDevice(true);

                }


            }
        });
        return view;
    }

    @Override
    public void onResume() {

        super.onResume();
        if (isEnableBluetooth) {
            searchDevice.setVisibility(View.VISIBLE);
            scanLeDevice(true);
            Toast.makeText(getContext(), "onResumeEnable", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getContext(), "onResumeFalse", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    isEnableBluetooth = true;
                } else {
                    //다이얼로그 띄우기
                }

                break;
        }

    }

    public void setDevice() {
        ArrayList<BluetoothDevice> devices = BluetoothConnection.getInstance().getDevices();
        for (int i = 0; i < devices.size(); i++) {

            BluetoothDeviceItem deviceItem = new BluetoothDeviceItem();
            deviceItem.deviceName = devices.get(i).getName();
            deviceItem.deviceAddress = devices.get(i).getAddress();
//            Log.i("setDevice",devices.get(i).getName());


            if (BluetoothConnection.getInstance().getConnectedValue(devices.get(i).getAddress())) {
                deviceAdapter.add(deviceItem, true);
            } else {
                deviceAdapter.add(deviceItem, false);
            }


        }

    }

    public void setFont() {
        textConnectDevice.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS_M));
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {

            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
        //핸들러로 제한 시간 5초
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    searchDevice.setVisibility(View.GONE);
                    BluetoothConnection.getInstance().addDevice(device);
                    BluetoothConnection.getInstance().setConnectedValue(device.getAddress(), false);

                    BluetoothDeviceItem deviceItem = new BluetoothDeviceItem();
                    deviceItem.deviceName = device.getName();
                    deviceItem.deviceAddress = device.getAddress();
                    deviceAdapter.add(deviceItem, false);
                    isButtonClick = true;


                }
            });
        }
    };

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            Log.i("---mGatt state---", "mGatt NULL");
            mGatt = device.connectGatt(getContext(), false, gattCallback);
            //객체 설정
            scanLeDevice(false);// will stop after first device detection
        } else {
            mGatt = device.connectGatt(getContext(), false, gattCallback);
            scanLeDevice(false);// will stop after first device detection

        }
    }

    public void disconnectToDevice(BluetoothDevice device) {

        if (mGatt != null) {
            mGatt.disconnect();
            mGatt = null;
            BluetoothConnection.getInstance().setGatt(null);
            BluetoothConnection.getInstance().setConnectedValue(device.getAddress(), false);

            //mGatt = device.connectGatt(getContext(), false, gattCallback);

            //객체 설정
            // scanLeDevice(false);// will stop after first device detection
        }
    }


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    //BluetoothConnection.getInstance()
                    BluetoothConnection.getInstance().setGatt(gatt);
                    BluetoothConnection.getInstance().setConnectedValue(gatt.getDevice().getAddress(), true);
                    //  BluetoothConnection.getInstance().setIsConnect(1);
                    mainFragment = (MainFragment) (getActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.TAG_MAIN));
                    mainFragment.connectionOnOff(1);
                    Log.i("connect!!Device", deviceAdapter.getItem(gatt.getDevice().getAddress()).deviceName);


                    //deviceAdapter.getItem(gatt.getDevice().getAddress()).isConnecting = true;
                    // Log.i("deviceConState", deviceAdapter.getItem(gatt.getDevice().getAddress()).isConnecting + "");

                    // BluetoothConnection.getInstance().getDevice(gatt.getDevice().getAddress())
                    Log.i("gattCallback", "STATE_CONNECTED");

                    gatt.discoverServices();


                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    //다이얼로그 띄우기
                    Log.e("gattCallback", "STATE_DISCONNECTED");

                    if (mGatt != null) {
                        mGatt.disconnect();
                        mGatt = null;
                        BluetoothConnection.getInstance().setGatt(null);
                        BluetoothConnection.getInstance().setConnectedValue(gatt.getDevice().getAddress(), false);

                        //mGatt = device.connectGatt(getContext(), false, gattCallback);

                        //객체 설정
                        // scanLeDevice(false);// will stop after first device detection
                    }
                    mainFragment = (MainFragment) (getActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.TAG_MAIN));
                    mainFragment.connectionOnOff(0);

                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            Log.i("onServicesDiscoCracter", gatt.getService(SERVICE_UUID).getCharacteristic(MY_UUID_SECURE).getUuid().toString());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            //Log.i("onCharacteristicRead", characteristic.getUuid().toString());
            Log.i("onCharacteristicRead", gatt.getService(SERVICE_UUID).getCharacteristic(MY_UUID_SECURE).getUuid().toString());
            //연결됬음
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //클라이언트에서 write 후 서버가 응답해주면 호출
            Log.i("onCaractorersticChanged", "onCharactoeristicWrite");

            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicWrite", gatt.getService(SERVICE_UUID).getCharacteristic(MY_UUID_SECURE).getUuid().toString());

            super.onCharacteristicWrite(gatt, characteristic, status);

        }
    };
}
