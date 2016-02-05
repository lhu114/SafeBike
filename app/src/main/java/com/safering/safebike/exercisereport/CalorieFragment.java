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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
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
public class CalorieFragment extends Fragment  implements OnChartValueSelectedListener,View.OnTouchListener{

    BarChart calorieChart;
    TextView parentCal;
    TextView parentSpeed;
    TextView parentDistance;
    ArrayList<String> xVals;
    ArrayList<BarEntry> yVals;
    ArrayList<BarDataSet> dataSets;
    ArrayList<ExerciseItem> collections;
    String recentDate;
    static final int READ_DES = 0;
    static final int READ_ASC = 1;
    float xbefore;
    float xafter;

    int collectCount;
    int readSize ;
    int lastReadIndex;
    int firstReadIndex;

    public CalorieFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calorie, container, false);

        collectCount = 0;
        recentDate = null;
        readSize = 8;
        firstReadIndex = 0;
        lastReadIndex = 0;
        xVals = new ArrayList<String>();
        yVals = new ArrayList<BarEntry>();
        collections = new ArrayList<ExerciseItem>();

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
        calorieChart.setOnChartValueSelectedListener(this);
        calorieChart.setOnTouchListener(this);

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
                    displayClickData(email, recentDate);
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
                } else {
                    if (collections.size() < readSize) {
                        initData(collections.size());
                    } else {
                        initData(readSize);

                    }

                }
            }

            @Override
            public void onFail(int code) {

            }
        });

    }

    private void initData(int readSize){

        int readIndex = 0;
        int maxSize = 8;

        BarData data;
        xVals = new ArrayList<String>();
        yVals = new ArrayList<BarEntry>();

            for (readIndex = 0; readIndex < maxSize; readIndex++) {
                if (readIndex == collections.size())
                    break;
                xVals.add(readIndex, collections.get(collections.size() - readSize + readIndex)._id);
                yVals.add(new BarEntry((collections.get(collections.size() - readSize + readIndex).calorie * 100) / 100, readIndex));
                lastReadIndex = collections.size() - readSize + readIndex;
                firstReadIndex = collections.size() - readSize;
            }

        BarDataSet set = new BarDataSet(yVals, "칼로리");
        set.setColor(Color.parseColor("#B6E2FF"));
        set.setBarSpacePercent(getBarSpacePercent(readIndex));

        dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set);
        data = new BarData(xVals, dataSets);
        data.setValueTextSize(0f);
        calorieChart.setData(data);
        calorieChart.notifyDataSetChanged();
        calorieChart.moveViewToX(calorieChart.getData().getXVals().size() - 1);
        calorieChart.invalidate();
    }
    private void updateData(int readSize,int type) {
        int readIndex = 0;

        BarData data;
        xVals = new ArrayList<String>();
        yVals = new ArrayList<BarEntry>();
        if(type == READ_DES) {
            firstReadIndex = firstReadIndex - readSize;
            for (readIndex = 0; readIndex < readSize; readIndex++) {
                xVals.add(readIndex, collections.get(firstReadIndex + readIndex)._id);
                yVals.add(new BarEntry((collections.get(firstReadIndex  + readIndex).calorie * 100) / 100, readIndex));
                lastReadIndex = firstReadIndex + readIndex;

            }
        }
        else if(type == READ_ASC){
            firstReadIndex = lastReadIndex + 1;
            for(readIndex = 0; readIndex < readSize; readIndex++){
                lastReadIndex++;
                xVals.add(readIndex, collections.get(lastReadIndex)._id);
                yVals.add(new BarEntry((collections.get(lastReadIndex).calorie * 100) / 100, readIndex));
            }
        }
        BarDataSet set = new BarDataSet(yVals, "칼로리");
        set.setColor(Color.parseColor("#B6E2FF"));
        set.setBarSpacePercent(getBarSpacePercent(readIndex));

        dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set);
        data = new BarData(xVals, dataSets);
        data.setValueTextSize(0f);


        calorieChart.setData(data);
        calorieChart.notifyDataSetChanged();
        calorieChart.moveViewToX(calorieChart.getData().getXVals().size() - 1);
        calorieChart.invalidate();

    }

    public float getBarSpacePercent(int displaySize){
        float barSpacePercent = 50f;

        switch (displaySize){
            case 1:
                barSpacePercent = 92f;
                break;
            case 2:
                barSpacePercent = 87f;
                break;
            case 3:
                barSpacePercent = 82f;
                break;
            case 4:
                barSpacePercent = 77f;
                break;
            case 5:
                barSpacePercent = 72f;
                break;
            case 6:
                barSpacePercent = 67f;
                break;
            case 7:
                barSpacePercent = 62f;
                break;
            default:
                barSpacePercent = 50f;

        }
        return barSpacePercent;
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
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

        String email = PropertyManager.getInstance().getUserEmail();
        String date = calorieChart.getXValue(e.getXIndex());
        displayClickData(email,date);


    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            xbefore = event.getX();
        }
        else if(event.getAction() == MotionEvent.ACTION_UP){
            xafter = event.getX();
            if(xbefore < xafter){
                if (calorieChart.getLowestVisibleXIndex() == 0) {
                    calorieChart.animateXY(3000, 3000);

                    if(firstReadIndex > 0) {
                        if (firstReadIndex - readSize < 0) {
                            updateData(firstReadIndex, READ_DES);
                        } else {
                            updateData(readSize, READ_DES);
                        }
                    }

                }
            }
            else if(xbefore > xafter){
                if(calorieChart.getHighestVisibleXIndex() == xVals.size() - 1){
                    calorieChart.animateXY(3000, 3000);

                    if(lastReadIndex < collections.size() - 1) {
                        updateData(readSize, READ_ASC);
                    }

                }
            }
        }
        return false;
    }
}
