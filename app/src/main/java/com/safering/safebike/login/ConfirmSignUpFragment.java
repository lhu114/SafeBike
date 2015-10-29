package com.safering.safebike.login;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfirmSignUpFragment extends Fragment {


    public ConfirmSignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_confirm_sign_up, container, false);
        Button btn = (Button) view.findViewById(R.id.btn_go_main);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //메인 페이지로 이동
                Intent intent = new Intent((LoginActivity) getActivity(), MainActivity.class);
                startActivity(intent);
                ((LoginActivity) getActivity()).finish();
            }
        });
        return view;
    }


}
