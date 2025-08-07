package com.adham_omran;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
        String sql = "INSERT INTO images (img, added_at, scheduled_at, viewed_at, a_factor, priority) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2.0, 0.5)";

        try {
            DatabaseUtils.executeUpdate(sql, pstmt -> {
                pstmt.setBinaryStream(1, input_stream, image_length);
            });
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.out.println(e);
            e.printStackTrace(System.err);
        }
    }

    public Image readImage() {
        String sql = "select img, scheduled_at from images order by scheduled_at desc";

        return ErrorHandler.handleDatabaseError("reading image", () -> {
            try {
                return DatabaseUtils.executeQuery(sql, null, rs -> {
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

    public Topic nextTopic() {
        String sql = "select rowid, * from images order by scheduled_at asc";

        return ErrorHandler.handleDatabaseError("loading next topic", () -> {
            try {
                Topic topic = DatabaseUtils.executeQuery(sql, null, rs -> {
                    if (rs.next()) {
                        return DatabaseUtils.createTopicFromResultSet(rs);
                    }
                    return null;
                });

                if (topic != null) {
                    ErrorHandler.executeWithErrorHandling("updating topic schedule", () -> {
                        try {
                            DatabaseUtils.withConnection(connection -> {
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
                return DatabaseUtils.executeQuery(sql, pstmt -> pstmt.setInt(1, rowid), rs -> {
                    if (rs.next()) {
                        return DatabaseUtils.createTopicFromResultSet(rs);
                    }
                    return null;
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
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

        try {
            // Serialize rich text content using OutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            richTextArea.write(outputStream);
            String richTextContent = outputStream.toString();

            DatabaseUtils.executeUpdate(sql, pstmt -> {
                pstmt.setString(1, richTextContent);
                pstmt.setInt(2, rowid);
            });

            System.out.println("Updated content for rowid " + rowid + ".");

        } catch (Exception e) {
            System.err.println("Error updating content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getParentPdfPath(int parentTopicId) {
        String sql = "SELECT pdf_path FROM images WHERE rowid = ?";
        try {
            return DatabaseUtils.executeQuery(sql, pstmt -> pstmt.setInt(1, parentTopicId), rs -> {
                if (rs.next()) {
                    return rs.getString("pdf_path");
                }
                return null;
            });
        } catch (SQLException e) {
            System.err.println("Error querying parent PDF path: " + e.getMessage());
            return null;
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
        String sql = "INSERT INTO images (pdf_path, current_page, added_at, scheduled_at, viewed_at, a_factor, priority) VALUES (?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2.0, 0.5)";

        try {
            DatabaseUtils.executeUpdate(sql, pstmt -> {
                pstmt.setString(1, pdfPath);
            });
            System.out.println("PDF saved: " + pdfPath);
        } catch (SQLException e) {
            System.out.println("Error saving PDF: " + e);
            e.printStackTrace(System.err);
        }
    }

    public void updatePDFPage(int rowid, int pageNumber) {
        String sql = "UPDATE images SET current_page = ? WHERE rowid = ?";

        try {
            DatabaseUtils.executeUpdate(sql, pstmt -> {
                pstmt.setInt(1, pageNumber);
                pstmt.setInt(2, rowid);
            });

            System.out.println("Updated PDF page for rowid " + rowid + " to page " + pageNumber);

        } catch (SQLException e) {
            System.err.println("Error updating PDF page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<TopicTableData> getAllTopics() {
        String sql = "SELECT rowid, added_at, scheduled_at, a_factor, priority, title, pdf_path FROM images ORDER BY rowid DESC";

        try {
            return DatabaseUtils.executeQueryList(sql, null, rs -> {
                int id = rs.getInt("rowid");
                String addedDate = rs.getString("added_at");
                String scheduledDate = rs.getString("scheduled_at");
                double aFactor = rs.getDouble("a_factor");
                double priority = rs.getDouble("priority");
                String title = rs.getString("title");
                String pdfPath = rs.getString("pdf_path");

                // Determine type
                String type = (pdfPath != null && !pdfPath.trim().isEmpty()) ? "PDF" : "Image";

                return new TopicTableData(id, type, title, addedDate, scheduledDate, priority, aFactor);
            });

        } catch (SQLException e) {
            System.err.println("Error fetching all topics: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveRectangle(RectangleData rectangle) {
        String sql = "INSERT INTO rectangles (item_id, pdf_page, rect_x1, rect_y1, rect_x2, rect_y2) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            DatabaseUtils.executeUpdate(sql, pstmt -> {
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
            return DatabaseUtils.executeQueryList(sql, pstmt -> {
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
            int deleted = DatabaseUtils.executeUpdate(sql, pstmt -> {
                pstmt.setInt(1, itemId);
                pstmt.setInt(2, pdfPage);
            });

            System.out.println("Deleted " + deleted + " rectangles for item " + itemId + " page " + pdfPage);

        } catch (SQLException e) {
            System.err.println("Error deleting rectangles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveExtractedTopic(InputStream input_stream, int image_length, int parentTopicId, int pdfPage) {
        String sql = "INSERT INTO images (img, kind, parent_topic, pdf_page, added_at, scheduled_at, viewed_at, a_factor, priority) VALUES (?, 'extract', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2.0, 0.5)";

        try {
            DatabaseUtils.executeUpdate(sql, pstmt -> {
                pstmt.setBinaryStream(1, input_stream, image_length);
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
