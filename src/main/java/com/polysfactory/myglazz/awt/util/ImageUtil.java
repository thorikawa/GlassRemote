package com.polysfactory.myglazz.awt.util;

import java.awt.Component;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class ImageUtil {

    private static final String IMAGE_OUTPUT_EXT = "png";

    public static void saveImage(Component parent, BufferedImage image, double zoom) {
        BufferedImage outImage = new BufferedImage((int) (image.getWidth() * zoom), (int) (image.getHeight() * zoom),
                image.getType());
        AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(zoom, zoom),
                AffineTransformOp.TYPE_BILINEAR);
        op.filter(image, outImage);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return "*." + IMAGE_OUTPUT_EXT;
            }

            @Override
            public boolean accept(File file) {
                String ext = file.getName().toLowerCase();
                return (ext.endsWith("." + IMAGE_OUTPUT_EXT));
            }
        });
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                String path = file.getAbsolutePath();
                if (!path.endsWith("." + IMAGE_OUTPUT_EXT)) {
                    file = new File(path + "." + IMAGE_OUTPUT_EXT);
                }
                ImageIO.write(outImage, IMAGE_OUTPUT_EXT, file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "Failed to save a image.", "Save Image",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
