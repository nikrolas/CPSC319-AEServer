package com.discovery.channel.database;

import com.discovery.channel.properties.DefaultProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class DbConnect {
    private static DefaultProperties PROPERTIES = DefaultProperties.getInstance();

    private static String HOSTNAME = PROPERTIES.getProperty("DATABASE.HOST");
    public static int PORT = PROPERTIES.getIntProperty("DATABASE.PORT");
    public static final String DRIVER = PROPERTIES.getProperty("DATABASE.DRIVER");
    public static final String USERNAME = PROPERTIES.getProperty("DATABASE.USERNAME");
    public static final String PASSWORD =  PROPERTIES.getProperty("DATABASE.PASSWORD");
    public static final String DEFAULT_DATABASE = PROPERTIES.getProperty("DATABASE");
    private static final String JDBC_URL = String.format(PROPERTIES.getProperty("DATABASE.JDBC.TEMPLATE"), DRIVER, HOSTNAME, PORT, DEFAULT_DATABASE, USERNAME, PASSWORD);

    private Connection connection;

    /**
     * Get DB connection
     */
    public void dbConnect(){

        connection = null;

        try {
            connection = DriverManager.getConnection(getJdbcUrl(DEFAULT_DATABASE), USERNAME, PASSWORD);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            System.out.println("Connection Failed. Check output console");
            e.printStackTrace();
        }

    }

    /**
     * Format DB credential
     */
    private static String getJdbcUrl(String database){
        return String.format(JDBC_URL, DRIVER, HOSTNAME, PORT, database, USERNAME, PASSWORD);
    }

    public Connection getConnection(){return connection;}

}
