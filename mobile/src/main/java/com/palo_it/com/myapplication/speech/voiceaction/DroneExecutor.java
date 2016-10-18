package com.palo_it.com.myapplication.speech.voiceaction;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;
import com.palo_it.com.myapplication.R;
import com.palo_it.com.myapplication.activity.SpeechRecognizingActivity;
import com.palo_it.com.myapplication.drone.*;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_PILOTING_POSTURE_TYPE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;

public class DroneExecutor extends VoiceActionExecutor implements DroneReadyListener {


    private final DroneService droneController = DroneService.getInstance();
    private final Activity activity;
    private ProgressDialog progressDialog;
    private JSDrone drone;
    private TextToSpeech tts;
    String name = "NULL";

    public DroneExecutor(SpeechRecognizingActivity speech) {
        super(speech);
        this.activity = speech;
        if (drone == null) {
            progressDialog = new ProgressDialog(speech, R.style.AppCompatAlertDialogStyle);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Waiting for drone to come online...");
            progressDialog.show();
        }
        droneController.initDiscoveryService(speech, this);
    }


    @Override
    public void setTts(TextToSpeech tts) {
        this.tts = tts;
        super.setTts(tts);
    }

    @Override
    public void speak(String toSay) {
        super.speak(toSay);
        while (tts.isSpeaking()) ;
    }




    public void doAction(String action, String message) {


        if (action != null) {
            switch (action.toUpperCase()) {
                case "MYNAMEIS":
                    name = "monsieur";
                    action = "Enchanté monsieur";
                    message = "";
                    //break;
                case "WHATSMYNAME":
                    if (name.equals("NULL"))
                    action = "Je ne sais pas";
                    else
                    action = "Vous vous zappelez " + name;
                    message = "";
                    //break;
            }
        }


        speak(action);
        Toast.makeText(activity, action + message , Toast.LENGTH_LONG).show();
        JSDrone.ACTIONS actionEnum = getOrNull(JSDrone.ACTIONS.class, action.toUpperCase());
        if (actionEnum != null) {
            if (actionEnum.equals(JSDrone.ACTIONS.STOP)) {
                activity.finish();
            } else {
                new AsyncTask<JSDrone.ACTIONS, Void, Void>() {
                    @Override
                    protected Void doInBackground(JSDrone.ACTIONS... params) {
                        if (drone != null) {
                            for (JSDrone.ACTIONS action : params) {
                                if (action != null) {
                                    drone.doSomething(action);
                                }
                            }
                        }
                        return null;
                    }
                }.execute(actionEnum);
            }
        }
    }

    @Override
    public void onDroneReady(JSDrone drone) {
        this.drone = drone;
        this.drone.addListener(new JSDroneListenerBase() {
        });
        final Handler handler = new Handler(activity.getMainLooper());
        drone.setAsyncListener(new JSDroneStatusListener() {
            @Override
            public void asyncReceiver(Runnable task) {
                handler.post(task);
            }
        });
        if (doConnectDrone()) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public boolean doConnectDrone() {
        return this.drone.connect();
    }

    @Override
    public boolean isDroneConnected() {
        return drone.getConnectionState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING);
    }

    private <T extends Enum<T>> T getOrNull(Class<T> enumClass, String enumValue) {
        try {
            return Enum.valueOf(enumClass, enumValue);
        } catch (IllegalArgumentException e) {
            //not found
            Log.d(TAG, "Drone command not found: " + enumValue);
            return null;
        }
    }
}
