package com.polysfactory.myglazz.awt.ui;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 180;
    private ControlPanelListener mListener;

    public ControlPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        // setLayout(new CardLayout());
        // setLayout(new BorderLayout());
        setLayout(null);

        final TextArea textArea = new TextArea(100, 20);
        // text.setSize(200, 100);
        // add(text, "timeline_text");
        textArea.setBounds(10, 10, 600, 100);
        add(textArea);

        Button sendButton = new Button("Send to Timeline");
        sendButton.setBounds(10, 120, 150, 40);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mListener != null) {
                    String text = textArea.getText();
                    mListener.onSendTimeline(text);
                }
            }
        });
        add(sendButton);
    }

    public void setControlPanelListener(ControlPanelListener listener) {
        mListener = listener;
    }

    public static interface ControlPanelListener {
        public abstract void onSendTimeline(String text);
    }
}
