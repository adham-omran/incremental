package com.adham_omran;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.image.Image;

public class DatabaseUtils {
    private static final String DB_PATH = "jdbc:sqlite:test.db";

    @FunctionalInterface
    public interface ConnectionConsumer {
        void accept(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface ResultSetHandler<T> {
        T handle(ResultSet rs) throws SQLException;
    }

    public static void withConnection(ConnectionConsumer consumer) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            consumer.accept(connection);
        }
    }

    public static <T> T executeQuery(String sql, PreparedStatementSetter setter, ResultSetHandler<T> handler) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            if (setter != null) {
                setter.setParameters(pstmt);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return handler.handle(rs);
            }
        }
    }

    public static <T> List<T> executeQueryList(String sql, PreparedStatementSetter setter, ResultSetHandler<T> handler) throws SQLException {
        return executeQuery(sql, setter, rs -> {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(handler.handle(rs));
            }
            return results;
        });
    }

    public static int executeUpdate(String sql, PreparedStatementSetter setter) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            connection.setAutoCommit(false);
            
            if (setter != null) {
                setter.setParameters(pstmt);
            }
            
            int result = pstmt.executeUpdate();
            connection.commit();
            return result;
        }
    }

    @FunctionalInterface
    public interface PreparedStatementSetter {
        void setParameters(PreparedStatement pstmt) throws SQLException;
    }

    public static Topic populateTopicFromResultSet(ResultSet rs, Topic topic) throws SQLException {
        topic.setRowId(rs.getInt("rowid"));
        String content = rs.getString("content");
        topic.setContent(content != null ? content : "");

        // Set kind and parent_topic fields
        topic.setKind(rs.getString("kind"));
        topic.setTopicParent(rs.getInt("parent_topic"));

        // Set pdf_page if it exists (for extracts)
        int pdfPageInt = rs.getInt("pdf_page");
        topic.setPdfPage(pdfPageInt);

        // Check for PDF first
        String pdfPath = rs.getString("pdf_path");
        if (pdfPath != null) {
            topic.setPdfPath(pdfPath);
            topic.setCurrentPage(rs.getInt("current_page"));
        } else {
            // Handle regular image
            try (InputStream is = rs.getBinaryStream("img")) {
                if (is != null) {
                    topic.setTopicImage(new Image(is));
                }
            } catch (Exception e) {
                // Handle image loading errors gracefully
                System.err.println("Error loading image for topic " + topic.getRowId() + ": " + e.getMessage());
            }
        }

        return topic;
    }

    public static Topic createTopicFromResultSet(ResultSet rs) throws SQLException {
        Topic topic = new Topic();
        return populateTopicFromResultSet(rs, topic);
    }
}