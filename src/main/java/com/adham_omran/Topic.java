package com.adham_omran;

import javafx.scene.image.Image;

// Class to imitate the Topic from SuperMemo

// aFactor is Absolute Factor
// https://super-memory.com/archive/help16/g.htm#A-Factor

public class Topic {
    private double aFactor;
    /**
     * The priority of a Topic, between 0.01 and 0.99, where 0.01 is higher
     * (compatibility with SuperMemo).
     */
    private double priority;

    /**
     * The text content of a Topic.
     * What to do with this?
     * - Content from the output of OCR for Arabic
     */
    private String content;
    private Image topicImage;
    private int rowId;
    /**
     * The parent topic rowId
     *
     * Use the parent topic to acquire the source name
     */
    private int topicParent;

    /**
     * PDF file path for PDF-based topics
     */
    private String pdfPath;

    /**
     * Current page number for PDF topics (1-based)
     */
    private int currentPage = 1;

    /**
     * Page number from the pdf_page column in database (for extracts)
     * This stores the original page number when the extract was created
     */
    private Integer pdfPage;

    /**
     * Kind of topic: "image", "pdf", or "extract"
     */
    private String kind;

    // Getters
    public double getAFactor() {
        return aFactor;
    }

    public double getPriority() {
        return priority;
    }

    public String getContent() {
        return content;
    }

    public Image getTopicImage() {
        return topicImage;
    }

    public int getRowId() {
        return rowId;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public boolean isPdf() {
        return pdfPath != null && !pdfPath.trim().isEmpty();
    }

    public String getKind() {
        return kind;
    }

    public int getTopicParent() {
        return topicParent;
    }

    public Integer getPdfPage() {
        return pdfPage;
    }

    // Setters with validation
    public void setAFactor(double aFactor) {
        if (aFactor <= 0) {
            throw new IllegalArgumentException("aFactor must be positive");
        }
        this.aFactor = aFactor;
    }

    public void setPriority(double priority) {
        if (priority < 0) {
            throw new IllegalArgumentException("Priority cannot be negative");
        }
        this.priority = priority;
    }

    public void setContent(String content) {
        // Allow null or empty content
        this.content = (content != null) ? content.trim() : "";
    }

    public void setTopicImage(Image topicImage) {
        // Allow null images for topics without images
        this.topicImage = topicImage;
    }

    public void setRowId(int rowId) {
        if (rowId <= 0) {
            throw new IllegalArgumentException("Row ID cannot bet less than 1.");
        }
        this.rowId = rowId;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setTopicParent(int topicParent) {
        this.topicParent = topicParent;
    }

    public void setPdfPage(Integer pdfPage) {
        this.pdfPage = pdfPage;
    }

}
