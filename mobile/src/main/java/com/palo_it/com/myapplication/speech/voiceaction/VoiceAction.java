//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.palo_it.com.myapplication.speech.voiceaction;

import java.util.List;
import root.gast.speech.voiceaction.OnNotUnderstoodListener;

public interface VoiceAction {
    boolean interpret(List<String> var1, float[] var2, boolean full);

    String getPrompt();

    void setPrompt(String var1);

    String getSpokenPrompt();

    void setSpokenPrompt(String var1);

    boolean hasSpokenPrompt();

    void setNotUnderstood(OnNotUnderstoodListener var1);

    OnNotUnderstoodListener getNotUnderstood();

    float getMinConfidenceRequired();

    float getNotACommandConfidenceThreshold();

    void setNotACommandConfidenceThreshold(float var1);

    float getInaccurateConfidenceThreshold();

    void setInaccurateConfidenceThreshold(float var1);
}
