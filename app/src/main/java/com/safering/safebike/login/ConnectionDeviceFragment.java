package com.safering.safebike.login;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.safering.safebike.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionDeviceFragment extends Fragment {


    public ConnectionDeviceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connection_device, container, false);

        ArrayList<String> bandList = new ArrayList<>();
        bandList.add("hand1");
        bandList.add("hand2");

        ArrayList<String> backlightList = new ArrayList<>();
        backlightList.add("backlight1");
        backlightList.add("bakcligth2");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,bandList);

        ListView listView = (ListView)view.findViewById(R.id.listview_band);
        listView.setAdapter(arrayAdapter);
        arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,backlightList);

        listView = (ListView)view.findViewById(R.id.listview_backlight);
        listView.setAdapter(arrayAdapter);


        Button btn = (Button)view.findViewById(R.id.btn_complete);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmSignUpFragment confirmSignUpFragment = new ConfirmSignUpFragment();
                FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.login_container,confirmSignUpFragment);
                ft.addToBackStack(null);

                ft.commit();
            }
        });
        return view;
    }


}
