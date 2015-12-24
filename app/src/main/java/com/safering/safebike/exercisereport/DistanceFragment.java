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
import com.safering.safebike.manager.InformDialogFragment;
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
public class DistanceFragment extends Fragment  implements OnChartGestureListener,OnChartValueSelectedListener{

    protected BarChart distanceChart;
    TextView parentCal;
    TextView parentSpeed;
    TextView parentDistance;
    ArrayList<String> xVals;
    ArrayList<BarEntry> yVals;
    ArrayList<BarDataSet> dataSets;
    ArrayList<ExerciseItem> collections;
    String recentDate;
    int collectCount;
    int readSize ;
    public DistanceFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_distance, container, false);

        collectCount = 0;
        recentDate = null;
        readSize = 5;
        xVals = new ArrayList<String>();
        yVals = new ArrayList<BarEntry>();
        collections = new ArrayList<ExerciseItem>();

        parentCal = (TextView) getParentFragment().getView().findViewById(R.id.text_value_calorie);
        parentSpeed = (TextView) getParentFragment().getView().findViewById(R.id.text_value_speed);
        parentDistance = (TextView) getParentFragment().getView().findViewById(R.id.text_value_distance);
        YAxisValueFormatter custom = new MyYAxisValueFormatter(MyYAxisValueFormatter.CHART_DISTANCE);

        distanceChart = (BarChart) view.findViewById(R.id.chart_distance);
        distanceChart.setVerticalScrollBarEnabled(false);
        distanceChart.setDrawBarShadow(false);
        distanceChart.setDrawGridBackground(false);
        distanceChart.setDrawHighlightArrow(false);
        distanceChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        distanceChart.getXAxis().setDrawGridLines(false);
        distanceChart.getAxisLeft().setDrawGridLines(false);
        distanceChart.getAxisLeft().setValueFormatter(custom);
        distanceChart.getAxisRight().setDrawGridLines(false);
        distanceChart.getAxisRight().setDrawLabels(false);
        distanceChart.setOnChartGestureListener(this);
        distanceChart.setOnChartValueSelectedListener(this);

        setFont();
        getExerciseDatas();
        setRecentData();
        return view;
    }
    public void displayClickData(String email,String date){
        NetworkManager.getInstance().getDayExerciseRecord(getContext(), email, date, new NetworkManager.OnResultListener<ExerciseDayResult>() {
            @Override
            public void onSuccess(ExerciseDayResult result) {
                if (result.workout.size() > 0) {
                    NumberFormat nf = NumberFormat.getInstance();

                    nf.setMaximumFractionDigits(2);//소수점 아래 최대자리수


                    parentCal.setText(String.valueOf(Math.round(result.workout.get(0).calorie)) + " kcal");
                    parentSpeed.setText(String.valueOf(Math.round((result.workout.get(0).speed * 3600.0) / 1000)) + " km/h");
                    parentDistance.setText(String.valueOf(nf.format(result.workout.get(0).road / 1000.0)) + " km");
                }
            }

            @Override
            public void onFail(int code) {
                InformDialogFragment dialog = new InformDialogFragment();
                dialog.setContent("네트워크 실패", "네트워크 연결에 실패했습니다. 다시 시도해주세요");
                dialog.show(getChildFragmentManager(), "network");

            }
        });
    }
    public void setRecentData(){

        NetworkManager.getInstance().getRecentExerciseDate(getContext(), PropertyManager.getInstance().getUserEmail(), new NetworkManager.OnResultListener<ExerciseRecentResult>() {
            @Override
            public void onSuccess(ExerciseRecentResult result) {

                if (result.workoutone.size() > 0) {
                    recentDate = result.workoutone.get(0).date;
                    String email = PropertyManager.getInstance().getUserEmail();
                    displayClickData(email,recentDate);


                }
            }

            @Override
            public void onFail(int code) {

            }
        });

    }

    public void getExerciseDatas(){

        NetworkManager.getInstance().getRecentExerciseDate(getContext(), PropertyManager.getInstance().getUserEmail(), new NetworkManager.OnResultListener<ExerciseRecentResult>() {
            @Override
            public void onSuccess(ExerciseRecentResult result) {

                if (result.workoutone.size() > 0) {
                    recentDate = result.workoutone.get(0).date;
                    collectDate(recentDate);
                }
            }

            @Override
            public void onFail(int code) {

            }
        });


    }


    private void collectDate(String today){
        String email = PropertyManager.getInstance().getUserEmail();

        final ArrayList<String> dateList = new ArrayList<String>();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final Calendar cal = Calendar.getInstance();

        dateList.add(today);

        for(int i = 0; i < 9; i++){
            try {
                Date d = dateFormat.parse(today);
                cal.setTime(d);
                cal.add(Calendar.DATE, -1);
                String date = dateFormat.format(cal.getTime());
                dateList.add(date);
                today = date;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        NetworkManager.getInstance().getExerciseRecord(getContext(), email, dateList, new NetworkManager.OnResultListener<ExcerciseResult>() {
            @Override
            public void onSuccess(ExcerciseResult result) {
                collectCount = result.workoutlist.size();
                if (collectCount > 0) {
                    for (int i = 0; i < result.workoutlist.size(); i++) {
                        collections.add(i, result.workoutlist.get(i));
                    }
                    try {
                        Date d = dateFormat.parse(dateList.get(dateList.size() - 1));
                        cal.setTime(d);
                        cal.add(Calendar.DATE, -1);
                        String date = dateFormat.format(cal.getTime());
                        collectDate(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }else {
                    if(collections.size() < readSize) {
                        updateData(collections.size());
                    }
                    else{
                        updateData(readSize);
                        readSize += 5;
                    }

                }
            }

            @Override
            public void onFail(int code) {

            }
        });

    }
    private void updateData(int readSize) {

        BarData data;
        xVals = new ArrayList<String>();
        yVals = new ArrayList<BarEntry>();

        for(int i = 0; i < readSize; i++){
            xVals.add(i,collections.get(collections.size() - readSize + i)._id);

            yVals.add(new BarEntry((collections.get(collections.size() - readSize + i).road * 100) / 100, i));

        }
        BarDataSet set = new BarDataSet(yVals, "거리");
        set.setColor(Color.parseColor("#B6E2FF"));
        set.setBarSpacePercent(70f);

        dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set);
        data = new BarData(xVals, dataSets);
        data.setValueTextSize(0f);
        distanceChart.setData(data);
        distanceChart.notifyDataSetChanged();
        distanceChart.moveViewToX(distanceChart.getData().getXVals().size() - 1);
        distanceChart.invalidate();

    }


    @Override
    public void onPause() {
        super.onPause();
        parentCal.setText("");
        parentSpeed.setText("");
        parentDistance.setText("");

    }

    public void setFont() {
        parentCal.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        parentSpeed.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        parentDistance.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        if (distanceChart.getLowestVisibleXIndex() == 0) {
            try {
                distanceChart.animateXY(3000, 3000);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                String getDate = distanceChart.getXValue(distanceChart.getLowestVisibleXIndex());
                Date d = dateFormat.parse(getDate);
                cal.setTime(d);
                cal.add(Calendar.DATE, -1);
                if(collections.size() < readSize) {
                    updateData(collections.size());
                }
                else{
                    updateData(readSize);
                    readSize += 5;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

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

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

        String email = PropertyManager.getInstance().getUserEmail();
        String date = distanceChart.getXValue(e.getXIndex());
        displayClickData(email,date);


    }

    @Override
    public void onNothingSelected() {

    }
}

