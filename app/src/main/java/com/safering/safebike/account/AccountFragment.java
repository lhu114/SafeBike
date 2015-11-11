package com.safering.safebike.account;


import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.safering.safebike.property.PropertyManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {
    private static final String SERVICE_FINISH = "finish";
    private static final String SERVICE_RUNNING = "running";
    private static final int BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION = 3;

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
                PropertyManager.getInstance().setUserId("");
                PropertyManager.getInstance().setUserPassword("");
                PropertyManager.getInstance().setUserEmail("");
                PropertyManager.getInstance().setUserJoin("");

                if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_RUNNING)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setTitle("내비게이션 안내종료");
                    builder.setMessage("현재 내비게이션 안내 중입니다. 정말로 종료하시겠습니까");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        /*
                         *  목적지 위도, 경도, searchoption 날리기
                         */
                            PropertyManager.getInstance().setServiceCondition(SERVICE_FINISH);
                            PropertyManager.getInstance().setDestinationLatitude(null);
                            PropertyManager.getInstance().setDestinationLongitude(null);
                            PropertyManager.getInstance().setFindRouteSearchOption(BICYCLE_ROUTE_BICYCLELANE_SEARCHOPTION);

                            Intent intent = new Intent(((MainActivity)getActivity()), LoginActivity.class);
                            startActivity(intent);
                            ((MainActivity)getActivity()).finish();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
//        builder.setCancelable(false);

                    builder.create().show();
                } else {
                    Intent intent = new Intent(((MainActivity)getActivity()), LoginActivity.class);
                    startActivity(intent);
                    ((MainActivity)getActivity()).finish();
                }
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
