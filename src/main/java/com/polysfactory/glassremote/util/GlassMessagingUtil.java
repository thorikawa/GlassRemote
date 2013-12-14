package com.polysfactory.glassremote.util;

import com.google.glass.companion.CompanionMessagingUtil;
import com.google.glass.companion.Proto.Envelope;
import com.google.glass.companion.Proto.MotionEvent;
import com.google.glass.companion.Proto.MotionEvent.PointerCoords;
import com.google.glass.companion.Proto.MotionEvent.PointerProperties;
import com.google.googlex.glass.common.proto.TimelineNano;
import com.google.googlex.glass.common.proto.TimelineNano.SourceType;
import com.google.googlex.glass.common.proto.TimelineNano.TimelineItem;

public class GlassMessagingUtil {

    public static final int ACTION_DOWN = 0;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_UP = 1;

    public static MotionEvent convertMouseEvent2MotionEvent(int action, float x, float y, long downTime) {
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
        point.x = normalize(x);
        point.y = normalize(y);
        me.pointerProperties = new PointerProperties[] { prop };
        me.pointerCoords = new PointerCoords[] { point };
        return me;
    }

    private static float normalize(float f) {
        if (f < 0.0F) {
            return 0.001F;
        } else if (f > 100.0F) {
            return 99.999F;
        } else {
            return f;
        }
    }

    public static Envelope createTimelineMessage(String text) {
        long now = System.currentTimeMillis();
        Envelope envelope = CompanionMessagingUtil.newEnvelope();
        TimelineItem timelineItem = new TimelineNano.TimelineItem();
        timelineItem.id = "com.polysfactory.glassremote.timeline.sample";
        timelineItem.title = "From GlassRemote";
        timelineItem.text = text;
        timelineItem.creationTime = now;
        timelineItem.modifiedTime = now;
        timelineItem.sourceType = SourceType.COMPANIONWARE;
        timelineItem.source = "GlassRemote";
        timelineItem.isDeleted = false;
        envelope.timelineItem = new TimelineItem[] { timelineItem };
        return envelope;
    }
}
