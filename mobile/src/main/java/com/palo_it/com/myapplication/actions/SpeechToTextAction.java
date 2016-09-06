package com.palo_it.com.myapplication.actions;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import com.palo_it.com.myapplication.R;
//import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.palo_it.com.myapplication.activity.StartSpeechActivity.TAG;

public class SpeechToTextAction {

    private static ProgressDialog loadingRecognizer;

    private static String wakeUpPhraseText;
    private static boolean isAwake = true;
    private static SpeechRecognizer speechRecognizer;
    private static Intent intent;
    private static TextView textView;

    public static SpeechRecognizer createRecognizer(AppCompatActivity activity, SpeechActionListener resultHandler, String wakeUpPhrase) {
        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(activity);
        loadingRecognizer = new ProgressDialog(activity, R.style.AppCompatAlertDialogStyle);
        loadingRecognizer.setIndeterminate(true);
        loadingRecognizer.setMessage("Starting up Voice Listener...");
        loadingRecognizer.show();
        speechRecognizer.setRecognitionListener(new SpeechListener(resultHandler));
        wakeUpPhraseText = wakeUpPhrase;
        textView = (TextView) activity.findViewById(R.id.spokenText);
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fr-FR");
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "fr-FR");
        intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
//        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000);
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

        AtomicLong timeDebug = new AtomicLong();

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
            timeDebug.set(System.currentTimeMillis());
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
                    text = "Je n'ai pas compris";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    if (isAwake) {
                        text = "Essayez encore s'il vous plaît...";
                    }
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:

            }
            try {
                speechRecognizer.stopListening();
                Thread.sleep(2000);
                speechRecognizer.startListening(intent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, text);
            textView.setText(text);
        }

        @Override
        public void onResults(Bundle results) {
            String str = "";
            partialResult.lazySet("");
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
                long now = System.currentTimeMillis();
                Log.d(TAG, "Time from beginningOfSpeech to voice recognition: " + (now - timeDebug.getAndSet(now)) + " ms");
                String partialText = data.get(data.size() - 1).toString();
                if (!partialText.isEmpty()) {
                    Log.d(TAG, String.format("onPartialResults: %s", partialText));
                    if (isAwake) {
//                        String onlyActions = StringUtils.remove(partialText, wakeUpPhraseText);
//                        if (!onlyActions.isEmpty()) {/
//                            findActions(partialText);
//                        }
                    } else {
//                        if (StringUtils.getLevenshteinDistance(partialText, wakeUpPhraseText) < 4) {
                            isAwake = true;
                            speechRecognizer.stopListening();
                            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000);
                            speechRecognizer.startListening(intent);
//                        }
                    }
                }
            }
        }

        private void findActions(String partialText) {
            String existing = partialResult.get();
//            String onlyActions = StringUtils.remove(partialText, existing);
//            if (!onlyActions.isEmpty()) {
//                if (textHandler.partialResult(onlyActions)) {
//                    speechRecognizer.stopListening();
//                }
//            }
//            partialResult.set(partialText);
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }
}
