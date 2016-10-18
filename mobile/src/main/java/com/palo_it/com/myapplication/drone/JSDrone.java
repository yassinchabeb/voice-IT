package com.palo_it.com.myapplication.drone;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.palo_it.com.myapplication.R;
import com.parrot.arsdk.arcommands.*;
import com.parrot.arsdk.arcontroller.*;
import com.parrot.arsdk.ardiscovery.*;
import com.parrot.arsdk.arsal.ARNativeData;
import com.parrot.arsdk.arutils.ARUtilsException;
import com.parrot.arsdk.arutils.ARUtilsFtpConnection;
import com.parrot.arsdk.arutils.ARUtilsManager;

import java.util.ArrayList;
import java.util.List;

public class JSDrone {
    private static final String TAG = "JSDrone";
    private String name = "NULL";

    private static final int DEVICE_PORT = 21;
    private JSDroneStatusListener asyncListener;

    private int audioStreamBitField;

    public void setAsyncListener(JSDroneStatusListener asyncListener) {
        this.asyncListener = asyncListener;
    }

    public enum JumpingStyle {
        HIGH, LONG, MAX
    }

    private final List<JSDroneListener> mJSDroneListeners;

    private ARDeviceController mDeviceController;
    //    private SDCardModule mSDCardModule;
    private ARCONTROLLER_DEVICE_STATE_ENUM mState;
    private String mCurrentRunId;
    private ARDISCOVERY_PRODUCT_ENUM mProductType;

    public JSDrone(@NonNull ARDiscoveryDeviceService deviceService) {
        mJSDroneListeners = new ArrayList<>();
        mState = ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED;
        // if the product type of the deviceService match with the types supported
        mProductType = ARDiscoveryService.getProductFromProductID(deviceService.getProductID());
        ARDISCOVERY_PRODUCT_FAMILY_ENUM family = ARDiscoveryService.getProductFamily(mProductType);
        if (ARDISCOVERY_PRODUCT_FAMILY_ENUM.ARDISCOVERY_PRODUCT_FAMILY_JS.equals(family)) {
            ARDiscoveryDevice discoveryDevice = createDiscoveryDevice(deviceService, mProductType);
            if (discoveryDevice != null) {
                mDeviceController = createDeviceController(discoveryDevice);
            }
            try {
                String productIP = ((ARDiscoveryDeviceNetService) (deviceService.getDevice())).getIp();

                ARUtilsManager ftpListManager = new ARUtilsManager();
                ARUtilsManager ftpQueueManager = new ARUtilsManager();

                ftpListManager.initWifiFtp(productIP, DEVICE_PORT, ARUtilsFtpConnection.FTP_ANONYMOUS, "");
                ftpQueueManager.initWifiFtp(productIP, DEVICE_PORT, ARUtilsFtpConnection.FTP_ANONYMOUS, "");

//                mSDCardModule = new SDCardModule(ftpListManager, ftpQueueManager);
//                mSDCardModule.addListener(mSDCardModuleListener);
            } catch (ARUtilsException e) {
                Log.e(TAG, "Exception", e);
            }

        } else {
            Log.e(TAG, "DeviceService type is not supported by JSDrone");
        }
    }

    //region Listener functions
    public void addListener(JSDroneListener JSDroneListener) {
        mJSDroneListeners.add(JSDroneListener);
    }

    public void removeListener(JSDroneListener JSDroneListener) {
        mJSDroneListeners.remove(JSDroneListener);
    }
    //endregion Listener

    /**
     * Connect to the drone
     *
     * @return true if operation was successful.
     * Returning true doesn't mean that device is connected.
     * You can be informed of the actual connection through {@link JSDroneListener#onDroneConnectionChanged}
     */
    public boolean connect() {
        boolean success = false;
        if ((mDeviceController != null) &&
                (ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED.equals(mState))) {
            ARCONTROLLER_ERROR_ENUM error = mDeviceController.start();
            if (error == ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
                success = true;
            }
        }
        return success;
    }

