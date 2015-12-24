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
import com.safering.safebike.navigation.NavigationFragment;
import com.safering.safebike.navigation.StartNavigationActivity;
import com.safering.safebike.property.PropertyManager;
import com.safering.safebike.setting.BluetoothConnection;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */


public class MainFragment extends Fragment {
    private static final String TAG_NAVIGATION = "navigation";
    private static final String SERVICE_RUNNING = "running";

    Button fwdNavigation, startNavigation;
    Button btnBacklight;
    Button btnBand;

    TextView textSafeBikeMainTitle, textMainTitle;
    TextView textBandOnOff;
    TextView textBackLightOnOff;

    ImageView imageBacklightIn;
    ImageView imageBacklightOut;

    boolean backlightStatus = false;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("safebike", "MainFragment.onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        Log.d("safebike", "MainFragment.onCreateView");
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        textSafeBikeMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_safebike_main_title);
        textMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_main_title);

        textBandOnOff = (TextView) view.findViewById(R.id.text_band_status);
        textBackLightOnOff = (TextView) view.findViewById(R.id.text_backlight_status);
        imageBacklightIn = (ImageView) view.findViewById(R.id.image_backlight_onoff_in);
        imageBacklightOut = (ImageView) view.findViewById(R.id.image_backlight_onoff_out);
        btnBacklight = (Button) view.findViewById(R.id.btn_onoff_backlight);
        btnBand = (Button) view.findViewById(R.id.btn_onoff_band);

        textSafeBikeMainTitle.setVisibility(View.VISIBLE);
        textMainTitle.setVisibility(View.GONE);


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();


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
                    textBackLightOnOff.setText("후미등 꺼짐");

                    backlightStatus = false;

                } else {
                    btnBacklight.setSelected(true);
                    PropertyManager.getInstance().setBluetoothSetting(1);
                    textBackLightOnOff.setText("후미등 켜짐");

                    backlightStatus = true;

                }

            }
        });

        fwdNavigation = (Button) view.findViewById(R.id.btn_fwd_navigation);
        fwdNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, new NavigationFragment(), TAG_NAVIGATION).addToBackStack(null).commit();
            }
        });
        if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_RUNNING)) {
            fwdNavigation.setVisibility(View.GONE);
            startNavigation = (Button) view.findViewById(R.id.btn_fwd_start_navigation);
            startNavigation.setVisibility(View.VISIBLE);
            startNavigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), StartNavigationActivity.class);
                    startActivity(intent);
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

        textSafeBikeMainTitle.setVisibility(View.VISIBLE);
        textMainTitle.setVisibility(View.GONE);
    }

    public void setFont() {
        textSafeBikeMainTitle.setText("Safe Bike");
        textSafeBikeMainTitle.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.BMJUA));
        textBandOnOff.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
        textBackLightOnOff.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));


    }

    public void checkConnection() {
        if (BluetoothConnection.getInstance().getIsConnect() == 1) {
            imageBacklightOut.setImageResource(R.drawable.on);
            imageBacklightIn.setImageResource(R.drawable.on);
        } else if (BluetoothConnection.getInstance().getIsConnect() == 0) {
            imageBacklightOut.setImageResource(R.drawable.off);
            imageBacklightIn.setImageResource(R.drawable.off);
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