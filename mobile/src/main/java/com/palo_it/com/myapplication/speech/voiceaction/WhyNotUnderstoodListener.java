//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.palo_it.com.myapplication.speech.voiceaction;

import android.content.Context;

import java.util.List;

import com.palo_it.com.myapplication.R;
import root.gast.speech.voiceaction.OnNotUnderstoodListener;

public class WhyNotUnderstoodListener implements OnNotUnderstoodListener {
    private Context context;
    private boolean retry;
    private VoiceActionExecutor executor;

    public WhyNotUnderstoodListener(Context context, VoiceActionExecutor executor, boolean retry) {
        this.context = context;
        this.executor = executor;
        this.retry = retry;
    }

    public void notUnderstood(List<String> heard, int reason) {
        String prompt;
        String retryPrompt;
        switch (reason) {
            case 0:
            default:
                prompt = this.context.getResources().getString(R.string.voiceaction_unknown);
                break;
            case 1:
                prompt = this.context.getResources().getString(R.string.voiceaction_inaccurate);
                break;
            case 2:
                retryPrompt = heard.get(0);
                String promptFormat = this.context.getResources().getString(R.string.voiceaction_not_command);
                prompt = String.format(promptFormat, retryPrompt);
        }

        if (this.retry) {
            retryPrompt = this.context.getResources().getString(R.string.voiceaction_retry);
            prompt = prompt + retryPrompt;
            this.executor.reExecute(prompt);
        } else {
            this.executor.speak(prompt);
        }

    }
}
