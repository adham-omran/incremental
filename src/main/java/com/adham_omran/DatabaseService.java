package com.adham_omran;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import javafx.scene.image.Image;
import jfx.incubator.scene.control.richtext.RichTextArea;

/**
 * Consolidated database service that combines Database and DatabaseUtils
 * functionality
 */
public class DatabaseService {
    private static final String DB_PATH = "jdbc:sqlite:test.db";

    // Functional interfaces for database operations
    @FunctionalInterface
    public interface ConnectionConsumer {
        void accept(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface ResultSetHandler<T> {
        T handle(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    public interface PreparedStatementConsumer {
        void accept(PreparedStatement pstmt) throws SQLException;
    }

    // Connection management
    public void withConnection(ConnectionConsumer consumer) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_PATH)) {
            consumer.accept(connection);
        }
    }

    // Query execution methods
    public <T> T executeQuery(String sql, PreparedStatementConsumer paramSetter, ResultSetHandler<T> handler)
            throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_PATH);
                PreparedStatement pstmt = connection.prepareStatement(sql)) {

            if (paramSetter != null) {
                paramSetter.accept(pstmt);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                return handler.handle(rs);
            }
        }
    }

    public <T> List<T> executeQueryList(String sql, PreparedStatementConsumer paramSetter, ResultSetHandler<T> handler)
            throws SQLException {
        List<T> results = new ArrayList<>();
        executeQuery(sql, paramSetter, rs -> {
            while (rs.next()) {
                results.add(handler.handle(rs));
            }
            return null;
        });
        return results;
    }

    public int executeUpdate(String sql, PreparedStatementConsumer paramSetter) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_PATH);
                PreparedStatement pstmt = connection.prepareStatement(sql)) {

            if (paramSetter != null) {
                paramSetter.accept(pstmt);
            }

            return pstmt.executeUpdate();
        }
    }

    // Topic creation methods
    public Topic createTopicFromResultSet(ResultSet rs) throws SQLException {
        Topic topic = new Topic();

        topic.setRowId(rs.getInt("rowid"));

        // Handle image data
        InputStream imgStream = rs.getBinaryStream("img");
        if (imgStream != null) {
            topic.setTopicImage(new Image(imgStream));
        }

        // Set other fields
        topic.setContent(rs.getString("content"));
        topic.setPdfPath(rs.getString("pdf_path"));
        topic.setCurrentPage(rs.getInt("current_page"));
        topic.setKind(rs.getString("kind"));
        topic.setTopicParent(rs.getInt("parent_topic"));
        topic.setPdfPage(rs.getInt("pdf_page"));
        topic.setAFactor(rs.getDouble("a_factor"));
        topic.setPriority(rs.getDouble("priority"));

        return topic;
    }

    // Image operations
    public void saveImage(InputStream inputStream, int imageLength) {
        String sql = "INSERT INTO images (img, kind, added_at, scheduled_at, viewed_at, a_factor, priority) VALUES (?, 'image', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2.0, 0.5)";

        try {
            executeUpdate(sql, pstmt -> pstmt.setBinaryStream(1, inputStream, imageLength));
        } catch (SQLException e) {
            System.out.println(e);
            e.printStackTrace(System.err);
        }
    }

    public Image readImage() {
        String sql = "select img, scheduled_at from images order by scheduled_at desc";

        return ErrorHandler.handleDatabaseError("reading image", () -> {
            try {
                return executeQuery(sql, null, rs -> {
                    if (rs.next()) {
                        try (InputStream is = rs.getBinaryStream("img")) {
                            if (is != null) {
                                Image img = new Image(is);
                                System.out.println("Last read: " + rs.getTimestamp("scheduled_at"));
                                return img;
                            }
                        } catch (Exception e) {
                            System.err.println("Error reading image: " + e.getMessage());
                        }
                    }
                    return null; // No image found
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Topic operations
    public Topic nextTopic() {
        String sql = "select rowid, * from images order by scheduled_at asc";

        return ErrorHandler.handleDatabaseError("loading next topic", () -> {
            try {
                Topic topic = executeQuery(sql, null, rs -> {
                    if (rs.next()) {
                        return createTopicFromResultSet(rs);
                    }
                    return null;
                });

                if (topic != null) {
                    ErrorHandler.executeWithErrorHandling("updating topic schedule", () -> {
                        try {
                            withConnection(connection -> {
                                try {
                                    increaseDate(topic.getRowId(), connection);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                return topic;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Topic findTopic(int rowid) {
        String sql = "select rowid, * from images WHERE rowid = ?";

        return ErrorHandler.handleDatabaseError("finding topic", () -> {
            try {
                return executeQuery(sql, pstmt -> pstmt.setInt(1, rowid), rs -> {
                    if (rs.next()) {
                        return createTopicFromResultSet(rs);
                    }
                    return null;
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void increaseDate(int rowid, Connection conn) throws Exception {
        String sql = "UPDATE images SET scheduled_at = datetime(scheduled_at, '+1 day') WHERE rowid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rowid);
            pstmt.executeUpdate();
            System.out.println("Updated rowid " + rowid + ".");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateContent(int rowid, RichTextArea richTextArea) {
        String sql = "UPDATE images SET content = ? WHERE rowid = ?";

        try {
            // Serialize rich text content using OutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            richTextArea.write(outputStream);
            String richTextContent = outputStream.toString();

            executeUpdate(sql, pstmt -> {
                pstmt.setString(1, richTextContent);
                pstmt.setInt(2, rowid);
            });

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

    // PDF operations
    public void savePDF(String pdfPath) {
        String sql = "INSERT INTO images (pdf_path, kind, current_page, added_at, scheduled_at, viewed_at, a_factor, priority) VALUES (?, 'pdf', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2.0, 0.5)";

        try {
            executeUpdate(sql, pstmt -> pstmt.setString(1, pdfPath));
            System.out.println("PDF saved: " + pdfPath);
        } catch (SQLException e) {
            System.out.println("Error saving PDF: " + e);
            e.printStackTrace(System.err);
        }
    }

    public void updatePDFPage(int rowid, int pageNumber) {
        String sql = "UPDATE images SET current_page = ? WHERE rowid = ?";

        try {
            executeUpdate(sql, pstmt -> {
                pstmt.setInt(1, pageNumber);
                pstmt.setInt(2, rowid);
            });

            System.out.println("Updated PDF page for rowid " + rowid + " to page " + pageNumber);

        } catch (SQLException e) {
            System.err.println("Error updating PDF page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Parent/extraction operations
    public String getParentPdfPath(int parentTopicId) {
        return findRootPdfPath(parentTopicId, 0);
    }

    private String findRootPdfPath(int topicId, int depth) {
        if (depth > 10) {
            System.err.println("Maximum depth reached while traversing parent chain for topic " + topicId);
            return null;
        }

        String sql = "SELECT pdf_path, kind, parent_topic FROM images WHERE rowid = ?";
        try {
            return executeQuery(sql, pstmt -> pstmt.setInt(1, topicId), rs -> {
                if (rs.next()) {
                    String pdfPath = rs.getString("pdf_path");
                    String kind = rs.getString("kind");
                    int parentTopicId = rs.getInt("parent_topic");

                    // If this topic has a PDF path, return it
                    if (pdfPath != null && !pdfPath.trim().isEmpty()) {
                        return pdfPath;
                    }

                    // If this is an extract with a parent, recurse to find the root PDF
                    if ("extract".equals(kind) && parentTopicId > 0 && parentTopicId != topicId) {
                        return findRootPdfPath(parentTopicId, depth + 1);
                    }

                    // If this is a non-extract with a parent, try parent
                    if (parentTopicId > 0 && parentTopicId != topicId) {
                        return findRootPdfPath(parentTopicId, depth + 1);
                    }

                    // No PDF path found
                    return null;
                }
                return null;
            });
        } catch (SQLException e) {
            System.err.println("Error querying parent PDF path: " + e.getMessage());
            return null;
        }
    }

    public Integer getRootPdfParentId(int parentTopicId) {
        return findRootPdfParentId(parentTopicId, 0);
    }

    private Integer findRootPdfParentId(int topicId, int depth) {
        if (depth > 10) {
            System.err.println("Maximum depth reached while traversing parent chain for topic " + topicId);
            return null;
        }

        String sql = "SELECT pdf_path, kind, parent_topic FROM images WHERE rowid = ?";
        try {
            return executeQuery(sql, pstmt -> pstmt.setInt(1, topicId), rs -> {
                if (rs.next()) {
                    String pdfPath = rs.getString("pdf_path");
                    String kind = rs.getString("kind");
                    int parentTopicId = rs.getInt("parent_topic");

                    // If this topic has a PDF path, return its ID
                    if (pdfPath != null && !pdfPath.trim().isEmpty()) {
                        return topicId;
                    }

                    // If this is an extract with a parent, recurse to find the root PDF
                    if ("extract".equals(kind) && parentTopicId > 0 && parentTopicId != topicId) {
                        return findRootPdfParentId(parentTopicId, depth + 1);
                    }

                    // No PDF parent found
                    return null;
                }
                return null;
            });
        } catch (SQLException e) {
            System.err.println("Error querying root PDF parent ID: " + e.getMessage());
            return null;
        }
    }

    // Table data operations
    public List<TopicTableData> getAllTopics() {
        String sql = "SELECT rowid, added_at, scheduled_at, viewed_at, a_factor, priority, title, pdf_path, kind, current_page, pdf_page, parent_topic FROM images ORDER BY rowid DESC";

        try {
            return executeQueryList(sql, null, rs -> {
                int id = rs.getInt("rowid");
                String addedDate = rs.getString("added_at");
                String scheduledDate = rs.getString("scheduled_at");
                String viewedDate = rs.getString("viewed_at");
                double aFactor = rs.getDouble("a_factor");
                double priority = rs.getDouble("priority");
                String title = rs.getString("title");
                String pdfPath = rs.getString("pdf_path");
                String kind = rs.getString("kind");
                int currentPage = rs.getInt("current_page");
                int pdfPage = rs.getInt("pdf_page");
                int parentTopic = rs.getInt("parent_topic");

                return new TopicTableData(id, addedDate, scheduledDate, viewedDate, aFactor, priority, title, pdfPath,
                        kind, currentPage, pdfPage, parentTopic);
            });

        } catch (SQLException e) {
            System.err.println("Error fetching all topics: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Rectangle operations
    public void saveRectangle(RectangleData rectangle) {
        String sql = "INSERT INTO rectangles (item_id, pdf_page, rect_x1, rect_y1, rect_x2, rect_y2) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            executeUpdate(sql, pstmt -> {
                pstmt.setInt(1, rectangle.getItemId());
                pstmt.setInt(2, rectangle.getPdfPage());
                pstmt.setDouble(3, rectangle.getX1());
                pstmt.setDouble(4, rectangle.getY1());
                pstmt.setDouble(5, rectangle.getX2());
                pstmt.setDouble(6, rectangle.getY2());
            });

            System.out.println("Saved rectangle for item " + rectangle.getItemId() + " page " + rectangle.getPdfPage());

        } catch (SQLException e) {
            System.err.println("Error saving rectangle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<RectangleData> getRectanglesForPage(int itemId, int pdfPage) {
        String sql = "SELECT * FROM rectangles WHERE item_id = ? AND pdf_page = ?";

        try {
            return executeQueryList(sql, pstmt -> {
                pstmt.setInt(1, itemId);
                pstmt.setInt(2, pdfPage);
            }, rs -> {
                return new RectangleData(
                        rs.getInt("item_id"),
                        rs.getInt("pdf_page"),
                        rs.getDouble("rect_x1"),
                        rs.getDouble("rect_y1"),
                        rs.getDouble("rect_x2"),
                        rs.getDouble("rect_y2"));
            });

        } catch (SQLException e) {
            System.err.println("Error loading rectangles: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void deleteRectanglesForPage(int itemId, int pdfPage) {
        String sql = "DELETE FROM rectangles WHERE item_id = ? AND pdf_page = ?";

        try {
            int deleted = executeUpdate(sql, pstmt -> {
                pstmt.setInt(1, itemId);
                pstmt.setInt(2, pdfPage);
            });

            System.out.println("Deleted " + deleted + " rectangles for item " + itemId + " page " + pdfPage);

        } catch (SQLException e) {
            System.err.println("Error deleting rectangles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveExtractedTopic(InputStream inputStream, int imageLength, int parentTopicId, int pdfPage) {
        String sql = "INSERT INTO images (img, kind, parent_topic, pdf_page, added_at, scheduled_at, viewed_at, a_factor, priority) VALUES (?, 'extract', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2.0, 0.5)";

        try {
            executeUpdate(sql, pstmt -> {
                pstmt.setBinaryStream(1, inputStream, imageLength);
                pstmt.setInt(2, parentTopicId);
                pstmt.setInt(3, pdfPage);
            });
            System.out.println("Saved extracted topic from parent " + parentTopicId + ", page " + pdfPage);
        } catch (SQLException e) {
            System.err.println("Error saving extracted topic: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
