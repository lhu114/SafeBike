package com.safering.safebike.login;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.safering.safebike.R;
import com.safering.safebike.property.FontManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    TextView textLoginMain;
    TextView textWarning;


    Button btnLogin;
    Button btnSignUp;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        textLoginMain = (TextView)view.findViewById(R.id.text_login_main);
        textWarning = (TextView)view.findViewById(R.id.text_login_warning);

        btnLogin = (Button)view.findViewById(R.id.btn_login);
        btnSignUp = (Button)view.findViewById(R.id.btn_login_sign);
        setFont();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoginInputFragment loginInputFragment = new LoginInputFragment();
                FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.login_container, loginInputFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpFragment signUpFragment = new SignUpFragment();
                FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.login_container,signUpFragment);
                ft.addToBackStack(null);

                ft.commit();

            }
        });
        return view;
    }

    public void setFont(){
        textLoginMain.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.BMJUA));
        textWarning.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        btnLogin.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        btnSignUp.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));


    }


}
