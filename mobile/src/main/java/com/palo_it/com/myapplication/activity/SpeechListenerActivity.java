package com.palo_it.com.myapplication.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import com.palo_it.com.myapplication.R;
import com.palo_it.com.myapplication.speech.drone.command.DroneActionLookup;
import com.palo_it.com.myapplication.speech.voiceaction.*;
import com.palo_it.com.myapplication.text.OntologySearcher;
import root.gast.speech.text.WordList;
import root.gast.speech.tts.TextToSpeechStartupListener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SpeechListenerActivity extends SpeechRecognizingAndSpeakingActivity implements
        TextToSpeechStartupListener {

    public static final String TAG = "SpeechListenerActivity";
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    public static final int MAX_RETRIES = 3;
    private TextView mText;

    private DroneExecutor executor;
    private VoiceAction droneAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_speech);
        initDialog();
    }

    @Override
    public void onSuccessfulInit(TextToSpeech tts) {
        Log.d(TAG, "successful init");
        super.onSuccessfulInit(tts);
        // Only for testing purposes: this is checked in WakeUpWordActivity
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            recreate();
        }
        executor.setTts(getTts());
        mText = (TextView) findViewById(R.id.spokenText);
        String ready = getString(R.string.ready);
        mText.setText(ready);
        executor.speak(ready);
        startRecognizing();
    }

    private void startRecognizing() {
        executor.execute(droneAction);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyRecognizer();
        Intent intent = new Intent(this, WakeUpWordActivity.class);
        startActivity(intent);
    }

    /**
     * determine if the user said the magic word and speak the result
     */
    @Override
    protected void receiveWhatWasHeard(List<String> heard, float[] confidenceScores, boolean full) {
        executor.handleReceiveWhatWasHeard(heard, confidenceScores, full);
        failureCount.set(0);
    }

    private AtomicInteger failureCount = new AtomicInteger(0);

    @Override
    protected void recognitionFailure(int errorCode) {
        super.recognitionFailure(errorCode);
        if (errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            if (failureCount.get() != MAX_RETRIES) {
                System.out.println(String.format("Haven't spoken %d times", failureCount.incrementAndGet()));
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startRecognizing();
            } else {
                finish();
            }
        }
    }

    private void initDialog() {
        if (executor == null) {
            executor = new DroneExecutor(this);
        }
        droneAction = makeDroneCommander();
    }

    private VoiceAction makeDroneCommander() {
        try {
            final VoiceActionCommand lookup =
                    new DroneActionLookup(this, executor, new OntologySearcher(getAssets().open("sumonto.owl")));
            AbstractVoiceAction voiceAction = new AbstractVoiceAction() {
                @Override
                public boolean interpret(List<String> said, float[] confidenceScores, boolean full) {
                    WordList saidWords = new WordList(said.get(0));
                    boolean understood = lookup.interpret(saidWords, confidenceScores, full);
                    if (understood) {
                        Log.d(TAG, "Command successful: " + lookup.getClass().getSimpleName());
                        startRecognizing();
                        return true;
                    } else {
                        Log.d(TAG, String.format("VoiceAction unsuccessful: %s, on: %s", this.getPrompt(),
                                full ? "FULL" : "PARTIAL"));
                        if (full) {
                            this.getNotUnderstood().notUnderstood(said, 0);
                        }
                    }
                    return false;
                }
            };
            voiceAction.setNotUnderstood(new WhyNotUnderstoodListener(this, executor, true));
            return voiceAction;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
