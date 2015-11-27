package com.safering.safebike.login;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.property.PropertyManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfirmSignUpFragment extends Fragment {

    private static final String SERVICE_FINISH = "finish";
    Button btnCompleteSign;
    TextView textCompleteSign;
    TextView textCompleteSign1;
    TextView textProgressSignup;
    boolean isSignup = false;

    public ConfirmSignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_confirm_sign_up, container, false);

        textProgressSignup = (TextView) view.findViewById(R.id.text_progress_signup);
        textCompleteSign = (TextView) view.findViewById(R.id.text_complete_sign);
        textCompleteSign1 = (TextView) view.findViewById(R.id.text_complete_sign_s);
        btnCompleteSign = (Button) view.findViewById(R.id.btn_go_main);
        setFont();
        sendUserInform();


        btnCompleteSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                /*
                 * 최초 가입 시 서비스 컨디션 상태 Finish
                 */
                if (isSignup) {
                    PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                    Intent intent = new Intent((LoginActivity) getActivity(), MainActivity.class);
                    startActivity(intent);
                    ((LoginActivity) getActivity()).finish();
                }

            }
        });
        return view;
    }


    public void setFont() {
        textCompleteSign.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        textCompleteSign1.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        btnCompleteSign.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS_M));
        textProgressSignup.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));

    }

    public void sendUserInform() {
        Bundle signBundle = getArguments();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        final String date = dateFormat.format(cal.getTime());
        final String email = signBundle.getString(SignUpFragment.SIGN_UP_EMAIL);
        final String id = signBundle.getString(SignUpFragment.SIGN_UP_ID);
        final String password = signBundle.getString(SignUpFragment.SIGN_UP_PASSWORD);
        Log.i("myphoneNumber",PropertyManager.getInstance().getUserPhoneNumber()+"");
        final String phone = PropertyManager.getInstance().getUserPhoneNumber();

/*
        PropertyManager.getInstance().setUserEmail(signBundle.getString(SignUpFragment.SIGN_UP_EMAIL));
        PropertyManager.getInstance().setUserId(signBundle.getString(SignUpFragment.SIGN_UP_ID));
        PropertyManager.getInstance().setUserPassword(signBundle.getString(SignUpFragment.SIGN_UP_PASSWORD));
        PropertyManager.getInstance().setUserJoin(date);
*/


        NetworkManager.getInstance().saveUserInform(getContext(), id, email, date, password, phone, new NetworkManager.OnResultListener() {
            @Override
            public void onSuccess(Object success) {

                PropertyManager.getInstance().setUserEmail(email);
                PropertyManager.getInstance().setUserId(id);
                PropertyManager.getInstance().setUserPassword(password);
                PropertyManager.getInstance().setUserJoin(date);
             //   PropertyManager.getInstance().setUserImagePath("null");

                textProgressSignup.setVisibility(View.GONE);
                textCompleteSign.setVisibility(View.VISIBLE);
                textCompleteSign1.setVisibility(View.VISIBLE);
                isSignup = true;

            }

            @Override
            public void onFail(int code) {

            }
        });

    }

}
