package com.safering.safebike.setting;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.safering.safebike.R;
import com.safering.safebike.adapter.BluetoothDeviceHeaderItem;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {
    boolean isEnableBluetooth = false;
    private static final int REQUEST_ENABLE_BT = 3;

    BluetoothAdapter mBluetoothAdapter = null;


    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        Button btn = (Button) view.findViewById(R.id.btn_connect_device);
        btn.setOnClickListener(new View.OnClickListener() {
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
                    Intent intent = new Intent(getContext(), ConnectionDeviceActivity.class);
                    startActivity(intent);
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
}
