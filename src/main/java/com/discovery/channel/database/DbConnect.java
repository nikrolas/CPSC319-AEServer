package com.discovery.channel.database;

import com.discovery.channel.properties.DefaultProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DbConnect {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbConnect.class);
    private static DefaultProperties PROPERTIES = DefaultProperties.getInstance();

    private static String HOSTNAME = PROPERTIES.getProperty("DATABASE.HOST");
    public static int PORT = PROPERTIES.getIntProperty("DATABASE.PORT");
    public static final String DRIVER = PROPERTIES.getProperty("DATABASE.DRIVER");
    public static final String DRIVER_NAME = PROPERTIES.getProperty("DATABASE.DRIVER_NAME");
    public static final String USERNAME = PROPERTIES.getProperty("DATABASE.USERNAME");
    public static final String PASSWORD = PROPERTIES.getProperty("DATABASE.PASSWORD");
    public static final String DEFAULT_DATABASE = PROPERTIES.getProperty("DATABASE");
    private static final String JDBC_URL = String.format(PROPERTIES.getProperty("DATABASE.JDBC.TEMPLATE"), DRIVER, HOSTNAME, PORT, DEFAULT_DATABASE, USERNAME, PASSWORD);

    // Init connection pool properties
    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(DRIVER_NAME);
        config.setJdbcUrl(getJdbcUrl());
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    // Hide constructor
    private DbConnect() {
    }

    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            LOGGER.error("Failed to get connection from connection pool", e);
            return null;
        }
    }

    /**
     * Format DB credential
     */
    private static String getJdbcUrl() {
        return String.format(JDBC_URL, DRIVER, HOSTNAME, PORT, DEFAULT_DATABASE);
    }

}
