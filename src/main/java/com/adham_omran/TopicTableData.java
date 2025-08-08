package com.adham_omran;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TopicTableData {
    private final int id;
    private final String type;
    private final String title;
    private final String addedDate;
    private final String scheduledDate;
    private final String viewedDate;
    private final double priority;
    private final double aFactor;
    private final String kind;
    private final int currentPage;
    private final int pdfPage;
    private final int parentTopic;
    private final String source;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");



    public TopicTableData(int id, String addedDate, String scheduledDate, String viewedDate,
            double aFactor, double priority, String title, String pdfPath,
            String kind, int currentPage, int pdfPage, int parentTopic) {
        this.id = id;
        this.addedDate = formatDate(addedDate);
        this.scheduledDate = formatDate(scheduledDate);
        this.viewedDate = formatDate(viewedDate);
        this.aFactor = aFactor;
        this.priority = priority;
        this.title = title != null && !title.trim().isEmpty() ? title : "No Title";
        this.kind = kind != null ? kind : "image";
        this.currentPage = currentPage;
        this.pdfPage = pdfPage;
        this.parentTopic = parentTopic;

        // Determine type and source
        if ("extract".equals(this.kind)) {
            this.type = "Extract";
            this.source = parentTopic > 0 ? "Topic #" + parentTopic + (pdfPage > 0 ? " (p." + pdfPage + ")" : "")
                    : "Unknown";
        } else if (pdfPath != null && !pdfPath.trim().isEmpty()) {
            this.type = "PDF";
            this.source = CoreUtils.getFileNameFromPath(pdfPath) + (currentPage > 0 ? " (p." + currentPage + ")" : "");
        } else {
            this.type = "Image";
            this.source = "Direct upload";
        }
    }


    private String formatDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "N/A";
        }
        try {
            // Parse the SQLite datetime format and format it nicely
            LocalDateTime dateTime = LocalDateTime.parse(dateString.replace(" ", "T"));
            return dateTime.format(DATE_FORMATTER);
        } catch (Exception e) {
            return dateString; // Return original if parsing fails
        }
    }

    // Getters for JavaFX TableView
    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        // Truncate long titles for table display
        return title.length() > 40 ? title.substring(0, 37) + "..." : title;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public String getViewedDate() {
        return viewedDate;
    }

    public double getPriority() {
        return priority;
    }

    public double getAFactor() {
        return aFactor;
    }

    public String getKind() {
        return kind;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPdfPage() {
        return pdfPage;
    }

    public int getParentTopic() {
        return parentTopic;
    }

    public String getSource() {
        return source.length() > 40 ? source.substring(0, 37) + "..." : source;
    }

    // For debugging
    @Override
    public String toString() {
        return String.format("TopicTableData{id=%d, type='%s', title='%s'}", id, type, title);
    }
}
