package com.adham_omran;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * Factory for creating common UI components with consistent styling and behavior
 */
public class UIComponentFactory {
    
    /**
     * Create a button with text, style class, and tooltip
     */
    public static Button createButton(String text, String styleClass, String tooltipText) {
        Button button = new Button(text);
        if (styleClass != null && !styleClass.isEmpty()) {
            button.getStyleClass().add(styleClass);
        }
        if (tooltipText != null && !tooltipText.isEmpty()) {
            button.setTooltip(new Tooltip(tooltipText));
        }
        return button;
    }
    
    /**
     * Create a button with text, style class, tooltip, and action handler
     */
    public static Button createButton(String text, String styleClass, String tooltipText, EventHandler<ActionEvent> handler) {
        Button button = createButton(text, styleClass, tooltipText);
        if (handler != null) {
            button.setOnAction(handler);
        }
        return button;
    }
    
    /**
     * Create a simple button with just text and style
     */
    public static Button createButton(String text, String styleClass) {
        return createButton(text, styleClass, null);
    }
    
    /**
     * Create a section container with title and content
     */
    public static VBox createSection(String title, String sectionStyle, String titleStyle) {
        VBox section = new VBox();
        section.getStyleClass().add(sectionStyle != null ? sectionStyle : "section-container");
        
        if (title != null && !title.isEmpty()) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add(titleStyle != null ? titleStyle : "section-title");
            section.getChildren().add(titleLabel);
        }
        
        return section;
    }
    
    /**
     * Create a standard section with default styling
     */
    public static VBox createSection(String title) {
        return createSection(title, "section-container", "section-title");
    }
    
    /**
     * Create a compact section with smaller styling
     */
    public static VBox createCompactSection(String title) {
        return createSection(title, "compact-section-container", "compact-section-title");
    }
    
    /**
     * Create a button group container
     */
    public static HBox createButtonGroup(String styleClass) {
        HBox buttonGroup = new HBox();
        buttonGroup.getStyleClass().add(styleClass != null ? styleClass : "button-group");
        return buttonGroup;
    }
    
    /**
     * Create a standard button group
     */
    public static HBox createButtonGroup() {
        return createButtonGroup("button-group");
    }
    
    /**
     * Create a compact button group
     */
    public static HBox createCompactButtonGroup() {
        return createButtonGroup("compact-button-group");
    }
    
    /**
     * Create a form group container
     */
    public static HBox createFormGroup(String styleClass) {
        HBox formGroup = new HBox();
        formGroup.getStyleClass().add(styleClass != null ? styleClass : "form-group");
        return formGroup;
    }
    
    /**
     * Create a standard form group
     */
    public static HBox createFormGroup() {
        return createFormGroup("form-group");
    }
    
    /**
     * Create a compact form group
     */
    public static HBox createCompactFormGroup() {
        return createFormGroup("compact-form-group");
    }
    
    /**
     * Create a text field with style and placeholder
     */
    public static TextField createTextField(String placeholder, String styleClass) {
        TextField textField = new TextField();
        if (placeholder != null && !placeholder.isEmpty()) {
            textField.setPromptText(placeholder);
        }
        if (styleClass != null && !styleClass.isEmpty()) {
            textField.getStyleClass().add(styleClass);
        }
        return textField;
    }
    
    /**
     * Create a standard text field
     */
    public static TextField createTextField(String placeholder) {
        return createTextField(placeholder, "text-field");
    }
    
    /**
     * Create a header box with title and action button on the right
     */
    public static HBox createHeaderBox(String title, Button actionButton) {
        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        
        // Add spacer to push action button to the right
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(titleLabel, spacer, actionButton);
        return headerBox;
    }
    
    /**
     * Add multiple children to a container
     */
    public static void addChildren(javafx.scene.Parent container, Node... children) {
        if (container instanceof VBox) {
            ((VBox) container).getChildren().addAll(children);
        } else if (container instanceof HBox) {
            ((HBox) container).getChildren().addAll(children);
        }
    }
}