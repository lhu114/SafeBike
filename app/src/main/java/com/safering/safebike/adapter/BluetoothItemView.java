package com.safering.safebike.adapter;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;

/**
 * Created by Tacademy on 2015-10-30.
 */
public class BluetoothItemView extends RelativeLayout{
    public TextView deviceName;
    public TextView deviceAddress;
    public TextView deviceOnOff;
    private Switch deviceSwitch;
    BluetoothDeviceItem bData;
    //public interface onSwitchListener

    public BluetoothItemView(Context context) {
        super(context);
        init();
    }

    public void init(){
        inflate(getContext(), R.layout.bluetooth_item_view, this);
        deviceName = (TextView)findViewById(R.id.bluetooth_device_name);
        deviceAddress = (TextView)findViewById(R.id.bluetooth_device_address);
        deviceOnOff = (TextView)findViewById(R.id.text_bluetooth_device_paired);
        deviceSwitch = (Switch)findViewById(R.id.text_bluetooth_device_paired);
        /*deviceSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSwitchClick(BluetoothItemView.this,bData,isChecked);

            }
        });*/

        deviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mListener.onSwitchClick(BluetoothItemView.this,bData,isChecked);
            }
        });

                //        android:id="@+id/text_bluetooth_device_paired"

        setFont();

    }

    public void setBluetoothData(BluetoothDeviceItem data,boolean isSelect){
        bData = data;
        deviceName.setText(data.deviceName);
        deviceAddress.setText(data.deviceAddress);
        if(isSelect){
            deviceSwitch.setChecked(true);
        }


    }

    public void setFont(){
        deviceName.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS_R));
        deviceAddress.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS_R));
    }



    public interface OnSwitchClickListener {
        public void onSwitchClick(BluetoothItemView view, BluetoothDeviceItem item,boolean isChecked);
    }
    OnSwitchClickListener mListener;

    public void setOnSwitchClickListener(OnSwitchClickListener listener) {
        mListener = listener;
    }
}
