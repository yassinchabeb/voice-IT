package com.palo_it.com.myapplication.parrot;

import android.content.*;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.android.internal.util.Predicate;
import com.palo_it.com.myapplication.drone.JSDrone;
import com.palo_it.com.myapplication.drone.JSDroneStatusListener;
import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.util.List;

import static com.palo_it.com.myapplication.activity.StartSpeechActivity.TAG;

/**
 * Starting point to controll the Parrot SUMO Drone! :)
 */
public class ParrotController implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate {

    private ARDiscoveryService mArdiscoveryService;
    private ServiceConnection mArdiscoveryServiceConnection;

    static {
        ARSDK.loadSDKLibs();
    }

    private static final ParrotController INSTANCE = new ParrotController();
    private AppCompatActivity context;
    private ARDiscoveryServicesDevicesListUpdatedReceiver receiver;
    private Predicate<JSDrone> droneListener;
    private JSDrone jsDrone;

    public static ParrotController getInstance() {
        return INSTANCE;
    }

    public void initDiscoveryService(AppCompatActivity context, Predicate<JSDrone> droneListener) {
        this.context = context;
        this.droneListener = droneListener;
        if (jsDrone != null) {
            droneListener.apply(jsDrone);
            return;
        }
        // create the service connection
        if (mArdiscoveryServiceConnection == null) {
            mArdiscoveryServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mArdiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();
                    startDiscovery();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mArdiscoveryService = null;
                }
            };
        }

        if (mArdiscoveryService == null) {
            // if the discovery service doesn't exists, bind to it
            Intent intent = new Intent(context, ARDiscoveryService.class);
            context.getApplicationContext().bindService(intent, mArdiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // if the discovery service already exists, start discovery
            startDiscovery();
        }
        registerReceivers();
    }

    private void startDiscovery() {
        if (mArdiscoveryService != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mArdiscoveryService.start();
                }
            }).start();
        }
    }

    private void registerReceivers() {
        receiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(this);
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(context.getApplicationContext());
        localBroadcastMgr.registerReceiver(receiver,
                new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }

    @Override
    public void onServicesDevicesListUpdated() {
        Log.d(TAG, "onServicesDevicesListUpdated ...");

        if (mArdiscoveryService != null) {
            List<ARDiscoveryDeviceService> deviceList = mArdiscoveryService.getDeviceServicesArray();
            // Do what you want with the device list
            for (ARDiscoveryDeviceService service : deviceList) {
                ARDISCOVERY_PRODUCT_ENUM productID = ARDiscoveryService.getProductFromProductID(service.getProductID());
                if (productID == ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_JS) {
                    jsDrone = new JSDrone(service);
                    droneListener.apply(jsDrone);
                }
            }
        }
    }

    private void unregisterReceivers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(context.getApplicationContext());
        localBroadcastMgr.unregisterReceiver(receiver);
    }

    public void closeServices() {
        Log.d(TAG, "closeServices ...");
        if (mArdiscoveryService != null) {
            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    mArdiscoveryService.stop();
                    context.getApplicationContext().unbindService(mArdiscoveryServiceConnection);
                    unregisterReceivers();
                    mArdiscoveryService = null;
                    jsDrone = null;
                    return null;
                }
            }.execute();
        }
    }

}
