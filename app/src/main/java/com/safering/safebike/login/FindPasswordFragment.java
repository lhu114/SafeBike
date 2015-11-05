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

import com.safering.safebike.R;
import com.safering.safebike.manager.NetworkManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FindPasswordFragment extends Fragment {
    EditText tempEmail;

    public FindPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_find_password, container, false);
        tempEmail = (EditText) view.findViewById(R.id.edit_temp_email);
        Button btn = (Button) view.findViewById(R.id.btn_temp_pass);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = tempEmail.getText().toString();
            /*    if(!TextUtils.isEmpty(email)) {

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
                }
                else{

                }*/
                TempPasswordDialog tempPasswordDialog = new TempPasswordDialog();
                tempPasswordDialog.show(getChildFragmentManager(), "tempPassDialog");

            }
        });
        return view;
    }


}
