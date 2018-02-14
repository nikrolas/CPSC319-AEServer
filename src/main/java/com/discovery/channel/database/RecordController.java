package com.discovery.channel.database;

import com.discovery.channel.model.Record;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class RecordController extends dbConnect{

    private Connection connection;
    private Statement statement;


//    private static String GET_RECORD_BY_ID_SQL = "SELECT * " +
//            "FROM records " +
//            "WHERE Id = ?";


    /**
     * Get DB connection
     */
    public RecordController(){
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


    /**
     * Retrieve records filtered by record number
     *
     * @param recordNumber
     * @return a list of records
     */
    public List<Record> getRecordByNumber(String recordNumber) throws SQLException{

        List<Record> records = new ArrayList<>();
        String query = "SELECT * FROM records WHERE Number LIKE " + "'%" + recordNumber + "%'"
                + "ORDER BY UpdatedAt LIMIT 20";
        ResultSet results = getResult(query);
        getListOfRecords(records, results);

        return records;
    }

    /**
     * Join tables for retrieve multiple records
     *
     * @param records
     * @param results
     */
    private void getListOfRecords(List<Record> records, ResultSet results) throws SQLException {
        while(results.next()){
            String locationName = getLocationName(results);
            String typeName = getTypeName(results);
            String stateName = getStateName(results);
            String containerName = getContainerNumber(results);

            HashMap schedule = getScheduleName(results);
            String scheduleName = schedule.get("Name").toString();
            String scheduleYear = schedule.get("Years").toString();

            records.add(new Record(results,
                    scheduleName,
                    scheduleYear,
                    typeName,
                    stateName,
                    containerName,
                    locationName));
        }
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
        getListOfRecords(records, results);

        return records;
    }


    /**
     * Retrieve a record
     *
     * @param id
     * @return a single record
     */
    public Record getRecordById(Integer id) throws SQLException {

        String query = "SELECT * FROM records WHERE id = " + id;
        ResultSet result = getResult(query);
        result.next();

        String locationName = getLocationName(result);
        String typeName = getTypeName(result);
        String stateName = getStateName(result);
        String containerName = getContainerNumber(result);

        HashMap schedule = getScheduleName(result);
        String scheduleName = schedule.get("Name").toString();
        String scheduleYear = schedule.get("Years").toString();


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
     * @param resultSet
     * @return location name
     */
    private String getLocationName(ResultSet resultSet) throws SQLException{

        if(resultSet.getString("LocationId") != null){
            String queryLocation = "SELECT locations.Name FROM records "
                    + "Inner JOIN recordr.locations on records.LocationId = locations.id"
                    + " WHERE records.id = " + resultSet.getString("id");

            ResultSet result = getResult(queryLocation);
            result.next();
            return result.getString("Name");

        }else{
            return null;
        }

    }


    /**
     * Join records table with recordtypes table to get type name
     *
     * @param resultSet
     * @return type name
     */
    private String getTypeName(ResultSet resultSet) throws SQLException{

        if(resultSet.getString("TypeId") != null){

            String queryType = "SELECT recordtypes.Name FROM records "
                    + "Inner JOIN recordr.recordtypes on records.TypeId = recordtypes.Id"
                    + " WHERE records.id = " + resultSet.getString("id");

            ResultSet result = getResult(queryType);
            result.next();
            return result.getString("Name");

        }else{
            return null;
        }

    }


    /**
     * Join records table with retentionschedules table to get schedule name and years
     *
     * @param resultSet
     * @return schedule name and years
     */
    private HashMap<String, String> getScheduleName(ResultSet resultSet) throws SQLException{

        if(resultSet.getString("ScheduleId") != null){

            String querySchedule = "SELECT * FROM records "
                    + "Inner JOIN recordr.retentionschedules on records.ScheduleId = retentionschedules.Id"
                    + " WHERE records.id = " + resultSet.getString("id");

            ResultSet result = getResult(querySchedule);
            result.next();

            HashMap<String, String> scheduleDict = new HashMap<>();
            scheduleDict.put("Name", result.getString("Name"));
            scheduleDict.put("Years", result.getString("Years"));

            return scheduleDict;

        }else{
            return null;
        }

    }


    /**
     * Join records table with containers table to get container name
     *
     * @param resultSet
     * @return container name
     */
    private String getContainerNumber(ResultSet resultSet) throws SQLException{

        if(resultSet.getString("ContainerId") != null){

            String queryContainer = "SELECT containers.Number FROM records "
                    + "Inner JOIN recordr.containers on records.ContainerId = containers.Id"
                    + " WHERE records.id = " + resultSet.getString("id");

            ResultSet result = getResult(queryContainer);
            result.next();
            return result.getString("Number");

        }else{
            return null;
        }

    }


    /**
     * Join records table with recordstates table to get state name
     *
     * @param resultSet
     * @return state name
     */
    private String getStateName(ResultSet resultSet) throws SQLException {

        if(resultSet.getString("StateId") != null){

            String queryState = "SELECT recordstates.Name FROM records "
                    + "Inner JOIN recordr.recordstates on records.StateId = recordstates.Id"
                    + " WHERE records.id = " + resultSet.getString("id");
            ResultSet result = getResult(queryState);

            result.next();
            return result.getString("Name");

        }else{
            return null;
        }

    }
}
