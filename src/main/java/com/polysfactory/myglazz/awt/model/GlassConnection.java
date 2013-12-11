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
    private GlassConnectionListener mListener;
    private GlassReaderThread mGlassReaderThread;
    private final Client mClient;
    private final Object mListenerLock = new Object();

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

    public void search() {
        try {
            mDiscoveryAgent.startInquiry(DiscoveryAgent.GIAC, mClient);
        } catch (BluetoothStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    public void setListener(GlassConnectionListener listener) {
        synchronized (mListenerLock) {
            this.mListener = listener;
        }
    }

    private class Client implements DiscoveryListener {

        @Override
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            // TODO Auto-generated method stub

            // TODO filter glass?
            System.out.println("deviceDiscovered");
            synchronized (mListenerLock) {
                if (mListener != null) {
                    mListener.onDeviceDiscovered(new Device(btDevice));
                }
            }
        }

        @Override
        public void inquiryCompleted(int discType) {
            // TODO Auto-generated method stub
            System.out.println("inquiryCompleted");
        }

        @Override
        public void serviceSearchCompleted(int transID, int respCode) {
            // TODO Auto-generated method stub
            System.out.println("serviceSearchCompleted:" + respCode);
            if (respCode == DiscoveryListener.SERVICE_SEARCH_NO_RECORDS) {
                synchronized (mListenerLock) {
                    if (mListener != null) {
                        mListener.onServiceNotFound();
                    }
                }
            } else if (respCode == DiscoveryListener.SERVICE_SEARCH_ERROR) {
                synchronized (mListenerLock) {
                    if (mListener != null) {
                        mListener.onServiceSearchError();
                    }
                }
            }
        }

        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            mConnectionURL = null;
            if (servRecord != null && servRecord.length > 0) {
                ServiceRecord service = servRecord[0];
                mConnectionURL = service.getConnectionURL(ServiceRecord.AUTHENTICATE_NOENCRYPT, false);
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
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (mListenerLock) {
                    if (mListener != null) {
                        mListener.onConnectionOpened();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(Envelope envelope) {
        try {
            GlassProtocol.writeMessage(envelope, mOutStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface GlassConnectionListener {
        public abstract void onDeviceDiscovered(Device device);

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
                        synchronized (mListenerLock) {
                            if (mListener != null && envelope != null) {
                                mListener.onReceivedEnvelope(envelope);
                            }
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
