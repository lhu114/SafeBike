package com.safering.safebike.exercisereport;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
public class SpeedFragment extends Fragment {
    protected BarChart speedChart;
    TextView parentCal;
    TextView parentSpeed;
    TextView parentDistance;

    public SpeedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_speed, container, false);
        parentCal = (TextView) getParentFragment().getView().findViewById(R.id.text_value_calorie);
        parentSpeed = (TextView) getParentFragment().getView().findViewById(R.id.text_value_speed);
        parentDistance = (TextView) getParentFragment().getView().findViewById(R.id.text_value_distance);



        speedChart = (BarChart) view.findViewById(R.id.chart_speed);
        setData();
        speedChart.setVerticalScrollBarEnabled(true);
        speedChart.setDrawBarShadow(false);
        speedChart.setDrawGridBackground(false);
        speedChart.setDrawHighlightArrow(false);
        speedChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        speedChart.getXAxis().setDrawGridLines(false);
        speedChart.getAxisLeft().setDrawGridLines(false);
        speedChart.getAxisRight().setDrawGridLines(false);
        speedChart.getAxisRight().setDrawLabels(false);
        speedChart.setScaleEnabled(false);
        speedChart.setScaleMinima(2f, 1f);


        speedChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                Log.i("index : " ,me.getActionIndex() + "");
                //me.

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
        speedChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
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
                BarDataSet set = new BarDataSet(yVals, "Speed");
                ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
                BarData data;
                int count = result.workoutlist.size();

                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        xVals.add(values.get(i).date);
                        yVals.add(new BarEntry(values.get(i).speed, i));
                    }
                }
                set.setBarSpacePercent(35f);
                dataSets.add(set);
                data = new BarData(xVals, dataSets);
                data.setValueTextSize(10f);

                speedChart.setData(data);
                speedChart.moveViewToX(speedChart.getData().getXVals().size() - 1);


            }

            @Override
            public void onFail(int code) {

            }
        });

       /* ArrayList<String> xVals = new ArrayList<String>();
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

        speedChart.setData(data);*/


    }

    @Override
    public void onPause() {
        super.onPause();
        parentCal.setText("");
        parentSpeed.setText("");
        parentDistance.setText("");

    }

}
