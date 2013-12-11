/*
 * Copyright (C) 2013 Poly's Factory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.polysfactory.myglazz.awt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.TimeZone;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.google.glass.companion.CompanionMessagingUtil;
import com.google.glass.companion.GlassProtocol;
import com.google.glass.companion.Proto.Envelope;
import com.google.glass.companion.Proto.ScreenShot;

public class GlassConnection {

    private static final String SECURE_UUID = "F15CC914E4BC45CE9930CB7695385850";
    private DiscoveryAgent discoveryAgent;
    private String connectionURL;
    private StreamConnection streamConnection;
    private OutputStream outStream;
    private InputStream inStream;
    private GlassConnectionListener listener;
    private GlassReaderThread mGlassReaderThread;

    public GlassConnection() {
        LocalDevice localDevice;
        try {
            localDevice = LocalDevice.getLocalDevice();
            discoveryAgent = localDevice.getDiscoveryAgent();
        } catch (BluetoothStateException e) {
            e.printStackTrace();
        }
    }

    public RemoteDevice[] getBondedDevices() {
        return discoveryAgent.retrieveDevices(DiscoveryAgent.PREKNOWN);
    }

    public void connect(RemoteDevice device) {
        UUID[] uuidSet = new UUID[1];
        uuidSet[0] = new UUID(SECURE_UUID, false);
        try {
            discoveryAgent.searchServices(null, uuidSet, device, new Client());
        } catch (BluetoothStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void close() {
        if (mGlassReaderThread != null) {
            mGlassReaderThread.interrupt();
            try {
                mGlassReaderThread.join(10000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        if (streamConnection != null) {
            try {
                streamConnection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setListener(GlassConnectionListener listener) {
        this.listener = listener;
    }

    private class Client implements DiscoveryListener {

        @Override
        public void deviceDiscovered(RemoteDevice arg0, DeviceClass arg1) {
            // TODO Auto-generated method stub
            System.out.println("deviceDiscovered");
        }

        @Override
        public void inquiryCompleted(int arg0) {
            // TODO Auto-generated method stub
            System.out.println("inquiryCompleted");
        }

        @Override
        public void serviceSearchCompleted(int transID, int respCode) {
            // TODO Auto-generated method stub
            System.out.println("serviceSearchCompleted");
        }

        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            connectionURL = null;
            if (servRecord != null && servRecord.length > 0) {
                ServiceRecord service = servRecord[0];
                connectionURL = service.getConnectionURL(ServiceRecord.AUTHENTICATE_NOENCRYPT, false);
                System.out.println(connectionURL);
            }

            try {
                streamConnection = (StreamConnection) Connector.open(connectionURL);
                outStream = streamConnection.openOutputStream();
                inStream = streamConnection.openInputStream();
                mGlassReaderThread = new GlassReaderThread();
                mGlassReaderThread.start();

                Envelope envelope = CompanionMessagingUtil.newEnvelope();
                envelope.timezoneC2G = TimeZone.getDefault().getID();
                write(envelope);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Envelope envelope2 = CompanionMessagingUtil.newEnvelope();
                ScreenShot screenShot = new ScreenShot();
                screenShot.startScreenshotRequestC2G = true;
                envelope2.screenshot = screenShot;
                write(envelope2);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(Envelope envelope) {
        try {
            GlassProtocol.writeMessage(envelope, outStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface GlassConnectionListener {
        public abstract void onReceivedEnvelope(Envelope envelope);
    }

    private class GlassReaderThread extends Thread {
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Envelope envelope = (Envelope) GlassProtocol.readMessage(new Envelope(), inStream);
                        if (listener != null && envelope != null) {
                            listener.onReceivedEnvelope(envelope);
                        }
                    } catch (InterruptedIOException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("Reader thread finished.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    };

}
