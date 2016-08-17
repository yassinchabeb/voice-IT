package com.palo_it.com.myapplication.actions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import com.android.internal.util.Predicate;

import java.util.ArrayList;

import static com.palo_it.com.myapplication.activity.StartSpeechActivity.TAG;

public class SpeechToTextAction {

    public static SpeechRecognizer createRecognizer(Context context, Predicate<String> resultHandler) {
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new SpeechListener(resultHandler));

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
//                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2500);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speechRecognizer.startListening(intent);
        return speechRecognizer;
    }

    private static class SpeechListener implements RecognitionListener {
        private final Predicate<String> textHandler;

        SpeechListener(Predicate<String> resultHandler) {
            this.textHandler = resultHandler;
        }

        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB) {
//            Log.d(TAG, "onRmsChanged");
        }

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }

        public void onError(int error) {
            String text = "error" + error;
            switch (error) {
                case SpeechRecognizer.ERROR_NO_MATCH:
                    text = "Didn't understand...";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    text = "You didn't speak, right...?";
            }
            Log.d(TAG, text);
            textHandler.apply(text);
        }

        public void onResults(Bundle results) {
            String str = "";
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++) {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i) + ", ";
            }
            textHandler.apply(String.format("results: %s", str));
        }

        public void onPartialResults(Bundle partialResults) {
            ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (!data.isEmpty()) {
                String partialText = data.get(0).toString();
                textHandler.apply(partialText);
                Log.d(TAG, String.format("onPartialResults: %s", partialText));
            }
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }
}
