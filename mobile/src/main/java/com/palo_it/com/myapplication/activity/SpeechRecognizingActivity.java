//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.palo_it.com.myapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import root.gast.speech.LanguageDetailsChecker;
import root.gast.speech.OnLanguageDetailsListener;
import root.gast.speech.SpeechRecognitionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public abstract class SpeechRecognizingActivity extends Activity implements RecognitionListener {
    private static final String TAG = "SpeechRecognizingActy";
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    public static final int UNKNOWN_ERROR = -1;
    private SpeechRecognizer recognizer;

    public SpeechRecognizingActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean recognizerIntent = SpeechRecognitionUtil.isSpeechAvailable(this);
        if (!recognizerIntent) {
            this.speechNotAvailable();
        }

        boolean direct = SpeechRecognizer.isRecognitionAvailable(this);
        if (!direct) {
            this.directSpeechNotAvailable();
        }
    }

    protected void checkForLanguage(final Locale language) {
        OnLanguageDetailsListener andThen = new OnLanguageDetailsListener() {
            public void onLanguageDetailsReceived(LanguageDetailsChecker data) {
                String languageToUse = data.matchLanguage(language);
                SpeechRecognizingActivity.this.languageCheckResult(languageToUse);
            }
        };
        SpeechRecognitionUtil.getLanguageDetails(this, andThen);
    }

    public void recognize(final Intent recognizerIntent) {
        // This will create a popup which won't finish until it hears something... maybe we can add a timeout to it?!?
        super.startActivityForResult(recognizerIntent, VOICE_RECOGNITION_REQUEST_CODE);
        // This code executes de listener straight away without popup but I'm having multiple problems afterwards...
//        getSpeechRecognizer().startListening(recognizerIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == UNKNOWN_ERROR) {
                ArrayList<String> heard = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                float[] scores = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);
                int i;
                if (scores == null) {
                    for (i = 0; i < heard.size(); ++i) {
                        Log.d(TAG, i + ": " + heard.get(i));
                    }
                } else {
                    for (i = 0; i < heard.size(); ++i) {
                        Log.d(TAG, i + ": " + heard.get(i) + " score: " + scores[i]);
                    }
                }

                this.receiveWhatWasHeard(heard, scores, true);
            } else {
                this.recognitionFailure(-1);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected abstract void speechNotAvailable();

    protected abstract void directSpeechNotAvailable();

    protected abstract void languageCheckResult(String var1);

    protected abstract void receiveWhatWasHeard(List<String> heard, float[] scores, boolean full);

    protected abstract void recognitionFailure(int var1);

    public void recognizeDirectly(Intent recognizerIntent) {
        if (!recognizerIntent.hasExtra("calling_package")) {
            recognizerIntent.putExtra("calling_package", "com.dummy");
        }

        SpeechRecognizer recognizer = this.getSpeechRecognizer();
        recognizer.startListening(recognizerIntent);
    }

    @Override
    public void onResults(Bundle results) {
        Log.d(TAG, "full results");
        this.receiveResults(results, true);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d(TAG, "partial results");
        this.receiveResults(partialResults, false);
    }

    private void receiveResults(Bundle results, boolean full) {
        if (results != null && results.containsKey("results_recognition")) {
            ArrayList<String> heard = results.getStringArrayList("results_recognition");
            float[] scores = results.getFloatArray("confidence_scores");
            this.receiveWhatWasHeard(heard, scores, full);
        }

    }

    @Override
    public void onError(int errorCode) {
        this.recognitionFailure(errorCode);
    }

    protected void onPause() {
        destroyRecognizer();
        super.onPause();
    }

    protected void destroyRecognizer() {
        if (this.getSpeechRecognizer() != null) {
            this.getSpeechRecognizer().stopListening();
            this.getSpeechRecognizer().cancel();
            this.getSpeechRecognizer().destroy();
        }
    }

    private SpeechRecognizer getSpeechRecognizer() {
        if (this.recognizer == null) {
            this.recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            this.recognizer.setRecognitionListener(this);
        }
        return this.recognizer;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "ready for speech " + params);
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "end of speech..");
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d(TAG, "on event...");
    }

    public void onPartialResultsUnsupported(Bundle partialResults) {
        Log.d(TAG, "partial results");
        if (partialResults.containsKey("com.google.android.voicesearch.UNSUPPORTED_PARTIAL_RESULTS")) {
            String[] heard =
                    partialResults.getStringArray("com.google.android.voicesearch.UNSUPPORTED_PARTIAL_RESULTS");
            float[] scores = partialResults.getFloatArray(
                    "com.google.android.voicesearch" + ".UNSUPPORTED_PARTIAL_RESULTS_CONFIDENCE");
            this.receiveWhatWasHeard(Arrays.asList(heard), scores, false);
        } else {
            this.receiveResults(partialResults, true);
        }

    }
}