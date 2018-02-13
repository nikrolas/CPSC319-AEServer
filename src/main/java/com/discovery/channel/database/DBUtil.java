package com.discovery.channel.database;

import com.discovery.channel.model.Record;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
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

    // Single record view
    public static Record getRecordById(Integer id) throws SQLException {

        String stateName;
        String containerName;
        HashMap schedule;
        String scheduleName = "";
        String scheduleYear = "";
        String typeName;
        String locationName;

        Connection conn = DriverManager.getConnection(getJdbcUrl(DEFAULT_DATABASE), USERNAME, PASSWORD);
        Statement statement = conn.createStatement();
        String query = "SELECT * FROM records WHERE id = " + id;
        ResultSet result = statement.executeQuery(query);
        result.next();

        if(result.getString("StateId") == null){
            stateName = null;
        }else{
            stateName = getStateName(id);
        }

        if(result.getString("ContainerId") == null){
            containerName = null;
        }else{
            containerName = getContainerNumber(id);
        }

        if(result.getString("ScheduleId") == null){
            scheduleName = null;
            scheduleYear = null;
        }else{
            schedule = getScheduleName(id);
            scheduleName = schedule.get("Name").toString();
            scheduleYear = schedule.get("Years").toString();
        }

        if(result.getString("TypeId") == null){
            typeName = null;
        }else{
            typeName = getTypeName(id);
        }

        if(result.getString("LocationId") == null){
            locationName = null;
        }else{
            locationName = getLocationName(id);

        }


        Record record = new Record(result,
                scheduleName,
                scheduleYear,
                typeName,
                stateName,
                containerName,
                locationName);

        return record;

    }

    private static String getLocationName(Integer id) throws SQLException{

        Connection conn = DriverManager.getConnection(getJdbcUrl(DEFAULT_DATABASE), USERNAME, PASSWORD);
        Statement statement = conn.createStatement();
        String queryLocation = "SELECT locations.Name FROM records "
                + "Inner JOIN recordr.locations on records.LocationId = locations.id"
                + " WHERE records.id = " + id;

        ResultSet result = statement.executeQuery(queryLocation);
        result.next();
        return result.getString("Name");

    }

    private static String getTypeName(Integer id) throws SQLException{

        Connection conn = DriverManager.getConnection(getJdbcUrl(DEFAULT_DATABASE), USERNAME, PASSWORD);
        Statement statement = conn.createStatement();
        String queryType = "SELECT recordtypes.Name FROM records "
                + "Inner JOIN recordr.recordtypes on records.TypeId = recordtypes.Id"
                + " WHERE records.id = " + id;

        ResultSet result = statement.executeQuery(queryType);
        result.next();
        return result.getString("Name");
    }

    private static HashMap<String, String> getScheduleName(Integer id) throws SQLException{
        Connection conn = DriverManager.getConnection(getJdbcUrl(DEFAULT_DATABASE), USERNAME, PASSWORD);
        Statement statement = conn.createStatement();
        String querySchedule = "SELECT * FROM records "
                + "Inner JOIN recordr.retentionschedules on records.ScheduleId = retentionschedules.Id"
                + " WHERE records.id = " + id;

        ResultSet result = statement.executeQuery(querySchedule);
        result.next();

        HashMap<String, String> temp = new HashMap<>();
        temp.put("Name", result.getString("Name"));
        temp.put("Years", result.getString("Years"));


        return temp;
    }

    private static String getContainerNumber(Integer id) throws SQLException{
        Connection conn = DriverManager.getConnection(getJdbcUrl(DEFAULT_DATABASE), USERNAME, PASSWORD);
        Statement statement = conn.createStatement();
        String queryContainer = "SELECT containers.Number FROM records "
                + "Inner JOIN recordr.containers on records.ContainerId = containers.Id"
                + " WHERE records.id = " + id;

        ResultSet result = statement.executeQuery(queryContainer);
        result.next();
        return result.getString("Number");
    }

    private static String getStateName(Integer id) throws SQLException {
        Connection conn = DriverManager.getConnection(getJdbcUrl(DEFAULT_DATABASE), USERNAME, PASSWORD);
        Statement statement = conn.createStatement();
        String queryState = "SELECT recordstates.Name FROM records "
                + "Inner JOIN recordr.recordstates on records.StateId = recordstates.Id"
                + " WHERE records.id = " + id;
        ResultSet result = statement.executeQuery(queryState);

        result.next();
        return result.getString("Name");
    }
}
