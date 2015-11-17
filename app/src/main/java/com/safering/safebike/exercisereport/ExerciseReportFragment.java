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
    TextView textMainTitle;



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
        textMainTitle = (TextView)((MainActivity)getActivity()).findViewById(R.id.text_main_title);
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
    }

    public void viewData(int calorie, int speed, int distance) {

    }

    public void setFont() {
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
        textMainTitle.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS_M));

    }


}
