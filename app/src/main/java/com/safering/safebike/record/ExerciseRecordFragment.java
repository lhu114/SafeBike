package com.safering.safebike.record;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.safering.safebike.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseRecordFragment extends Fragment {

    FragmentTabHost tabHost;
    public ExerciseRecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise_record, container, false);
        tabHost = (FragmentTabHost)view.findViewById(R.id.tabHost);
        tabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);
        tabHost.addTab(tabHost.newTabSpec("calorie").setIndicator("칼로리"), CalorieFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("speed").setIndicator("속력"), SpeedFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("distance").setIndicator("거리"), DialogFragment.class, null);
        return view;
    }


}
