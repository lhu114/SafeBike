package com.safering.safebike.exercisereport;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseReportFragment extends Fragment {
    View calorieView;
    View speedView;
    View distanceView;
    TextView textCalorie;
    TextView textSpeed;
    TextView textDistance;

    TextView textResultCalorie;
    TextView textResultSpeed;
    TextView textResultDistance;

    TextView textValueCalorie;
    TextView textValueSpeed;
    TextView textValueDistance;
    TextView textSafeBikeMainTitle, textMainTitle;



    FragmentTabHost tabHost;
    private static final String SPEC_CALORIE = "calorie";
    private static final String SPEC_SPEED = "speed";
    private static final String SPEC_DISTANCE = "distance";

    public ExerciseReportFragment() {
        // Required empty public constructor


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise_record, container, false);
        // TODO Auto-generated method stub
        textResultCalorie = (TextView)view.findViewById(R.id.text_report_calorie);
        textResultSpeed = (TextView)view.findViewById(R.id.text_report_speed);
        textResultDistance = (TextView)view.findViewById(R.id.text_report_distance);

        textValueCalorie = (TextView)view.findViewById(R.id.text_value_calorie);
        textValueSpeed = (TextView)view.findViewById(R.id.text_value_speed);
        textValueDistance = (TextView)view.findViewById(R.id.text_value_distance);


        calorieView = getActivity().getLayoutInflater().inflate(R.layout.tabitem_calorie_view, null);
        speedView = getActivity().getLayoutInflater().inflate(R.layout.tabitem_speed_view, null);
        distanceView = getActivity().getLayoutInflater().inflate(R.layout.tabitem_distance_view, null);

        textCalorie = (TextView)calorieView.findViewById(R.id.text_tab_calorie);
        textSpeed = (TextView)speedView.findViewById(R.id.text_tab_speed);
        textDistance = (TextView)distanceView.findViewById(R.id.text_tab_distance);

        textSafeBikeMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_safebike_main_title);
        textMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_main_title);

        textSafeBikeMainTitle.setVisibility(View.GONE);
        textMainTitle.setVisibility(View.VISIBLE);

        setFont();
        tabHost = (FragmentTabHost) view.findViewById(R.id.exercise_tabHost);
        tabHost.setup(getActivity(), getChildFragmentManager(), R.id.exercise_realtabcontent);

        tabHost.addTab(tabHost.newTabSpec(SPEC_CALORIE).setIndicator(calorieView), CalorieFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(SPEC_SPEED).setIndicator(speedView), SpeedFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(SPEC_DISTANCE).setIndicator(distanceView), DistanceFragment.class, null);



        tabHost.getTabWidget().setDividerDrawable(null);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitleFont();

        textSafeBikeMainTitle.setVisibility(View.GONE);
        textMainTitle.setVisibility(View.VISIBLE);
    }

    public void viewData(int calorie, int speed, int distance) {

    }

    public void setFont() {
      /*  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        final int calorie = CalculatorCalorie.getInstance().getCalorie(1.5f, 65, 5);
        final int speed = 2;
        final int distance = 5;
        final String date = sdf.format(cal.getTime());


        NetworkManager.getInstance().saveExercise(getContext(), PropertyManager.getInstance().getUserEmail(), date, calorie, speed, distance, new NetworkManager.OnResultListener() {
            @Override
            public void onSuccess(Object result) {
               // Log.d(DEBUG_TAG, "StartNavigationActivity.sendExerciseReport.saveExercise.onSuccess.result : " + result);

                if ((int) result == 1) {
*//*
                    Log.d("safebike", "SelectRouteActivity.removeFavorite.onSuccess.200");
                    Toast.makeText(getContext(), "saveExercise.SUCCESS.200", Toast.LENGTH_SHORT).show();

                    mSpeedList.clear();
                    mDistanceList.clear();
*//*

                } else {
*//*
                    Log.d("safebike", "SelectRouteActivity.removeFavorite.onSuccess.else");
                    Toast.makeText(getContext(), "saveExercise.SUCCESS.200.else", Toast.LENGTH_SHORT).show();
*//*
                }
                    *//*
                     *  비정상 종료 처리 시에 기존 데이터(칼로리, 스피드, 거리 리스트 저장해 두었다가 onCreate 에서 저장) bundle 이용
                     *
                     *  또는 비정상 종료 시에 데이터 그냥 서버로 보내버림(이 방법이 좋을듯)
                     *//*
            }

            @Override
            public void onFail(int code) {
                //Log.d(DEBUG_TAG, "StartNavigationActivity.sendExerciseReport.saveExercise.onFail.result : " + Integer.toString(code));
                Toast.makeText(getContext(), "saveExercise.FAIL", Toast.LENGTH_SHORT).show();
            }
        });
*/


        textDistance.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textSpeed.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textCalorie.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textResultCalorie.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textResultSpeed.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textResultDistance.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textValueCalorie.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textValueSpeed.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        textValueDistance.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));



    }

    public void setTitleFont(){
        textMainTitle.setText("기록");
        textMainTitle.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));

    }


}
