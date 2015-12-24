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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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
import java.util.Collections;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class CalorieFragment extends Fragment {

    protected BarChart calorieChart;
    TextView parentCal;
    TextView parentSpeed;
    TextView parentDistance;
    int isDate = 0;
    ArrayList<String> xVals;
    ArrayList<BarEntry> yVals;
    ArrayList<BarDataSet> dataSets;
    ArrayList<String> values;
    ArrayList<ExerciseItem> workresults;
    ArrayList<ExerciseItem> collections;
    int collectCount;
    int total;
    int yTotal;
    int index;
    private int READ_SIZE = 5;
    public CalorieFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calorie, container, false);

        parentCal = (TextView) getParentFragment().getView().findViewById(R.id.text_value_calorie);
        parentSpeed = (TextView) getParentFragment().getView().findViewById(R.id.text_value_speed);
        parentDistance = (TextView) getParentFragment().getView().findViewById(R.id.text_value_distance);
        YAxisValueFormatter custom = new MyYAxisValueFormatter(MyYAxisValueFormatter.CHART_CALORIE);

        calorieChart = (BarChart) view.findViewById(R.id.chart_calorie);
        calorieChart.setVerticalScrollBarEnabled(false);
        calorieChart.setDrawBarShadow(false);
        calorieChart.setDrawGridBackground(false);
        calorieChart.setDrawHighlightArrow(false);
        calorieChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        calorieChart.getXAxis().setDrawGridLines(false);
        calorieChart.getAxisLeft().setDrawGridLines(false);
        calorieChart.getAxisLeft().setValueFormatter(custom);
        calorieChart.getAxisRight().setDrawGridLines(false);
        calorieChart.getAxisRight().setDrawLabels(false);
        NetworkManager.getInstance().getRecentExerciseDate(getContext(), PropertyManager.getInstance().getUserEmail(), new NetworkManager.OnResultListener<ExerciseRecentResult>() {
            @Override
            public void onSuccess(ExerciseRecentResult result) {
                Log.i("recentdate",result.workoutone.get(0).date);
            }

            @Override
            public void onFail(int code) {

            }
        });

        calorieChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {


            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                if (calorieChart.getLowestVisibleXIndex() == 0) {
                    calorieChart.animateXY(3000, 3000);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar cal = Calendar.getInstance();
                    String getDate = calorieChart.getXValue(calorieChart.getLowestVisibleXIndex());
                    try {
                        Date d = dateFormat.parse(getDate);
                        cal.setTime(d);
                        cal.add(Calendar.DATE, -1);


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.i("requestDate", dateFormat.format(cal.getTime()));
                   // requestData(dateFormat.format(cal.getTime()));
                    if(collections.size() < READ_SIZE) {
                        updateData(collections.size());
                    }
                    else{
                        updateData(READ_SIZE);
                        READ_SIZE += 5;
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
        });
        calorieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {


                String email = PropertyManager.getInstance().getUserEmail();
                String date = calorieChart.getXValue(e.getXIndex());


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

            @Override
            public void onNothingSelected() {

            }
        });

        total = 0;
        yTotal = 0;
        index = 0;
        collectCount = 0;
        xVals = new ArrayList<String>();
        yVals = new ArrayList<BarEntry>();
        values = new ArrayList<String>();
        workresults = new ArrayList<ExerciseItem>();
        collections = new ArrayList<ExerciseItem>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(cal.getTime());
       // requestData(today);
        collectDate(today);

        setFont();

        return view;
    }
    public void setToday(){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String email = PropertyManager.getInstance().getUserEmail();
        //String date = distanceChart.getXValue(e.getXIndex());
        String date = calorieChart.getXValue(calorieChart.getHighestVisibleXIndex());



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
    private void collectDate(String today){
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
                Log.i("date : ",date);
            } catch (ParseException e) {
                e.printStackTrace();
            }




        }
        String email = PropertyManager.getInstance().getUserEmail();

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
                    Log.i("collectionsSize", collections.size() + "");

                    for (int i = 0; i < collections.size(); i++) {
                        Log.i("collectionsItem", collections.get(i)._id + "");
                    }

                }else {
                /*if(collections.size() > 0) {
                    updateData(collections.get(collections.size() - 1)._id,5);
                }*/
                    if(collections.size() < READ_SIZE) {
                        updateData(collections.size());
                    }
                    else{
                        updateData(READ_SIZE);
                        READ_SIZE += 5;
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

            yVals.add(new BarEntry((collections.get(collections.size() - readSize + i).calorie * 100) / 100, i));

        }
        BarDataSet set = new BarDataSet(yVals, "calorie");
        set.setColor(Color.parseColor("#B6E2FF"));
        //set.setBarSpacePercent(70f);

        dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set);

        data = new BarData(xVals, dataSets);
        data.setValueTextSize(0f);

        calorieChart.setData(data);

        calorieChart.notifyDataSetChanged();
        calorieChart.moveViewToX(calorieChart.getData().getXVals().size() - 1);
        calorieChart.invalidate();

    }
    private void requestData(String today) {

        final ArrayList<String> dateList = new ArrayList<String>();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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
        NetworkManager.getInstance().getExerciseRecord(getContext(), email, dateList, new NetworkManager.OnResultListener<ExcerciseResult>() {
            @Override
            public void onSuccess(ExcerciseResult result) {

                BarData data;
                int count = result.workoutlist.size();
                for (int i = 0; i < result.workoutlist.size(); i++) {
                    workresults.add(i, result.workoutlist.get(i));
                }
                Log.i("counter", count + "");
                Log.i("workresults ", workresults.size() + "");

                xVals = new ArrayList<String>();
                yVals = new ArrayList<BarEntry>();
                if (count > 0) {


                    Collections.sort(dateList);


                    for (int i = 0; i < workresults.size(); i++) {

                        xVals.add(i,workresults.get(i)._id);
                        yVals.add(new BarEntry((workresults.get(i).calorie * 100) / 100, i));

                    }

                    yTotal += dateList.size();



                    /*
                    for(int i = 0; i < count)
                        */


                    /*for (int i = 0; i < count; i++) {
                        xVals.add(i,result.workoutlist.get(i)._id);
                        //new BarEntry();
                        yVals.add(new BarEntry((result.workoutlist.get(i).calorie * 100) / 100, i));

                    }*/


                    total += count;
                    BarDataSet set = new BarDataSet(yVals, "calorie");
                    set.setColor(Color.parseColor("#B6E2FF"));
                    //set.setBarSpacePercent(70f);

                    dataSets = new ArrayList<BarDataSet>();
                    dataSets.add(set);

                    data = new BarData(xVals, dataSets);
                    data.setValueTextSize(0f);

                    calorieChart.setData(data);

                    calorieChart.notifyDataSetChanged();
                    calorieChart.moveViewToX(calorieChart.getData().getXVals().size() - 1);
                    calorieChart.invalidate();

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

    public void setFont() {
        parentCal.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        parentSpeed.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        parentDistance.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
    }

}
