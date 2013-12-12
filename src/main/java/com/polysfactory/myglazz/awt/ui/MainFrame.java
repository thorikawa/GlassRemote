/*
 * Copyright (C) 2009-2013 adakoda
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
package com.polysfactory.myglazz.awt.ui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import com.google.glass.companion.CompanionMessagingUtil;
import com.google.glass.companion.Proto.Envelope;
import com.google.glass.companion.Proto.ScreenShot;
import com.google.googlex.glass.common.proto.TimelineNano;
import com.google.googlex.glass.common.proto.TimelineNano.TimelineItem;
import com.polysfactory.myglazz.awt.model.Device;
import com.polysfactory.myglazz.awt.model.GlassConnection;
import com.polysfactory.myglazz.awt.model.GlassConnection.GlassConnectionListener;
import com.polysfactory.myglazz.awt.ui.ControlPanel.ControlPanelListener;
import com.polysfactory.myglazz.awt.util.ImageUtil;
import com.polysfactory.myglazz.awt.util.SwingUtil;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements GlassConnectionListener, ControlPanelListener {

    private static final String TITLE = "MyGlazz";

    private ScreencastPanel mPanel;
    private JPopupMenu mPopupMenu;

    private Preferences mPrefs;
    private double mZoom = 1.0;

    private Device mDevice;

    private GlassConnection mGlassConnection;

    private DeviceDialog mDialog;

    private DefaultListModel mDeviceListModel;

    private ControlPanel mControlPanel;

    public MainFrame() {
        initializePrefs();
        initializeFrame();
        initializePanel();
        initializeMenu();

        addMouseListener(mMouseListener);
        addWindowListener(mWindowListener);

        mGlassConnection = new GlassConnection();
        mGlassConnection.setListener(this);

        pack();
        setImage(null);
    }

    public void selectDevice() {
        mGlassConnection.close();

        mDeviceListModel = SwingUtil.list2ListModel(mGlassConnection.getBondedDevices());
        // mDeviceListModel = new DefaultListModel();
        mDialog = new DeviceDialog(this, mDeviceListModel);

        mGlassConnection.search();

        mDialog.setLocationRelativeTo(this);
        mDialog.setVisible(true);
        if (mDialog.isOK()) {
            int selectedIndex = mDialog.getSelectedIndex();
            if (selectedIndex >= 0) {
                mDevice = (Device) mDeviceListModel.get(selectedIndex);
                mGlassConnection.connect(mDevice);
                setImage(null);
            }
        }
        mDeviceListModel = null;
        mDialog = null;
    }

    private void setZoom(double zoom) {
        if (mZoom != zoom) {
            mZoom = zoom;
            savePrefs();
            updateSize();
        }
    }

    private void saveImage() {
        BufferedImage inImage = mPanel.getImage();
        if (inImage != null) {
            ImageUtil.saveImage(this, inImage, mZoom);
        }
    }

    private void about() {
        AboutDialog dialog = new AboutDialog(this, true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateSize() {
        // int width = mRawImageWidth;
        // int height = mRawImageHeight;
        int width = mPanel.getWidth();
        width = Math.max(width, mControlPanel.getWidth());

        int height = mPanel.getHeight() + mControlPanel.getHeight();
        System.out.println(width + ":" + height);
        Insets insets = getInsets();
        // int newWidth = (int) (width * mZoom) + insets.left + insets.right;
        // int newHeight = (int) (height * mZoom) + insets.top + insets.bottom;
        int newWidth = width + insets.left + insets.right;
        int newHeight = height + insets.top + insets.bottom;

        // Known bug
        // If new window size is over physical window size, cannot update window
        // size...
        // FIXME
        if ((getWidth() != newWidth) || (getHeight() != newHeight)) {
            setSize(newWidth, newHeight);
        }
    }

    private void setImage(BufferedImage image) {
        mPanel.setImage(image);
        updateSize();
    }

    private void savePrefs() {
        if (mPrefs != null) {
            mPrefs.putInt("PrefVer", 1);
            mPrefs.putDouble("Zoom", mZoom);
        }
    }

    private void initializePrefs() {
        mPrefs = Preferences.userNodeForPackage(this.getClass());
        if (mPrefs != null) {
            int prefVer = mPrefs.getInt("PrefVer", 1);
            if (prefVer == 1) {
                mZoom = mPrefs.getDouble("Zoom", 1.0);
            }
        }
    }

    private void initializeFrame() {
        setTitle(TITLE);
        // setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png")));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void initializePanel() {

        mPanel = new ScreencastPanel();
        mPanel.setZoom(mZoom);
        add(mPanel, BorderLayout.NORTH);

        mControlPanel = new ControlPanel();
        add(mControlPanel, BorderLayout.SOUTH);
    }

    private void initializeMenu() {
        mPopupMenu = new JPopupMenu();

        initializeSelectDeviceMenu();
        mPopupMenu.addSeparator();
        initializeZoomMenu();
        mPopupMenu.addSeparator();
        initializeSaveImageMenu();
        mPopupMenu.addSeparator();
        initializeAbout();
    }

    private void initializeSelectDeviceMenu() {
        JMenuItem menuItemSelectDevice = new JMenuItem("Select Device...");
        menuItemSelectDevice.setMnemonic(KeyEvent.VK_D);
        menuItemSelectDevice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectDevice();
            }
        });
        mPopupMenu.add(menuItemSelectDevice);
    }

    private void initializeZoomMenu() {
        JMenu menuZoom = new JMenu("Zoom");
        menuZoom.setMnemonic(KeyEvent.VK_Z);
        mPopupMenu.add(menuZoom);

        ButtonGroup buttonGroup = new ButtonGroup();

        addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 0.1, "10%", -1, mZoom);
        addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 0.25, "25%", -1, mZoom);
        addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 0.5, "50%", KeyEvent.VK_5, mZoom);
        addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 0.75, "75%", KeyEvent.VK_7, mZoom);
        addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 1.0, "100%", KeyEvent.VK_1, mZoom);
        addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 1.5, "150%", KeyEvent.VK_0, mZoom);
        addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 2.0, "200%", KeyEvent.VK_2, mZoom);
    }

    private void addRadioButtonMenuItemZoom(JMenu menuZoom, ButtonGroup buttonGroup, final double zoom, String caption,
            int nemonic, double currentZoom) {
        JRadioButtonMenuItem radioButtonMenuItemZoom = new JRadioButtonMenuItem(caption);
        if (nemonic != -1) {
            radioButtonMenuItemZoom.setMnemonic(nemonic);
        }
        radioButtonMenuItemZoom.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setZoom(zoom);
            }
        });
        if (currentZoom == zoom) {
            radioButtonMenuItemZoom.setSelected(true);
        }
        buttonGroup.add(radioButtonMenuItemZoom);
        menuZoom.add(radioButtonMenuItemZoom);
    }

    private void initializeSaveImageMenu() {
        JMenuItem menuItemSaveImage = new JMenuItem("Save Image...");
        menuItemSaveImage.setMnemonic(KeyEvent.VK_S);
        menuItemSaveImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveImage();
            }
        });
        mPopupMenu.add(menuItemSaveImage);
    }

    private void initializeAbout() {
        JMenuItem menuItemAbout = new JMenuItem("About MyGlazz");
        menuItemAbout.setMnemonic(KeyEvent.VK_A);
        menuItemAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                about();
            }
        });
        mPopupMenu.add(menuItemAbout);
    }

    private MouseListener mMouseListener = new MouseListener() {
        public void mouseReleased(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                mPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    };

    private WindowListener mWindowListener = new WindowListener() {
        public void windowOpened(WindowEvent arg0) {
        }

        public void windowIconified(WindowEvent arg0) {
        }

        public void windowDeiconified(WindowEvent arg0) {
        }

        public void windowDeactivated(WindowEvent arg0) {
        }

        public void windowClosing(WindowEvent arg0) {
            mGlassConnection.close();
        }

        public void windowClosed(WindowEvent arg0) {
        }

        public void windowActivated(WindowEvent arg0) {
        }
    };

    @Override
    public void onReceivedEnvelope(Envelope envelope) {
        if (envelope.screenshot != null) {
            if (envelope.screenshot.screenshotBytesG2C != null) {
                InputStream in = new ByteArrayInputStream(envelope.screenshot.screenshotBytesG2C);
                try {
                    final BufferedImage image = ImageIO.read(in);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setImage(image);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDeviceDiscovered(Device device) {
        synchronized (mDeviceListModel) {
            if (mDeviceListModel != null) {
                mDeviceListModel.addElement(device);
            }
        }
    }

    @Override
    public void onServiceNotFound() {
        JOptionPane.showMessageDialog(this, "Companion service not found.", "MyGlazz", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onServiceSearchError() {
        JOptionPane.showMessageDialog(this, "Error happend while searching companion service.", "MyGlazz",
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onSendTimeline(String text) {
        Envelope envelope = CompanionMessagingUtil.newEnvelope();
        TimelineItem timelineItem = new TimelineNano.TimelineItem();
        timelineItem.title = "From MyGlazz";
        timelineItem.text = text;
        envelope.timelineItem = new TimelineItem[] { timelineItem };
        mGlassConnection.write(envelope);
    }

    @Override
    public void onConnectionOpened() {
        Envelope envelope = CompanionMessagingUtil.newEnvelope();
        ScreenShot screenShot = new ScreenShot();
        screenShot.startScreenshotRequestC2G = true;
        envelope.screenshot = screenShot;
        mGlassConnection.write(envelope);
    }
}
