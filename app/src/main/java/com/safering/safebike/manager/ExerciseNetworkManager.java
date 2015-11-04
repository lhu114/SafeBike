package com.safering.safebike.manager;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;

/**
 * Created by Tacademy on 2015-11-04.
 */
public class ExerciseNetworkManager {
    private static ExerciseNetworkManager instance;
    public static ExerciseNetworkManager getInstance(){
        if(instance == null){
            instance = new ExerciseNetworkManager();
        }
        return instance;
    }

    AsyncHttpClient client;
    Gson gson;
}
