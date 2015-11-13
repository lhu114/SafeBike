package com.safering.safebike.login;


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

import com.safering.safebike.R;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.FontManager;

import java.nio.channels.GatheringByteChannel;

/**
 * A simple {@link Fragment} subclass.
 */
public class FindPasswordFragment extends Fragment {
    EditText editTempEmail;
    Button btnSendTemp;
    TextView textFindPassword;
    TextView textEmailFail;

    public FindPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_find_password, container, false);
        editTempEmail = (EditText) view.findViewById(R.id.edit_temp_email);
        btnSendTemp = (Button) view.findViewById(R.id.btn_temp_pass);
        textEmailFail = (TextView)view.findViewById(R.id.text_email_fail);
        textFindPassword = (TextView)view.findViewById(R.id.text_find_pass_main);
        setFont();
        btnSendTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTempEmail.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    textEmailFail.setVisibility(View.VISIBLE);
                } else {
/*
                NetworkManager.getInstance().sendTempPassword(getContext(), email, new NetworkManager.OnResultListener() {

                    @Override
                    public void onSuccess(Object success) {
                        TempPasswordDialog tempPasswordDialog = new TempPasswordDialog();
                        tempPasswordDialog.show(getChildFragmentManager(),"tempPassDialog");
                    }

                    @Override
                    public void onFail(int code) {

                    }
                });
                    */

                }

                TempPasswordDialog tempPasswordDialog = new TempPasswordDialog();
                tempPasswordDialog.show(getChildFragmentManager(), "tempPassDialog");

            }
        });
        return view;
    }

    public void setFont(){
        editTempEmail.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textFindPassword.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        btnSendTemp.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));

    }


}
