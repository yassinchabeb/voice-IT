package com.palo_it.com.myapplication.speech.tts;

import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

public abstract class FinishedSpeakingListener extends UtteranceProgressListener {

    private static final String TAG = "UtterancePrgListener";

    @Override
    public void onStart(String utteranceId) {
        Log.d(TAG, "Started speaking...");
    }


    @Override
    public void onError(String utteranceId) {
        Log.d(TAG, "Error speaking...");
    }
}