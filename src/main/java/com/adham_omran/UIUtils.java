package com.adham_omran;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

public class UIUtils {
    
    /**
     * Create a compact section container with title
     */
    public static VBox createCompactSection(String title) {
        VBox section = new VBox();
        section.getStyleClass().add("compact-section-container");
        section.setSpacing(4);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("compact-section-title");
        
        section.getChildren().add(titleLabel);
        return section;
    }
    
    /**
     * Create a compact button group container
     */
    public static HBox createCompactButtonGroup() {
        HBox buttonGroup = new HBox();
        buttonGroup.getStyleClass().add("compact-button-group");
        return buttonGroup;
    }
    
    /**
     * Create a compact form group container
     */
    public static HBox createCompactFormGroup() {
        HBox formGroup = new HBox();
        formGroup.getStyleClass().add("compact-form-group");
        formGroup.setSpacing(4);
        return formGroup;
    }
    
    /**
     * Apply compact button style based on type
     */
    public static void applyCompactButtonStyle(Button button, String type) {
        switch (type.toLowerCase()) {
            case "primary":
                button.getStyleClass().add("compact-primary-button");
                break;
            case "secondary":
                button.getStyleClass().add("compact-secondary-button");
                break;
            case "tertiary":
                button.getStyleClass().add("compact-tertiary-button");
                break;
            default:
                button.getStyleClass().add("compact-secondary-button");
                break;
        }
    }
    
    /**
     * Create a compact styled button
     */
    public static Button createCompactButton(String text, String type) {
        Button button = new Button(text);
        applyCompactButtonStyle(button, type);
        return button;
    }
}