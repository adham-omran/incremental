package com.adham_omran;

import javafx.scene.image.Image;

// Class to imitate the Topic from SuperMemo

// aFactor is Absolute Factor
// https://super-memory.com/archive/help16/g.htm#A-Factor

public class Topic {
    private double aFactor;
    private double priority;
    private String content;
    private Image topicImage;
    private int rowId;

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
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        this.content = content.trim();
    }

    public void setTopicImage(Image topicImage) {
        if (topicImage == null) {
            throw new IllegalArgumentException("Topic image cannot be null");
        }
        this.topicImage = topicImage;
    }

    public void setRowId(int rowId) {
        if (rowId <= 0) {
            throw new IllegalArgumentException("Row ID cannot bet less than 1.");
        }
        this.rowId = rowId;
    }
}
