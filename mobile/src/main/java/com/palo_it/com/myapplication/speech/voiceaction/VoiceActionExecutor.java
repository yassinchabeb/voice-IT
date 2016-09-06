//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.palo_it.com.myapplication.speech.voiceaction;

import android.content.Intent;
import android.os.Build.VERSION;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import com.palo_it.com.myapplication.activity.SpeechRecognizingActivity;
import com.palo_it.com.myapplication.speech.activation.WordActivator;

import java.util.List;

public class VoiceActionExecutor {
    private static final String TAG = "VoiceActionExecutor";
    private VoiceAction active;
    private SpeechRecognizingActivity speech;
    private final String EXECUTE_AFTER_SPEAK = "EXECUTE_AFTER_SPEAK";
    private TextToSpeech tts;

    public VoiceActionExecutor(SpeechRecognizingActivity speech) {
        this.speech = speech;
        this.active = null;
    }

    public void setTts(TextToSpeech tts) {
        this.tts = tts;
        if (VERSION.SDK_INT >= 15) {
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                public void onDone(String utteranceId) {
                    VoiceActionExecutor.this.onDoneSpeaking(utteranceId);
                }

                public void onError(String utteranceId) {
                }

                public void onStart(String utteranceId) {
                }
            });
        } else {
            Log.d(TAG, "set utterance completed listener");
            tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
                public void onUtteranceCompleted(String utteranceId) {
                    VoiceActionExecutor.this.onDoneSpeaking(utteranceId);
                }
            });
        }

    }

    public boolean handleReceiveWhatWasHeard(List<String> heard, float[] confidenceScores, boolean full) {
        return this.active.interpret(heard, confidenceScores, full);
    }

    private void onDoneSpeaking(String utteranceId) {
        if (utteranceId.equals(EXECUTE_AFTER_SPEAK)) {
//            speech.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
            doRecognitionOnActive();
//                }
//            });
        }
    }

    public void speak(String toSay) {
        this.tts.speak(toSay, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void reExecute(String extraPrompt) {
        if (extraPrompt != null && extraPrompt.length() > 0) {
            this.tts.speak(extraPrompt, TextToSpeech.QUEUE_FLUSH, null, EXECUTE_AFTER_SPEAK);
            while (tts.isSpeaking()) ;
        } else {
            this.execute(this.getActive());
        }

    }

    public void execute(VoiceAction voiceAction) {
        if (this.tts == null) {
            throw new RuntimeException("Text to speech not initialized");
        } else {
            this.setActive(voiceAction);
            if (voiceAction.hasSpokenPrompt()) {
                Log.d(TAG, "speaking prompt: " + voiceAction.getSpokenPrompt());
                this.tts.speak(voiceAction.getSpokenPrompt(), TextToSpeech.QUEUE_FLUSH, null, EXECUTE_AFTER_SPEAK);
            } else {
                this.doRecognitionOnActive();
            }

        }
    }

    private void doRecognitionOnActive() {
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        // accept partial results if they come
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, speech.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, WordActivator.LANGUAGE);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, WordActivator.LANGUAGE);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, WordActivator.LANGUAGE);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, this.getActive().getPrompt());
        this.speech.recognize(recognizerIntent);
    }

    private VoiceAction getActive() {
        return this.active;
    }

    private void setActive(VoiceAction active) {
        this.active = active;
    }
}
