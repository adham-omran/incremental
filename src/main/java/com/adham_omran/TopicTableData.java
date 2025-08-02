package com.adham_omran;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TopicTableData {
    private final int id;
    private final String type;
    private final String title;
    private final String addedDate;
    private final String scheduledDate;
    private final double priority;
    private final double aFactor;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public TopicTableData(int id, String type, String title, String addedDate, String scheduledDate, double priority, double aFactor) {
        this.id = id;
        this.type = type;
        this.title = title != null && !title.trim().isEmpty() ? title : "No Title";
        this.addedDate = formatDate(addedDate);
        this.scheduledDate = formatDate(scheduledDate);
        this.priority = priority;
        this.aFactor = aFactor;
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
    public int getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { 
        // Truncate long titles for table display
        return title.length() > 50 ? title.substring(0, 47) + "..." : title;
    }
    public String getAddedDate() { return addedDate; }
    public String getScheduledDate() { return scheduledDate; }
    public double getPriority() { return priority; }
    public double getAFactor() { return aFactor; }
    
    // For debugging
    @Override
    public String toString() {
        return String.format("TopicTableData{id=%d, type='%s', title='%s'}", id, type, title);
    }
}