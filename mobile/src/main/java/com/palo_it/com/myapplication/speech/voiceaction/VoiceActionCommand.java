//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.palo_it.com.myapplication.speech.voiceaction;

import root.gast.speech.text.WordList;

public interface VoiceActionCommand {
    boolean interpret(WordList heard, float[] score, boolean full);
}
