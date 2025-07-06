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
        try (
                // create a database connection
                Connection connection = DriverManager.getConnection(DB_PATH);
                Statement statement = connection.createStatement();) {
            statement.setQueryTimeout(1);
            statement.executeUpdate("create table if not exists images (id integer, img blob)");
            // Binary Stream Statement
            String updateImage = "INSERT INTO images (id, img) VALUES (1,?)";
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
        String sql = "select img from images where id = 1";

        try (Connection connection = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) { // Check if result exists
                try (InputStream is = rs.getBinaryStream("img")) {
                    if (is != null) {
                        Image img = new Image(is);
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
