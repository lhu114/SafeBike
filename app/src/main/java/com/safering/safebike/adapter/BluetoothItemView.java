package com.safering.safebike.adapter;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.safering.safebike.R;

/**
 * Created by Tacademy on 2015-10-30.
 */
public class BluetoothItemView extends RelativeLayout{
    public TextView deviceName;
    public TextView deviceAddress;
    public TextView deviceOnOff;
    public BluetoothItemView(Context context) {
        super(context);
        init();
    }

    public void init(){
        inflate(getContext(), R.layout.bluetooth_item_view, this);
        deviceName = (TextView)findViewById(R.id.bluetooth_device_name);
        deviceAddress = (TextView)findViewById(R.id.bluetooth_device_address);
        deviceOnOff = (TextView)findViewById(R.id.text_bluetooth_device_paired);


    }

    public void setBluetoothData(BluetoothDeviceItem data){
        deviceName.setText(data.deviceName);
        deviceAddress.setText(data.deviceAddress);
        deviceOnOff.setText(data.isSelect);

    }
}
