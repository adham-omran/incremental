package com.adham_omran;

import java.awt.datatransfer.DataFlavor;

public class ClipboardUtils {
    /**
     * Get an image off the system clipboard.
     *
     * @return Returns an Image if successful; otherwise returns null.
     */
    public void getImageFromClipboard() {
        System.out.println("Inside `getImageFromClipboard`.");
        try {
            var tk = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            System.out.println(tk);
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("No content.");
        }
    }
}
