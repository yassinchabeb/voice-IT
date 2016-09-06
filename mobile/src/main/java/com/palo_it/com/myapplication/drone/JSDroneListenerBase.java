package com.palo_it.com.myapplication.drone;

import android.util.Log;
import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;

public abstract class JSDroneListenerBase implements JSDroneListener {
    @Override
    public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
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
