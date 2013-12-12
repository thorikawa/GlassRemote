package com.polysfactory.myglazz.awt.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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
import com.google.glass.companion.Proto.GlassInfoRequest;
import com.polysfactory.myglazz.awt.MyGlazz;

/**
 * Handling connection with Google Glass companion service
 * 
 * @author poly
 * 
 */
public class GlassConnection {

    private static final String SECURE_UUID = "F15CC914E4BC45CE9930CB7695385850";
    private DiscoveryAgent mDiscoveryAgent;
    private String mConnectionURL;
    private StreamConnection mStreamConnection;
    private OutputStream mOutStream;
    private InputStream mInStream;
    private List<GlassConnectionListener> mListeners = new ArrayList<GlassConnection.GlassConnectionListener>();
    private GlassReaderThread mGlassReaderThread;
    private final Client mClient;

    public GlassConnection() {
        LocalDevice localDevice;
        try {
            localDevice = LocalDevice.getLocalDevice();
            mDiscoveryAgent = localDevice.getDiscoveryAgent();
        } catch (BluetoothStateException e) {
            e.printStackTrace();
        }
        mClient = new Client();
    }

    public List<Device> getBondedDevices() {
        RemoteDevice[] devices = mDiscoveryAgent.retrieveDevices(DiscoveryAgent.PREKNOWN);
        List<Device> list = new ArrayList<Device>();
        for (RemoteDevice remoteDevice : devices) {
            list.add(new Device(remoteDevice));
        }
        return list;
    }

    public void scan() {
        try {
            mDiscoveryAgent.startInquiry(DiscoveryAgent.GIAC, mClient);
        } catch (BluetoothStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void cancelScan() {
        mDiscoveryAgent.cancelInquiry(mClient);
    }

    public void connect(Device device) {
        UUID[] uuidSet = new UUID[1];
        uuidSet[0] = new UUID(SECURE_UUID, false);
        try {
            mDiscoveryAgent.searchServices(null, uuidSet, device.getRemoteDevice(), mClient);
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
        if (mStreamConnection != null) {
            try {
                mStreamConnection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerListener(GlassConnectionListener glassConnectionListener) {
        if (glassConnectionListener == null) {
            return;
        }
        synchronized (mListeners) {
            final int size = mListeners.size();
            for (int i = 0; i < size; i++) {
                GlassConnectionListener listener = mListeners.get(i);
                if (listener == glassConnectionListener) {
                    return;
                }
            }
            this.mListeners.add(glassConnectionListener);
        }
    }

    public void unregisterListener(GlassConnectionListener glassConnectionListener) {
        if (glassConnectionListener == null) {
            return;
        }
        synchronized (mListeners) {
            final int size = mListeners.size();
            for (int i = 0; i < size; i++) {
                GlassConnectionListener listener = mListeners.get(i);
                if (listener == glassConnectionListener) {
                    mListeners.remove(i);
                    break;
                }
            }
        }
    }

    private class Client implements DiscoveryListener {

        @Override
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            // TODO Auto-generated method stub

            // TODO filter glass?
            if (MyGlazz.DEBUG) {
                System.out.println("deviceDiscovered");
            }
            synchronized (mListeners) {
                for (GlassConnectionListener listener : mListeners) {
                    listener.onDeviceDiscovered(new Device(btDevice));
                }
            }
        }

        @Override
        public void inquiryCompleted(int discType) {
            // TODO Auto-generated method stub
            if (MyGlazz.DEBUG) {
                System.out.println("inquiryCompleted");
            }
            synchronized (mListeners) {
                for (GlassConnectionListener listener : mListeners) {
                    listener.onDeviceScanCompleted();
                }
            }
        }

        @Override
        public void serviceSearchCompleted(int transID, int respCode) {
            // TODO Auto-generated method stub
            if (MyGlazz.DEBUG) {
                System.out.println("serviceSearchCompleted:" + respCode);
            }
            if (respCode == DiscoveryListener.SERVICE_SEARCH_NO_RECORDS) {
                synchronized (mListeners) {
                    for (GlassConnectionListener listener : mListeners) {
                        listener.onServiceNotFound();
                    }
                }
            } else if (respCode == DiscoveryListener.SERVICE_SEARCH_ERROR) {
                synchronized (mListeners) {
                    for (GlassConnectionListener listener : mListeners) {
                        listener.onServiceSearchError();
                    }
                }
            }
        }

        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            mConnectionURL = null;
            if (servRecord == null && servRecord.length <= 0) {
                System.err.println("invalid service discovered");
                return;
            }

            ServiceRecord service = servRecord[0];
            mConnectionURL = service.getConnectionURL(ServiceRecord.AUTHENTICATE_NOENCRYPT, false);
            if (MyGlazz.DEBUG) {
                System.out.println(mConnectionURL);
            }

            try {
                mStreamConnection = (StreamConnection) Connector.open(mConnectionURL);
                mOutStream = mStreamConnection.openOutputStream();
                mInStream = mStreamConnection.openInputStream();
                mGlassReaderThread = new GlassReaderThread();
                mGlassReaderThread.start();

                // handshaking
                Envelope envelope = CompanionMessagingUtil.newEnvelope();
                envelope.timezoneC2G = TimeZone.getDefault().getID();
                write(envelope);

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // handshaking
                Envelope envelope2 = CompanionMessagingUtil.newEnvelope();
                GlassInfoRequest glassInfoRequest = new GlassInfoRequest();
                glassInfoRequest.requestBatteryLevel = true;
                glassInfoRequest.requestStorageInfo = true;
                glassInfoRequest.requestDeviceName = true;
                envelope2.glassInfoRequestC2G = glassInfoRequest;
                write(envelope2);

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (mListeners) {
                    for (GlassConnectionListener listener : mListeners) {
                        listener.onConnectionOpened();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(Envelope envelope) {
        try {
            if (MyGlazz.DEBUG) {
                System.out.println("write:" + envelope);
            }
            GlassProtocol.writeMessage(envelope, mOutStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface GlassConnectionListener {
        public abstract void onDeviceDiscovered(Device device);

        public abstract void onDeviceScanCompleted();

        public abstract void onServiceSearchError();

        public abstract void onReceivedEnvelope(Envelope envelope);

        public abstract void onServiceNotFound();

        public abstract void onConnectionOpened();
    }

    private class GlassReaderThread extends Thread {
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Envelope envelope = (Envelope) GlassProtocol.readMessage(new Envelope(), mInStream);
                        if (MyGlazz.DEBUG) {
                            System.out.println("read:" + envelope);
                        }
                        if (envelope != null) {
                            synchronized (mListeners) {
                                for (GlassConnectionListener listener : mListeners) {
                                    listener.onReceivedEnvelope(envelope);
                                }
                            }
                        }
                    } catch (InterruptedIOException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (MyGlazz.DEBUG) {
                    System.out.println("Reader thread finished.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    };

}
