package com.safering.safebike.login;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfirmSignUpFragment extends Fragment {
    private static final String SERVICE_FINISH = "finish";

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
                Bundle signBundle = getArguments();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();

                final String date = dateFormat.format(cal.getTime());
                final String email = signBundle.getString(SignUpFragment.SIGN_UP_EMAIL);
                final String id = signBundle.getString(SignUpFragment.SIGN_UP_ID);
                final String password = signBundle.getString(SignUpFragment.SIGN_UP_PASSWORD);
                String phone = PropertyManager.getInstance().getUserPhoneNumber();

                PropertyManager.getInstance().setUserEmail(signBundle.getString(SignUpFragment.SIGN_UP_EMAIL));
                PropertyManager.getInstance().setUserId(signBundle.getString(SignUpFragment.SIGN_UP_ID));
                PropertyManager.getInstance().setUserPassword(signBundle.getString(SignUpFragment.SIGN_UP_PASSWORD));
                PropertyManager.getInstance().setUserJoin(date);

                NetworkManager.getInstance().saveUserInform(getContext(), id, email, date, password,phone, new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object success) {

                        PropertyManager.getInstance().setUserEmail(email);
                        PropertyManager.getInstance().setUserId(id);
                        PropertyManager.getInstance().setUserPassword(password);
                        PropertyManager.getInstance().setUserJoin(date);

                        Intent intent = new Intent((LoginActivity) getActivity(), MainActivity.class);
                        startActivity(intent);
                        ((LoginActivity) getActivity()).finish();
                    }

                    @Override
                    public void onFail(int code) {

                    }
                });

                /*
                 * 최초 가입 시 서비스 컨디션 상태 Finish
                 */
                PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                //프로퍼티 매니저에 저장
                //메인 페이지로 이동
                Intent intent = new Intent((LoginActivity) getActivity(), MainActivity.class);
                startActivity(intent);
                ((LoginActivity) getActivity()).finish();
            }
        });
        return view;
    }


}
