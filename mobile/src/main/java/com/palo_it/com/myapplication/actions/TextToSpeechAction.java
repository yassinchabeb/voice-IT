package com.palo_it.com.myapplication.actions;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

import static android.widget.Toast.makeText;
import static com.palo_it.com.myapplication.activity.StartSpeechActivity.TAG;

public class TextToSpeechAction implements TextToSpeech.OnInitListener {

    private static TextToSpeech textToSpeech;
    private final Context context;

    public TextToSpeechAction(Context context) {
        this.context = context;
        textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int i) {
        if (i != TextToSpeech.ERROR) {
            Log.d(TAG, "TextToSpeechAction ALIVE!");
            textToSpeech.setLanguage(Locale.FRANCE);
            textToSpeech.setVoice(textToSpeech.getDefaultVoice());
        }
    }

    public int speak(final String text) {
        makeText(context, text, Toast.LENGTH_SHORT).show();
        return textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
    }
}
