package com.discovery.channel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbConnect {

    public static String HOSTNAME = "cs319-discoverychannel.csgbwdrdx2ye.us-east-2.rds.amazonaws.com";
    public static int PORT = 3306;
    public static String DRIVER ="mysql";
    public static String USERNAME = "cs319_rds";
    public static String PASSWORD = "discoverychannel";
    private Connection connection;

    public static String DEFAULT_DATABASE = "recordr";

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

    private static String getJdbcUrl(String database){

        //String template = "jdbc:%s://%s:%s/%s?useSSL=false";
        //jdbc:driver://hostname:port/dbName?user=userName&password=password
        String template = "jdbc:%s://%s:%d/%s?user=%s&password=%s&useSSL=false";
        return String.format(template, DRIVER, HOSTNAME, PORT, database, USERNAME, PASSWORD);
    }

    public Connection getConnection(){return connection;}

}
