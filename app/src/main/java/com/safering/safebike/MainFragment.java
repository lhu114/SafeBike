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
import com.safering.safebike.property.PropertyManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    private static final String TAG_NAVIGATION = "navigation";
    private static final String ARG_NAME = "name";
    private static final String SERVICE_RUNNING = "running";

//    String serviceCondition;
    Button fwdNavigation, startNavigation;

    public MainFragment() {
        // Required empty public constructor
    }

//    public static MainFragment newInstance(String name) {
//        MainFragment fragment = new MainFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_NAME, name);
//        fragment.setArguments(args);
//
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(getContext(), "MainFragment.onCreate", Toast.LENGTH_SHORT).show();
//        if (getArguments() != null) {
//            serviceCondition = getArguments().getString(ARG_NAME);
//        }



        /*
         * SharedPreferences Service Condition 불러오기 String 에 저장
         */
//        serviceCondition = PropertyManager.getInstance().getServiceCondition();

//        Toast.makeText(getContext(), "MainFragment.onCreate : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Toast.makeText(getContext(), "MainFragment.onCreateView", Toast.LENGTH_SHORT).show();
        // Inflate the layout for this fragment

//        Toast.makeText(getContext(), "MainFragment.onCreateView : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

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
//                Toast.makeText(getContext(), "btn_fwd_navigation.Clicked", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, new NavigationFragment(), TAG_NAVIGATION).addToBackStack(null).commit();
            }
        });


//        if (serviceCondition != null && serviceCondition.equals(SERVICE_RUNNING)) {
        if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_RUNNING)) {
//            Toast.makeText(getContext(), "MainFragment.onCreateView : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

            fwdNavigation.setVisibility(View.GONE);

            startNavigation = (Button) view.findViewById(R.id.btn_fwd_start_navigation);
            startNavigation.setVisibility(View.VISIBLE);
            startNavigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), StartNavigationActivity.class);
                    startActivity(intent);
//                    getActivity().finish();
                }
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(getContext(), "MainFragment.onResume", Toast.LENGTH_SHORT).show();
    }
}