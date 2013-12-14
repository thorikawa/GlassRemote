package com.polysfactory.glassremote.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import com.google.glass.companion.Proto.Envelope;
import com.polysfactory.glassremote.model.GlassConnection;
import com.polysfactory.glassremote.util.GlassMessagingUtil;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel {

    private GlassConnection mGlassConnection;

    public ControlPanel(GlassConnection glassConnection) {
        GroupLayout layout = new GroupLayout(this);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        setLayout(layout);

        mGlassConnection = glassConnection;

        final JTextArea textArea = new JTextArea();
        textArea.setColumns(20);
        Border border = BorderFactory.createLineBorder(Color.black);
        textArea.setBorder(border);
        add(textArea);

        JButton sendButton = new JButton("SEND_TO_TIMELINE");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textArea.getText();
                Envelope envelope = GlassMessagingUtil.createTimelineMessage(text);
                mGlassConnection.write(envelope);
            }
        });
        add(sendButton);

        final JButton swipeLeftButton = new JButton("SWIPE_LEFT");
        swipeLeftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Envelope> envelopes = GlassMessagingUtil.getSwipeLeftEvents();
                mGlassConnection.writeAsync(envelopes);
            }
        });
        final JButton swipeRightButton = new JButton("SWIPE_RIGHT");
        swipeRightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Envelope> envelopes = GlassMessagingUtil.getSwipeRightEvents();
                mGlassConnection.writeAsync(envelopes);
            }
        });
        final JButton swipeDownButton = new JButton("SWIPE_DOWN");
        swipeDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Envelope> envelopes = GlassMessagingUtil.getSwipeDownEvents();
                mGlassConnection.writeAsync(envelopes);
            }
        });

        layout.setHorizontalGroup(layout
                .createParallelGroup()
                .addGroup(
                        layout.createSequentialGroup().addComponent(swipeLeftButton).addComponent(swipeRightButton)
                                .addComponent(swipeDownButton))
                .addGroup(layout.createSequentialGroup().addComponent(textArea).addComponent(sendButton)));
        layout.setVerticalGroup(layout
                .createSequentialGroup()
                .addGroup(
                        layout.createParallelGroup().addComponent(swipeLeftButton).addComponent(swipeRightButton)
                                .addComponent(swipeDownButton))
                .addGroup(layout.createParallelGroup(Alignment.CENTER).addComponent(textArea).addComponent(sendButton)));

    }
}
