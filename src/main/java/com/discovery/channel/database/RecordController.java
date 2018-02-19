package com.discovery.channel.database;

import com.discovery.channel.model.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecordController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordController.class);

    /**
     * Retrieve records filtered by record number
     *
     * @param recordNumber
     * @return a list of records
     */
    private static final String GET_RECORD_BY_NUMBER = "SELECT Id FROM records " +
            "WHERE Number LIKE ? " +
            "ORDER BY UpdatedAt LIMIT 20";
    public static List<Record> getRecordByNumber(String recordNumber) throws SQLException {
        List<Record> records = new ArrayList<>();
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_RECORD_BY_NUMBER)) {
            ps.setString(1, "%" + recordNumber + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Record record = parseResultSet(rs);
                    loadRecordDetail(record);
                    records.add(record);
                }
            }
        }
        return records;
    }

    /**
     * Retrieve all records
     *
     * @return a list of records, currently limit 20 order by UpdatedAt
     */
    private static final String GET_ALL_RECORDS = "SELECT Id " +
            "FROM records " +
            "ORDER BY UpdatedAt LIMIT 20";
    public static List<Record> getAllRecords() throws SQLException {
        List<Record> records = new ArrayList<>();
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_ALL_RECORDS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Record record = parseResultSet(rs);
                    loadRecordDetail(record);
                    records.add(record);
                }
            }
        }
        return records;
    }


    /**
     * Retrieve a record
     *
     * @param id
     * @return a single record
     */
    private static final String GET_RECORD_BY_ID =
            "SELECT * " +
                    "FROM records WHERE Id = ?";
    public static Record getRecordById(Integer id) throws SQLException {
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_RECORD_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    Record record = parseResultSet(resultSet);
                    loadRecordDetail(record);
                    return record;
                }
            }
        }
        LOGGER.info("Record {} does not exist");
        return null;
    }

    /**
     * Load record details, including location, type, state, and retention schedule
     *
     * @param record
     * @throws SQLException
     */
    private static void loadRecordDetail(Record record) throws SQLException {
        record.setLocation(getLocationName(record.getLocationId()));
        record.setType(getTypeName(record.getTypeId()));
        record.setState(getStateName(record.getStateId()));
        record.setContainer(getContainerNumber(record.getContainerId()));
        Map<String, String> schedule = getRetentionSchedule(record.getScheduleId());
        record.setSchedule(schedule.get("Name"));
        record.setScheduleYear(Integer.valueOf(schedule.get("Years")));
    }

    /**
     * Parse result set from record table
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private static Record parseResultSet(ResultSet resultSet) throws SQLException {
            int id = resultSet.getInt("Id");
            String title = resultSet.getString("Title");
            String number = resultSet.getString("Number");
            int scheduleId = resultSet.getInt("ScheduleId");
            int typeId = resultSet.getInt("TypeId");
            String consignmentCode = resultSet.getString("ConsignmentCode");
            int stateId = resultSet.getInt("StateId");
            int containerId = resultSet.getInt("ContainerId");
            int locationId = resultSet.getInt("LocationId");
            Date createdAt = resultSet.getDate("CreatedAt");
            Date updatedAt = resultSet.getDate("UpdatedAt");
            Date closedAt = resultSet.getDate("ClosedAt");
            return new Record(id,
                    title,
                    number,
                    scheduleId,
                    typeId,
                    consignmentCode,
                    stateId,
                    containerId,
                    locationId,
                    createdAt,
                    updatedAt,
                    closedAt);
    }


    /**
     * Retrieve name for a location given location id
     *
     * @param location id
     * @return location name
     */
    private static final String GET_LOCATION_NAME_BY_ID = "SELECT Name " +
            "FROM locations " +
            "WHERE Id=?";

    private static String getLocationName(int locationId) throws SQLException {
        try (Connection con = DbConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_LOCATION_NAME_BY_ID)) {
            ps.setInt(1, locationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
            }
        }
        return null;
    }


    /**
     * Join records table with recordtypes table to get type name
     *
     * @param resultSet
     * @return type name
     */
    private static final String GET_TYPE_BY_ID = "SELECT Name " +
            "FROM recordtypes " +
            "WHERE Id = ?";

    private static String getTypeName(int typeId) throws SQLException {
        try (Connection con = DbConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_TYPE_BY_ID)) {
            ps.setInt(1, typeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
            }
        }
        return null;
    }


    /**
     * retentionschedules table to get schedule name and years by Id
     *
     * @param retention schedule id
     * @return schedule name and years
     */
    private static final String GET_RECORD_SCHEDULE = "SELECT * " +
            "FROM retentionschedules " +
            "WHERE Id=?";

    private static Map<String, String> getRetentionSchedule(int id) throws SQLException {
        Map<String, String> schedule = new HashMap<String, String>();
        try (Connection con = DbConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_RECORD_SCHEDULE)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    schedule.put("Name", rs.getString("Name"));
                    schedule.put("Years", String.valueOf(rs.getInt("Years")));
                }
            }
        }
        return schedule;
    }


    /**
     * Join records table with containers table to get container name
     *
     * @param resultSet
     * @return container name
     */
    private static final String GET_CONTAINER_NAME = "SELECT Number " +
            "FROM containers " +
            "WHERE Id = ?";

    private static String getContainerNumber(int containerId) throws SQLException {
        try (Connection con = DbConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_CONTAINER_NAME)) {
            ps.setInt(1, containerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Number");
                }
            }
        }
        return null;
    }


    /**
     * Get state name by id
     *
     * @param stateId
     * @return state name
     */
    private static final String GET_STATE_BY_ID = "SELECT Name " +
            "FROM recordstates " +
            "WHERE Id = ?";

    private static String getStateName(int stateId) throws SQLException {
        try (Connection con = DbConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_STATE_BY_ID)) {
            ps.setInt(1, stateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
            }
        }
        return null;
    }
}
