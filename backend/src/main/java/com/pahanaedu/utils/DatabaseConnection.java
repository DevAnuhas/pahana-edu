package com.pahanaedu.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static DatabaseConnection instance;
    private Connection connection;
    private Properties properties;

    private final String jdbcUrl;
    private final String username;
    private final String password;

    private DatabaseConnection() {
        try {
            loadProperties();
            this.jdbcUrl = properties.getProperty("app.datasource.url");
            this.username = properties.getProperty("app.datasource.username");
            this.password = properties.getProperty("app.datasource.password");

            Class.forName("com.mysql.cj.jdbc.Driver");

            LOGGER.info("Database driver loaded successfully");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load database driver", e);
            throw new RuntimeException("Failed to load database driver", e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load application.properties", e);
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    private void loadProperties() throws IOException {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                LOGGER.severe("Unable to find application.properties");
                throw new IOException("Unable to find application.properties");
            }
            properties.load(input);
            LOGGER.info("application.properties loaded successfully");
        }
    }

    /**
     * Gets the singleton instance of the DatabaseConnection class.
     *
     * @return The singleton instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Gets a connection to the database.
     *
     * @return A database connection
     * @throws SQLException If a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(jdbcUrl, username, password);
                LOGGER.info("Database connection established successfully");
            }
            return connection;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection", e);
            throw e;
        }
    }

    /**
     * Closes the database connection if it's open.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed successfully");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to close database connection", e);
        } finally {
            connection = null;
        }
    }
}
