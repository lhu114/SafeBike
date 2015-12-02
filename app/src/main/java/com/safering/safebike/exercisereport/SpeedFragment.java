package com.safering.safebike.exercisereport;


import android.graphics.Color;
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
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class SpeedFragment extends Fragment {
    protected BarChart speedChart;
    TextView parentCal;
    TextView parentSpeed;
    TextView parentDistance;


    ArrayList<String> xVals;
    ArrayList<BarEntry> yVals;
    ArrayList<BarDataSet> dataSets;

    int total = 0;

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
        YAxisValueFormatter custom = new MyYAxisValueFormatter(MyYAxisValueFormatter.CHART_SPEED);



        speedChart = (BarChart) view.findViewById(R.id.chart_speed);
        speedChart.setVerticalScrollBarEnabled(true);
        speedChart.setDrawBarShadow(false);
        speedChart.setDrawGridBackground(false);
        speedChart.setDrawHighlightArrow(false);
        speedChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        speedChart.getXAxis().setDrawGridLines(false);
        speedChart.getAxisLeft().setDrawGridLines(false);
        speedChart.getAxisLeft().setValueFormatter(custom);
        speedChart.getAxisRight().setDrawGridLines(false);
        speedChart.getAxisRight().setDrawLabels(false);


       /* setFont();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(cal.getTime());

        requestData(today);*/

        speedChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                if (speedChart.getLowestVisibleXIndex() == 0) {
                    speedChart.animateXY(3000, 3000);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar cal = Calendar.getInstance();
                    String getDate = speedChart.getXValue(speedChart.getLowestVisibleXIndex());
                    try {
                        Date d = dateFormat.parse(getDate);
                        cal.setTime(d);
                        cal.add(Calendar.DATE, -1);


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.i("requestDate", dateFormat.format(cal.getTime()));
                    requestData(dateFormat.format(cal.getTime()));
                }

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
 //               String date = dateFormat.format(cal.getTime());
                String date = speedChart.getXValue(e.getXIndex());


                NetworkManager.getInstance().getDayExerciseRecord(getContext(), email, date, new NetworkManager.OnResultListener<ExerciseDayResult>() {
                    @Override
                    public void onSuccess(ExerciseDayResult result) {
                        if (result.workout.size() > 0) {
                            NumberFormat nf = NumberFormat.getInstance();
                            nf.setMaximumFractionDigits(2);//소수점 아래 최대자리수
                             parentCal.setText(String.valueOf(Math.round(result.workout.get(0).calorie)) + " kcal");
                            parentSpeed.setText(String.valueOf(Math.round((result.workout.get(0).speed * 3600.0)/1000)) + " km/h");
                            parentDistance.setText(String.valueOf(nf.format(result.workout.get(0).road/1000.0)) + " km");
                        }
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

        total = 0;
        xVals = new ArrayList<String>();
        yVals = new ArrayList<BarEntry>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(cal.getTime());
        requestData(today);
        setFont();
        return view;
    }
    public void setToday(){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String email = PropertyManager.getInstance().getUserEmail();
        //String date = distanceChart.getXValue(e.getXIndex());
        String date = speedChart.getXValue(speedChart.getHighestVisibleXIndex());



        NetworkManager.getInstance().getDayExerciseRecord(getContext(), email, date, new NetworkManager.OnResultListener<ExerciseDayResult>() {
            @Override
            public void onSuccess(ExerciseDayResult result) {
                if (result.workout.size() > 0) {
                    NumberFormat nf = NumberFormat.getInstance();

                    nf.setMaximumFractionDigits(2);//소수점 아래 최대자리수


                    parentCal.setText(String.valueOf(Math.round(result.workout.get(0).calorie)) + " kcal");
                    parentSpeed.setText(String.valueOf(Math.round((result.workout.get(0).speed * 3600.0)/1000)) + " km/h");
                    parentDistance.setText(String.valueOf(nf.format(result.workout.get(0).road/1000.0)) + " km");
                }
            }

            @Override
            public void onFail(int code) {

            }
        });

    }


    private void requestData(String today) {
        int count = 0;
        int range = 0;
        ArrayList<String> dateList = new ArrayList<String>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();

        dateList.add(today);


        for(int i = 0; i < 9; i++){
            try {
                Date d = dateFormat.parse(today);
                cal.setTime(d);
                cal.add(Calendar.DATE, -1);
                String date = dateFormat.format(cal.getTime());
                dateList.add(date);
                today = date;
                Log.i("date : ",date);
            } catch (ParseException e) {
                e.printStackTrace();
            }



        }

        String email = PropertyManager.getInstance().getUserEmail();
        NetworkManager.getInstance().getExerciseRecord(getContext(), email,dateList, new NetworkManager.OnResultListener<ExcerciseResult>() {
            @Override
            public void onSuccess(ExcerciseResult result) {

                ArrayList<ExerciseItem> values = result.workoutlist;

                BarData data;
                int count = result.workoutlist.size();
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        xVals.add(result.workoutlist.get(i)._id);
                        yVals.add(new BarEntry(result.workoutlist.get(i).speed, total+i));
                    }
                    total += count;
                    BarDataSet set = new BarDataSet(yVals, "speed");
                    set.setBarSpacePercent(45f);

                    set.setColor(Color.parseColor("#B6E2FF"));
                    dataSets = new ArrayList<BarDataSet>();
                    dataSets.add(set);

                    data = new BarData(xVals, dataSets);
                    data.setValueTextSize(10f);
                    speedChart.setData(data);
   /*                 calorieChart.getXValue(0);
   */


                    speedChart.notifyDataSetChanged();
                    speedChart.moveViewToX(speedChart.getData().getXVals().size() - 1);
                    speedChart.invalidate();
                    setToday();

                }



            }

            @Override
            public void onFail(int code) {

            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        parentCal.setText("");
        parentSpeed.setText("");
        parentDistance.setText("");

    }

    public void setFont(){
        parentCal.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        parentSpeed.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        parentDistance.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
    }

}
