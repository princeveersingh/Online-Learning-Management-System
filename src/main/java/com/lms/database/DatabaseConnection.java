package com.lms.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection — Singleton Pattern (OOP: Encapsulation + Design Pattern)
 *
 * This class manages a single shared JDBC connection to the MySQL database.
 * Singleton ensures only one connection object exists at a time, preventing
 * resource leaks. The connection parameters are loaded from config.properties
 * at runtime — credentials are NEVER hardcoded in source code.
 *
 * Thread-safety: getInstance() is synchronized to avoid race conditions when
 * multiple threads call it simultaneously.
 */
public class DatabaseConnection {

    // The single instance — volatile ensures visibility across threads
    private static volatile DatabaseConnection instance = null;

    // The actual JDBC connection object
    private Connection connection;

    // Config keys matching config.properties
    private static final String CONFIG_FILE = "config.properties";

    // Private constructor prevents instantiation from outside (Singleton)
    private DatabaseConnection() throws IOException, SQLException, ClassNotFoundException {
        Properties props = loadProperties();

        String host     = props.getProperty("db.host", "localhost");
        String port     = props.getProperty("db.port", "3306");
        String dbName   = props.getProperty("db.name", "lms_db");
        String user     = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        if (user == null || password == null) {
            throw new IOException(
                "Database credentials missing in config.properties. " +
                "Please copy config.properties.example to config.properties and fill in your credentials."
            );
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                     + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        // Load the MySQL JDBC driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        this.connection = DriverManager.getConnection(url, user, password);
        System.out.println("[DatabaseConnection] Connected to MySQL database: " + dbName);
    }

    /**
     * Returns the singleton instance of DatabaseConnection.
     * Double-checked locking for thread safety (Multithreading: synchronization).
     */
    public static DatabaseConnection getInstance() throws IOException, SQLException, ClassNotFoundException {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {          // Thread-safe lock
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the active JDBC Connection.
     * Re-connects if the connection was closed (e.g. after timeout).
     */
    public Connection getConnection() throws IOException, SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            // Re-initialise singleton if connection died
            instance = null;
            return DatabaseConnection.getInstance().connection;
        }
        return connection;
    }

    /**
     * Loads database configuration from the resources/config.properties file.
     * The file is read from the classpath so both IDE and jar execution work.
     */
    private Properties loadProperties() throws IOException {
        Properties props = new Properties();

        // Try classpath first (when running from IDE or jar)
        InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);

        if (in == null) {
            // Fallback: load from file system (resources/ folder relative to working dir)
            java.io.File file = new java.io.File("resources/" + CONFIG_FILE);
            if (!file.exists()) {
                throw new IOException(
                    "config.properties not found. " +
                    "Please create resources/config.properties from config.properties.example."
                );
            }
            in = new java.io.FileInputStream(file);
        }

        props.load(in);
        in.close();
        return props;
    }

    /**
     * Closes the connection. Call this when the application exits.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DatabaseConnection] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Error closing connection: " + e.getMessage());
        }
    }
}
