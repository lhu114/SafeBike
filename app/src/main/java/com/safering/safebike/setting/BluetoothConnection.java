package com.safering.safebike.setting;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;
import android.widget.Toast;

import com.safering.safebike.property.PropertyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Tacademy on 2015-11-20.
 */
public class BluetoothConnection {
    public static UUID SERVICE_UUID = UUID.fromString("1706BBC0-88AB-4B8D-877E-2237916EE929");
    private static UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private ArrayList<BluetoothDevice> devices;
    private static BluetoothConnection instance;
    public BluetoothGatt mGatt = null;
    public HashMap<String,Boolean> deviceMap;
    private int isConnect = 0;

    public static BluetoothConnection getInstance(){
        if(instance == null){
            instance = new BluetoothConnection();
        }
        return instance;
    }

    private BluetoothConnection(){
        devices = new ArrayList<BluetoothDevice>();
        deviceMap = new HashMap<String,Boolean>();

    }

    public void addDevice(BluetoothDevice device){
        boolean isDevice = false;
        for(int i = 0; i < devices.size(); i++){
            if(devices.get(i).getAddress().equals(device.getAddress())){
                isDevice = true;
            }
        }
        if(isDevice == false) {
            devices.add(device);
            //deviceMap.put(device.getAddress(),false);
        }
    }

    public void setConnectedValue(String address,boolean isChecked){
        deviceMap.put(address,isChecked);

    }

    public boolean getConnectedValue(String address){
        return deviceMap.get(address);

    }

    /*public BluetoothDevice getDevice(String address){
        for(int i = 0; i < devices.size(); i++){
            if(devices.get(i).getAddress().equals(address)){
                return devices.get(i);
            }
        }
        return null;

    }*/

    public ArrayList<BluetoothDevice> getDevices(){
        return devices;
    }

    public void setGatt(BluetoothGatt gatt){
        mGatt = gatt;

    }

    public BluetoothGatt getGatt(){
        return mGatt;
    }

    public void writeLeftValue() {
        // Log.i("writeValue : ", mGatt.writeCharacteristic(mGatt.getService(SERVICE_UUID).getCharacteristic(MY_UUID_SECURE)) + "");
        byte[] arr = new byte[1];
        arr[0] = 1;
        if(mGatt != null) {
            if(PropertyManager.getInstance().getBluetoothSetting() == 1) {
                BluetoothGattCharacteristic characteristic = mGatt.getService(SERVICE_UUID).getCharacteristic(MY_UUID_SECURE);
                characteristic.setValue(arr);
                mGatt.writeCharacteristic(characteristic);
            }
        }



    }

    public void writeRightValue() {
        //  Log.i("writeValue : ", mGatt.writeCharacteristic(mGatt.getService(SERVICE_UUID).getCharacteristic(MY_UUID_SECURE)) + "");
        byte[] arr = new byte[1];
        arr[0] = 2;
        if(mGatt != null) {
            if(PropertyManager.getInstance().getBluetoothSetting() == 1) {
                BluetoothGattCharacteristic characteristic = mGatt.getService(SERVICE_UUID).getCharacteristic(MY_UUID_SECURE);
                characteristic.setValue(arr);
                mGatt.writeCharacteristic(characteristic);
            }
        }
    }

    public void writeOffValue(){
        byte[] arr = new byte[1];
        arr[0] = 3;
        if(mGatt != null){
            if(PropertyManager.getInstance().getBluetoothSetting() == 1) {
                BluetoothGattCharacteristic characteristic = mGatt.getService(SERVICE_UUID).getCharacteristic(MY_UUID_SECURE);
                characteristic.setValue(arr);
                mGatt.writeCharacteristic(characteristic);
            }
        }
    }



    public int getIsConnect(){
        return isConnect;
    }
    public void setIsConnect(int status){
        isConnect = status;
    }


}