    /**
     * Disconnect from the drone
     *
     * @return true if operation was successful.
     * Returning true doesn't mean that device is disconnected.
     * You can be informed of the actual disconnection through {@link JSDroneListener#onDroneConnectionChanged}
     */
    public boolean disconnect() {
        boolean success = false;
        if ((mDeviceController != null) &&
                (ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mState))) {
            ARCONTROLLER_ERROR_ENUM error = mDeviceController.stop();
            if (error == ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
                success = true;
            }
        }
        return success;
    }

    /**
     * Get the current connection state
     *
     * @return the connection state of the drone
     */
    public ARCONTROLLER_DEVICE_STATE_ENUM getConnectionState() {
        return mState;
    }

    public void takePicture() {
        if ((mDeviceController != null) &&
                (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
            // JumpingSumo (not evo) are still using old deprecated command
            if (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_JS.equals(mProductType)) {
                mDeviceController.getFeatureJumpingSumo().sendMediaRecordPicture((byte) 0);
            } else {
                mDeviceController.getFeatureJumpingSumo().sendMediaRecordPictureV2();
            }

        }
    }

    public void setAudioStreamEnabled(boolean input, boolean output) {
        byte value = (byte) ((input ? 1 : 0) | (output ? 2 : 0));

        if ((mDeviceController != null) &&
                (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
            mDeviceController.getFeatureCommon().sendAudioControllerReadyForStreaming(value);
        }
    }

    /**
     * Set the speed of the Jumping Sumo
     * Note that {@link JSDrone#setFlag(byte)} should be set to 1 in order to take in account the speed value
     *
     * @param speed value in percentage from -100 to 100
     */
    public void setSpeed(byte speed) {
        if ((mDeviceController != null) &&
                (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
            mDeviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed(speed);
        }
    }

    /**
     * Set the speed of the Jumping Sumo
     * Note that {@link JSDrone#setFlag(byte)} should be set to 1 in order to take in account the speed value
     */
    public void doJump(JumpingStyle style) {
        if ((mDeviceController != null) &&
                (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
            switch (style) {
                case HIGH:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsJump
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_HIGH);
                    break;
                case LONG:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsJump
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_LONG);
                    break;
                case MAX:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsJump
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_MAX);
            }
        }
    }

    /**
     * Set the turn angle of the Jumping Sumo
     * Note that {@link JSDrone#setFlag(byte)} should be set to 1 in order to take in account the turn value
     *
     * @param turn value in percentage from -100 to 100
     */
    public void setTurn(byte turn) {
        if ((mDeviceController != null) &&
                (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
            mDeviceController.getFeatureJumpingSumo().setPilotingPCMDTurn(turn);
        }
    }

    /**
     * Take in account or not the speed and turn values
     *
     * @param flag 1 if the speed and turn values should be used, 0 otherwise
     */
    public void setFlag(byte flag) {
        if ((mDeviceController != null) &&
                (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
            mDeviceController.getFeatureJumpingSumo().setPilotingPCMDFlag(flag);
        }
    }

    /**
     * Download the last flight medias
     * Uses the run id to download all medias related to the last flight
     * If no run id is available, download all medias of the day
     */
    public void getLastFlightMedias() {
        String runId = mCurrentRunId;
//        if ((runId != null) && !runId.isEmpty()) {
//            mSDCardModule.getFlightMedias(runId);
//        } else {
//            Log.e(TAG, "RunID not available, fallback to the day's medias");
//            mSDCardModule.getTodaysFlightMedias();
//        }
    }

    public void cancelGetLastFlightMedias() {
//        mSDCardModule.cancelGetFlightMedias();
    }


    public void doATourAndComeBack() {
        if (mDeviceController != null) {
            mDeviceController.getFeatureCommon().sendAnimationsStartAnimation
                    (ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM
                            .ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_HEADLIGHTS_BLINK);
        }
    }

    private ARDiscoveryDevice createDiscoveryDevice(@NonNull ARDiscoveryDeviceService service,
                                                    ARDISCOVERY_PRODUCT_ENUM productType) {
        ARDiscoveryDevice device = null;
        try {
            device = new ARDiscoveryDevice();
            ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();
            device.initWifi(productType, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService
                    .getPort());

        } catch (ARDiscoveryException e) {
            Log.e(TAG, "Exception", e);
            Log.e(TAG, "Error: " + e.getError());
        }

        return device;
    }

    private ARDeviceController createDeviceController(@NonNull ARDiscoveryDevice discoveryDevice) {
        ARDeviceController deviceController = null;
        try {
            deviceController = new ARDeviceController(discoveryDevice);
            deviceController.addListener(mDeviceControllerListener);
            deviceController.addStreamListener(mStreamListener);
            deviceController.addAudioStreamListener(mAudioStreamListener);
        } catch (ARControllerException e) {
            Log.e(TAG, "Exception", e);
        }

        return deviceController;
    }

    public void sendStreamingFrame(ARNativeData data) {
        if ((mDeviceController != null) &&
                (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
            mDeviceController.sendStreamingFrame(data);
        }
    }

    public boolean hasOutputVideoStream() {
        boolean res = false;

        if ((mDeviceController != null) &&
                (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
            try {
                res = mDeviceController.hasOutputVideoStream();
            } catch (ARControllerException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    public boolean hasOutputAudioStream() {
        boolean res = false;

        if (mDeviceController != null) {
            try {
                res = mDeviceController.hasOutputAudioStream();
            } catch (ARControllerException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    public boolean hasInputAudioStream() {
        boolean res = false;

        if (mDeviceController != null) {
            try {
                res = mDeviceController.hasInputAudioStream();
            } catch (ARControllerException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    //region notify listener block
    private void notifyConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
        List<JSDroneListener> listenersCpy = new ArrayList<>(mJSDroneListeners);
        for (JSDroneListener droneListener : listenersCpy) {
            droneListener.onDroneConnectionChanged(state);
        }
    }

    private void notifyBatteryChanged(int battery) {
        List<JSDroneListener> listenersCpy = new ArrayList<>(mJSDroneListeners);
        for (JSDroneListener droneListener : listenersCpy) {
            droneListener.onBatteryChargeChanged(battery);
        }
    }

    private void notifyPictureTaken(ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {
        List<JSDroneListener> listenersCpy = new ArrayList<>(mJSDroneListeners);
        for (JSDroneListener droneListener : listenersCpy) {
            droneListener.onPictureTaken(error);
        }
    }

    private void notifyAudioState(boolean inputEnabled, boolean outputEnabled) {
        List<JSDroneListener> listenersCpy = new ArrayList<>(mJSDroneListeners);
        for (JSDroneListener droneListener : listenersCpy) {
            droneListener.onAudioStateReceived(inputEnabled, outputEnabled);
        }
    }

    private void notifyConfigureDecoder(ARControllerCodec codec) {
        List<JSDroneListener> listenersCpy = new ArrayList<>(mJSDroneListeners);
        for (JSDroneListener JSDroneListener : listenersCpy) {
            JSDroneListener.configureDecoder(codec);
        }
    }

    private void notifyFrameReceived(ARFrame frame) {
        List<JSDroneListener> listenersCpy = new ArrayList<>(mJSDroneListeners);
        for (JSDroneListener JSDroneListener : listenersCpy) {
            JSDroneListener.onFrameReceived(frame);
        }
    }

    private void notifyConfigureAudioDecoder(ARControllerCodec codec) {
        List<JSDroneListener> listenersCpy = new ArrayList<>(mJSDroneListeners);
        for (JSDroneListener JSDroneListener : listenersCpy) {
            JSDroneListener.configureAudioDecoder(codec);
        }
    }

    private void notifyAudioFrameReceived(ARFrame frame) {
        List<JSDroneListener> listenersCpy = new ArrayList<>(mJSDroneListeners);
        for (JSDroneListener JSDroneListener : listenersCpy) {
            JSDroneListener.onAudioFrameReceived(frame);
        }
    }

    private void notifyMatchingMediasFound(int nbMedias) {
        List<JSDroneListener> listenersCpy = new ArrayList<>(mJSDroneListeners);
        for (JSDroneListener JSDroneListener : listenersCpy) {
            JSDroneListener.onMatchingMediasFound(nbMedias);
        }
    }

    private void notifyDownloadProgressed(String mediaName, int progress) {
        List<JSDroneListener> listenersCpy = new ArrayList<>(mJSDroneListeners);
        for (JSDroneListener JSDroneListener : listenersCpy) {
            JSDroneListener.onDownloadProgressed(mediaName, progress);
        }
    }

    private void notifyDownloadComplete(String mediaName) {
        List<JSDroneListener> listenersCpy = new ArrayList<>(mJSDroneListeners);
        for (JSDroneListener JSDroneListener : listenersCpy) {
            JSDroneListener.onDownloadComplete(mediaName);
        }
    }
    //endregion notify listener block

//    private final SDCardModule.Listener mSDCardModuleListener = new SDCardModule.Listener() {
//        @Override
//        public void onMatchingMediasFound(final int nbMedias) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    notifyMatchingMediasFound(nbMedias);
//                }
//            });
//        }
//
//        @Override
//        public void onDownloadProgressed(final String mediaName, final int progress) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    notifyDownloadProgressed(mediaName, progress);
//                }
//            });
//        }
//
//        @Override
//        public void onDownloadComplete(final String mediaName) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    notifyDownloadComplete(mediaName);
//                }
//            });
//        }
//    };

    private final ARDeviceControllerListener mDeviceControllerListener = new ARDeviceControllerListener() {
        @Override
        public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState,
                                   ARCONTROLLER_ERROR_ENUM error) {
            mState = newState;
            if (ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mState)) {
                mDeviceController.getFeatureJumpingSumo().sendMediaStreamingVideoEnable((byte) 1);
            } else if (ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED.equals(mState)) {
//                mSDCardModule.cancelGetFlightMedias();
            }
            asyncListener.asyncReceiver(new Runnable() {
                @Override
                public void run() {
                    notifyConnectionChanged(mState);
                }
            });
        }

        @Override
        public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM
                newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error) {
        }

        @Override
        public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM
                commandKey, ARControllerDictionary elementDictionary) {
            // if event received is the battery update
            if ((commandKey ==
                    ARCONTROLLER_DICTIONARY_KEY_ENUM
                            .ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED) &&
                    (elementDictionary != null)) {
                ARControllerArgumentDictionary<Object> args =
                        elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    final int battery =
                            (Integer) args.get(ARFeatureCommon
                                    .ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT);
                    asyncListener.asyncReceiver(new Runnable() {
                        @Override
                        public void run() {
                            notifyBatteryChanged(battery);
                        }
                    });
                }
            }
            // if event received is the picture notification
            else if ((commandKey ==
                    ARCONTROLLER_DICTIONARY_KEY_ENUM
                            .ARCONTROLLER_DICTIONARY_KEY_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED) &&
                    (elementDictionary != null)) {
                ARControllerArgumentDictionary<Object> args =
                        elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    final ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error =
                            ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM.getFromValue(
                                    (Integer) args.get(ARFeatureJumpingSumo
                                            .ARCONTROLLER_DICTIONARY_KEY_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR));
                    asyncListener.asyncReceiver(new Runnable() {
                        @Override
                        public void run() {
                            notifyPictureTaken(error);
                        }
                    });
                }
            }
            // if event received is the run id
            else if ((commandKey ==
                    ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_RUNSTATE_RUNIDCHANGED) &&
                    (elementDictionary != null)) {
                ARControllerArgumentDictionary<Object> args =
                        elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    final String runID =
                            (String) args.get(ARFeatureCommon
                                    .ARCONTROLLER_DICTIONARY_KEY_COMMON_RUNSTATE_RUNIDCHANGED_RUNID);
                    asyncListener.asyncReceiver(new Runnable() {
                        @Override
                        public void run() {
                            mCurrentRunId = runID;
                        }
                    });
                }
            }
            // if event received is the audio state notification
            else if ((commandKey ==
                    ARCONTROLLER_DICTIONARY_KEY_ENUM
                            .ARCONTROLLER_DICTIONARY_KEY_COMMON_AUDIOSTATE_AUDIOSTREAMINGRUNNING) &&
                    (elementDictionary != null)) {
                ARControllerArgumentDictionary<Object> args =
                        elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    final int state =
                            (Integer) args.get(ARFeatureCommon
                                    .ARCONTROLLER_DICTIONARY_KEY_COMMON_AUDIOSTATE_AUDIOSTREAMINGRUNNING_RUNNING);
                    final boolean inputEnabled = (state & 0x01) != 0;
                    final boolean outputEnabled = (state & 0x02) != 0;

                    asyncListener.asyncReceiver(new Runnable() {
                        @Override
                        public void run() {
                            notifyAudioState(inputEnabled, outputEnabled);
                        }
                    });
                }
            }
        }
    };

    private final ARDeviceControllerStreamListener mStreamListener = new ARDeviceControllerStreamListener() {
        @Override
        public ARCONTROLLER_ERROR_ENUM configureDecoder(ARDeviceController deviceController, final ARControllerCodec
                codec) {
            notifyConfigureDecoder(codec);
            return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
        }

        @Override
        public ARCONTROLLER_ERROR_ENUM onFrameReceived(ARDeviceController deviceController, final ARFrame frame) {
            notifyFrameReceived(frame);
            return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
        }

        @Override
        public void onFrameTimeout(ARDeviceController deviceController) {
        }
    };

    private final ARDeviceControllerStreamListener mAudioStreamListener = new ARDeviceControllerStreamListener() {
        @Override
        public ARCONTROLLER_ERROR_ENUM configureDecoder(ARDeviceController deviceController, final ARControllerCodec
                codec) {
            notifyConfigureAudioDecoder(codec);
            return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
        }

        @Override
        public ARCONTROLLER_ERROR_ENUM onFrameReceived(ARDeviceController deviceController, final ARFrame frame) {
            notifyAudioFrameReceived(frame);
            return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
        }

        @Override
        public void onFrameTimeout(ARDeviceController deviceController) {
        }
    };

    public enum ACTIONS {
        FORWARD, BACKWARD, LEFT, RIGHT, STOP, ANIMATIONSLONGJUMP, ANIMATIONSTAP, ANIMATIONSSPIRAL,
        ANIMATIONSSPINTOPOSTURE, ANIMATIONSSPINJUMP, ANIMATIONSSPIN, ANIMATIONSSLOWSHAKE, ANIMATIONSSLALOM,
        ANIMATIONSONDULATION, ANIMATIONSMETRONOME, ANIMATIONSHIGHJUMP, UNKNOWN, TRIANGLE, SQUARE, RETURN, FOLLOW, SAYRIGHT, SAYLEFT, WHATSMYNAME, MYNAMEIS
    }

    public void doSomething(ACTIONS action) {
        if (action != null) {
            switch (action) {
                case LEFT:
                    doTurn((byte) -10, 1000);
                    break;
                case RIGHT:
                    doTurn((byte) 10, 1000);
                    break;
                case FORWARD:
                    doMove((byte) 50, 1000);
                    break;
                case BACKWARD:
                    doMove((byte) -50, 1000);
                    break;
                case RETURN:
                    doMove((byte) -120, 1000);
                    doMove((byte) -120, 1000);
                    doMove((byte) -120, 1000);
                    break;
                case FOLLOW:
                    doMove((byte) 120, 1000 );
                    doMove((byte) 120, 1000 );
                    doMove((byte) 120, 1000 );
                    break;
                case MYNAMEIS:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsSimpleAnimation
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_TAP);
                    break;
                case WHATSMYNAME:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsSimpleAnimation
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_TAP);
                    break;
                case SAYRIGHT:
                    doTurn((byte) 10, 1000);
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsSimpleAnimation
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_TAP);
                    break;
                case SAYLEFT:
                    doTurn((byte) -10, 1000);
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsSimpleAnimation
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_TAP);
                    break;
                case STOP:
                    mDeviceController.getFeatureCommon().sendAnimationsStopAllAnimations();
                    break;
                case ANIMATIONSLONGJUMP:
                    mDeviceController.getFeatureJumpingSumo().sendPilotingPosture
                            (ARCOMMANDS_JUMPINGSUMO_PILOTING_POSTURE_TYPE_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_PILOTING_POSTURE_TYPE_JUMPER);
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsJump
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_LONG);
                    break;
                case ANIMATIONSSPIN:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsSimpleAnimation
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_SPIN);
                    break;
                case ANIMATIONSSPIRAL:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsSimpleAnimation
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_SPIRAL);
                    break;
                case ANIMATIONSTAP:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsSimpleAnimation
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_TAP);
                    break;
                case ANIMATIONSSLALOM:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsSimpleAnimation
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_SLALOM);
                    break;
                case ANIMATIONSSLOWSHAKE:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsSimpleAnimation
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_SLOWSHAKE);
                    break;
                case ANIMATIONSMETRONOME:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsSimpleAnimation
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_METRONOME);
                    break;
                case ANIMATIONSONDULATION:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsSimpleAnimation
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ONDULATION);
                    break;
                case ANIMATIONSHIGHJUMP:
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsJump
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_HIGH);
                    break;
                case ANIMATIONSSPINJUMP:
                    mDeviceController.getFeatureJumpingSumo().sendPilotingPosture
                            (ARCOMMANDS_JUMPINGSUMO_PILOTING_POSTURE_TYPE_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_PILOTING_POSTURE_TYPE_JUMPER);
                    mDeviceController.getFeatureJumpingSumo().sendAnimationsSimpleAnimation
                            (ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_ENUM
                                    .ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_SIMPLEANIMATION_ID_SPINJUMP);
                    break;
                case TRIANGLE:
                    drawTriangle();
                    break;
                case SQUARE:
                    drawSquare();
                case UNKNOWN:
                    mDeviceController.getFeatureCommon().sendAnimationsStartAnimation
                            (ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM
                                    .ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_HEADLIGHTS_BLINK);
            }
        }
    }

    private void drawSquare() {
        doMove((byte) 50, 888);
        doTurn((byte) 15, 888);
        doMove((byte) 50, 888);
        doTurn((byte) 15, 888);
        doMove((byte) 50, 888);
        doTurn((byte) 15, 888);
        doMove((byte) 50, 888);
    }

    private void drawTriangle() {
        doMove((byte) 50, 1000);
        doTurn((byte) 16.67, 1000);
        doMove((byte) 50, 1000);
        doTurn((byte) 16.67, 1000);
        doMove((byte) 50, 1000);
    }

    private void doTurn(byte turn, int animationTime) {
        try {
            setTurn(turn);
            setFlag((byte) 1);
            Thread.sleep(animationTime);
            setTurn((byte) 0);
            setFlag((byte) 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doMove(byte speed, int animationTime) {
        try {
            setSpeed(speed);
            setFlag((byte) 1);
            Thread.sleep(animationTime);
            setSpeed((byte) 0);
            setFlag((byte) 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
