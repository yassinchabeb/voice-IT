package com.palo_it.com.myapplication.drone;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import com.palo_it.com.myapplication.R;
import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;

public class JSDroneListenerImpl implements JSDroneListener {

    private final Activity activity;
    private final ProgressDialog progressDialog;

    public JSDroneListenerImpl(Activity activity) {
        this.activity = activity;
        progressDialog = new ProgressDialog(activity, R.style.AppCompatAlertDialogStyle);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Waiting for drone to come online...");
        progressDialog.show();
    }

    @Override
    public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
        progressDialog.setMessage("Drone online!");
        progressDialog.dismiss();
    }

    @Override
    public void onBatteryChargeChanged(int batteryPercentage) {
        Log.d(TAG, "Drone battery update: " + batteryPercentage);
    }

    @Override
    public void onPictureTaken(ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {

    }

    @Override
    public void onAudioStateReceived(boolean inputEnabled, boolean outputEnabled) {

    }

    @Override
    public void configureDecoder(ARControllerCodec codec) {

    }

    @Override
    public void onFrameReceived(ARFrame frame) {

    }

    @Override
    public void configureAudioDecoder(ARControllerCodec codec) {
    }

    @Override
    public void onAudioFrameReceived(ARFrame frame) {

    }

    @Override
    public void onMatchingMediasFound(int nbMedias) {

    }

    @Override
    public void onDownloadProgressed(String mediaName, int progress) {

    }

    @Override
    public void onDownloadComplete(String mediaName) {

    }
}