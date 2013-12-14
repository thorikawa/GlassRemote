package com.polysfactory.glassremote.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ScreencastPanel extends JPanel {

    private BufferedImage mImage;
    private double mZoom;
    private ScreencastMouseEventListener mListener;
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 360;
    private static int mWidth = DEFAULT_WIDTH;
    private static int mHeight = DEFAULT_HEIGHT;

    public ScreencastPanel() {
        setBackground(Color.BLACK);
        mZoom = 1.0;
        setPreferredSize(new Dimension(mWidth, mHeight));
        addMouseListener(mMouseListener);
        addMouseMotionListener(mMouseMotionListener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mImage != null) {
            int srcWidth = mImage.getWidth();
            int srcHeight = mImage.getHeight();
            int dstWidth = (int) (srcWidth * mZoom);
            int dstHeight = (int) (srcHeight * mZoom);
            if (dstHeight != mHeight || dstWidth != mWidth) {
                Dimension d = new Dimension(dstWidth, dstHeight);
                setPreferredSize(d);
                setSize(d);
                mWidth = dstWidth;
                mHeight = dstHeight;
            }
            // System.out.println(dstWidth + "," + dstHeight);
            if (mZoom == 1.0) {
                g.drawImage(mImage, 0, 0, dstWidth, dstHeight, 0, 0, srcWidth, srcHeight, null);
            } else {
                Image image = mImage.getScaledInstance(dstWidth, dstHeight, Image.SCALE_SMOOTH);
                g.drawImage(image, 0, 0, dstWidth, dstHeight, 0, 0, dstWidth, dstHeight, null);
            }
        }
    }

    public void setImage(BufferedImage image) {
        mImage = image;
        repaint();
    }

    public BufferedImage getImage() {
        return mImage;
    }

    public void setZoom(double zoom) {
        this.mZoom = zoom;
        updateSize();
    }

    public void updateSize() {
        int dstWidth = (int) (DEFAULT_WIDTH * mZoom);
        int dstHeight = (int) (DEFAULT_HEIGHT * mZoom);
        if (dstHeight != mHeight || dstWidth != mWidth) {
            Dimension d = new Dimension(dstWidth, dstHeight);
            setPreferredSize(d);
            setSize(d);
            mWidth = dstWidth;
            mHeight = dstHeight;
        }
    }

    public void setScreencastMouseEventListener(ScreencastMouseEventListener screencastMouseEventListener) {
        mListener = screencastMouseEventListener;
    }

    private static final int ACTION_DOWN = 0;
    private static final int ACTION_MOVE = 2;
    private static final int ACTION_UP = 1;

    private long mDownTime;

    private MouseMotionListener mMouseMotionListener = new MouseMotionListener() {
        long mLastMouseMoveTracked = -1;

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (mListener != null) {
                long now = System.currentTimeMillis();
                // HACK ALERT: Since mouseDragged is too often for companion service to detect correct motion gesture,
                // we should drop some events in the specific interval after we send the event
                if (now < mLastMouseMoveTracked + 50) {
                    return;
                }
                mListener.onMouseEvent(ACTION_MOVE, e.getX(), e.getY(), mDownTime);
                mLastMouseMoveTracked = now;
            }
        }
    };

    private MouseListener mMouseListener = new MouseListener() {
        @Override
        public void mouseReleased(MouseEvent e) {
            if (mListener != null) {
                mListener.onMouseEvent(ACTION_UP, e.getX(), e.getY(), mDownTime);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mDownTime = System.currentTimeMillis();
            if (mListener != null) {
                mListener.onMouseEvent(ACTION_DOWN, e.getX(), e.getY(), mDownTime);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }
    };

    public interface ScreencastMouseEventListener {
        public void onMouseEvent(int action, int x, int y, long downTime);
    }
}
