package com.adham_omran;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Consolidated core utilities combining file operations and validation
 */
public class CoreUtils {
    
    // File utilities
    
    /**
     * Extract filename from a file path
     */
    public static String getFileNameFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return "Unknown file";
        }
        return path.substring(path.lastIndexOf('/') + 1);
    }
    
    /**
     * Extract filename using Path API (preferred method)
     */
    public static String getFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "Unknown file";
        }
        Path filePath = Paths.get(path);
        Path fileName = filePath.getFileName();
        return fileName != null ? fileName.toString() : "Unknown file";
    }
    
    /**
     * Get file extension from a path
     */
    public static String getFileExtension(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        String fileName = getFileName(path);
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Get filename without extension
     */
    public static String getFileNameWithoutExtension(String path) {
        if (path == null || path.isEmpty()) {
            return "Unknown file";
        }
        String fileName = getFileName(path);
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
    
    /**
     * Check if file exists
     */
    public static boolean fileExists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return new File(path).exists();
    }
    
    /**
     * Check if path is a PDF file
     */
    public static boolean isPdfFile(String path) {
        return "pdf".equals(getFileExtension(path));
    }
    
    /**
     * Check if path is an image file
     */
    public static boolean isImageFile(String path) {
        String extension = getFileExtension(path);
        return extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("gif") || extension.equals("bmp");
    }
    
    /**
     * Validate file path for safety (basic validation)
     */
    public static boolean isValidPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        // Basic validation - check for dangerous characters
        return !path.contains("..") && !path.contains("~");
    }
    
    // Validation utilities
    
    /**
     * Validate page number range (1-based indexing)
     */
    public static boolean isValidPageNumber(int pageNumber, int totalPages) {
        return pageNumber >= 1 && pageNumber <= totalPages;
    }
    
    /**
     * Validate page number range with error logging
     */
    public static boolean validatePageNumber(int pageNumber, int totalPages, String context) {
        if (!isValidPageNumber(pageNumber, totalPages)) {
            System.out.println("Page " + pageNumber + " is out of bounds (1-" + totalPages + ") in " + context);
            return false;
        }
        return true;
    }
    
    /**
     * Convert 1-based page number to 0-based with validation
     */
    public static int convertToZeroBasedPage(int pageNumber, int totalPages) {
        if (!isValidPageNumber(pageNumber, totalPages)) {
            return -1; // Invalid page
        }
        return pageNumber - 1;
    }
    
    /**
     * Validate and clamp a value to specified bounds
     */
    public static int clampToRange(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
    
    /**
     * Validate and clamp a double value to specified bounds
     */
    public static double clampToRange(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
    
    /**
     * Calculate normalized rectangle bounds within image dimensions
     */
    public static RectangleBounds calculateImageBounds(double x1, double y1, double x2, double y2, 
                                                      int imageWidth, int imageHeight) {
        // Calculate rectangle bounds in pixel coordinates
        int rectX = (int) (Math.min(x1, x2) * imageWidth);
        int rectY = (int) (Math.min(y1, y2) * imageHeight);
        int rectWidth = (int) (Math.abs(x2 - x1) * imageWidth);
        int rectHeight = (int) (Math.abs(y2 - y1) * imageHeight);
        
        // Ensure bounds are within image
        rectX = clampToRange(rectX, 0, imageWidth - 1);
        rectY = clampToRange(rectY, 0, imageHeight - 1);
        rectWidth = Math.min(rectWidth, imageWidth - rectX);
        rectHeight = Math.min(rectHeight, imageHeight - rectY);
        
        return new RectangleBounds(rectX, rectY, rectWidth, rectHeight);
    }
    
    /**
     * Validate rectangle dimensions
     */
    public static boolean isValidRectangle(int width, int height) {
        return width > 0 && height > 0;
    }
    
    /**
     * Validate rectangle dimensions with minimum size requirement
     */
    public static boolean isValidRectangle(int width, int height, int minWidth, int minHeight) {
        return width >= minWidth && height >= minHeight;
    }
    
    /**
     * Validate normalized coordinates (should be between 0.0 and 1.0)
     */
    public static boolean areValidNormalizedCoordinates(double x1, double y1, double x2, double y2) {
        return x1 >= 0.0 && x1 <= 1.0 && y1 >= 0.0 && y1 <= 1.0 &&
               x2 >= 0.0 && x2 <= 1.0 && y2 >= 0.0 && y2 <= 1.0;
    }
    
    /**
     * Helper class for rectangle bounds
     */
    public static class RectangleBounds {
        private final int x, y, width, height;
        
        public RectangleBounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        
        public boolean isValid() {
            return CoreUtils.isValidRectangle(width, height);
        }
    }
}