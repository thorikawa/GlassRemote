package com.polysfactory.myglazz.awt.model;

import java.io.IOException;

import javax.bluetooth.RemoteDevice;

/**
 * Wrapper class for Bluecove's RemoteDevice
 * 
 * @author poly
 * 
 */
public class Device {

    RemoteDevice remoteDevice;

    public Device(RemoteDevice remoteDevice) {
        this.remoteDevice = remoteDevice;
    }

    @Override
    public String toString() {
        String name = "";
        try {
            name = remoteDevice.getFriendlyName(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name + ":" + remoteDevice.getBluetoothAddress();
    }

    public RemoteDevice getRemoteDevice() {
        return remoteDevice;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Device)) {
            return false;
        }
        Device other = (Device) obj;
        return this.toString().equals(other.toString());
    }
}
