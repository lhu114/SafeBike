package com.safering.safebike.setting;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.UUID;

/**
 * Created by Tacademy on 2015-11-11.
 */
public class BluetoothConnection {
    private Handler mHandler;
    public static final int COMPLETE_PARIED = 1;

    public BluetoothConnection(Handler handler) {
        mHandler = handler;

    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        BluetoothSocket mmSocket;
        InputStream mmInStream;
        OutputStream mmOutStream;

        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            Log.i("socket true", "temp sockets not created");

            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e("socket fail", "temp sockets not created", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        String message = "1234";
        byte[] send = message.getBytes();

        try {
            Log.i("send ok", "send true");

            mmOutStream.write(send);
        } catch (IOException e) {
            Log.i("send fail", "send true");

            e.printStackTrace();
        }
    }

    public void connect(BluetoothDevice device, UUID uuid) {
        new ConnectThread(device, uuid).start();

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.i("con thread", "------con-----");
            mmDevice = device;
            BluetoothSocket tmp = null;
            // mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                //  if (secure) {
                tmp = device.createRfcommSocketToServiceRecord(
                        uuid);
                // } else {

                // }
            } catch (IOException e) {
                //    Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {

            // Always cancel discovery because it will slow down a connection

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                Log.i("connect", "------fail------");
                try {
                    mmSocket.close();
                    //다이얼로그 취소 눌렀을시 리던
                } catch (IOException e2) {
                }
                //connectionFailed();
                return;
            }
            //여기 까지 페어링
            Message msg = mHandler.obtainMessage();
            msg.obj = mmDevice;
            msg.arg1 = COMPLETE_PARIED;
            mHandler.sendMessage(msg);

            connected(mmSocket, mmDevice);//인풋스트림 생성
        }

        public void cancel() {

            try {
                mmSocket.close();
            } catch (IOException e) {
                //s  Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

}
