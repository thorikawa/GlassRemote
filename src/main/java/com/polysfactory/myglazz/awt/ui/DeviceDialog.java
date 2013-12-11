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
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

@SuppressWarnings("serial")
public class DeviceDialog extends JDialog implements ListDataListener {
    private JList mList;
    private JScrollPane mScrollPane;
    private JButton mOK;
    private JButton mCancel;

    private DefaultListModel mModel;
    private boolean mIsOK = false;
    private int mSelectedIndex = -1;

    public DeviceDialog(Frame owner, DefaultListModel model) {
        super(owner, true);

        setTitle("Select your Glass");
        setBounds(0, 0, 240, 164);
        setResizable(false);

        // create list for display name
        mModel = model;
        mModel.addListDataListener(this);
        mList = new JList(mModel);
        if (mModel.getSize() > 0) {
            mSelectedIndex = 0;
            mList.setSelectedIndex(mSelectedIndex);
        }
        mList.addMouseListener(new MouseListener() {
            public void mouseReleased(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    onOK();
                }
            }
        });

        mScrollPane = new JScrollPane(mList);
        mScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        mOK = new JButton("OK");
        mOK.setEnabled(mModel.getSize() > 0);
        mOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        mCancel = new JButton("Cancel");
        mCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        Container container1 = new Container();
        GridLayout gridLayout = new GridLayout(1, 2, 0, 0);
        container1.setLayout(gridLayout);
        container1.add(mOK);
        container1.add(mCancel);

        Container containger = getContentPane();
        containger.add(mScrollPane, BorderLayout.CENTER);
        containger.add(container1, BorderLayout.SOUTH);

        AbstractAction actionOK = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        };
        AbstractAction actionCancel = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        };

        JComponent targetComponent = getRootPane();
        InputMap inputMap = targetComponent.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OK");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
        targetComponent.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
        targetComponent.getActionMap().put("OK", actionOK);
        targetComponent.getActionMap().put("Cancel", actionCancel);
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public boolean isOK() {
        return mIsOK;
    }

    private void onOK() {
        mSelectedIndex = mList.getSelectedIndex();
        mIsOK = true;
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    @Override
    public void contentsChanged(ListDataEvent arg0) {
        System.out.println("contentsChanged");
    }

    @Override
    public void intervalAdded(ListDataEvent arg0) {
        System.out.println("intervalAdded");
        mOK.setEnabled(mModel.getSize() > 0);
    }

    @Override
    public void intervalRemoved(ListDataEvent arg0) {
        System.out.println("intervalRemoved");
    }

}
