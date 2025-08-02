package com.adham_omran;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument; 
import org.apache.pdfbox.rendering.PDFRenderer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PDFImageRenderer {
    
    public static class PDFInfo {
        private final PDDocument document;
        private final PDFRenderer renderer;
        private final int totalPages;
        
        public PDFInfo(PDDocument document, PDFRenderer renderer, int totalPages) {
            this.document = document;
            this.renderer = renderer;
            this.totalPages = totalPages;
        }
        
        public PDDocument getDocument() { return document; }
        public PDFRenderer getRenderer() { return renderer; }
        public int getTotalPages() { return totalPages; }
    }
    
    public static PDFInfo loadPDF(String filePath) throws IOException {
        File file = new File(filePath);
        PDDocument document = Loader.loadPDF(file);
        PDFRenderer renderer = new PDFRenderer(document);
        int totalPages = document.getNumberOfPages();
        
        return new PDFInfo(document, renderer, totalPages);
    }
    
    public static BufferedImage renderPageToImage(PDFInfo pdfInfo, int pageNumber, float dpi) {
        try {
            // Convert from 1-based to 0-based page indexing
            int zeroBasedPage = pageNumber - 1;
            
            if (zeroBasedPage < 0 || zeroBasedPage >= pdfInfo.getTotalPages()) {
                System.err.println("Page number out of range: " + pageNumber);
                return null;
            }
            
            return pdfInfo.getRenderer().renderImageWithDPI(zeroBasedPage, dpi);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Image renderPageToFXImage(PDFInfo pdfInfo, int pageNumber, float dpi) {
        BufferedImage bufferedImage = renderPageToImage(pdfInfo, pageNumber, dpi);
        if (bufferedImage != null) {
            return SwingFXUtils.toFXImage(bufferedImage, null);
        }
        return null;
    }
    
    public static BufferedImage renderPageToImage(PDFInfo pdfInfo, int pageNumber) {
        return renderPageToImage(pdfInfo, pageNumber, 150f);
    }
    
    public static Image renderPageToFXImage(PDFInfo pdfInfo, int pageNumber) {
        return renderPageToFXImage(pdfInfo, pageNumber, 150f);
    }
}