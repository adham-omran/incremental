package com.adham_omran;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    public static void main(InputStream input_stream, int image_length) {
        // NOTE: Connection and Statement are AutoCloseable.
        // Don't forget to close them both in order to avoid leaks.
        var DB_PATH = "jdbc:sqlite:test.db";
        try (
                // create a database connection
                Connection connection = DriverManager.getConnection(DB_PATH);
                Statement statement = connection.createStatement();) {
            statement.setQueryTimeout(1);
            statement.executeUpdate("create table if not exists images (id integer, img blob)");
            // Binary Blob Statement
            String updateImage = "INSERT INTO images (id, img) VALUES (1,?)";
            try (PreparedStatement updateImg = connection.prepareStatement(updateImage);)
                {
                    connection.setAutoCommit(false);
                    updateImg.setBinaryStream(1, input_stream, image_length);
                    updateImg.executeUpdate();
                    connection.commit();
                }


            statement.executeUpdate("drop table if exists person");
            ResultSet rs = statement.executeQuery("select * from person");
            while (rs.next()) {
                // read the result set
                System.out.println("name = " + rs.getString("name"));
                System.out.println("id = " + rs.getInt("id"));
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.out.println(e);
            e.printStackTrace(System.err);
        }
    }
}
