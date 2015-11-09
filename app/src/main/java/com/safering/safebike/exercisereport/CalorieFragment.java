package com.safering.safebike.exercisereport;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.listener.OnDrawListener;
import com.safering.safebike.R;
import com.safering.safebike.login.LoginActivity;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class CalorieFragment extends Fragment {
    protected BarChart calorieChart;
    private static final int TYPE_CALORIE = 1;
    private static final int REQUEST_NUMBER = 14;


    public CalorieFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calorie, container, false);
        calorieChart = (BarChart) view.findViewById(R.id.chart_calorie);
        setData();
        calorieChart.setVerticalScrollBarEnabled(true);
        calorieChart.setDrawBarShadow(false);
        calorieChart.setDrawGridBackground(false);

        calorieChart.setDrawHighlightArrow(false);
        calorieChart.getXAxis().setLabelsToSkip(10);
        calorieChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        calorieChart.getXAxis().setDrawGridLines(false);
        calorieChart.getXAxis().setSpaceBetweenLabels(2);
        calorieChart.getAxisLeft().setDrawGridLines(false);
        calorieChart.getAxisRight().setDrawGridLines(false);
        calorieChart.setScaleMinima(2f, 1f);
        calorieChart.moveViewToX(calorieChart.getData().getXVals().size() - 1);
        calorieChart.setOnChartGestureListener(new OnChartGestureListener() {
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
       calorieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
             /*
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                String date = dateFormat.format(cal.getTime());
                String email = PropertyManager.getInstance().getUserEmail();
               NetworkManager.getInstance().getDayExerciseRecord(getContext(), email, date, new NetworkManager.OnResultListener<ExerciseDayResult>() {
                    @Override
                    public void onSuccess(ExerciseDayResult result) {

                    }

                    @Override
                    public void onFail(int code) {

                    }
                });*/

            }

            @Override
            public void onNothingSelected() {

            }
        });

        return view;
    }

    private void setData() {
        int count = 20;
        int range = 50;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String email = PropertyManager.getInstance().getUserEmail();
       /* NetworkManager.getInstance().getExerciseRecord(getContext(), email, TYPE_CALORIE, REQUEST_NUMBER, date, new NetworkManager.OnResultListener<ExcerciseResult>() {
            @Override
            public void onSuccess(ExcerciseResult result) {
                ArrayList<ExerciseItem> values = result.values;
                ArrayList<String> xVals = new ArrayList<String>();
                ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
                BarDataSet set = new BarDataSet(yVals, "DataSet");
                ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
                BarData data;
                int count = result.values.size();

                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        xVals.add(values.get(i).date);
                        yVals.add(new BarEntry(values.get(i).value, i));
                    }
                }
                set.setBarSpacePercent(35f);
                dataSets.add(set);
                data = new BarData(xVals, dataSets);
                data.setValueTextSize(10f);
                calorieChart.setData(data);
            }

            @Override
            public void onFail(int code) {

            }
        });
*/

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add("x/" + (i + 1));
        }

        ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();

        for (int i = 0; i < count; i++) {
            float mult = (range + 1);
            float val = (float) (Math.random() * mult);
            yVals.add(new BarEntry(val, i));
        }

        BarDataSet set1 = new BarDataSet(yVals, "DataSet");
        set1.setBarSpacePercent(35f);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10f);


        calorieChart.setData(data);


    }


}
