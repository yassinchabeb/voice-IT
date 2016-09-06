package com.palo_it.com.myapplication.drone;

import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;

/**
 * Created by arielo on 8/22/16.
 */
public interface JSDroneListener {

    String TAG = "JSDroneListener";

    /**
     * Called when the connection to the drone changes
     * Called in the main thread
     *
     * @param state the state of the drone
     */
    void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state);

    /**
     * Called when the battery charge changes
     * Called in the main thread
     *
     * @param batteryPercentage the battery remaining (in percent)
     */
    void onBatteryChargeChanged(int batteryPercentage);

    /**
     * Called when a picture is taken
     * Called on a separate thread
     *
     * @param error ERROR_OK if picture has been taken, otherwise describe the error
     */
    void onPictureTaken(ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error);

    /**
     * Called when audio state received
     * Called on a separate thread
     *
     * @param inputEnabled  true if the audio stream input is enabled else false
     * @param outputEnabled true if the audio stream output is enabled else false
     */
    void onAudioStateReceived(boolean inputEnabled, boolean outputEnabled);

    /**
     * Called when the video decoder should be configured
     * Called on a separate thread
     *
     * @param codec the codec to configure the decoder with
     */
    void configureDecoder(ARControllerCodec codec);

    /**
     * Called when a video frame has been received
     * Called on a separate thread
     *
     * @param frame the video frame
     */
    void onFrameReceived(ARFrame frame);

    /**
     * Called when the audio decoder should be configured
     * Called on a separate thread
     *
     * @param codec the codec to configure the decoder with
     */
    void configureAudioDecoder(ARControllerCodec codec);

    /**
     * Called when a audio frame has been received
     * Called on a separate thread
     *
     * @param frame the audio frame
     */
    void onAudioFrameReceived(ARFrame frame);

    /**
     * Called before medias will be downloaded
     * Called in the main thread
     *
     * @param nbMedias the number of medias that will be downloaded
     */
    void onMatchingMediasFound(int nbMedias);

    /**
     * Called each time the progress of a download changes
     * Called in the main thread
     *
     * @param mediaName the name of the media
     * @param progress  the progress of its download (from 0 to 100)
     */
    void onDownloadProgressed(String mediaName, int progress);

    /**
     * Called when a media download has ended
     * Called in the main thread
     *
     * @param mediaName the name of the media
     */
    void onDownloadComplete(String mediaName);
}
