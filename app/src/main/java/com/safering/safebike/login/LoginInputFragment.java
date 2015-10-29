package com.safering.safebike.login;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
public class LoginInputFragment extends Fragment {
    int logdummy = 1;


    public LoginInputFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_input, container, false);
        Button btn = (Button)view.findViewById(R.id.btn_login);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //서버와 체크해서 확인
                //인증 성공시 메인 페이지로 이동
                if(logdummy == 1){
                    Intent intent = new Intent((LoginActivity)getActivity(),MainActivity.class);
                    startActivity(intent);

                    ((LoginActivity)getActivity()).finish();
                }
                //인증 실패시 실패 다이얼로그 띄우기
                if(logdummy == 0){
                    LoginFailDialogFragment loginFailDialogFragment = new LoginFailDialogFragment();
                    loginFailDialogFragment.show(getChildFragmentManager(),"loginFail");


                }
            }
        });

        btn = (Button)view.findViewById(R.id.btn_find_pass);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //비밀번호 찾기 페이지로 이동
                FindPasswordFragment findPasswordFragment = new FindPasswordFragment();
                FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.login_container,findPasswordFragment);
                ft.addToBackStack(null);
                ft.commit();

            }
        });
        return view;


    }


}
