//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.palo_it.com.myapplication.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.palo_it.com.myapplication.speech.activation.WordActivator;

import java.util.Locale;

import root.gast.speech.SpeechRecognitionUtil;
import root.gast.speech.tts.TextToSpeechInitializer;
import root.gast.speech.tts.TextToSpeechStartupListener;

public abstract class SpeechRecognizingAndSpeakingActivity extends SpeechRecognizingActivity implements
        TextToSpeechStartupListener {
    private static final String TAG = "SRASpeakingActivity";
    private TextToSpeechInitializer ttsInit;
    private TextToSpeech tts;

    public SpeechRecognizingAndSpeakingActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.init();
    }

    private void init() {
        this.deactivateUi();
        this.ttsInit = new TextToSpeechInitializer(this, Locale.forLanguageTag(WordActivator.LANGUAGE), this);
    }

    public void onSuccessfulInit(TextToSpeech tts) {
        Log.d(TAG, "successful init");
        this.tts = tts;
        this.activateUi();
        this.setTtsListener();
    }

    private void setTtsListener() {
        int listenerResult;
        if (VERSION.SDK_INT >= 15) {
            listenerResult = this.tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                public void onDone(String utteranceId) {
                    SpeechRecognizingAndSpeakingActivity.this.onDone(utteranceId);
                }

                public void onError(String utteranceId) {
                    SpeechRecognizingAndSpeakingActivity.this.onError(utteranceId);
                }

                public void onStart(String utteranceId) {
                    SpeechRecognizingAndSpeakingActivity.this.onStart(utteranceId);
                }
            });
            if (listenerResult != 0) {
                Log.e(TAG, "failed to add utterance progress listener");
            }
        } else {
            listenerResult = this.tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
                public void onUtteranceCompleted(String utteranceId) {
                    SpeechRecognizingAndSpeakingActivity.this.onDone(utteranceId);
                }
            });
            if (listenerResult != 0) {
                Log.e(TAG, "failed to add utterance completed listener");
            }
        }

    }

    public void onDone(String utteranceId) {
    }

    public void onError(String utteranceId) {
    }

    public void onStart(String utteranceId) {
    }

    public void onFailedToInit() {
        OnClickListener onClickOk = this.makeOnFailedToInitHandler();
        AlertDialog a =
                (new Builder(this)).setTitle("Error").setMessage("Unable to create text to speech").setNeutralButton
                        ("Ok", onClickOk).create();
        a.show();
    }

    public void onRequireLanguageData() {
        OnClickListener onClickOk = this.makeOnClickInstallDialogListener();
        OnClickListener onClickCancel = this.makeOnFailedToInitHandler();
        AlertDialog a = (new Builder(this)).setTitle("Error").setMessage(
                "Requires Language data to proceed, would you like " +
                        "to install?").setPositiveButton("Ok", onClickOk).setNegativeButton("Cancel", onClickCancel)
                .create();
        a.show();
    }

    public void onWaitingForLanguageData() {
        OnClickListener onClickWait = this.makeOnFailedToInitHandler();
        OnClickListener onClickInstall = this.makeOnClickInstallDialogListener();
        AlertDialog a = (new Builder(this)).setTitle("Info").setMessage("Please wait for the language data to finish " +
                "installing and try again.").setNegativeButton("Wait", onClickWait).setPositiveButton("Retry",
                onClickInstall).create();
        a.show();
    }

    private OnClickListener makeOnClickInstallDialogListener() {
        return new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SpeechRecognizingAndSpeakingActivity.this.ttsInit.installLanguageData();
            }
        };
    }

    private OnClickListener makeOnFailedToInitHandler() {
        return new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SpeechRecognizingAndSpeakingActivity.this.finish();
            }
        };
    }

    protected void deactivateUi() {
        Log.d(TAG, "deactivate ui");
    }

    protected void activateUi() {
        Log.d(TAG, "activate ui");
    }

    protected void speechNotAvailable() {
        OnClickListener onClickOk = this.makeOnFailedToInitHandler();
        AlertDialog a = (new Builder(this)).setTitle("Error").setMessage(
                "This device does not support speech recognition. " +
                        "Click ok to quit.").setPositiveButton("Ok", onClickOk).create();
        a.show();
    }

    protected void directSpeechNotAvailable() {
        Log.d(TAG, "Direct Speeck not available!!");
    }

    protected void languageCheckResult(String languageToUse) {
    }

    protected void recognitionFailure(int errorCode) {
        String message = SpeechRecognitionUtil.diagnoseErrorCode(errorCode);
        Log.d(TAG, "speech error: " + message);
    }

    protected TextToSpeech getTts() {
        return this.tts;
    }

    protected void onDestroy() {
        if (this.getTts() != null) {
            this.getTts().shutdown();
        }

        super.onDestroy();
    }
}
