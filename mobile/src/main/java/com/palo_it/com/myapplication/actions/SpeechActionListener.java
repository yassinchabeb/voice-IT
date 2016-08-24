package com.palo_it.com.myapplication.actions;

/**
 * Created by arielo on 8/22/16.
 */
public interface SpeechActionListener {
    void partialResult(String partialText);

    void finalResult(String finalText);
}
