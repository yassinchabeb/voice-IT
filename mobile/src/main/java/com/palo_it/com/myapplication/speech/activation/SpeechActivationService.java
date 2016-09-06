//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.palo_it.com.myapplication.speech.activation;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.util.Log;
import com.palo_it.com.myapplication.R;
import root.gast.speech.activation.SpeechActivationListener;
import root.gast.speech.activation.SpeechActivator;

import java.util.Arrays;

public class SpeechActivationService extends Service implements SpeechActivationListener {
    private static final String TAG = "SpeechActivationService";
    public static final String ACTIVATION_RESULT_INTENT_KEY = "ACTIVATION_RESULT_INTENT_KEY";
    public static final String ACTIVATION_RESULT_BROADCAST_NAME = "com.palo_it.voiceit.speech.ACTIVATION";
    public static final String ACTIVATION_STOP_INTENT_KEY = "ACTIVATION_STOP_INTENT_KEY";
    public static final int NOTIFICATION_ID = 10298;
    private boolean isStarted;
    private SpeechActivator activator;

    public void onCreate() {
        super.onCreate();
        this.isStarted = false;
    }

    public static Intent makeStartServiceIntent(Context context) {
        return new Intent(context, SpeechActivationService.class);
    }

    public static Intent makeServiceStopIntent(Context context) {
        Intent i = new Intent(context, SpeechActivationService.class);
        i.putExtra(ACTIVATION_STOP_INTENT_KEY, true);
        return i;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.hasExtra(ACTIVATION_STOP_INTENT_KEY)) {
                Log.d(TAG, "stop service intent");
                this.activated(false);
            } else if (this.isStarted) {
                if (this.isDifferentType()) {
                    Log.d(TAG, "is differnet type");
                    this.stopActivator();
                    this.startDetecting();
                } else {
                    Log.d(TAG, "already started this type");
                }
            } else {
                this.startDetecting();
            }
        }

        return 3;
    }

    private void startDetecting() {
        this.activator = this.getRequestedActivator();
        Log.d(TAG, "started: " + this.activator.getClass().getSimpleName());
        this.isStarted = true;
        this.activator.detectActivation();
        this.startForeground(NOTIFICATION_ID, this.getNotification());
    }

    private SpeechActivator getRequestedActivator() {
        return new WordActivator(this, this, getResources().getStringArray(R.array.voiceaction_wakeupphrases));
    }

    private boolean isDifferentType() {
        boolean different;
        if (this.activator == null) {
            return true;
        } else {
            SpeechActivator possibleOther = this.getRequestedActivator();
            different = !possibleOther.getClass().getName().equals(this.activator.getClass().getName());
            return different;
        }
    }

    public void activated(boolean success) {
        this.stopActivator();
        Intent intent = new Intent(ACTIVATION_RESULT_BROADCAST_NAME);
        intent.putExtra(ACTIVATION_RESULT_INTENT_KEY, success);
        this.sendBroadcast(intent);
        this.stopSelf();
    }

    public void onDestroy() {
        Log.d(TAG, "On destroy");
        super.onDestroy();
        this.stopActivator();
        this.stopForeground(true);
    }

    private void stopActivator() {
        if (this.activator != null) {
            Log.d(TAG, "stopped: " + this.activator.getClass().getSimpleName());
            this.activator.stop();
            this.isStarted = false;
        }
    }

    private Notification getNotification() {
        String message =
                "Listening for " + Arrays.toString(getResources().getStringArray(R.array.voiceaction_wakeupphrases));
        String title = "Speech Activation";
        PendingIntent pi = PendingIntent.getService(this, 0, makeServiceStopIntent(this), 0);
        Notification notification = null;
        if (VERSION.SDK_INT >= 11) {
            Builder builder = new Builder(this);
            builder.setSmallIcon(android.support.design.R.drawable.notification_template_icon_bg).setWhen(System
                    .currentTimeMillis()).setTicker(message).setContentTitle(title).setContentText(message)
                    .setContentIntent(pi);
            notification = builder.build();
        }

        return notification;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
