package com.safering.safebike;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;


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

    Button fwdNavigation, startNavigation;
    TextView textSafeBikeMainTitle, textMainTitle;
    TextView textMainMesage;
    TextView textRunningMesage;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        final String uEmail = PropertyManager.getInstance().getUserEmail();
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        textSafeBikeMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_safebike_main_title);
        textMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_main_title);
        textMainMesage = (TextView)view.findViewById(R.id.text_main_message);
        textRunningMesage = (TextView)view.findViewById(R.id.text_running_message);
        textSafeBikeMainTitle.setVisibility(View.VISIBLE);
        textMainTitle.setVisibility(View.GONE);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();



        fwdNavigation = (Button) view.findViewById(R.id.btn_fwd_navigation);
        fwdNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "btn_fwd_navigation.Clicked", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, new NavigationFragment(), TAG_NAVIGATION).addToBackStack(null).commit();
            }
        });


        if (PropertyManager.getInstance().getServiceCondition().equals(SERVICE_RUNNING)) {

            fwdNavigation.setVisibility(View.GONE);
            textRunningMesage.setVisibility(View.VISIBLE);

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        textSafeBikeMainTitle.setVisibility(View.VISIBLE);
        textMainTitle.setVisibility(View.GONE);
    }

    public void setFont() {
        textSafeBikeMainTitle.setText("Safe Bike");
        textSafeBikeMainTitle.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.BMJUA));
        textMainMesage.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS_R));
        textRunningMesage.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS_R));
    }


}