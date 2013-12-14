package com.polysfactory.glassremote.ui;

import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class InfoPanel extends JPanel {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 40;
    private JTextArea textArea;

    public InfoPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        GroupLayout layout = new GroupLayout(this);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        setLayout(layout);

        textArea = new JTextArea();
        textArea.setBounds(5, 5, 630, 30);
        textArea.setEditable(false);

        layout.setHorizontalGroup(layout.createParallelGroup().addComponent(textArea));
        layout.setVerticalGroup(layout.createSequentialGroup().addComponent(textArea));
    }

    public void setText(String text) {
        textArea.setText(text);
    }
}
