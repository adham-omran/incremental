package com.adham_omran;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    
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
     * Extract filename from a file path using File.separator for cross-platform compatibility
     */
    public static String getFileNameFromPathCrossPlatform(String path) {
        if (path == null || path.isEmpty()) {
            return "Unknown file";
        }
        return path.substring(path.lastIndexOf(File.separator) + 1);
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
}