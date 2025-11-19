package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

// IMPORTS ADDED: These are needed for the logging statements inside closeConnection()
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseHandler {
    // LOGGER DEFINITION ADDED: This allows you to use LOGGER.info() and
    // LOGGER.log()
    private static final Logger LOGGER = Logger.getLogger(DatabaseHandler.class.getName());

    private static DatabaseHandler instance;
    private Connection connection;

    // Your MySQL connection details
    private final String URL = "jdbc:mysql://localhost:3306/aquaflow_db";
    private final String USER = "root";
    private final String PASS = "";

    private DatabaseHandler() {
        try {
            // Attempt to load the driver (if using older Java or specific drivers)
            // Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASS);
            LOGGER.info("Database connection established successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "DB Connection Failed! Check if MySQL server is running and credentials are correct.");
            // Log the error for debugging
            LOGGER.log(Level.SEVERE, "Failed to connect to MySQL database.", e);
        }
    }

    public static DatabaseHandler getInstance() {
        try {
            // Re-establish connection if it's null or closed
            if (instance == null || instance.getConnection() == null || instance.getConnection().isClosed()) {
                instance = new DatabaseHandler();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Log the error during connection check/re-creation
            LOGGER.log(Level.WARNING, "Error checking or re-creating database connection instance.", e);
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Closes the database connection gracefully.
     * This is crucial to prevent resource leaks when the application shuts down.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    // Logging success
                    LOGGER.info("Database connection closed successfully.");
                }
            } catch (SQLException e) {
                // Logging error during closing
                LOGGER.log(Level.SEVERE, "Error closing the database connection.", e);
            }
        }
    }
}