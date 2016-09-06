package com.palo_it.com.myapplication.drone;

public interface DroneReadyListener {

    String TAG = "DroneReadyListener";

    void onDroneReady(JSDrone jsDrone);

    boolean doConnectDrone();

    boolean isDroneConnected();
}
