package com.polysfactory.myglazz.awt.util;

import java.util.List;

import javax.swing.DefaultListModel;

import com.google.glass.companion.Proto.MotionEvent;
import com.google.glass.companion.Proto.MotionEvent.PointerCoords;
import com.google.glass.companion.Proto.MotionEvent.PointerProperties;

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

    public static MotionEvent MouseEvent2MotionEvent(int action, float x, float y, long downTime) {
        MotionEvent me = new MotionEvent();
        me.downTime = downTime;
        me.eventTime = System.currentTimeMillis();
        me.action = action;
        me.metaState = 0;
        me.buttonState = 0;
        me.pointerCount = 1;
        me.xPrecision = 1.0007813f;
        me.yPrecision = 1.0013889f;
        me.deviceId = 1;
        me.edgeFlags = 0;
        me.source = 4098;
        me.flags = 0;
        PointerProperties prop = new MotionEvent.PointerProperties();
        prop.toolType = 1;
        PointerCoords point = new MotionEvent.PointerCoords();
        point.orientation = 1.5f;
        point.pressure = 1.0f;
        point.size = 0.045f;
        point.toolMajor = 9.0f;
        point.toolMinor = 9.0f;
        point.touchMajor = 10.0f;
        point.touchMinor = 9.0f;
        point.x = regularize(x);
        point.y = regularize(y);
        me.pointerProperties = new PointerProperties[] { prop };
        me.pointerCoords = new PointerCoords[] { point };
        return me;
    }

    private static float regularize(float f) {
        if (f < 0.0F) {
            return 0.001F;
        } else if (f > 100.0F) {
            return 99.999F;
        } else {
            return f;
        }
    }
}
