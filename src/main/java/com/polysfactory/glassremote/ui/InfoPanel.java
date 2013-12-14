package com.polysfactory.glassremote.ui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class InfoPanel extends JPanel {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 40;
    private JTextArea textArea;

    public InfoPanel() {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(null);

        textArea = new JTextArea();
        textArea.setBounds(5, 5, 630, 30);
        textArea.setEditable(false);
        add(textArea);
    }

    public void setText(String text) {
        textArea.setText(text);
    }
}
