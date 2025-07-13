package com.adham_omran;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.scene.image.Image;

public class Database {
    String DB_PATH = "jdbc:sqlite:test.db";

    public void saveImage(InputStream input_stream, int image_length) {
        // NOTE: Connection and Statement are AutoCloseable.
        // Don't forget to close them both in order to avoid leaks.

        // Save the image with the current timestamp.
        try (
                Connection connection = DriverManager.getConnection(DB_PATH);
                Statement statement = connection.createStatement();) {
            statement.setQueryTimeout(1);
            statement.executeUpdate("create table if not exists images (img blob)");
            // Binary Stream Statement
            String updateImage =
                "INSERT INTO images (img, added_at, scheduled_at, viewed_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
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

   public Image nextImage() {
        String sql = "select * from images order by scheduled_at asc";

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
}
