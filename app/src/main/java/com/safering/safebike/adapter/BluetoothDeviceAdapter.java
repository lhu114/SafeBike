package com.safering.safebike.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tacademy on 2015-10-29.
 */
public class BluetoothDeviceAdapter extends BaseAdapter implements BluetoothItemView.OnSwitchClickListener {

    List<BluetoothDeviceItem> items = new ArrayList<BluetoothDeviceItem>();

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    public BluetoothDeviceItem getItem(String address) {
        BluetoothDeviceItem item = null;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).deviceAddress.equals(address)) {
                return items.get(i);

            }
        }
        ;
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothItemView view;
        if (convertView == null) {
            view = new BluetoothItemView(parent.getContext());
            view.setOnSwitchClickListener(this);
        } else {
            view = (BluetoothItemView) convertView;
        }

        BluetoothDeviceItem item = items.get(position);
        view.setBluetoothData(item);
        // view.setBluetoothData(items.get(position),false);


        return view;
    }

    public void add(BluetoothDeviceItem item, boolean isSel) {

        boolean isDuple = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).deviceAddress.equals(item.deviceAddress)) {

                //if(items.get(i).isSel == true )
               items.remove(i);
                //isDuple = true;

                break;
            }

        }
       // if (isDuple == false) {

            item.isSel = isSel;
            items.add(item);
            notifyDataSetChanged();
        //}


    }
/*
    public void addRegisterDevice(Blu

    etoothDeviceItem item){
        boolean isDuple = false;
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).deviceAddress.equals(item.deviceAddress))
                isDuple = true;
        }
        if(isDuple == false) {
            items.add(item);
            notifyDataSetChanged();
        }
    }*/

    public void removeAll() {
        if (items.size() > 0) {
            items.clear();
            notifyDataSetChanged();
        }
    }

    public void remove(String deviceAddress) {
        for (int i = 0; i < items.size(); i++) {
            BluetoothDeviceItem item = items.get(i);
            if (item.deviceAddress.equals(deviceAddress)) {
                items.remove(i);
                notifyDataSetChanged();
                break;
            }
        }

    }

    BluetoothItemView.OnSwitchClickListener bListener;

    public void setOnSwitchClickListener(BluetoothItemView.OnSwitchClickListener listener) {
        bListener = listener;

    }

    @Override
    public void onSwitchClick(BluetoothItemView view, BluetoothDeviceItem item, boolean isChecked) {
        bListener.onSwitchClick(view, item, isChecked);
    }
}
