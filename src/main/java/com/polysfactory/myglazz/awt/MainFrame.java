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
package com.polysfactory.myglazz.awt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.RemoteDevice;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;

import com.google.glass.companion.Proto.Envelope;
import com.polysfactory.myglazz.awt.GlassConnection.GlassConnectionListener;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements GlassConnectionListener {

    private static final String TITLE = "MyGlazz";

    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 360;

    private static final String EXT_PNG = "png";

    private MainPanel mPanel;
    private JPopupMenu mPopupMenu;

    private Preferences mPrefs;
    private int mRawImageWidth = DEFAULT_WIDTH;
    private int mRawImageHeight = DEFAULT_HEIGHT;
    private boolean mPortrait = true;
    private double mZoom = 1.0;

    private RemoteDevice mDevice;

    private GlassConnection mGlassConnection;

    public MainFrame(String[] args) {
        initialize(args);
        mGlassConnection = new GlassConnection();
    }

    public void selectDevice() {
        mGlassConnection.setListener(null);
        mGlassConnection.close();

        RemoteDevice[] mDevices = null;
        try {
            mDevices = mGlassConnection.getBondedDevices();

            if (mDevices != null) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < mDevices.length; i++) {
                    String name = mDevices[i].getFriendlyName(false) + ":" + mDevices[i].getBluetoothAddress();
                    list.add(name);
                }
                SelectDeviceDialog dialog = new SelectDeviceDialog(this, true, list);
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
                if (dialog.isOK()) {
                    int selectedIndex = dialog.getSelectedIndex();
                    if (selectedIndex >= 0) {
                        mDevice = mDevices[selectedIndex];
                        mGlassConnection.setListener(this);
                        mGlassConnection.connect(mDevice);
                        setImage(null);
                    }
                }
            }
        } catch (BluetoothStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOrientation(boolean portrait) {
        if (mPortrait != portrait) {
            mPortrait = portrait;
            savePrefs();
            updateSize();
        }
    }

    public void setZoom(double zoom) {
        if (mZoom != zoom) {
            mZoom = zoom;
            savePrefs();
            updateSize();
        }
    }

    public void saveImage() {
        BufferedImage inImage = mPanel.getImage();
        if (inImage != null) {
            BufferedImage outImage = new BufferedImage((int) (inImage.getWidth() * mZoom),
                    (int) (inImage.getHeight() * mZoom), inImage.getType());
            if (outImage != null) {
                AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(mZoom, mZoom),
                        AffineTransformOp.TYPE_BILINEAR);
                op.filter(inImage, outImage);
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public String getDescription() {
                        return "*." + EXT_PNG;
                    }

                    @Override
                    public boolean accept(File f) {
                        String ext = f.getName().toLowerCase();
                        return (ext.endsWith("." + EXT_PNG));
                    }
                });
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = fileChooser.getSelectedFile();
                        String path = file.getAbsolutePath();
                        if (!path.endsWith("." + EXT_PNG)) {
                            file = new File(path + "." + EXT_PNG);
                        }
                        ImageIO.write(outImage, EXT_PNG, file);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Failed to save a image.", "Save Image",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    public void about() {
        AboutDialog dialog = new AboutDialog(this, true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void updateSize() {
        int width;
        int height;
        if (mPortrait) {
            width = mRawImageWidth;
            height = mRawImageHeight;
        } else {
            width = mRawImageHeight;
            height = mRawImageWidth;
        }
        Insets insets = getInsets();
        int newWidth = (int) (width * mZoom) + insets.left + insets.right;
        int newHeight = (int) (height * mZoom) + insets.top + insets.bottom;

        // Known bug
        // If new window size is over physical window size, cannot update window
        // size...
        // FIXME
        if ((getWidth() != newWidth) || (getHeight() != newHeight)) {
            setSize(newWidth, newHeight);
        }
    }

    public void setImage(BufferedImage fbImage) {
        if (fbImage != null) {
            mRawImageWidth = fbImage.getWidth();
            mRawImageHeight = fbImage.getHeight();
        }
        mPanel.setFBImage(fbImage);
        updateSize();
    }

    private void initialize(String[] args) {

        initializePrefs();
        initializeFrame();
        initializePanel();
        initializeMenu();
        initializeActionMap();

        addMouseListener(mMouseListener);
        addWindowListener(mWindowListener);

        pack();
        setImage(null);
    }

    private void savePrefs() {
        if (mPrefs != null) {
            mPrefs.putInt("PrefVer", 1);
            mPrefs.putBoolean("Portrait", mPortrait);
            mPrefs.putDouble("Zoom", mZoom);
        }
    }

    private void initializePrefs() {
        mPrefs = Preferences.userNodeForPackage(this.getClass());
        if (mPrefs != null) {
            int prefVer = mPrefs.getInt("PrefVer", 1);
            if (prefVer == 1) {
                mPortrait = mPrefs.getBoolean("Portrait", true);
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
        mPanel = new MainPanel();
        add(mPanel);
    }

    private void initializeMenu() {
        mPopupMenu = new JPopupMenu();

        initializeSelectDeviceMenu();
        mPopupMenu.addSeparator();
        initializeOrientationMenu();
        initializeZoomMenu();
        mPopupMenu.addSeparator();
        initializeSaveImageMenu();
        mPopupMenu.addSeparator();
        initializeAbout();

        mPopupMenu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
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

    private void initializeOrientationMenu() {
        JMenu menuOrientation = new JMenu("Orientation");
        menuOrientation.setMnemonic(KeyEvent.VK_O);
        mPopupMenu.add(menuOrientation);

        ButtonGroup buttonGroup = new ButtonGroup();

        // Portrait
        JRadioButtonMenuItem radioButtonMenuItemPortrait = new JRadioButtonMenuItem("Portrait");
        radioButtonMenuItemPortrait.setMnemonic(KeyEvent.VK_P);
        radioButtonMenuItemPortrait.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setOrientation(true);
            }
        });
        if (mPortrait) {
            radioButtonMenuItemPortrait.setSelected(true);
        }
        buttonGroup.add(radioButtonMenuItemPortrait);
        menuOrientation.add(radioButtonMenuItemPortrait);

        // Landscape
        JRadioButtonMenuItem radioButtonMenuItemLandscape = new JRadioButtonMenuItem("Landscape");
        radioButtonMenuItemLandscape.setMnemonic(KeyEvent.VK_L);
        radioButtonMenuItemLandscape.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setOrientation(false);
            }
        });
        if (!mPortrait) {
            radioButtonMenuItemLandscape.setSelected(true);
        }
        buttonGroup.add(radioButtonMenuItemLandscape);
        menuOrientation.add(radioButtonMenuItemLandscape);
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

    private void initializeActionMap() {
        AbstractAction actionSelectDevice = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectDevice();
            }
        };
        AbstractAction actionPortrait = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setOrientation(true);
            }
        };
        AbstractAction actionLandscape = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setOrientation(false);
            }
        };
        AbstractAction actionZoom50 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setZoom(0.5);
            }
        };
        AbstractAction actionZoom75 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setZoom(0.75);
            }
        };
        AbstractAction actionZoom100 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setZoom(1.0);
            }
        };
        AbstractAction actionZoom150 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setZoom(1.5);
            }
        };
        AbstractAction actionZoom200 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setZoom(2.0);
            }
        };
        AbstractAction actionSaveImage = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                saveImage();
            }
        };
        AbstractAction actionAbout = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                about();
            }
        };

        JComponent targetComponent = getRootPane();
        InputMap inputMap = targetComponent.getInputMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "Select Device");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK), "Portrait");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "Landscape");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK), "50%");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_DOWN_MASK), "75%");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK), "100%");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK), "150%");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK), "200%");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "Save Image");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), "About MyGlazz");

        targetComponent.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);

        targetComponent.getActionMap().put("Select Device", actionSelectDevice);
        targetComponent.getActionMap().put("Portrait", actionPortrait);
        targetComponent.getActionMap().put("Landscape", actionLandscape);
        targetComponent.getActionMap().put("Select Device", actionSelectDevice);
        targetComponent.getActionMap().put("50%", actionZoom50);
        targetComponent.getActionMap().put("75%", actionZoom75);
        targetComponent.getActionMap().put("100%", actionZoom100);
        targetComponent.getActionMap().put("150%", actionZoom150);
        targetComponent.getActionMap().put("200%", actionZoom200);
        targetComponent.getActionMap().put("Save Image", actionSaveImage);
        targetComponent.getActionMap().put("About MyGlazz", actionAbout);
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
            // TODO
            // if (mADB != null) {
            // mADB.terminate();
            // }
        }

        public void windowClosed(WindowEvent arg0) {
        }

        public void windowActivated(WindowEvent arg0) {
        }
    };

    public class MainPanel extends JPanel {
        private BufferedImage mImage;

        public MainPanel() {
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (mImage != null) {
                int srcWidth;
                int srcHeight;
                int dstWidth;
                int dstHeight;
                if (mPortrait) {
                    srcWidth = mRawImageWidth;
                    srcHeight = mRawImageHeight;
                } else {
                    srcWidth = mRawImageHeight;
                    srcHeight = mRawImageWidth;
                }
                dstWidth = (int) (srcWidth * mZoom);
                dstHeight = (int) (srcHeight * mZoom);
                if (mZoom == 1.0) {
                    g.drawImage(mImage, 0, 0, dstWidth, dstHeight, 0, 0, srcWidth, srcHeight, null);
                } else {
                    Image image = mImage.getScaledInstance(dstWidth, dstHeight, Image.SCALE_SMOOTH);
                    if (image != null) {
                        g.drawImage(image, 0, 0, dstWidth, dstHeight, 0, 0, dstWidth, dstHeight, null);
                    }
                }
            }
        }

        public void setFBImage(BufferedImage image) {
            mImage = image;
            repaint();
        }

        public BufferedImage getImage() {
            return mImage;
        }
    }

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
}
