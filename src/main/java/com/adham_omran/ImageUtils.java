package com.adham_omran;

import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    /**
     * Convert BufferedImage to JavaFX Image using byte array method
     */
    public static Image bufferedImageToFXImage(BufferedImage bufferedImage) {
        return ErrorHandler.executeWithErrorHandling("in image processing (converting BufferedImage to JavaFX Image)", () -> {
            try {
                // Convert BufferedImage to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                byte[] imageBytes = baos.toByteArray();

                // Create JavaFX Image from byte array
                ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                return new Image(bais);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * Convert BufferedImage to InputStream for database storage
     */
    public static InputStream bufferedImageToInputStream(BufferedImage bufferedImage, String format) {
        return ErrorHandler.executeWithErrorHandling("in image processing (converting BufferedImage to InputStream)", () -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, format, baos);
                return new ByteArrayInputStream(baos.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Convert BufferedImage to InputStream using PNG format
     */
    public static InputStream bufferedImageToInputStream(BufferedImage bufferedImage) {
        return bufferedImageToInputStream(bufferedImage, "png");
    }

    /**
     * Convert any AWT Image to BufferedImage
     */
    public static BufferedImage awtImageToBufferedImage(java.awt.Image awtImage) {
        if (awtImage == null) {
            return null;
        }

        if (awtImage instanceof BufferedImage) {
            return (BufferedImage) awtImage;
        }

        // Convert to BufferedImage
        BufferedImage bufferedImage = new BufferedImage(
                awtImage.getWidth(null),
                awtImage.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(awtImage, 0, 0, null);
        g2d.dispose();

        return bufferedImage;
    }

    /**
     * Get the byte array size for a BufferedImage in PNG format
     */
    public static int getBufferedImageByteSize(BufferedImage bufferedImage) {
        return ErrorHandler.executeWithErrorHandling("calculating BufferedImage byte size", () -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                return baos.size();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 0);
    }

}
