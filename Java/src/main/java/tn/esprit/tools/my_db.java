package tn.esprit.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class my_db {
    private static final String URL = "jdbc:mysql://localhost:3306/falleh_Tech";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static my_db instance;
    private Connection con;

    private my_db() {
        try {
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage());
        }
    }

    public static synchronized my_db getInstance() {
        if (instance == null) {
            instance = new my_db();
        }
        return instance;
    }

    public Connection getConnection() {
        return con;
    }
}
