package com.safering.safebike.login;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.safering.safebike.R;

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
