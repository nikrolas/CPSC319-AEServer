package com.discovery.channel.database;

import com.discovery.channel.model.Record;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Qiushan on 2018/1/20.
 */
public class DBUtil extends dbConnect{

    private Connection connection;
    private Statement statement;

    /**
     * Get DB connection
     */
    public DBUtil(){
        this.dbConnect();
        this.connection = this.getConnection();
    }


    /**
     * Prepare SQL statement
     *
     * @param
     * @return SQL query
     */
    public Statement getStatement() throws SQLException {
        this.statement = connection.createStatement();
        return this.statement;
    }


    /**
     * Execute SQL statement
     *
     * @param
     * @return SQL result after executing SQL query
     */
    public ResultSet getResult(String query) throws SQLException {
        return this.getStatement().executeQuery(query);
    }


//    // TODO move to config file
//    public static String HOSTNAME = "cs319-discoverychannel.csgbwdrdx2ye.us-east-2.rds.amazonaws.com";
//    public static int PORT = 3306;
//    public static String DRIVER ="mysql";
//    public static String USERNAME = "cs319_rds";
//    public static String PASSWORD = "discoverychannel";
//
//    public static String DEFAULT_DATABASE = "recordr";
//
//    private static String getJdbcUrl(String database){
//        // TODO move to config file
//        //String template = "jdbc:%s://%s:%s/%s?useSSL=false";
//        //jdbc:driver://hostname:port/dbName?user=userName&password=password
//        String template = "jdbc:%s://%s:%d/%s?user=%s&password=%s&useSSL=false";
//        return String.format(template, DRIVER, HOSTNAME, PORT, database, USERNAME, PASSWORD);
//    }

    // Find record with give ID; If not, return null
//    private static String GET_RECORD_BY_ID_SQL = "SELECT * " +
//            "FROM records " +
//            "WHERE Id = ?";



    /**
     * Retrieve records filtered by record number
     *
     * @param recordNumber
     * @return a list of records
     */
    public List<Record> getRecordByNumber(String recordNumber) throws SQLException{
        List<Record> records = new ArrayList<>();
        String query = "SELECT * FROM records WHERE Number LIKE " + "'%" + recordNumber + "%'";
        ResultSet results = getResult(query);
        while(results.next()){
            records.add(new Record(results));
        }

        return records;
    }


    /**
     * Retrieve all records
     *
     * @return a list of records, currently limit 20 order by UpdatedAt
     */
    public List<Record> getAllRecords() throws SQLException{

        List<Record> records = new ArrayList<>();
        String query = "SELECT * FROM records ORDER BY UpdatedAt LIMIT 20";
        ResultSet results = getResult(query);
        while(results.next()){
            records.add(new Record(results));
        }
        return records;
    }



    /**
     * Retrieve a record
     *
     * @param id
     * @return a single record
     */
    public Record getRecordById(Integer id) throws SQLException {

        String stateName = null;
        String containerName = null;
        HashMap schedule;
        String scheduleName = null;
        String scheduleYear = null;
        String typeName = null;
        String locationName = null;

        String query = "SELECT * FROM records WHERE id = " + id;
        ResultSet result = getResult(query);
        result.next();

        if(result.getString("StateId") != null)
            stateName = getStateName(id);

        if(result.getString("ContainerId") != null)
            containerName = getContainerNumber(id);

        if(result.getString("ScheduleId") != null){
            schedule = getScheduleName(id);
            scheduleName = schedule.get("Name").toString();
            scheduleYear = schedule.get("Years").toString();
        }

        if(result.getString("TypeId") == null)
            typeName = getTypeName(id);

        if(result.getString("LocationId") == null)
            locationName = getLocationName(id);


        Record record = new Record(result,
                scheduleName,
                scheduleYear,
                typeName,
                stateName,
                containerName,
                locationName);

        return record;

    }

    /**
     * Join records table with locations table to get location name
     *
     * @param id
     * @return location name
     */
    private String getLocationName(Integer id) throws SQLException{

        String queryLocation = "SELECT locations.Name FROM records "
                + "Inner JOIN recordr.locations on records.LocationId = locations.id"
                + " WHERE records.id = " + id;

        ResultSet result = getResult(queryLocation);
        result.next();
        return result.getString("Name");

    }


    /**
     * Join records table with recordtypes table to get type name
     *
     * @param id
     * @return type name
     */
    private String getTypeName(Integer id) throws SQLException{

        String queryType = "SELECT recordtypes.Name FROM records "
                + "Inner JOIN recordr.recordtypes on records.TypeId = recordtypes.Id"
                + " WHERE records.id = " + id;

        ResultSet result = getResult(queryType);
        result.next();
        return result.getString("Name");
    }


    /**
     * Join records table with retentionschedules table to get schedule name and years
     *
     * @param id
     * @return schedule name and years
     */
    private HashMap<String, String> getScheduleName(Integer id) throws SQLException{

        String querySchedule = "SELECT * FROM records "
                + "Inner JOIN recordr.retentionschedules on records.ScheduleId = retentionschedules.Id"
                + " WHERE records.id = " + id;

        ResultSet result = getResult(querySchedule);
        result.next();

        HashMap<String, String> temp = new HashMap<>();
        temp.put("Name", result.getString("Name"));
        temp.put("Years", result.getString("Years"));


        return temp;
    }


    /**
     * Join records table with containers table to get container name
     *
     * @param id
     * @return container name
     */
    private String getContainerNumber(Integer id) throws SQLException{

        String queryContainer = "SELECT containers.Number FROM records "
                + "Inner JOIN recordr.containers on records.ContainerId = containers.Id"
                + " WHERE records.id = " + id;

        ResultSet result = getResult(queryContainer);
        result.next();
        return result.getString("Number");
    }


    /**
     * Join records table with recordstates table to get state name
     *
     * @param id
     * @return state name
     */
    private String getStateName(Integer id) throws SQLException {

        String queryState = "SELECT recordstates.Name FROM records "
                + "Inner JOIN recordr.recordstates on records.StateId = recordstates.Id"
                + " WHERE records.id = " + id;
        ResultSet result = getResult(queryState);

        result.next();
        return result.getString("Name");
    }
}
