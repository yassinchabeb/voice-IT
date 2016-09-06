/*
 * Copyright 2012 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palo_it.com.myapplication.speech.activation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import com.palo_it.com.myapplication.R;
import com.palo_it.com.myapplication.speech.text.match.SoundsLikeWordMatcher;
import root.gast.speech.SpeechRecognitionUtil;
import root.gast.speech.activation.SpeechActivationListener;
import root.gast.speech.activation.SpeechActivator;

import java.util.List;

/**
 * Uses direct speech recognition to activate when the user speaks
 * one of the target words
 *
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class WordActivator implements SpeechActivator, RecognitionListener {
    private static final String TAG = "WordActivator";
    public static final String LANGUAGE = "fr-FR";

    private Context context;
    private SpeechRecognizer recognizer;
    private SoundsLikeWordMatcher matcher;

    private SpeechActivationListener resultListener;

    public WordActivator(Context context, SpeechActivationListener resultListener, String... targetWords) {
        this.context = context;
        this.matcher = new SoundsLikeWordMatcher(targetWords);
        this.resultListener = resultListener;
    }

    @Override
    public void detectActivation() {
        recognizeSpeechDirectly();
    }

    private void recognizeSpeechDirectly() {
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        // accept partial results if they come
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANGUAGE);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, LANGUAGE);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, LANGUAGE);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        SpeechRecognitionUtil.recognizeSpeechDirectly(context, recognizerIntent, this, getSpeechRecognizer());
    }

    public void stop() {
        if (getSpeechRecognizer() != null) {
            getSpeechRecognizer().stopListening();
            getSpeechRecognizer().cancel();
            getSpeechRecognizer().destroy();
        }
    }

    @Override
    public void onResults(Bundle results) {
        Log.d(TAG, "full results");
        receiveResults(results, true);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d(TAG, "partial results");
        receiveResults(partialResults, false);
    }

    /**
     * common method to process any results bundle from {@link SpeechRecognizer}
     */
    private void receiveResults(Bundle results, boolean full) {
        if ((results != null) && results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
            List<String> heard = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            Log.d(TAG, heard.toString());
            float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
            receiveWhatWasHeard(heard, scores, full);
        } else {
            Log.d(TAG, "no results");
        }
    }

    private void receiveWhatWasHeard(List<String> heard, float[] scores, boolean full) {
        boolean heardTargetWord = false;
        // find the target phrase
        String searchedKeyword = context.getResources().getString(R.string.voiceaction_wakeupkey);
        for (String possible : heard) {
            if (!possible.isEmpty() && possible.contains(searchedKeyword)) {
                String fromKeyword = possible.substring(possible.indexOf(searchedKeyword));
                if (matcher.isIn(fromKeyword)) {
                    heardTargetWord = true;
                    break;
                }
            }
        }

        if (heardTargetWord) {
            Log.d(TAG, "HEARD IT!");
            resultListener.activated(true);

        } else if (full) {
            Log.d(TAG, "Didn't hear it! Continue...");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recognizeSpeechDirectly();
        }
    }

    @Override
    public void onError(int errorCode) {
        Log.d(TAG, "in onError");
        if ((errorCode == SpeechRecognizer.ERROR_NO_MATCH) || (errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
            stop();
            Log.d(TAG, "didn't recognize anything");
            // keep going
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recognizeSpeechDirectly();
        } else {
            Log.d(TAG, "FAILED " + SpeechRecognitionUtil.diagnoseErrorCode(errorCode));
        }
    }

    /**
     * lazy initialize the speech recognizer
     */
    private SpeechRecognizer getSpeechRecognizer() {
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        }
        return recognizer;
    }

    // other unused methods from RecognitionListener...

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "ready for speech " + params);
    }

    @Override
    public void onEndOfSpeech() {
    }

    /**
     * @see android.speech.RecognitionListener#onBeginningOfSpeech()
     */
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
    }
}
