package com.adham_omran;

import java.util.function.Supplier;

public class ErrorHandler {

    /**
     * Execute a supplier operation with standard error handling
     * Returns null if operation fails
     */
    public static <T> T executeWithErrorHandling(String operation, Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            logError(operation, e);
            return null;
        }
    }

    /**
     * Execute a supplier operation with custom error handling
     * Returns defaultValue if operation fails
     */
    public static <T> T executeWithErrorHandling(String operation, Supplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception e) {
            logError(operation, e);
            return defaultValue;
        }
    }

    /**
     * Execute a runnable operation with standard error handling
     * Returns true if successful, false if failed
     */
    public static boolean executeWithErrorHandling(String operation, Runnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (Exception e) {
            logError(operation, e);
            return false;
        }
    }

    /**
     * Log an error with consistent format
     */
    public static void logError(String operation, Exception e) {
        System.err.println("Error " + operation + ": " + e.getMessage());
        e.printStackTrace();
    }

    /**
     * Log an error with custom message and exception
     */
    public static void logError(String operation, String customMessage, Exception e) {
        System.err.println("Error " + operation + ": " + customMessage);
        if (e != null) {
            e.printStackTrace();
        }
    }

    /**
     * Handle PDF operation errors specifically
     */
    public static <T> T handlePdfError(String operation, Supplier<T> supplier) {
        return executeWithErrorHandling("in PDF operation (" + operation + ")", supplier);
    }

}
