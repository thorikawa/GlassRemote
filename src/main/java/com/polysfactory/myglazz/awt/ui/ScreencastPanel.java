package com.polysfactory.myglazz.awt.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ScreencastPanel extends JPanel {

    private BufferedImage mImage;
    private double mZoom;
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 360;

    public ScreencastPanel() {
        setBackground(Color.BLACK);
        mZoom = 1.0;
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mImage != null) {
            int srcWidth = mImage.getWidth();
            int srcHeight = mImage.getHeight();
            int dstWidth = (int) (srcWidth * mZoom);
            int dstHeight = (int) (srcHeight * mZoom);
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
    }
}
