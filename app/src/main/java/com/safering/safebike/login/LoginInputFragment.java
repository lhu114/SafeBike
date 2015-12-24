package com.safering.safebike.login;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.manager.InformDialogFragment;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.property.PropertyManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginInputFragment extends Fragment {

    EditText editUserEmail;
    EditText editUserPassword;
    TextView textLoginMain;
    TextView textFindPassword;
    Button btnLogin;
    InformDialogFragment dialog;

    public LoginInputFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_input, container, false);
        editUserEmail = (EditText) view.findViewById(R.id.edit_user_mail_login);
        editUserPassword = (EditText) view.findViewById(R.id.edit_user_password_login);
        textLoginMain = (TextView)view.findViewById(R.id.text_login_input_main);
        textFindPassword = (TextView)view.findViewById(R.id.text_find_pass);
        btnLogin = (Button) view.findViewById(R.id.btn_login_input);
        dialog = new InformDialogFragment();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = editUserEmail.getText().toString();
                final String password = editUserPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    dialog.setContent("로그인","아이디 또는 비밀번호를 확인해주세요.");
                    dialog.show(getChildFragmentManager(), "loginfail");
                } else {
                    NetworkManager.getInstance().userAuthorization(getContext(), email, password, new NetworkManager.OnResultListener<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult logResult) {
                            if(logResult != null){
                                LoginItem userInform = logResult.userlogin;
                                PropertyManager.getInstance().setUserEmail(email);
                                PropertyManager.getInstance().setUserPassword(password);
                                PropertyManager.getInstance().setUserId(userInform.id);
                                PropertyManager.getInstance().setUserJoin(userInform.join);
                                PropertyManager.getInstance().setUserImagePath(userInform.photo);
                                Intent intent = new Intent((LoginActivity) getActivity(), MainActivity.class);
                                startActivity(intent);
                                ((LoginActivity) getActivity()).finish();
                            }
                            else{
                                dialog.setContent("로그인","로그인에 실패했습니다. 로그인정보를 확인해주세요.");
                                dialog.show(getChildFragmentManager(), "loginfail");
                            }
                        }

                        @Override
                        public void onFail(int code) {


                        }
                    });

                }
            }
        });

        textFindPassword = (TextView) view.findViewById(R.id.text_find_pass);
        textFindPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FindPasswordFragment findPasswordFragment = new FindPasswordFragment();
                FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.login_container, findPasswordFragment);
                ft.addToBackStack(null);
                ft.commit();

            }
        });
        setFont();

        return view;


    }
    public void setFont(){
        editUserEmail.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        editUserPassword.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textLoginMain.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textFindPassword.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        btnLogin.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS_M));

    }


}
