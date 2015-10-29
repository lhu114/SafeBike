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
public class SignUpFragment extends Fragment {


    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        Button btn = (Button)view.findViewById(R.id.btn_go_next);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectionDeviceFragment connectionDeviceFragment = new ConnectionDeviceFragment();
                FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.login_container,connectionDeviceFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        btn = (Button)view.findViewById(R.id.btn_display_agree);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AgreeDialogFragment agreeDialogFragment = new AgreeDialogFragment();
                agreeDialogFragment.show(getChildFragmentManager(),"agreePolicy");
            }
        });
        btn = (Button)view.findViewById(R.id.btn_display_policy);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PolicyDialogFragment policyDialogFragment = new PolicyDialogFragment();
                policyDialogFragment.show(getChildFragmentManager(),"poicey");
            }
        });
        return view;

    }


}
