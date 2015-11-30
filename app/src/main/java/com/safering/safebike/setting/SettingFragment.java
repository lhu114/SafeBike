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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
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
    private static String BACKLIGHT_ADDRESS = "E4:96:03:27:F6:4B";
    private static final int REQUEST_ENABLE_BT = 3;
    public BluetoothGatt mGatt;
    boolean isEnableBluetooth = false;
    // boolean isButtonClick = false;
    private ScanSettings settings;
    private List<ScanFilter> filters;


    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothLeScanner mLEScanner;
    BluetoothDeviceAdapter deviceAdapter;
    ProgressBar searchDevice;
    ListView deviceList;
 /*   Button tmpLeft;
    Button tmpRight;
    Button tmpOff;*/
    public int onServerResponse = 0;
    TextView textConnectDevice, textMainTitle;
    boolean isConn = false;

    MainFragment mainFragment;

    public SettingFragment() {

        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        textMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_main_title);
        textConnectDevice = (TextView) view.findViewById(R.id.btn_connect_device);
        deviceList = (ListView) view.findViewById(R.id.connective_device_list);
        searchDevice = (ProgressBar) view.findViewById(R.id.progressBar_search);
//        tmpLeft = (Button) view.findViewById(R.id.btn_tmp_left);
//        tmpRight = (Button) view.findViewById(R.id.btn_tmp_right);
//        tmpOff = (Button) view.findViewById(R.id.btn_tmp_off);
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        isEnableBluetooth = false;

        deviceAdapter = new BluetoothDeviceAdapter();
        deviceList.setAdapter(deviceAdapter);
        //       deviceList.addView(n);
        if (Build.VERSION.SDK_INT >= 21) {
            Log.i("sdk>21","lescan");
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();
        }

        /*tmpLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BluetoothConnection.getInstance() == null) {
                    Log.i("bleLeft", "null");
                }

                BluetoothConnection.getInstance().writeLeftValue();
            }
        });

        tmpRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BluetoothConnection.getInstance() == null) {
                    Log.i("bleRight", "null");
                }

                BluetoothConnection.getInstance().writeRightValue();
            }
        });

        tmpOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothConnection.getInstance().writeOffValue();
            }
        });*/
        deviceAdapter.setOnSwitchClickListener(new BluetoothItemView.OnSwitchClickListener() {
            @Override
            public void onSwitchClick(BluetoothItemView view, BluetoothDeviceItem item, boolean isChecked) {
                Log.i("deviceName", item.deviceName);
                if (isChecked) {
                    if (BluetoothConnection.getInstance().getConnectedValue(item.deviceAddress) == false) {
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(item.deviceAddress);
                        connectToDevice(device);
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
                if (isConn == false) {


                    if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                        //다이얼로그
                        return;
                    }

                    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    } else {
                        searchDevice.setVisibility(View.VISIBLE);
                        scanLeDevice(true);

                    }
                }


            }
        });
        setFont();
        setDevice();
        return view;
    }

    @Override
    public void onResume() {

        super.onResume();
        if (isEnableBluetooth) {
            searchDevice.setVisibility(View.VISIBLE);
            scanLeDevice(true);

        } else {
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
                Log.i("disconnect", "true");

                deviceAdapter.add(deviceItem, true);
            } else {
                Log.i("disconnect", "false");

                deviceAdapter.add(deviceItem, false);
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //scanLeDevice(false);
    }

    public void setFont() {
        textMainTitle.setText("설정");
        textMainTitle.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS_M));
        textConnectDevice.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS_M));
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {

            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                isConn = true;
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
                isConn = true;

            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                isConn = false;
            } else {
                mLEScanner.stopScan(mScanCallback);
                isConn = false;

            }
        }
        //핸들러로 제한 시간 5초
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            connectToDevice(btDevice);

            BluetoothDevice device = result.getDevice();

            if (device.getAddress().equals(BACKLIGHT_ADDRESS)) {
                searchDevice.setVisibility(View.GONE);

                for (int i = 0; i < BluetoothConnection.getInstance().getDevices().size(); i++) {
                    if (BluetoothConnection.getInstance().getDevices().get(i).getAddress().equals(device.getAddress())) {
                        return;
                    }
                }
                BluetoothDeviceItem deviceItem = new BluetoothDeviceItem();
                deviceItem.deviceName = device.getName();
                deviceItem.deviceAddress = device.getAddress();
                deviceAdapter.add(deviceItem, true);
                BluetoothConnection.getInstance().addDevice(device);
                BluetoothConnection.getInstance().setConnectedValue(device.getAddress(), true);
                BluetoothConnection.getInstance().setIsConnect(1);

            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };



    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //if(device.ge)
                    if (device.getAddress().equals(BACKLIGHT_ADDRESS)) {
                        searchDevice.setVisibility(View.GONE);

                        for (int i = 0; i < BluetoothConnection.getInstance().getDevices().size(); i++) {
                            if (BluetoothConnection.getInstance().getDevices().get(i).getAddress().equals(device.getAddress())) {
                                return;
                            }
                        }
                        BluetoothDeviceItem deviceItem = new BluetoothDeviceItem();
                        deviceItem.deviceName = device.getName();
                        deviceItem.deviceAddress = device.getAddress();
                        deviceAdapter.add(deviceItem, false);
                        BluetoothConnection.getInstance().addDevice(device);
                        BluetoothConnection.getInstance().setConnectedValue(device.getAddress(), false);

                    }


                }
            });
        }
    };


    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            Log.i("---mGatt state---", "mGatt NULL");
            mGatt = device.connectGatt(getContext(), false, gattCallback);
        }

        scanLeDevice(false);
    }

    public void disconnectToDevice(BluetoothDevice device) {

        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();

            mGatt = null;
        }
        BluetoothConnection.getInstance().setGatt(null);
        BluetoothConnection.getInstance().setConnectedValue(device.getAddress(), false);
        BluetoothConnection.getInstance().setIsConnect(0);
        /*mainFragment = (MainFragment) (getActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.TAG_MAIN));
        mainFragment.setConnectionOnOff(0);
        */


        isConn = false;
    }


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    /*Log.e("gattCallback", "STATE_CONNECTED!!!!!!!");
                    BluetoothConnection.getInstance().setGatt(gatt);
                    BluetoothConnection.getInstance().setConnectedValue(gatt.getDevice().getAddress(), true);
                    mainFragment = (MainFragment) (getActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.TAG_MAIN));
                    mainFragment.setConnectionOnOff(1);

                    isConn = true;
                    */

                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Toast.makeText(getContext(), "연결이 끊겼습니다", Toast.LENGTH_SHORT).show();
                    Log.e("---gattCallback---", "STATE_DISCONNECTED");
                    BluetoothConnection.getInstance().setConnectedValue(gatt.getDevice().getAddress(), false);
                    if (mGatt != null) {
                        mGatt.disconnect();
                        mGatt.close();
                        mGatt = null;
                    }
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
            BluetoothConnection.getInstance().setServerResponse(1);
            Log.e("gattCallback", "STATE_CONNECTED!!!!!!!");

            BluetoothConnection.getInstance().setGatt(gatt);
            BluetoothConnection.getInstance().setConnectedValue(gatt.getDevice().getAddress(), true);
            BluetoothConnection.getInstance().setIsConnect(1);
            /*    mainFragment = (MainFragment) (getActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.TAG_MAIN));
                mainFragment.setConnectionOnOff(1);
            */
            isConn = true;


            /*SettingFragment settingFragment;
            settingFragment = (SettingFragment)(getActivity().getSupportFragmentManager().findFragmentByTag(MainActivity.TAG_SETTING));
            settingFragment.setDevice();
            */

            //.setConnectionOnOff(1);
     /*       BluetoothDeviceItem deviceItem = new BluetoothDeviceItem();
            deviceItem.deviceName = gatt.getDevice().getName();
            deviceItem.deviceAddress = gatt.getDevice().getAddress();
            deviceAdapter.add(deviceItem, true);*/

            //setDevice();
          /*  BluetoothDeviceItem deviceItem = new BluetoothDeviceItem();
            deviceItem.deviceName = gatt.getDevice().getName();
            deviceItem.deviceAddress = gatt.getDevice().getAddress();
            deviceAdapter.add(deviceItem, true);*/

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", gatt.getService(SERVICE_UUID).getCharacteristic(MY_UUID_SECURE).getUuid().toString());
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
