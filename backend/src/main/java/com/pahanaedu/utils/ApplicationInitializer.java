package com.pahanaedu.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Application initializer that runs when the web application starts.
 * Handles database initialization and any other startup tasks.
 */
@WebListener
public class ApplicationInitializer implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(ApplicationInitializer.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Pahana Edu Bookshop application starting up...");

        try {
            initializeDatabase();
            LOGGER.info("Database initialization completed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize application", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Pahana Edu Bookshop application shutting down...");

        DatabaseConnection.getInstance().closeConnection();
    }

    /**
     * Initializes the database tables by running the schema.sql script
     */
    private void initializeDatabase() {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

        Connection connection;
        try {
            connection = dbConnection.getConnection();
            executeInitScript(connection);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database tables", e);
        }
    }

    /**
     * Executes the database initialization SQL script
     *
     * @param connection The database connection
     */
    private void executeInitScript(Connection connection) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
            if (is == null) {
                LOGGER.severe("Could not find schema.sql in classpath");
                return;
            }

            StringBuilder sqlScript = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sqlScript.append(line).append("\n");
                }
            }

            String[] statements = sqlScript.toString().split(";");

            try (Statement stmt = connection.createStatement()) {
                for (String statement : statements) {
                    String trimmedStatement = statement.trim();
                    if (!trimmedStatement.isEmpty()) {
                        LOGGER.fine("Executing SQL: " + trimmedStatement);
                        stmt.execute(trimmedStatement);
                    }
                }
            }

            LOGGER.info("Database initialization script executed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing database initialization script", e);
        }
    }
}
