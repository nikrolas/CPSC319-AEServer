package com.discovery.channel.database;

import com.discovery.channel.model.Record;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Qiushan on 2018/1/20.
 */
public class DBUtil {
    // TODO move to config file
    public static String HOSTNAME = "cs319-discoverychannel.csgbwdrdx2ye.us-east-2.rds.amazonaws.com";
    public static int PORT = 3306;
    public static String DRIVER ="mysql";
    public static String USERNAME = "cs319_rds";
    public static String PASSWORD = "discoverychannel";

    public static String DEFAULT_DATABASE = "recordr";

    private static String getJdbcUrl(String database){
        // TODO move to config file
        //String template = "jdbc:%s://%s:%s/%s?useSSL=false";
        //jdbc:driver://hostname:port/dbName?user=userName&password=password
        String template = "jdbc:%s://%s:%d/%s?user=%s&password=%s&useSSL=false";
        return String.format(template, DRIVER, HOSTNAME, PORT, database, USERNAME, PASSWORD);
    }

    // Find record with give ID; If not, return null
//    private static String GET_RECORD_BY_ID_SQL = "SELECT * " +
//            "FROM records " +
//            "WHERE Id = ?";



    public static List<Record> getRecordByNumber(String recordNumber) throws SQLException{
        List<Record> records = new ArrayList<>();
        Connection conn = DriverManager.getConnection(getJdbcUrl(DEFAULT_DATABASE), USERNAME, PASSWORD);
        Statement statement = conn.createStatement();
        String query = "SELECT * FROM records WHERE Number LIKE " + "'%" + recordNumber + "%'";
        ResultSet results = statement.executeQuery(query);
        while(results.next()){
            records.add(new Record(results));
        }

        return records;
    }


    public static List<Record> getAllRecords() throws SQLException{

        List<Record> records = new ArrayList<>();
        Connection conn = DriverManager.getConnection(getJdbcUrl(DEFAULT_DATABASE), USERNAME, PASSWORD);
        Statement statement = conn.createStatement();
        String query = "SELECT * FROM records ORDER BY UpdatedAt LIMIT 20";
        ResultSet results = statement.executeQuery(query);
        while(results.next()){
            records.add(new Record(results));
        }
        return records;
    }

    public static Record getRecordById(Integer id) throws SQLException {
        Connection conn = DriverManager.getConnection(getJdbcUrl(DEFAULT_DATABASE), USERNAME, PASSWORD);
        Statement statement = conn.createStatement();
        String query = "SELECT * FROM records WHERE id = " + id;
        ResultSet result = statement.executeQuery(query);
        result.next();
        Record record = new Record(result);

        return record;

    }
}
