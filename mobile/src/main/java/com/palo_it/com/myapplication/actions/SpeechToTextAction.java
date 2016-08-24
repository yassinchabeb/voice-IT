package com.palo_it.com.myapplication.actions;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import com.palo_it.com.myapplication.R;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.palo_it.com.myapplication.activity.StartSpeechActivity.TAG;

public class SpeechToTextAction {

    private static ProgressDialog loadingRecognizer;

    public static SpeechRecognizer createRecognizer(Context context, SpeechActionListener resultHandler) {
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        loadingRecognizer = new ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
        loadingRecognizer.setIndeterminate(true);
        loadingRecognizer.setMessage("Starting up Voice Listener...");
        loadingRecognizer.show();
        speechRecognizer.setRecognitionListener(new SpeechListener(resultHandler));
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
//                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000);
//        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500);
//        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2500);
//            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speechRecognizer.startListening(intent);
        return speechRecognizer;
    }

    private static class SpeechListener implements RecognitionListener {
        private final SpeechActionListener textHandler;

        SpeechListener(SpeechActionListener resultHandler) {
            this.textHandler = resultHandler;
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
            loadingRecognizer.dismiss();
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
//            Log.d(TAG, "onRmsChanged");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }

        @Override
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
        }

        @Override
        public void onResults(Bundle results) {
            String str = "";
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++) {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i) + ", ";
            }
            textHandler.finalResult(str);
        }

        AtomicReference<String> partialResult = new AtomicReference<>("");

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (!data.isEmpty()) {
                String partialText = data.get(data.size() - 1).toString();
                if (!partialText.isEmpty()) {
                    String existing = partialResult.get();
                    if (!existing.isEmpty() && !existing.equals(partialText) && existing.contains(partialText)) {
                        String textDiff = existing.substring(existing.lastIndexOf(partialText));
                        if (!textDiff.isEmpty()) {
                            //Added some text!
                            partialResult.set(partialText);
                            textHandler.partialResult(partialResult.get());
                        }
                    } else if (existing.isEmpty()){
                        //Added some text!
                        partialResult.set(partialText);
                        textHandler.partialResult(partialResult.get());
                    }
                }
                Log.d(TAG, String.format("onPartialResults: %s", partialText));
            }
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }
}
