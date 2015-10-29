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
public class FindPasswordFragment extends Fragment {


    public FindPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_find_password, container, false);
        Button btn = (Button)view.findViewById(R.id.btn_temp_pass);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //서버와 통신해서 임시비밀번호 전송했시 다이얼로그 띄우기
                TempPasswordDialog tempPasswordDialog = new TempPasswordDialog();
                tempPasswordDialog.show(getChildFragmentManager(),"tempPassDialog");
            }
        });
        return view;
    }


}
