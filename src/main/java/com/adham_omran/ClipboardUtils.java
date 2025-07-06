package com.adham_omran;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ClipboardUtils {
    /**
     * Get an image off the system clipboard.
     *
     * @return Returns an Image if successful; otherwise returns null.
     */
    public Image getImageFromClipboard() {
        System.out.println("Inside `getImageFromClipboard`.");

        try {
            Transferable tns = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

            if (tns == null) {
                System.out.println("Clipboard is empty.");
                return null;
            }

            if (tns.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                return (Image) tns.getTransferData(DataFlavor.imageFlavor);
            } else {
                System.out.println("No image flavor supported in clipboard.");
                return null;
            }

        } catch (UnsupportedFlavorException | IOException e) {
            System.err.println("Error accessing clipboard: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IllegalStateException e) {
            System.err.println("Clipboard unavailable: " + e.getMessage());
            return null;
        }
    }
}
