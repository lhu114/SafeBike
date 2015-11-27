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
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.property.PropertyManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginInputFragment extends Fragment {
    int dummy = 1;
    EditText editUserEmail;
    EditText editUserPassword;
    TextView textLoginFail;
    TextView textLoginMain;
    TextView textFindPassword;
    Button btnLogin;
    public LoginInputFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_input, container, false);
        editUserEmail = (EditText) view.findViewById(R.id.edit_user_mail_login);
        editUserPassword = (EditText) view.findViewById(R.id.edit_user_password_login);
        textLoginFail = (TextView)view.findViewById(R.id.text_login_fail);
        textLoginMain = (TextView)view.findViewById(R.id.text_login_input_main);
        textFindPassword = (TextView)view.findViewById(R.id.text_find_pass);
        btnLogin = (Button) view.findViewById(R.id.btn_login_input);
        setFont();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = editUserEmail.getText().toString();
                final String password = editUserPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    textLoginFail.setVisibility(View.VISIBLE);

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
                                Log.i("userEmail", email);
                                Log.i("userPassword",password);
                                Log.i("userInform",userInform.id);
                                Log.i("userJoin",userInform.join);
                                Log.i("userPhoto",userInform.photo + "");


                                Intent intent = new Intent((LoginActivity) getActivity(), MainActivity.class);
                                startActivity(intent);
                                ((LoginActivity) getActivity()).finish();
                            }
                            else{
                                //Toast.makeText(getContext(),"로그인실패",Toast.LENGTH_SHORT).show();
                                LoginFailDialogFragment loginFailDialogFragment = new LoginFailDialogFragment();


                                loginFailDialogFragment.setContent("로그인","로그인에 실패했습니다");
                                loginFailDialogFragment.show(getChildFragmentManager(),"loginfail");

                            }
                            /*if(userInform != null) {
                                PropertyManager.getInstance().setUserEmail(email);
                                PropertyManager.getInstance().setUserPassword(password);
                                PropertyManager.getInstance().setUserId(userInform.id);
                                PropertyManager.getInstance().setUserJoin(userInform.join);

                                Intent intent = new Intent((LoginActivity) getActivity(), MainActivity.class);
                                startActivity(intent);
                                ((LoginActivity) getActivity()).finish();
                            }
                            else{
                                Toast.makeText(getContext(),"로그인실패",Toast.LENGTH_SHORT).show();
                            }*/

                        }

                        @Override
                        public void onFail(int code) {


                        }
                    });
                    //더미데이터로 테스트
                    /*if (dummy == 1) {
                        Intent intent = new Intent((LoginActivity) getActivity(), MainActivity.class);
                        startActivity(intent);
                        ((LoginActivity) getActivity()).finish();
                    } else if (dummy == 0) {
                        LoginFailDialogFragment loginFailDialogFragment = new LoginFailDialogFragment();
                        loginFailDialogFragment.show(getChildFragmentManager(), "loginFail");
                    }*/
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
        return view;


    }
    public void setFont(){
        editUserEmail.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        editUserPassword.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));

        textLoginMain.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textFindPassword.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        btnLogin.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS_M));
        textLoginFail.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));

    }


}
