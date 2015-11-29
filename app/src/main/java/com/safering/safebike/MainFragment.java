package com.safering.safebike;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.navigation.NavigationFragment;
import com.safering.safebike.navigation.StartNavigationActivity;
import com.safering.safebike.property.PropertyManager;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */

/**
 * saveExcercise - 운동정보 저장
 * getFavorite - 즐겨찾기 리스트
 * saveFavorite - 즐겨찾기 추가
 * removeFavorite - 즐겨찾기 삭제
 * removeAllFavorite - 즐겨찾기 전체삭제
 */
public class MainFragment extends Fragment {
    private static final String TAG_NAVIGATION = "navigation";
    private static final String ARG_NAME = "name";
    private static final String SERVICE_RUNNING = "running";

    //    String serviceCondition;
    Button fwdNavigation, startNavigation;
    TextView textMainTitle;
    TextView textBandOnOff;
    TextView textBackLightOnOff;
    ImageView imageBacklightIn;
    ImageView imageBacklightOut;
    Button btnBacklight;
    Button btnBand;
    boolean backlightStatus = false;
    boolean bandStatus = false;
    int deviceStatus = 0;

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

        textBandOnOff = (TextView) view.findViewById(R.id.text_band_status);
        textBackLightOnOff = (TextView) view.findViewById(R.id.text_backlight_status);
        imageBacklightIn = (ImageView) view.findViewById(R.id.image_backlight_onoff_in);
        imageBacklightOut = (ImageView) view.findViewById(R.id.image_backlight_onoff_out);
        btnBacklight = (Button) view.findViewById(R.id.btn_onoff_backlight);
        btnBand = (Button) view.findViewById(R.id.btn_onoff_band);

     //   startActivityForResult();

        // btnBacklight.setSelected(true);




    /*    Button favorite = (Button)view.findViewById(R.id.btn_favorite_list);
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
        });*/

       /* favorite = (Button)view.findViewById(R.id.btn_favorite_add);
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
*/
       /* favorite = (Button)view.findViewById(R.id.btn_favorite_remove);
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
        });*/
/*
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
        });*/

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        final String date = dateFormat.format(cal.getTime());

     /*   favorite = (Button)view.findViewById(R.id.btn_save_exercise);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkManager.getInstance().saveExercise(getContext(), uEmail, date, 120, 60, 100, new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object result) {

                    }

                    @Override
                    public void onFail(int code) {

                    }
                });
            }
        });*/


        textMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_main_title);


        // setBluetooth();

        //  Button btn = (Button) view.findViewById(R.id.btn_onoff_band);
        btnBand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnBacklight = (Button) view.findViewById(R.id.btn_onoff_backlight);
        btnBacklight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backlightStatus) {
                    btnBacklight.setSelected(false);
                    PropertyManager.getInstance().setBluetoothSetting(0);

                    backlightStatus = false;
                } else {
                    btnBacklight.setSelected(true);
                    PropertyManager.getInstance().setBluetoothSetting(1);

                    backlightStatus = true;

                }

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
        setFont();
        setBluetooth();
        checkConnection();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("safebike", "MainFragment.onResume");
        //setFont();

    }

    public void setFont() {
        textMainTitle.setText("Safe Bike");
        textMainTitle.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.BMJUA));
        textBandOnOff.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        textBackLightOnOff.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));


    }

    public void setConnectionOnOff(int status) {
        deviceStatus = status;
        /*if (deviceStatus == 1) {
            imageBacklightOut.setImageResource(R.drawable.on);
            imageBacklightIn.setImageResource(R.drawable.on);


        } else if (status == 0) {
            imageBacklightOut.setImageResource(R.drawable.off);
            imageBacklightIn.setImageResource(R.drawable.off);

        }*/
    }

    public int getConnectionOnOff(){
        return deviceStatus;
    }

    public void checkConnection() {
        if (deviceStatus == 1) {
            imageBacklightOut.setImageResource(R.drawable.on);
            imageBacklightIn.setImageResource(R.drawable.on);
            textBackLightOnOff.setText("후미등 켜짐");

        } else if (deviceStatus == 0) {
            imageBacklightOut.setImageResource(R.drawable.off);
            imageBacklightIn.setImageResource(R.drawable.off);
            textBackLightOnOff.setText("후미등 꺼짐");



        }
    }

    public void setBluetooth() {
        if (PropertyManager.getInstance().getBluetoothSetting() == 0) {
            btnBacklight.setSelected(false);

        } else {
            btnBacklight.setSelected(true);
        }
    }
}