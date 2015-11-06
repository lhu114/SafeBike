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
import android.widget.TextView;

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
    TextView loginFail;
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
        loginFail = (TextView)view.findViewById(R.id.text_login_fail);
        Button btn = (Button) view.findViewById(R.id.btn_login);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = userEmail.getText().toString();
                final String password = userPassword.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    loginFail.setVisibility(View.VISIBLE);

                } else {
                /*  NetworkManager.getInstance().userAuthorization(getContext(), email, password, new NetworkManager.OnResultListener<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult logResult) {
                            LoginItem userInform = logResult.result;

                            PropertyManager.getInstance().setUserJoin(userInform.join);
                            PropertyManager.getInstance().setUserId(userInform.id);
                            PropertyManager.getInstance().setUserPassword(password);
                            PropertyManager.getInstance().setUserEmail(email);

                            Intent intent = new Intent((LoginActivity) getActivity(), MainActivity.class);

                            startActivity(intent);
                            ((LoginActivity) getActivity()).finish();




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
