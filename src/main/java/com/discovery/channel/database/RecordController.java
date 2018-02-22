package com.discovery.channel.database;

import com.discovery.channel.authenticator.Authenticator;
import com.discovery.channel.authenticator.Role;
import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.model.Classification;
import com.discovery.channel.model.Record;
import com.discovery.channel.model.RecordState;
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
        record.setType(RecordTypeController.getTypeName(record.getTypeId()));
        record.setState(getStateName(record.getStateId()));
        record.setContainer(getContainerNumber(record.getContainerId()));
        Map<String, String> schedule = getRetentionSchedule(record.getScheduleId());
        record.setSchedule(schedule.get("Name"));
        record.setScheduleYear(Integer.valueOf(schedule.get("Years")));
        List<Integer> classIds = getRecordClassifications(record.getId());
        List<Classification> classifications = new ArrayList<>();
        for (int classId : classIds) {
            classifications.add(ClassificationController.findClassificationById(classId));
        }
        record.setClassifications(Classification.buildClassificationString(classifications));
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

    /**
     * Get ordered list of classifications Ids for a record
     *
     * @param recordId
     * @return
     * @throws SQLException
     */
    private static final String GET_RECORD_CLASS_IDS =
            "SELECT ClassId " +
                    "FROM recordclassifications " +
                    "WHERE RecordId=? ORDER BY Ordinal ASC";
    private static List<Integer> getRecordClassifications(int recordId) throws SQLException {
        List<Integer> classIds = new ArrayList<>();
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(GET_RECORD_CLASS_IDS)) {
            ps.setInt(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    classIds.add(rs.getInt("ClassId"));
                }
            }
        }
        return classIds;
    }

    /**
     * Create a record
     *
     * @param record
     * @param userId
     * @return
     */
    public static Record createRecord (Record record, int userId) throws SQLException {
        if (!Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("User %d is not authenticated to create record", userId));
        }

        if (!Authenticator.authenticateLocation(userId, record.getLocationId())) {
            throw new AuthenticationException(String.format("User %d is not authenticated to create record under localtion %d", userId, record.getLocationId()));
        }

        if (!record.validateRecordNum()) {
            throw new IllegalArgumentException(String.format("Invalid record number: %s for record type %d", record.getNumber(), record.getTypeId()));
        }

        if (!record.validateClassifications()) {
            throw new IllegalArgumentException(String.format("Invalid classifications: %s", record.getClassifications()));
        }

        if (!record.validateRetentionSchedule()) {
            throw new IllegalArgumentException(String.format("Invalid retention schedule: %s for record type %d", record.getScheduleId(), record.getTypeId()));
        }

        LOGGER.info("Passed all validation checks. Creating record {}", record);

        record.setStateId(RecordState.ACTIVE.getId());

        int newRecordId = saveRecordToDb(record);
        if (newRecordId < 0) {
            return null;
        }
        LOGGER.info("Created record. Record Id {}", newRecordId);
        // TODO save notes
        // TODO audit log : Need to determine the schema
        return getRecordById(newRecordId);
    }


    private static final String CREATE_RECORD_SQL =
            "INSERT INTO records (Number, Title, ScheduleId, TypeId, ConsignmentCode, StateId, ContainerId, LocationId, CreatedAt, UpdatedAt) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
    private static int saveRecordToDb(Record record) throws SQLException {
        try(Connection conn = DbConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(CREATE_RECORD_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, record.getNumber());
            ps.setString(2, record.getTitle());
            ps.setInt(3, record.getScheduleId());
            ps.setInt(4, record.getTypeId());
            ps.setString(5, record.getConsignmentCode() == null? "" : record.getConsignmentCode());
            ps.setInt(6, record.getStateId());
            if (record.getContainerId() <= 0){
                ps.setNull(7, java.sql.Types.INTEGER);
            }else {
                ps.setInt(7, record.getContainerId());
            }
            ps.setInt(8,record.getLocationId());
            ps.executeUpdate();

            int newRecordId = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    newRecordId =  rs.getInt(1);
                }
            }

            if (newRecordId < 0) {
                LOGGER.error("Failed to save new record to DB. Returning -1");
                return -1;
            }
            // TODO notes
            saveClassificationForRecord(newRecordId, record.getClassifications());
            return newRecordId;
        }
    }

    private static final String INSERT_RECORD_CLASSIFICATION =
            "INSERT INTO recordclassifications (RecordId, ClassId, Ordinal) " +
            "VALUES (?, ?, ?)";
    private static void saveClassificationForRecord(int recordId, String classificationStr) throws SQLException {
        List<Integer> classificationIds = Classification.parseClassificationStrToIds(classificationStr);
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_RECORD_CLASSIFICATION)){
            for (int i = 0; i < classificationIds.size(); i++) {
                ps.setInt(1, recordId);
                ps.setInt(2, classificationIds.get(i));
                ps.setInt(3, i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Delete a record by Id
     *
     * @param id
     * @param userId
     * @return
     */
    private static final String DELETE_RECORD_BY_ID = "DELETE FROM records " +
            "where Id=?";
    public static boolean deleteRecord(Integer id, int userId) throws SQLException {
        // TODO : delete note
        // TODO : audit log
        if (!Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("User %d is not authenticated to create record", userId));
        }

        Record record = getRecordById(id);

        if (record == null) {
            throw new NoResultsFoundException(String.format("Record %d does not exist", id));
        }

        if (!Authenticator.authenticateLocation(userId, record.getLocationId())) {
            throw new AuthenticationException(String.format("User %d is not authenticated to delete record under localtion %d", userId, record.getLocationId()));
        }

        LOGGER.info("About to delete record {}", id);

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_RECORD_BY_ID)){
            ps.setInt(1, id);
            int rowsModified = ps.executeUpdate();
            return rowsModified == 1;
        }
    }
}
