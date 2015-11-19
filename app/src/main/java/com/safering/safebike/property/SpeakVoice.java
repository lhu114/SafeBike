package com.safering.safebike.property;

import android.speech.tts.TextToSpeech;

/**
 * Created by Tacademy on 2015-11-19.
 */
public class SpeakVoice implements TextToSpeech.OnInitListener{
    TextToSpeech tts;
    public SpeakVoice(){
        tts = new TextToSpeech(MyApplication.getContext(),this);
    }


    @Override
    public void onInit(int status) {

    }

    public void translate(String des){
        tts.speak(des,TextToSpeech.QUEUE_FLUSH,null);

    }

    public void close(){
        tts.shutdown();
    }
}
