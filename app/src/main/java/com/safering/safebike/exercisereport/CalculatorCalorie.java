package com.safering.safebike.exercisereport;

/**
 * Created by Tacademy on 2015-11-24.
 */
public class CalculatorCalorie {
    float coefficientArr[][] = {
            {13, 0.0650f}, {16, 0.0783f}, {19, 0.0939f}, {22, 0.113f}
            , {24, 0.124f}, {26, 0.136f}, {27, 0.149f}, {29, 0.163f}
            , {31, 0.179f}, {32, 0.196f}, {34, 0.215f}, {37, 0.259f}, {40, 0.311f}
    };
    int weight = 65;
    private static CalculatorCalorie instance;

    private CalculatorCalorie() {

    }

    public static CalculatorCalorie getInstance() {
        if (instance == null) {
            instance = new CalculatorCalorie();
        }
        return instance;
    }

    public int getCalorie(float vel, float weight, int interval){
        int khm = (int)((vel * 3600.0f)/1000.0f);
        int min = (int)Math.abs(khm - coefficientArr[0][0]);
        int index = 0;
        for(int i = 0; i < coefficientArr.length; i++){
            int diff = (int)Math.abs(khm - coefficientArr[i][0]);
            if(diff < min){
                min = diff;
                index = i;
            }
        }
        float cal = coefficientArr[index][1] * weight * interval;
        return (int)cal;


    }
}
