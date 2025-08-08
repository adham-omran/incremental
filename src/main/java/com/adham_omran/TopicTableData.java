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

    private TopicTableData(Builder builder) {
        this.id = builder.id;
        this.addedDate = formatDate(builder.addedDate);
        this.scheduledDate = formatDate(builder.scheduledDate);
        this.viewedDate = formatDate(builder.viewedDate);
        this.aFactor = builder.aFactor;
        this.priority = builder.priority;
        this.title = builder.title != null && !builder.title.trim().isEmpty() ? builder.title : "No Title";
        this.kind = builder.kind != null ? builder.kind : "image";
        this.currentPage = builder.currentPage;
        this.pdfPage = builder.pdfPage;
        this.parentTopic = builder.parentTopic;

        // Determine type and source
        if ("extract".equals(this.kind)) {
            this.type = "Extract";
            this.source = parentTopic > 0 ? "Topic #" + parentTopic + (pdfPage > 0 ? " (p." + pdfPage + ")" : "")
                    : "Unknown";
        } else if (builder.pdfPath != null && !builder.pdfPath.trim().isEmpty()) {
            this.type = "PDF";
            this.source = getFileNameFromPath(builder.pdfPath) + (currentPage > 0 ? " (p." + currentPage + ")" : "");
        } else {
            this.type = "Image";
            this.source = "Direct upload";
        }
    }

    public static class Builder {
        private int id;
        private String addedDate;
        private String scheduledDate;
        private String viewedDate;
        private double aFactor = 2.0;
        private double priority = 0.5;
        private String title;
        private String pdfPath;
        private String kind = "image";
        private int currentPage = 1;
        private int pdfPage = 0;
        private int parentTopic = 0;

        public Builder(int id) {
            this.id = id;
        }

        public Builder addedDate(String addedDate) {
            this.addedDate = addedDate;
            return this;
        }

        public Builder scheduledDate(String scheduledDate) {
            this.scheduledDate = scheduledDate;
            return this;
        }

        public Builder viewedDate(String viewedDate) {
            this.viewedDate = viewedDate;
            return this;
        }

        public Builder aFactor(double aFactor) {
            this.aFactor = aFactor;
            return this;
        }

        public Builder priority(double priority) {
            this.priority = priority;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder pdfPath(String pdfPath) {
            this.pdfPath = pdfPath;
            return this;
        }

        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        public Builder currentPage(int currentPage) {
            this.currentPage = currentPage;
            return this;
        }

        public Builder pdfPage(int pdfPage) {
            this.pdfPage = pdfPage;
            return this;
        }

        public Builder parentTopic(int parentTopic) {
            this.parentTopic = parentTopic;
            return this;
        }

        public TopicTableData build() {
            return new TopicTableData(this);
        }
    }

    // Legacy constructor for backward compatibility
    public TopicTableData(int id, String addedDate, String scheduledDate, String viewedDate,
            double aFactor, double priority, String title, String pdfPath,
            String kind, int currentPage, int pdfPage, int parentTopic) {
        this(new Builder(id)
                .addedDate(addedDate)
                .scheduledDate(scheduledDate)
                .viewedDate(viewedDate)
                .aFactor(aFactor)
                .priority(priority)
                .title(title)
                .pdfPath(pdfPath)
                .kind(kind)
                .currentPage(currentPage)
                .pdfPage(pdfPage)
                .parentTopic(parentTopic));
    }

    private String getFileNameFromPath(String path) {
        if (path == null)
            return "";
        String[] parts = path.split("[/\\\\]");
        return parts[parts.length - 1];
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
