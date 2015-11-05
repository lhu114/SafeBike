package com.safering.safebike.login;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginInputFragment extends Fragment {
    int logdummy = 1;
    EditText userEmail;
    EditText userPassword;

    public LoginInputFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_input, container, false);
        userEmail = (EditText) view.findViewById(R.id.edit_user_mail_login);
        userPassword = (EditText) view.findViewById(R.id.edit_user_password_login);

        Button btn = (Button) view.findViewById(R.id.btn_login);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = userEmail.getText().toString();
                String password = userPassword.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

                } else {
             /*       NetworkManager.getInstance().userAuthorization(getContext(), email, password, new NetworkManager.OnResultListener() {
                        @Override
                        public void onSuccess(Object success) {
                            Intent intent = new Intent((LoginActivity) getActivity(), MainActivity.class);
                            startActivity(intent);
                            ((LoginActivity) getActivity()).finish();
                            PropertyManager.getInstance().setUserEmail();
                            PropertyManager.getInstance().setUserPassword();
                            PropertyManager.getInstance().setUserId();
                            PropertyManager.getInstance().setUserJoin();


                        }

                        @Override
                        public void onFail(int code) {

                        }
                    });*/
                    //더미데이터로 테스트
                    if (logdummy == 1) {
                        Intent intent = new Intent((LoginActivity) getActivity(), MainActivity.class);
                        startActivity(intent);
                        ((LoginActivity) getActivity()).finish();
                    } else if (logdummy == 0) {
                        LoginFailDialogFragment loginFailDialogFragment = new LoginFailDialogFragment();
                        loginFailDialogFragment.show(getChildFragmentManager(), "loginFail");
                    }
                }
            }
        });

        btn = (Button) view.findViewById(R.id.btn_find_pass);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //비밀번호 찾기 페이지로 이동
                FindPasswordFragment findPasswordFragment = new FindPasswordFragment();
                FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.login_container, findPasswordFragment);
                ft.addToBackStack(null);
                ft.commit();

            }
        });
        return view;


    }


}
