//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.palo_it.com.myapplication.speech.voiceaction;

import android.util.Log;

import java.util.List;

import root.gast.speech.voiceaction.OnNotUnderstoodListener;

public abstract class AbstractVoiceAction implements VoiceAction, OnNotUnderstoodListener {
    private static final String TAG = "AbstractVoiceAction";
    private String prompt;
    private String spokenPrompt;
    private OnNotUnderstoodListener notUnderstood = this;
    private float minConfidenceRequired = -1.0F;
    private float notACommandConfidenceThreshold = 0.9F;
    private float inaccurateConfidenceThreshold = 0.3F;

    public AbstractVoiceAction() {
    }

    public void setMinConfidenceRequired(float minConfidenceRequired) {
        this.minConfidenceRequired = minConfidenceRequired;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public boolean hasSpokenPrompt() {
        return this.spokenPrompt != null && this.spokenPrompt.length() > 0;
    }

    public String getPrompt() {
        return this.prompt;
    }

    public void setNotUnderstood(OnNotUnderstoodListener notUnderstood) {
        this.notUnderstood = notUnderstood;
    }

    public OnNotUnderstoodListener getNotUnderstood() {
        return this.notUnderstood;
    }

    public float getMinConfidenceRequired() {
        return this.minConfidenceRequired;
    }

    public String getSpokenPrompt() {
        return this.spokenPrompt;
    }

    public void setSpokenPrompt(String prompt) {
        this.spokenPrompt = prompt;
    }

    public float getNotACommandConfidenceThreshold() {
        return this.notACommandConfidenceThreshold;
    }

    public void setNotACommandConfidenceThreshold(float notACommandConfidenceThreshold) {
        this.notACommandConfidenceThreshold = notACommandConfidenceThreshold;
    }

    public float getInaccurateConfidenceThreshold() {
        return this.inaccurateConfidenceThreshold;
    }

    public void setInaccurateConfidenceThreshold(float inaccurateConfidenceThreshold) {
        this.inaccurateConfidenceThreshold = inaccurateConfidenceThreshold;
    }

    public void notUnderstood(List<String> heard, int reason) {
        Log.d("AbstractVoiceAction", "not understood because of " + reason);
    }
}
