package com.adham_omran;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.ArrayList;

import javafx.scene.image.Image;
import jfx.incubator.scene.control.richtext.RichTextArea;

// Move this into a method for creating a database and checking for it
// Update the schema
// statement.executeUpdate("create table if not exists images (img blob)");

public class Database {
    String DB_PATH = "jdbc:sqlite:test.db";

    public void saveImage(InputStream input_stream, int image_length) {
        try (Connection connection = DriverManager.getConnection(DB_PATH);
                Statement statement = connection.createStatement();) {
            statement.setQueryTimeout(1);
            // Binary Stream Statement
            String updateImage = "INSERT INTO images (img, added_at, scheduled_at, viewed_at, a_factor, priority) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2.0, 0.5)";
            try (PreparedStatement updateImg = connection.prepareStatement(updateImage);) {
                connection.setAutoCommit(false);
                updateImg.setBinaryStream(1, input_stream, image_length);
                updateImg.executeUpdate();
                connection.commit();
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.out.println(e);
            e.printStackTrace(System.err);
        }

    }

    public Image readImage() {
        String sql = "select img, scheduled_at from images order by scheduled_at desc";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
                PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) { // Check if result exists
                try (InputStream is = rs.getBinaryStream("img")) {
                    if (is != null) {
                        Image img = new Image(is);
                        System.out.println("Last read: " + rs.getTimestamp("scheduled_at"));
                        return img;
                    }
                }
            }
            return null; // No image found

        } catch (Exception ex) {
            ex.printStackTrace();
            return null; // Return null on error
        }
    }

    /* TODO: Rename to nextTopic */
    public Topic nextTopic() {
        String sql = "select rowid, * from images order by scheduled_at asc";
        Topic topic = new Topic();
        try (Connection connection = DriverManager.getConnection(DB_PATH);
                PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) { // Check if result exists
                topic.setRowId(rs.getInt("rowid"));
                String content = rs.getString("content");
                topic.setContent(content != null ? content : "");

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
                    }
                }

                increaseDate(topic.getRowId(), connection);
                return topic;
            }
            return null; // No topic found

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading topic");
            return null; // Return null on error
        }
    }

    public Topic findTopic(int rowid) {
        String sql = "select rowid, * from images WHERE rowid = ?";
        Topic topic = new Topic();
        try (Connection connection = DriverManager.getConnection(DB_PATH);
                PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, rowid);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                topic.setContent(rs.getString("content"));
                topic.setRowId(rowid);

                // Check for PDF first
                String pdfPath = rs.getString("pdf_path");
                if (pdfPath != null) {
                    topic.setPdfPath(pdfPath);
                    topic.setCurrentPage(rs.getInt("current_page"));
                } else if (rs.getBinaryStream("img") != null) {
                    InputStream is = rs.getBinaryStream("img");
                    topic.setTopicImage(new Image(is));
                }
            }
            return topic;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null; // Return null on error
        }
    }

    public void increaseDate(int rowid, Connection conn) throws Exception {
        // Increase the `scheduled_at` value for an item with `rowid`.
        String sql = "UPDATE images SET scheduled_at = datetime(scheduled_at, '+1 day') WHERE rowid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setInt(1, rowid);
            pstmt.executeUpdate();
            System.out.println("Updated rowid " + rowid + ".");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateContent(int rowid, RichTextArea richTextArea) {
        String sql = "UPDATE images SET content = ? WHERE rowid = ?";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
                PreparedStatement pstmt = connection.prepareStatement(sql)) {

            // Serialize rich text content using OutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            richTextArea.write(outputStream);
            String richTextContent = outputStream.toString();

            pstmt.setString(1, richTextContent);
            pstmt.setInt(2, rowid);
            pstmt.executeUpdate();

            System.out.println("Updated content for rowid " + rowid + ".");

        } catch (Exception e) {
            System.err.println("Error updating content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadContentIntoRichTextArea(String content, RichTextArea richTextArea) {
        try {
            if (content != null && !content.trim().isEmpty()) {
                // Try to load as rich text first using InputStream
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
                richTextArea.read(inputStream);
            } else {
                // Clear the text area if no content
                richTextArea.clear();
            }
        } catch (Exception e) {
            // Fallback to plain text if rich text parsing fails
            System.err.println("Failed to load rich text, falling back to plain text: " + e.getMessage());
            richTextArea.clear();
            if (content != null) {
                richTextArea.appendText(content);
            }
        }
    }

    public void savePDF(String pdfPath) {
        try (Connection connection = DriverManager.getConnection(DB_PATH);
                Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(1);

            String insertSQL = "INSERT INTO images (pdf_path, current_page, added_at, scheduled_at, viewed_at, a_factor, priority) VALUES (?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2.0, 0.5)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
                connection.setAutoCommit(false);
                pstmt.setString(1, pdfPath);
                pstmt.executeUpdate();
                connection.commit();
                System.out.println("PDF saved: " + pdfPath);
            }
        } catch (SQLException e) {
            System.out.println("Error saving PDF: " + e);
            e.printStackTrace(System.err);
        }
    }

    public void updatePDFPage(int rowid, int pageNumber) {
        String sql = "UPDATE images SET current_page = ? WHERE rowid = ?";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
                PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, pageNumber);
            pstmt.setInt(2, rowid);
            pstmt.executeUpdate();

            System.out.println("Updated PDF page for rowid " + rowid + " to page " + pageNumber);

        } catch (SQLException e) {
            System.err.println("Error updating PDF page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<TopicTableData> getAllTopics() {
        List<TopicTableData> topics = new ArrayList<>();
        String sql = "SELECT rowid, added_at, scheduled_at, a_factor, priority, title, pdf_path FROM images ORDER BY rowid DESC";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
                PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("rowid");
                String addedDate = rs.getString("added_at");
                String scheduledDate = rs.getString("scheduled_at");
                double aFactor = rs.getDouble("a_factor");
                double priority = rs.getDouble("priority");
                String title = rs.getString("title");
                String pdfPath = rs.getString("pdf_path");

                // Determine type
                String type = (pdfPath != null && !pdfPath.trim().isEmpty()) ? "PDF" : "Image";

                TopicTableData tableData = new TopicTableData(id, type, title, addedDate, scheduledDate, priority,
                        aFactor);
                topics.add(tableData);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all topics: " + e.getMessage());
            e.printStackTrace();
        }

        return topics;
    }

    public void saveRectangle(RectangleData rectangle) {
        String sql = "INSERT INTO rectangles (item_id, pdf_page, rect_x1, rect_y1, rect_x2, rect_y2) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
                PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, rectangle.getItemId());
            pstmt.setInt(2, rectangle.getPdfPage());
            pstmt.setDouble(3, rectangle.getX1());
            pstmt.setDouble(4, rectangle.getY1());
            pstmt.setDouble(5, rectangle.getX2());
            pstmt.setDouble(6, rectangle.getY2());
            pstmt.executeUpdate();

            System.out.println("Saved rectangle for item " + rectangle.getItemId() + " page " + rectangle.getPdfPage());

        } catch (SQLException e) {
            System.err.println("Error saving rectangle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<RectangleData> getRectanglesForPage(int itemId, int pdfPage) {
        List<RectangleData> rectangles = new ArrayList<>();
        String sql = "SELECT * FROM rectangles WHERE item_id = ? AND pdf_page = ?";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
                PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            pstmt.setInt(2, pdfPage);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                RectangleData rect = new RectangleData(
                        rs.getInt("item_id"),
                        rs.getInt("pdf_page"),
                        rs.getDouble("rect_x1"),
                        rs.getDouble("rect_y1"),
                        rs.getDouble("rect_x2"),
                        rs.getDouble("rect_y2"));
                rectangles.add(rect);
            }

        } catch (SQLException e) {
            System.err.println("Error loading rectangles: " + e.getMessage());
            e.printStackTrace();
        }

        return rectangles;
    }

    public void deleteRectanglesForPage(int itemId, int pdfPage) {
        String sql = "DELETE FROM rectangles WHERE item_id = ? AND pdf_page = ?";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
                PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            pstmt.setInt(2, pdfPage);
            int deleted = pstmt.executeUpdate();

            System.out.println("Deleted " + deleted + " rectangles for item " + itemId + " page " + pdfPage);

        } catch (SQLException e) {
            System.err.println("Error deleting rectangles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveExtractedTopic(InputStream input_stream, int image_length, int parentTopicId, int pdfPage) {
        try (Connection connection = DriverManager.getConnection(DB_PATH);
                Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(1);

            String insertSQL = "INSERT INTO images (img, kind, parent_topic, pdf_page, added_at, scheduled_at, viewed_at, a_factor, priority) VALUES (?, 'extract', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2.0, 0.5)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
                connection.setAutoCommit(false);
                pstmt.setBinaryStream(1, input_stream, image_length);
                pstmt.setInt(2, parentTopicId);
                pstmt.setInt(3, pdfPage);
                pstmt.executeUpdate();
                connection.commit();
                System.out.println("Saved extracted topic from parent " + parentTopicId + ", page " + pdfPage);
            }
        } catch (SQLException e) {
            System.err.println("Error saving extracted topic: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
