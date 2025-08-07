package com.adham_omran;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.Button;
import java.util.HashMap;
import java.util.Map;

public class ButtonStateManager {
    
    // Store original button states to restore later
    private static final Map<Button, ButtonOriginalState> originalStates = new HashMap<>();
    
    private static class ButtonOriginalState {
        final String originalText;
        final String originalStyleClass;
        
        ButtonOriginalState(String text, String styleClass) {
            this.originalText = text;
            this.originalStyleClass = styleClass;
        }
    }
    
    /**
     * Store the original state of a button for later restoration
     */
    private static void storeOriginalState(Button button) {
        if (!originalStates.containsKey(button)) {
            // Get the primary CSS class (first non-default class)
            String primaryClass = button.getStyleClass().stream()
                .filter(cls -> !cls.equals("button"))  // Skip default JavaFX button class
                .findFirst()
                .orElse("button");
            
            originalStates.put(button, new ButtonOriginalState(button.getText(), primaryClass));
        }
    }
    
    /**
     * Set button state with text and CSS class
     */
    public static void setState(Button button, String text, String cssClass) {
        storeOriginalState(button);
        
        button.setText(text);
        
        // Remove all existing style classes except 'button'
        button.getStyleClass().removeIf(cls -> !cls.equals("button"));
        button.getStyleClass().add(cssClass);
        
        button.setDisable(false); // Ensure button is enabled unless specifically disabled
    }
    
    /**
     * Create a timer that resets the button to its original state after a delay
     */
    public static Timeline createResetTimer(Button button, double delaySeconds) {
        return new Timeline(new KeyFrame(Duration.seconds(delaySeconds), event -> {
            resetToOriginal(button);
        }));
    }
    
    /**
     * Reset button to its original stored state
     */
    public static void resetToOriginal(Button button) {
        ButtonOriginalState original = originalStates.get(button);
        if (original != null) {
            button.setText(original.originalText);
            button.getStyleClass().removeIf(cls -> !cls.equals("button"));
            button.getStyleClass().add(original.originalStyleClass);
            button.setDisable(false);
        }
    }
    
    /**
     * Show loading state with custom text
     */
    public static void showLoadingState(Button button, String loadingText) {
        setState(button, loadingText, "loading-button");
        button.setDisable(true);
    }
    
    /**
     * Show success state and auto-reset after delay
     */
    public static void showSuccessState(Button button, String successText, double resetDelaySeconds) {
        setState(button, successText, "success-button");
        
        Timeline resetTimer = createResetTimer(button, resetDelaySeconds);
        resetTimer.play();
    }
    
    /**
     * Show error state and auto-reset after delay
     */
    public static void showErrorState(Button button, String errorText, double resetDelaySeconds) {
        setState(button, errorText, "error-button");
        
        Timeline resetTimer = createResetTimer(button, resetDelaySeconds);
        resetTimer.play();
    }
    
    /**
     * Complete workflow: Loading -> Success with auto-reset
     */
    public static void showLoadingThenSuccess(Button button, String loadingText, String successText, double successDelaySeconds) {
        showLoadingState(button, loadingText);
        // Note: The calling code should call showSuccessState when the operation completes
    }
    
    /**
     * Complete workflow: Loading -> Error with auto-reset
     */
    public static void showLoadingThenError(Button button, String loadingText, String errorText, double errorDelaySeconds) {
        showLoadingState(button, loadingText);
        // Note: The calling code should call showErrorState when the operation fails
    }
    
    /**
     * Clear the stored original state (useful for cleanup)
     */
    public static void clearOriginalState(Button button) {
        originalStates.remove(button);
    }
    
    /**
     * Clear all stored states (useful for cleanup)
     */
    public static void clearAllStates() {
        originalStates.clear();
    }
}