package com.safering.safebike.account;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.login.LoginActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {
    Button btn;
    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        btn = (Button)view.findViewById(R.id.btn_logout);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(((MainActivity)getActivity()), LoginActivity.class);
                startActivity(intent);
                ((MainActivity)getActivity()).finish();
            }
        });

        btn = (Button)view.findViewById(R.id.btn_user_profile);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(((MainActivity)getActivity()),ProfileActivity.class);
                startActivity(intent);
            }
        });

        btn = (Button)view.findViewById(R.id.btn_help);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(((MainActivity)getActivity()),AccountHelpActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }


}
