package com.adham_omran;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * Builder for complex UI layouts used in the main application
 */
public class UIBuilder {

    /**
     * Build the main content actions section
     */
    public static VBox buildContentActionsSection(EventHandler<ActionEvent> clipboardHandler,
            EventHandler<ActionEvent> pdfHandler) {
        VBox contentSection = UIComponentFactory.createSection("Content Actions");

        HBox contentButtons = UIComponentFactory.createButtonGroup();

        Button btnClipboard = UIComponentFactory.createButton(
                "Save from Clipboard",
                "primary-button",
                "Save an image from your clipboard to the database",
                clipboardHandler);

        Button btnAddPDF = UIComponentFactory.createButton(
                "Add PDF",
                "secondary-button",
                "Import a PDF file for study",
                pdfHandler);

        UIComponentFactory.addChildren(contentButtons, btnClipboard, btnAddPDF);
        UIComponentFactory.addChildren(contentSection, contentButtons);

        return contentSection;
    }

    /**
     * Build the navigation section
     */
    public static VBox buildNavigationSection(EventHandler<ActionEvent> nextHandler,
            EventHandler<ActionEvent> tableHandler) {
        VBox navigationSection = UIComponentFactory.createSection("Navigation");

        HBox navigationButtons = UIComponentFactory.createButtonGroup();

        Button btnNext = UIComponentFactory.createButton(
                "Next Topic",
                "secondary-button",
                "Open the next topic scheduled for review",
                nextHandler);

        Button btnTable = UIComponentFactory.createButton(
                "View Table",
                "tertiary-button",
                "View all topics in a sortable table",
                tableHandler);

        UIComponentFactory.addChildren(navigationButtons, btnNext, btnTable);
        UIComponentFactory.addChildren(navigationSection, navigationButtons);

        return navigationSection;
    }

    /**
     * Build the direct access section
     */
    public static VBox buildDirectAccessSection(TextField textField, EventHandler<ActionEvent> openHandler) {
        VBox accessSection = UIComponentFactory.createSection("Direct Access");

        HBox accessForm = UIComponentFactory.createFormGroup();

        Label idLabel = new Label("Topic ID:");

        Button btnTopicWithId = UIComponentFactory.createButton(
                "\uD83D\uDD0D Open",
                "tertiary-button",
                "Open the topic with the specified ID",
                openHandler);

        UIComponentFactory.addChildren(accessForm, idLabel, textField, btnTopicWithId);
        UIComponentFactory.addChildren(accessSection, accessForm);

        return accessSection;
    }

    /**
     * Build topic controls section (main controls like Next Item, Close)
     */
    public static VBox buildTopicControlsSection(EventHandler<ActionEvent> nextHandler,
            EventHandler<ActionEvent> closeHandler) {
        VBox mainControlsSection = UIComponentFactory.createSection("Topic Controls");
        mainControlsSection.setSpacing(8);

        HBox mainButtons = UIComponentFactory.createButtonGroup();

        Button btnNextItem = UIComponentFactory.createButton("Next Item", "primary-button", null, nextHandler);
        Button btnClose = UIComponentFactory.createButton("Close", "secondary-button", null, closeHandler);

        UIComponentFactory.addChildren(mainButtons, btnNextItem, btnClose);
        UIComponentFactory.addChildren(mainControlsSection, mainButtons);

        return mainControlsSection;
    }

    /**
     * Build page navigation controls for PDFs
     */
    public static VBox buildPageNavigationSection(EventHandler<ActionEvent> prevHandler,
            EventHandler<ActionEvent> nextHandler,
            TextField pageField, Label totalPagesLabel, EventHandler<ActionEvent> jumpHandler) {
        VBox navigationSection = UIComponentFactory.createCompactSection("Page Navigation");
        navigationSection.setSpacing(4);

        HBox pageNavBox = UIComponentFactory.createCompactButtonGroup();
        Button btnPrevPage = UIComponentFactory.createButton("◀ Prev", "compact-secondary-button", null, prevHandler);
        Button btnNextPage = UIComponentFactory.createButton("Next ▶", "compact-secondary-button", null, nextHandler);
        UIComponentFactory.addChildren(pageNavBox, btnPrevPage, btnNextPage);

        HBox pageInfoBox = UIComponentFactory.createCompactFormGroup();
        pageInfoBox.setSpacing(4);

        Label pageLabel = new Label("Page:");
        Button btnJumpToPage = UIComponentFactory.createButton("Go", "compact-tertiary-button",
                "Jump to the specified page", jumpHandler);

        UIComponentFactory.addChildren(pageInfoBox, pageLabel, pageField, totalPagesLabel, btnJumpToPage);
        UIComponentFactory.addChildren(navigationSection, pageNavBox, pageInfoBox);

        return navigationSection;
    }

    /**
     * Build zoom/view controls section
     */
    public static VBox buildViewControlsSection(EventHandler<ActionEvent> fitPageHandler,
            EventHandler<ActionEvent> fitWidthHandler,
            EventHandler<ActionEvent> zoomInHandler, EventHandler<ActionEvent> zoomOutHandler,
            EventHandler<ActionEvent> resetHandler, Label zoomLabel) {
        VBox viewSection = UIComponentFactory.createCompactSection("View Options");
        viewSection.setSpacing(4);

        // Preset fit buttons
        HBox fitBox = UIComponentFactory.createCompactButtonGroup();
        Button btnFitToPage = UIComponentFactory.createButton("Fit Page", "compact-tertiary-button", null,
                fitPageHandler);
        Button btnFitToWidth = UIComponentFactory.createButton("Fit Width", "compact-tertiary-button", null,
                fitWidthHandler);
        UIComponentFactory.addChildren(fitBox, btnFitToPage, btnFitToWidth);

        // Zoom controls
        HBox zoomBox = UIComponentFactory.createCompactFormGroup();
        zoomBox.setSpacing(4);

        Button btnZoomOut = UIComponentFactory.createButton("➖", "compact-secondary-button", "Zoom out (Ctrl + -)",
                zoomOutHandler);
        Button btnZoomIn = UIComponentFactory.createButton("➕", "compact-secondary-button", "Zoom in (Ctrl + +)",
                zoomInHandler);
        Button btnZoomReset = UIComponentFactory.createButton("Reset", "compact-tertiary-button", "Reset zoom to 100%",
                resetHandler);

        UIComponentFactory.addChildren(zoomBox, btnZoomOut, zoomLabel, btnZoomIn, btnZoomReset);
        UIComponentFactory.addChildren(viewSection, fitBox, zoomBox);

        return viewSection;
    }

    /**
     * Build drawing tools section
     */
    public static VBox buildDrawingSection(EventHandler<ActionEvent> drawModeHandler,
            EventHandler<ActionEvent> clearHandler) {
        VBox drawingSection = UIComponentFactory.createCompactSection("Drawing Tools");
        drawingSection.setSpacing(4);

        HBox drawBox = UIComponentFactory.createCompactButtonGroup();

        Button btnDrawMode = UIComponentFactory.createButton("Draw Mode", "compact-tertiary-button",
                "Toggle drawing mode to draw rectangles", drawModeHandler);
        Button btnClearRects = UIComponentFactory.createButton("Clear", "compact-secondary-button",
                "Clear all rectangles on this page", clearHandler);

        UIComponentFactory.addChildren(drawBox, btnDrawMode, btnClearRects);
        UIComponentFactory.addChildren(drawingSection, drawBox);

        return drawingSection;
    }
}
