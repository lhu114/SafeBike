package com.safering.safebike.login;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.safering.safebike.R;
import com.safering.safebike.adapter.BluetoothDeviceAdapter;
import com.safering.safebike.adapter.BluetoothDeviceHeaderItem;
import com.safering.safebike.adapter.BluetoothDeviceItem;
import com.safering.safebike.adapter.BluetoothItemHeaderView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionDeviceFragment extends Fragment {

    ListView listViewBand;
    ListView listViewBacklight;

    public ConnectionDeviceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connection_device, container, false);
        listViewBand = (ListView) view.findViewById(R.id.listview_band);
        listViewBacklight = (ListView) view.findViewById(R.id.listview_backlight);
        //detectDevices();
        Button btn = (Button) view.findViewById(R.id.btn_complete);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConfirmSignUpFragment confirmSignUpFragment = new ConfirmSignUpFragment();
                Bundle signBundle = getArguments();
                Log.i("singId", signBundle.getString(SignUpFragment.SIGN_UP_ID));
                Log.i("singPass",signBundle.getString(SignUpFragment.SIGN_UP_EMAIL));
                Log.i("singEmail",signBundle.getString(SignUpFragment.SIGN_UP_PASSWORD));

                confirmSignUpFragment.setArguments(signBundle);
                FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.login_container, confirmSignUpFragment);
                ft.addToBackStack(null);

                ft.commit();
            }
        });
        return view;
    }

  /*  public void detectDevices() {
        //블루투스 디바이스들 감지해서 화면에 뿌려줌
        BluetoothDeviceAdapter bandAdapter = new BluetoothDeviceAdapter();
        BluetoothDeviceAdapter backligthAdapter = new BluetoothDeviceAdapter();
        for (int i = 0; i < 4; i++) {
            BluetoothDeviceItem data = new BluetoothDeviceItem();
            data.deviceName = "bluetoothBand " + i;
            data.deviceAddress = "A:B:C:D:E:F " + "/" + i;
            bandAdapter.add(data);
        }
        for (int i = 0; i < 4; i++) {
            BluetoothDeviceItem data = new BluetoothDeviceItem();
            data.deviceName = "bluetoothBackligth " + i;
            data.deviceAddress = "F:E:D:C:B:A " + "/" + i;
            backligthAdapter.add(data);

        }
        BluetoothDeviceHeaderItem Header = new BluetoothDeviceHeaderItem();
        Header.deviceType = "진동밴드 선택";
        BluetoothItemHeaderView bluetoothBandHeaderView = new BluetoothItemHeaderView(getContext());
        bluetoothBandHeaderView.setHeaderItem(Header);

        listViewBand.addHeaderView(bluetoothBandHeaderView);
        listViewBand.setAdapter(bandAdapter);

        Header = new BluetoothDeviceHeaderItem();
        Header.deviceType = "후미등 선택";
        bluetoothBandHeaderView = new BluetoothItemHeaderView(getContext());
        bluetoothBandHeaderView.setHeaderItem(Header);

        listViewBacklight.addHeaderView(bluetoothBandHeaderView);

        listViewBacklight.setAdapter(backligthAdapter);


    }*/


}
