package database;

import java.sql.*;
import javax.swing.JOptionPane;

public class DatabaseHandler {
    private static DatabaseHandler instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/aquaflow_db";
    private final String USER = "root";
    private final String PASS = "";

    private DatabaseHandler() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "DB Connection Failed!");
        }
    }

    public static DatabaseHandler getInstance() {
        try {
            if (instance == null || instance.getConnection().isClosed()) {
                instance = new DatabaseHandler();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}