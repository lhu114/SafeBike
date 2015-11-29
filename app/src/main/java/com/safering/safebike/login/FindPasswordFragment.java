package com.safering.safebike.login;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.InformDialogFragment;
import com.safering.safebike.manager.NetworkManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FindPasswordFragment extends Fragment {
    EditText editTempEmail;
    Button btnSendTemp;
    TextView textFindPassword;
    TextView textEmailFail;
    InformDialogFragment dialog;
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
        dialog = new InformDialogFragment();
        setFont();
        btnSendTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTempEmail.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    //textEmailFail.setVisibility(View.VISIBLE);
                    dialog.setContent("로그인","이메일을 확인해주세요.");
                    dialog.show(getChildFragmentManager(), "login");
                } else {

                NetworkManager.getInstance().sendTempPassword(getContext(), email, new NetworkManager.OnResultListener() {

                    @Override
                    public void onSuccess(Object success) {
                        if(success.toString().equals("201")) {
                            dialog.setContent("로그인", "존재하지 않는 이메일입니다.");
                            dialog.show(getChildFragmentManager(), "login");


                        }
                        else{
                            dialog.setContent("로그인", "임시 비밀번호를 보냈습니다.");
                            dialog.show(getChildFragmentManager(), "login");

                        }


                    }

                    @Override
                    public void onFail(int code) {
                        dialog.setContent("로그인","존재하지 않는 이메일입니다.");
                        dialog.show(getChildFragmentManager(), "login");

                    }
                });


                }


            }
        });
        return view;
    }

    public void setFont(){
        editTempEmail.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textFindPassword.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        btnSendTemp.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS_M));

    }


}
