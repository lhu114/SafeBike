package com.safering.safebike.login;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.safering.safebike.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {

    public static String SIGN_UP_ID = "SIGN_UP_ID";
    public static String SIGN_UP_EMAIL = "SIGN_UP_EMAIL";
    public static String SIGN_UP_PASSWORD = "SIGN_UP_PASSWORD";
    EditText inputId;
    EditText inputEmail;
    EditText inputPassword;
    EditText inputPasswordConfirm;


    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        inputId = (EditText)view.findViewById(R.id.edit_user_name);
        inputEmail = (EditText)view.findViewById(R.id.edit_user_email);
        inputPassword = (EditText)view.findViewById(R.id.edit_user_password);
        inputPasswordConfirm = (EditText)view.findViewById(R.id.edit_user_password_confirm);

        Button btn = (Button)view.findViewById(R.id.btn_go_next);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //다음 프래그먼트로 값전달
                ConnectionDeviceFragment connectionDeviceFragment = new ConnectionDeviceFragment();
                Bundle signBundle = new Bundle();

                signBundle.putString(SIGN_UP_ID,inputId.getText().toString());
                signBundle.putString(SIGN_UP_EMAIL,inputEmail.getText().toString());
                signBundle.putString(SIGN_UP_PASSWORD,inputPassword.getText().toString());

                Log.i("singId", signBundle.getString(SignUpFragment.SIGN_UP_ID));
                Log.i("singPass", signBundle.getString(SignUpFragment.SIGN_UP_EMAIL));
                Log.i("singEmail", signBundle.getString(SignUpFragment.SIGN_UP_PASSWORD));
                connectionDeviceFragment.setArguments(signBundle);
                //다음페이지 넘어가기전에 공백/중복 검사
                FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.login_container,connectionDeviceFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        btn = (Button)view.findViewById(R.id.btn_display_agree);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AgreeDialogFragment agreeDialogFragment = new AgreeDialogFragment();
                agreeDialogFragment.show(getChildFragmentManager(),"agreePolicy");
            }
        });
        btn = (Button)view.findViewById(R.id.btn_display_policy);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PolicyDialogFragment policyDialogFragment = new PolicyDialogFragment();
                policyDialogFragment.show(getChildFragmentManager(),"poicey");
            }
        });
        return view;

    }


}
