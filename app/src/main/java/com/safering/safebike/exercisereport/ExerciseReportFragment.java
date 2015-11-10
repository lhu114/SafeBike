package com.safering.safebike.exercisereport;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.safering.safebike.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseReportFragment extends Fragment {

    FragmentTabHost tabHost;
    public ExerciseReportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise_record, container, false);
        tabHost = (FragmentTabHost)view.findViewById(R.id.exercise_tabHost);
        tabHost.setup(getActivity(), getChildFragmentManager(), R.id.exercise_realtabcontent);
        tabHost.addTab(tabHost.newTabSpec("calorie").setIndicator("칼로리"), CalorieFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("speed").setIndicator("속력"), SpeedFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("distance").setIndicator("거리"), DistanceFragment.class, null);
        return view;
    }

    public void viewData(int calorie,int speed,int distance){

    }


}
