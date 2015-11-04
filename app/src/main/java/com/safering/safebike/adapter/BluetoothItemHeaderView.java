package com.safering.safebike.adapter;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.safering.safebike.R;

/**
 * Created by Tacademy on 2015-11-02.
 */
public class BluetoothItemHeaderView extends RelativeLayout{
    TextView deviceName;
    public BluetoothItemHeaderView(Context context) {
        super(context);
        init();
    }
    public void init(){
        inflate(getContext(), R.layout.bluetooth_item_header_view,this);
        deviceName = (TextView)findViewById(R.id.text_device_type);

    }

    public void setHeaderItem(BluetoothDeviceHeaderItem data){
        deviceName.setText(data.deviceType);
    }
}

