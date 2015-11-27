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
import android.widget.Toast;

import com.nostra13.universalimageloader.utils.L;
import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {

    public static String SIGN_UP_ID = "SIGN_UP_ID";
    public static String SIGN_UP_EMAIL = "SIGN_UP_EMAIL";
    public static String SIGN_UP_PASSWORD = "SIGN_UP_PASSWORD";
    public static final int EMPTY_FORM = -1;
    public static final int DUPLICATE_EMAIL = -2;
    public static final int NON_CHECKUP_POLICY = -3;
    public static final int SIGN_UP_OK = 1;

    EditText inputId;
    EditText inputEmail;
    EditText inputPassword;
    //   EditText inputPasswordConfirm;
    TextView signupFail;
    TextView signupDuple;
    TextView signupPolicy;
    TextView userManner;
    TextView privatePolicy;
    TextView textSignupMain;
    CheckBox checkPolicy;
    Button btnMakeAccount;
    int signUpMessage = SIGN_UP_OK;

    TextView textTempLine1;
    TextView textTempLine2;
    TextView textTempLine3;
    TextView textTempLine4;
    boolean nextPage = true;
    boolean checkDuple = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        textTempLine1 = (TextView) view.findViewById(R.id.text_temp_line1);
        textTempLine2 = (TextView) view.findViewById(R.id.text_temp_line2);
        textTempLine3 = (TextView) view.findViewById(R.id.text_temp_line3);
        textTempLine4 = (TextView) view.findViewById(R.id.text_temp_line4);

        textSignupMain = (TextView) view.findViewById(R.id.text_signup_main);

        inputId = (EditText) view.findViewById(R.id.edit_user_name);
        inputEmail = (EditText) view.findViewById(R.id.edit_user_email);
        inputPassword = (EditText) view.findViewById(R.id.edit_user_password);
        checkPolicy = (CheckBox) view.findViewById(R.id.check_policy);

        signupFail = (TextView) view.findViewById(R.id.text_signup_fail);
        signupDuple = (TextView) view.findViewById(R.id.text_duple_fail);
        signupPolicy = (TextView) view.findViewById(R.id.text_policy_fail);

        userManner = (TextView) view.findViewById(R.id.text_user_mannger);
        privatePolicy = (TextView) view.findViewById(R.id.text_private_policy);

        btnMakeAccount = (Button) view.findViewById(R.id.btn_go_next);

        setFont();
        btnMakeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signUpMessage = SIGN_UP_OK;
                signupFail.setVisibility(View.GONE);
                signupDuple.setVisibility(View.GONE);
                signupPolicy.setVisibility(View.GONE);
                //다음페이지 넘어가기전에 공백/중복 검사
                switch (checkSignFormat()) {
                    case EMPTY_FORM:
                        signupFail.setVisibility(View.VISIBLE);
                        //nextPage = false;
                        return;
                    case NON_CHECKUP_POLICY:
                        //nextPage = false;
                        signupPolicy.setVisibility(View.VISIBLE);
                        return;
                }

                String email = inputEmail.getText().toString();

                NetworkManager.getInstance().checkEmail(getContext(), email, new NetworkManager.OnResultListener<String>() {
                    @Override
                    public void onSuccess(String result) {
                        Toast.makeText(getContext(),"result : "  +result , Toast.LENGTH_SHORT).show();
                       // checkDuple = true;

                        if (result.equals("1")) {
                            Toast.makeText(getContext(),"dupple!!",Toast.LENGTH_SHORT).show();
                            signupDuple.setVisibility(View.VISIBLE);

                        }
                        else{


                            ConfirmSignUpFragment confirmFragment = new ConfirmSignUpFragment();
                            Bundle signBundle = new Bundle();
                            signBundle.putString(SIGN_UP_ID, inputId.getText().toString());
                            signBundle.putString(SIGN_UP_EMAIL, inputEmail.getText().toString());
                            signBundle.putString(SIGN_UP_PASSWORD, inputPassword.getText().toString());
                            confirmFragment.setArguments(signBundle);
                            FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.login_container, confirmFragment);
                            ft.addToBackStack(null);
                            ft.commit();



                        }
                    }

                    @Override
                    public void onFail(int code) {

                    }
                });



            }
        });


        userManner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AgreeDialogFragment agreeDialogFragment = new AgreeDialogFragment();
                agreeDialogFragment.show(getChildFragmentManager(), "agreePolicy");
            }
        });
        privatePolicy.setOnClickListener(new View.OnClickListener() {
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
        Toast.makeText(getContext(), "inputPass : " + password, Toast.LENGTH_SHORT).show();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(id) || TextUtils.isEmpty(password)) {
            signUpMessage = EMPTY_FORM;
            return signUpMessage;
        }
        if (!checkPolicy.isChecked()) {
            signUpMessage = NON_CHECKUP_POLICY;
            return signUpMessage;
        }

        return signUpMessage;
    }

    public boolean checkDuple(){
        checkDuple = false;
        String email = inputEmail.getText().toString();

        NetworkManager.getInstance().checkEmail(getContext(), email, new NetworkManager.OnResultListener<String>() {
            @Override
            public void onSuccess(String result) {
                checkDuple = true;
                if (result.equals("1")) {
                    nextPage = false;
                    signUpMessage = DUPLICATE_EMAIL;

                }
            }

            @Override
            public void onFail(int code) {

            }
        });
        return checkDuple;

    }

    public void setFont() {
        inputId.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        inputEmail.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        inputPassword.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));

        textSignupMain.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        btnMakeAccount.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS_M));
        signupPolicy.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        signupDuple.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        signupFail.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        textTempLine1.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        textTempLine2.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        textTempLine3.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        textTempLine4.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        userManner.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        privatePolicy.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));


    }


}
