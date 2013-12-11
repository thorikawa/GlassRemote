package com.polysfactory.myglazz.awt;

import javax.swing.SwingUtilities;

import com.polysfactory.myglazz.awt.ui.MainFrame;

public class MyGlazz {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setLocationRelativeTo(null);
                mainFrame.setVisible(true);
                mainFrame.setFocusable(true);
                mainFrame.selectDevice();
            }
        });
    }
}
