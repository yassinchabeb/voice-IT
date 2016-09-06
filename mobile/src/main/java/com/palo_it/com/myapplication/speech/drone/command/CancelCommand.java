/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palo_it.com.myapplication.speech.drone.command;

import android.content.Context;
import com.palo_it.com.myapplication.R;
import com.palo_it.com.myapplication.speech.voiceaction.DroneExecutor;
import root.gast.speech.text.WordList;
import root.gast.speech.text.match.WordMatcher;
import root.gast.speech.voiceaction.VoiceActionCommand;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class CancelCommand implements VoiceActionCommand {
    private DroneExecutor executor;
    private String cancelledPrompt;
    private WordMatcher matcher;

    public CancelCommand(Context context, DroneExecutor executor) {
        this.executor = executor;
        this.cancelledPrompt = context.getResources().getString(R.string.stop_recognizing);
        this.matcher = new WordMatcher(cancelledPrompt);
    }

    @Override
    public boolean interpret(WordList heard, float[] confidence) {
        boolean understood = false;
        if (matcher.isIn(heard.getWords())) {
            executor.speak(cancelledPrompt);
//            executor.stopRecognizing();
            understood = true;
        }
        return understood;
    }
}
