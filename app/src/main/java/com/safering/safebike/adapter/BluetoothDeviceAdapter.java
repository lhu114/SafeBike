package com.safering.safebike.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tacademy on 2015-10-29.
 */
public class BluetoothDeviceAdapter extends BaseAdapter{
    List<BluetoothDeviceItem> items = new ArrayList<BluetoothDeviceItem>();
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothItemView view;
        if(convertView == null){
            view = new BluetoothItemView(parent.getContext());
        }
        else{
            view = (BluetoothItemView)convertView;
        }
        view.setBluetoothData(items.get(position));
        return view;
    }

    public void add(BluetoothDeviceItem item){
        items.add(item);
        notifyDataSetChanged();

    }

    public void removeAll(){
        if(items.size() > 0){
            items.clear();
        }
    }

}
