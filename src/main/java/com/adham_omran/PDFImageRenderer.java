package com.adham_omran;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.io.File;
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

        public PDDocument getDocument() {
            return document;
        }

        public PDFRenderer getRenderer() {
            return renderer;
        }

        public int getTotalPages() {
            return totalPages;
        }
    }

    public static PDFInfo loadPDF(String filePath) throws IOException {
        File file = new File(filePath);
        PDDocument document = Loader.loadPDF(file);
        PDFRenderer renderer = new PDFRenderer(document);
        int totalPages = document.getNumberOfPages();

        return new PDFInfo(document, renderer, totalPages);
    }

    /**
     * Load PDF with error handling (returns null on error)
     */
    public static PDFInfo loadPDFSafe(String filePath) {
        return ErrorHandler.handlePdfError("loading PDF " + filePath, () -> {
            try {
                return loadPDF(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static BufferedImage renderPageToImage(PDFInfo pdfInfo, int pageNumber, float dpi) {
        return ErrorHandler.handlePdfError("rendering page to image", () -> {
            // Convert from 1-based to 0-based page indexing using ValidationUtils
            int zeroBasedPage = CoreUtils.convertToZeroBasedPage(pageNumber, pdfInfo.getTotalPages());

            if (zeroBasedPage == -1) {
                System.err.println("Page number out of range: " + pageNumber);
                return null;
            }

            try {
                return pdfInfo.getRenderer().renderImageWithDPI(zeroBasedPage, dpi);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Image renderPageToFXImage(PDFInfo pdfInfo, int pageNumber, float dpi) {
        BufferedImage bufferedImage = renderPageToImage(pdfInfo, pageNumber, dpi);
        if (bufferedImage != null) {
            return SwingFXUtils.toFXImage(bufferedImage, null);
        }
        return null;
    }

    public static BufferedImage renderPageToImage(PDFInfo pdfInfo, int pageNumber) {
        return renderPageToImage(pdfInfo, pageNumber, 300f);
    }

    public static Image renderPageToFXImage(PDFInfo pdfInfo, int pageNumber) {
        return renderPageToFXImage(pdfInfo, pageNumber, 300f);
    }

    public static BufferedImage extractRectangleFromPDF(PDFInfo pdfInfo, int pageNumber, double x1, double y1,
            double x2, double y2) {
        return ErrorHandler.handlePdfError("extracting rectangle from PDF", () -> {
            // Render page at high resolution for better extraction quality
            BufferedImage fullPage = renderPageToImage(pdfInfo, pageNumber, 600f);
            if (fullPage == null)
                return null;

            // Calculate rectangle bounds using ValidationUtils
            int pageWidth = fullPage.getWidth();
            int pageHeight = fullPage.getHeight();

            CoreUtils.RectangleBounds bounds = CoreUtils.calculateImageBounds(
                    x1, y1, x2, y2, pageWidth, pageHeight);

            // Extract the rectangle if bounds are valid
            if (bounds.isValid()) {
                return fullPage.getSubimage(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
            }

            System.err.println("Invalid rectangle dimensions for extraction");
            return null;
        });
    }
}
