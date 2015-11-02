package com.safering.safebike;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.safering.safebike.navigation.NavigationFragment;
import com.safering.safebike.navigation.StartNavigationActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    private static final String TAG_NAVIGATION = "navigation";
    private static final String ARG_NAME = "name";

    String getMessage;
    Button fwdNavigation, startNavigation;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(String name) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            getMessage = getArguments().getString(ARG_NAME);
        }

        Toast.makeText(getContext(), "MainFragment.onCreate : " + getMessage, Toast.LENGTH_SHORT).show();

        /*
         * SharedPreferences Service Condition 불러오기 String 에 저장
         */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        Button btn = (Button) view.findViewById(R.id.btn_onoff_band);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn = (Button) view.findViewById(R.id.btn_onoff_backlight);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        fwdNavigation = (Button) view.findViewById(R.id.btn_fwd_navigation);
        fwdNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, new NavigationFragment(), TAG_NAVIGATION).addToBackStack(null).commit();

            }
        });



        if (getMessage != null && getMessage.equals(RUNNING_NAVIGATION)) {
//            Toast.makeText(getContext(), "MainFragment.onCreateView : " + getMessage, Toast.LENGTH_SHORT).show();

            fwdNavigation.setVisibility(View.GONE);

            startNavigation = (Button) view.findViewById(R.id.btn_fwd_start_navigation);
            startNavigation.setVisibility(View.VISIBLE);
            startNavigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), StartNavigationActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        }

        return view;
    }
}