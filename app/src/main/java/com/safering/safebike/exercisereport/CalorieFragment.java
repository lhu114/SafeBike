package com.safering.safebike.exercisereport;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
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
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

import org.w3c.dom.Text;

import java.lang.annotation.Annotation;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class CalorieFragment extends Fragment {
    protected BarChart calorieChart;


    TextView parentCal;
    TextView parentSpeed;
    TextView parentDistance;
    ArrayList<String> xVals = new ArrayList<String>();
    ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
    ArrayList<BarDataSet> dataSets;
    String lastDate = "";
    ArrayList<ExerciseItem> values = new ArrayList<ExerciseItem>();
    BarDataSet set;
    Button moveRecent;
    int total = 0;
    //sCalendar cal;

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

        calorieChart = (BarChart) view.findViewById(R.id.chart_calorie);
        calorieChart.setVerticalScrollBarEnabled(true);
        calorieChart.setDrawBarShadow(false);
        calorieChart.setDrawGridBackground(false);
        calorieChart.setDrawHighlightArrow(false);
        calorieChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        calorieChart.getXAxis().setDrawGridLines(false);
        calorieChart.getAxisLeft().setDrawGridLines(false);
        calorieChart.getAxisRight().setDrawGridLines(false);
        calorieChart.getAxisRight().setDrawLabels(false);
        //calorieChart.setScaleMinima(2f, 1f);
        calorieChart.setVerticalScrollBarEnabled(false);
        calorieChart.setGridBackgroundColor(Color.parseColor("#B6E2FF"));


        calorieChart.setGridBackgroundColor(Color.parseColor("#FFFFFF"));
        calorieChart.setBorderColor(Color.parseColor("#000000"));
     //   calorieChart

        //calorieChart.set
        setFont();
        //cal = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(cal.getTime());

        requestData(today);

        calorieChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {


            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                if (calorieChart.getLowestVisibleXIndex() == 0) {

                    calorieChart.animateX(2000);
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
                    Log.i("requestDate",dateFormat.format(cal.getTime()));
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

        calorieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {


              //  Toast.makeText(getContext(),"ClickDate :"+ calorieChart.getXValue(  e.getXIndex()) + "", Toast.LENGTH_SHORT).show();


                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                String email = PropertyManager.getInstance().getUserEmail();
                String date = calorieChart.getXValue(  e.getXIndex());



                NetworkManager.getInstance().getDayExerciseRecord(getContext(), email, date, new NetworkManager.OnResultListener<ExerciseDayResult>() {
                    @Override
                    public void onSuccess(ExerciseDayResult result) {
                        if (result.workout.size() > 0) {
                            parentCal.setText(String.valueOf(result.workout.get(0).calorie) + " kcal");
                            parentSpeed.setText(String.valueOf(result.workout.get(0).speed) + " km/h");
                            parentDistance.setText(String.valueOf(result.workout.get(0).road) + " km");
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
/*
        moveRecent = (Button)view.findViewById(R.id.btn_move_calorie);
        moveRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calorieChart.moveViewToX(calorieChart.getData().getXVals().size() - 1);
            }
        })*/;


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();


    }
    float bscale = 1f;


    private void requestData(String today) {
        int count = 0;
        int range = 0;
        ArrayList<String> dateList = new ArrayList<String>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //2015-12-23
        Calendar cal = Calendar.getInstance();

        dateList.add(today);

/*
        Date d = dateFormat.parse(getDate);
        cal.setTime(d);
        cal.add(Calendar.DATE, -1);
        */
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


        /*    cal.add(Calendar.DATE,-1);
            String date = dateFormat.format(cal.getTime());
            dateList.add(date);
            Log.i("date : ",date);
*/
        }

        String email = PropertyManager.getInstance().getUserEmail();
        NetworkManager.getInstance().getExerciseRecord(getContext(), email,dateList, new NetworkManager.OnResultListener<ExcerciseResult>() {
            @Override
            public void onSuccess(ExcerciseResult result) {

                ArrayList<ExerciseItem> values = result.workoutlist;

                BarData data;
                int count = result.workoutlist.size();
               // Toast.makeText(getContext(),"count : " + count,Toast.LENGTH_SHORT).show();
                Log.i("count : ",count + "");
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        xVals.add(result.workoutlist.get(i)._id);
                        yVals.add(new BarEntry(result.workoutlist.get(i).calorie, total+i));
                    }
                   total += count;
                    BarDataSet set = new BarDataSet(yVals, "Distance");
                    dataSets = new ArrayList<BarDataSet>();
                    dataSets.add(set);

                    data = new BarData(xVals, dataSets);
                    data.setValueTextSize(10f);
                    calorieChart.setData(data);
   /*                 calorieChart.getXValue(0);
   */


                    calorieChart.notifyDataSetChanged();
                    calorieChart.moveViewToX(calorieChart.getData().getXVals().size() - 1);
                    calorieChart.invalidate();

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
