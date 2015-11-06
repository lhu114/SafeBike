package com.safering.safebike.login;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.safering.safebike.R;
import com.safering.safebike.manager.NetworkManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {

    public static String SIGN_UP_ID = "SIGN_UP_ID";
    public static String SIGN_UP_EMAIL = "SIGN_UP_EMAIL";
    public static String SIGN_UP_PASSWORD = "SIGN_UP_PASSWORD";
    public static final int EMPTY_FORM = -1;
    public static final int PASSWORD_CONFIRM = -2;
    public static final int DUPLICATE_EMAIL = -3;
    public static final int NON_CHECKUP_POLICY = -4;
    public static final int SIGN_UP_OK = 1;

    EditText inputId;
    EditText inputEmail;
    EditText inputPassword;
    EditText inputPasswordConfirm;
    TextView signupFail;
    TextView signupDuple;
    TextView signupPolicy;
    TextView signupConfirm;

    CheckBox checkPolicy;
    CheckBox checkAgree;

    int signUpMessage = SIGN_UP_OK;
    int PolicyMessage = SIGN_UP_OK;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        inputId = (EditText) view.findViewById(R.id.edit_user_name);
        inputEmail = (EditText) view.findViewById(R.id.edit_user_email);
        inputPassword = (EditText) view.findViewById(R.id.edit_user_password);
        inputPasswordConfirm = (EditText) view.findViewById(R.id.edit_user_password_confirm);
        checkPolicy = (CheckBox) view.findViewById(R.id.check_policy);
        checkAgree = (CheckBox) view.findViewById(R.id.check_agree);
        signupFail = (TextView) view.findViewById(R.id.text_signup_fail);
        signupDuple = (TextView) view.findViewById(R.id.text_duple_fail);
        signupConfirm = (TextView)view.findViewById(R.id.text_confirm_fail);
        signupPolicy = (TextView) view.findViewById(R.id.text_signup_policy_fail);

        Button btn = (Button) view.findViewById(R.id.btn_go_next);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean nextPage = true;
                signUpMessage = SIGN_UP_OK;
                signupFail.setVisibility(View.GONE);
                signupConfirm.setVisibility(View.GONE);
                signupDuple.setVisibility(View.GONE);
                signupPolicy.setVisibility(View.GONE);

                //다음페이지 넘어가기전에 공백/중복 검사
                switch (checkSignFormat()) {
                    case EMPTY_FORM:
                        signupFail.setVisibility(View.VISIBLE);
                        nextPage = false;
                        break;
                    case PASSWORD_CONFIRM:
                        signupConfirm.setVisibility(View.VISIBLE);
                        nextPage = false;
                        break;
                    case DUPLICATE_EMAIL:
                        signupDuple.setVisibility(View.VISIBLE);
                        nextPage = false;
                        break;
                }

                if (checkPolicy() == NON_CHECKUP_POLICY) {
                    signupPolicy.setVisibility(View.VISIBLE);
                    nextPage = false;
                }

                if (nextPage) {
                    ConnectionDeviceFragment connectionDeviceFragment = new ConnectionDeviceFragment();
                    Bundle signBundle = new Bundle();
                    signBundle.putString(SIGN_UP_ID, inputId.getText().toString());
                    signBundle.putString(SIGN_UP_EMAIL, inputEmail.getText().toString());
                    signBundle.putString(SIGN_UP_PASSWORD, inputPassword.getText().toString());
                    connectionDeviceFragment.setArguments(signBundle);
                    FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.login_container, connectionDeviceFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            }
        });

        btn = (Button) view.findViewById(R.id.btn_display_agree);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AgreeDialogFragment agreeDialogFragment = new AgreeDialogFragment();
                agreeDialogFragment.show(getChildFragmentManager(), "agreePolicy");
            }
        });
        btn = (Button) view.findViewById(R.id.btn_display_policy);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PolicyDialogFragment policyDialogFragment = new PolicyDialogFragment();
                policyDialogFragment.show(getChildFragmentManager(), "poicey");
            }
        });
        return view;

    }

    public int checkSignFormat() {
        //   int result = 1;
        String email = inputEmail.getText().toString();
        String id = inputId.getText().toString();
        String password = inputPassword.getText().toString();
        String passwordConfirm = inputPasswordConfirm.getText().toString();


        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(id) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordConfirm)) {
            signUpMessage = EMPTY_FORM;
            //return result;
        }
        if (!password.equals(passwordConfirm)) {
            signUpMessage = PASSWORD_CONFIRM;
            //return result;
        }

/*
        NetworkManager.getInstance().checkEmail(getContext(), email, new NetworkManager.OnResultListener() {
            @Override
            public void onSuccess(Object success) {

                signUpMessage = DUPLICATE_EMAIL;


            }

            @Override
            public void onFail(int code) {
            }
        });
*/

        return signUpMessage;
    }

    public int checkPolicy() {
        PolicyMessage = SIGN_UP_OK;
        Boolean isPolicy = checkPolicy.isChecked();
        Boolean isAgree = checkAgree.isChecked();
        if (!isPolicy || !isAgree) {
            PolicyMessage = NON_CHECKUP_POLICY;
        }
        return PolicyMessage;
    }


}
