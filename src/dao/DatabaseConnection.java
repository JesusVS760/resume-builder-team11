package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private static final String DB_URL = "jdbc:sqlite:database/database.db";
    private Connection connection;
    
    private DatabaseConnection() {
        initializeDatabase();
    }
    
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeDatabase() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Create database connection
            connection = DriverManager.getConnection(DB_URL);
            
            // Read and execute schema
            executeSchema();
            
            System.out.println("Database initialized successfully!");
            
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database initialization failed!");
            e.printStackTrace();
        }
    }
    
    private void executeSchema() {
        try {
            StringBuilder schema = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader("resources/schema.sql"));
            String line;
            
            while ((line = reader.readLine()) != null) {
                schema.append(line).append("\n");
            }
            reader.close();
            
            Statement stmt = connection.createStatement();
            String[] statements = schema.toString().split(";");
            
            for (String statement : statements) {
                if (statement.trim().length() > 0) {
                    stmt.execute(statement);
                }
            }
            stmt.close();
            
        } catch (IOException | SQLException e) {
            // Only print error if it's not about table already existing
            if (!e.getMessage().contains("already exists")) {
                System.err.println("Error executing schema: " + e.getMessage());
            }
        }
    }
}
