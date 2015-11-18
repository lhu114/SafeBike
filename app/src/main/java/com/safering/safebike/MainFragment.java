package com.safering.safebike;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.navigation.NavigationFragment;
import com.safering.safebike.navigation.StartNavigationActivity;
import com.safering.safebike.property.PropertyManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    private static final String TAG_NAVIGATION = "navigation";
    private static final String ARG_NAME = "name";
    private static final String SERVICE_RUNNING = "running";

//    String serviceCondition;
    Button fwdNavigation, startNavigation;
    TextView textMainTitle;
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

        Log.d("safebike", "MainFragment.onCreate");
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

        Log.d("safebike", "MainFragment.onCreateView");
        // Inflate the layout for this fragment

//        Toast.makeText(getContext(), "MainFragment.onCreateView : " + PropertyManager.getInstance().getServiceCondition(), Toast.LENGTH_SHORT).show();

        final String uEmail = PropertyManager.getInstance().getUserEmail();
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        Button favorite = (Button)view.findViewById(R.id.btn_favorite_list);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkManager.getInstance().getFavorite(getContext(), uEmail, new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object result) {

                    }

                    @Override
                    public void onFail(int code) {

                    }
                });

            }
        });

        favorite = (Button)view.findViewById(R.id.btn_favorite_add);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkManager.getInstance().saveFavorite(getContext(), uEmail, "청담", 123.1, 121.2, new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object result) {

                    }

                    @Override
                    public void onFail(int code) {

                    }
                });

            }
        });

        favorite = (Button)view.findViewById(R.id.btn_favorite_remove);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkManager.getInstance().removeFavorite(getContext(), uEmail, "청담", new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object result) {

                    }

                    @Override
                    public void onFail(int code) {

                    }
                });

            }
        });

        favorite = (Button)view.findViewById(R.id.btn_favorite_removeall);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkManager.getInstance().removeAllFavorite(getContext(), uEmail, new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object result) {

                    }

                    @Override
                    public void onFail(int code) {

                    }
                });

            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        final String date = dateFormat.format(cal.getTime());

        favorite = (Button)view.findViewById(R.id.btn_save_exercise);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkManager.getInstance().saveExcercise(getContext(), uEmail, date, 120, 60, 100, new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object result) {

                    }

                    @Override
                    public void onFail(int code) {

                    }
                });
            }
        });



        textMainTitle = (TextView)((MainActivity)getActivity()).findViewById(R.id.text_main_title);

        Button btn = (Button) view.findViewById(R.id.btn_onoff_band);
        setFont();
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
        Log.d("safebike",  "MainFragment.onResume");
        //setFont();

    }

    public void setFont(){
        textMainTitle.setText("SafeBike");
        textMainTitle.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.BMJUA));


    }
}