package com.adham_omran;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.scene.image.Image;

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

    public Topic nextImage() {
        String sql = "select rowid, * from images order by scheduled_at asc";
        Topic topic = new Topic();
        try (Connection connection = DriverManager.getConnection(DB_PATH);
                PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) { // Check if result exists
                try (InputStream is = rs.getBinaryStream("img")) {
                    if (is != null) {
                        topic.setTopicImage(new Image(is));
                        topic.setRowId(rs.getInt("rowid"));

                        increaseDate(topic.getRowId(), connection);
                        return topic;
                    }
                }
            }
            return null; // No image found

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
}
