package com.safering.safebike.exercisereport;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.text.DecimalFormat;

public class MyYAxisValueFormatter implements YAxisValueFormatter {

    private DecimalFormat mFormat;
    private int type;
    public static int CHART_CALORIE = 1;
    public static int CHART_DISTANCE = 2;
    public static int CHART_SPEED = 3;

    public MyYAxisValueFormatter(int charType) {
        mFormat = new DecimalFormat();
        type = charType;
    }

    @Override
    public String getFormattedValue(float value, YAxis yAxis) {
        if(type == CHART_CALORIE) {
            return mFormat.format(value) + " kcal";
        }
        else if(type == CHART_DISTANCE){
            return mFormat.format(Math.round(value)) + " m";
        }
        else {
            return mFormat.format(value) + " m/s";
        }
    }

}
