package com.polysfactory.glassremote.util;

import java.util.List;

import javax.swing.DefaultListModel;

public class SwingUtil {
    public static DefaultListModel list2ListModel(List<? extends Object> list) {
        DefaultListModel model = new DefaultListModel();
        if (list != null) {
            for (Object o : list) {
                model.addElement(o);
            }
        }
        return model;
    }
}
