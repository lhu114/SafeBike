package com.safering.safebike.exercisereport;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.safering.safebike.R;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class DistanceFragment extends Fragment {

    protected BarChart distanceChart;
    TextView parentCal;
    TextView parentSpeed;
    TextView parentDistance;
    public DistanceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_distance, container, false);
        parentCal = (TextView) getParentFragment().getView().findViewById(R.id.text_value_calorie);
        parentSpeed = (TextView) getParentFragment().getView().findViewById(R.id.text_value_speed);
        parentDistance = (TextView) getParentFragment().getView().findViewById(R.id.text_value_distance);

        distanceChart = (BarChart) view.findViewById(R.id.chart_distance);
        setData();
        distanceChart.setVerticalScrollBarEnabled(true);
        distanceChart.setDrawBarShadow(false);
        distanceChart.setDrawGridBackground(false);
        distanceChart.setDrawHighlightArrow(false);
        distanceChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        distanceChart.getXAxis().setDrawGridLines(false);
        distanceChart.getAxisLeft().setDrawGridLines(false);
        distanceChart.getAxisRight().setDrawGridLines(false);
        distanceChart.getAxisRight().setDrawLabels(false);
        distanceChart.setScaleEnabled(false);

        distanceChart.setScaleMinima(2f, 1f);


        distanceChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {


            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }
        });
        distanceChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                String email = PropertyManager.getInstance().getUserEmail();
                String date = dateFormat.format(cal.getTime());


                NetworkManager.getInstance().getDayExerciseRecord(getContext(), email, date, new NetworkManager.OnResultListener<ExerciseDayResult>() {
                    @Override
                    public void onSuccess(ExerciseDayResult result) {
                        parentCal.setText(String.valueOf(result.workout.get(0).calorie));
                        parentSpeed.setText(String.valueOf(result.workout.get(0).speed));
                        parentDistance.setText(String.valueOf(result.workout.get(0).road));
                    }

                    @Override
                    public void onFail(int code) {

                    }
                });
            }

            @Override
            public void onNothingSelected() {

            }
        });
        return view;
    }

    private void setData() {
        int count = 0;
        int range = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String email = PropertyManager.getInstance().getUserEmail();
        NetworkManager.getInstance().getExerciseRecord(getContext(), email, date, new NetworkManager.OnResultListener<ExcerciseResult>() {
            @Override
            public void onSuccess(ExcerciseResult result) {

                ArrayList<ExerciseItem> values = result.workoutlist;
                ArrayList<String> xVals = new ArrayList<String>();
                ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
                BarDataSet set = new BarDataSet(yVals, "Distance");
                ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
                BarData data;
                int count = result.workoutlist.size();

                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        xVals.add(values.get(i).date);
                        yVals.add(new BarEntry(values.get(i).road, i));
                    }
                }
                set.setBarSpacePercent(35f);
                dataSets.add(set);
                data = new BarData(xVals, dataSets);
                data.setValueTextSize(10f);

                distanceChart.setData(data);
                distanceChart.moveViewToX(distanceChart.getData().getXVals().size() - 1);



            }

            @Override
            public void onFail(int code) {

            }
        });
/*

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add("x/" + (i + 1));
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < count; i++) {
            float mult = (range + 1);
            float val = (float) (Math.random() * mult);
            yVals1.add(new BarEntry(val, i));
        }

        BarDataSet set1 = new BarDataSet(yVals1, "DataSet");
        set1.setBarSpacePercent(35f);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10f);

        distanceChart.setData(data);
*/



    }

    @Override
    public void onPause() {
        super.onPause();
        parentCal.setText("");
        parentSpeed.setText("");
        parentDistance.setText("");

    }

}
