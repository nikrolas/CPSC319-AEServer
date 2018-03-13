package com.discovery.channel.database;

import com.discovery.channel.authenticator.Authenticator;
import com.discovery.channel.authenticator.Role;
import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.form.DeleteRecordsForm;
import com.discovery.channel.form.UpdateRecordForm;
import com.discovery.channel.model.*;
import com.discovery.channel.response.BatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class RecordController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordController.class);

    /**
     * Retrieve records filtered by record number
     *
     * @param recordNumber
     * @return a list of records
     */
    private static final String GET_RECORD_BY_NUMBER = "SELECT * FROM records " +
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
    private static final String GET_ALL_RECORDS = "SELECT * " +
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
     * Retrieve multiple records
     *
     * @param id
     * @return List of records
     */
    private static final String GET_RECORDS_BY_IDS =
            "SELECT * " +
                    "FROM records WHERE Id IN (?)";
    public static List<Record> getRecordsByIds(List<Integer> ids, boolean verbose) throws SQLException {
        List<Record> records = new ArrayList<>();

        if (ids == null || ids.isEmpty()) {
            return records;
        }

        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_RECORD_BY_ID)) {
            ps.setArray(1, connection.createArrayOf("int",ids.toArray()));
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    Record record = parseResultSet(resultSet);
                    if (verbose) {
                        loadRecordDetail(record);
                    }
                    records.add(record);
                }
            }
        }
        return records;
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

        // Load classifications
        List<Integer> classIds = getRecordClassifications(record.getId());
        List<Classification> classifications = new ArrayList<>();
        for (int classId : classIds) {
            classifications.add(ClassificationController.findClassificationById(classId));
        }
        record.setClassifications(Classification.buildClassificationString(classifications));

        // Load notes
        record.setNotes(getRecordNotes(record.getId()));
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

        if (!Authenticator.isUserAuthenticatedForLocation(userId, record.getLocationId())) {
            throw new AuthenticationException(String.format("User %d is not authenticated to create record under location %d", userId, record.getLocationId()));
        }


        String pattern = RecordTypeController.getNumberPattern(record.getTypeId());
        RecordNumber.NUMBER_PATTERN numberPattern = RecordNumber.NUMBER_PATTERN.fromString(pattern);
        if (!numberPattern.match(record.getNumber()) ||
                numberPattern.matchLocation(
                        LocationController.getLocationCodeById(record.getId()),
                        record.getNumber())) {
            throw new IllegalArgumentException(String.format("Invalid record number: %s for record type %d", record.getNumber(), record.getTypeId()));
        }

        record.setNumber(numberPattern.fillAutoGenField(record.getNumber()));
        LOGGER.debug("Set recordNumber {}", record.getNumber());

        if (!record.validateClassifications()) {
            throw new IllegalArgumentException(String.format("Invalid classifications: %s", record.getClassifications()));
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
            saveClassificationForRecord(newRecordId, record.getClassifications());
            if (!StringUtils.isEmpty(record.getNotes()) ) {
                saveNotesForRecord(newRecordId, record.getNotes());
            }
            return newRecordId;
        }
    }

    private static final String INSERT_RECORD_CLASSIFICATION =
            "INSERT INTO recordclassifications (RecordId, ClassId, Ordinal) " +
            "VALUES (?, ?, ?)";

    /**
     *
     * @param recordId
     * @param classificationStr
     * @throws SQLException
     */
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
        LOGGER.info("Saved classifications {} for record {}", classificationStr, recordId);
    }

    /**
     * Get ordered list of classifications Ids for a record
     *
     * @param recordId
     * @return
     * @throws SQLException
     */
    private static final String GET_RECORD_NOTES = "SELECT Text " +
            "FROM notes " +
            "WHERE TableId=? AND RowId=? " +
            "ORDER BY Chunk ASC";
    private static String getRecordNotes(int recordId) throws SQLException {
        String notes = "";
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(GET_RECORD_NOTES)) {
            ps.setInt(1, NoteTable.RECORDS.id);
            ps.setInt(2, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notes = notes + rs.getString("Text");
                }
            }
        }
        return notes;
    }

    /**
     * Save notes to db
     *
     * @param recordId
     * @param notes
     * @throws SQLException
     */
    private static final int MAX_NOTE_LEN = Integer.MAX_VALUE;
    private static final String INSERT_RECORD_NOTE = "INSERT INTO notes (TableId, RowId, Chunk, Text) " +
            "VALUES(?, ? , ? , ?)";
    private static void saveNotesForRecord(int recordId, String notes) throws SQLException {
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_RECORD_NOTE)){
            int chunkNum = 0;
            int startIndex = 0;
            while (startIndex < notes.length()) {
                ps.setInt(1, NoteTable.RECORDS.id);
                ps.setInt(2, recordId);
                ps.setInt(3, chunkNum);
                ps.setString(4, notes.substring(startIndex,
                        startIndex + MAX_NOTE_LEN >= notes.length()? notes.length() : startIndex + MAX_NOTE_LEN));
                ps.addBatch();
                startIndex = startIndex + MAX_NOTE_LEN;
                chunkNum = chunkNum + 1;
            }
            ps.executeBatch();
        }
        LOGGER.info("Saved notes {} for record {}", notes, recordId);
    }

    /**
     * Delete all chunks of notes for a record
     * @param recordId
     * @return
     * @throws SQLException
     */
    private static final String DELETE_NOTE_FOR_RECORD = "DELETE FROM notes " +
            "WHERE TableId=? AND RowId = ?";
    private static int deleteNotesForRecord(int recordId) throws SQLException {
        int rowsUpdated = 0;
        try(Connection connection = DbConnect.getConnection();
            PreparedStatement ps = connection.prepareStatement(DELETE_NOTE_FOR_RECORD)) {
            ps.setInt(1, NoteTable.RECORDS.id);
            ps.setInt(2, recordId);
            rowsUpdated = ps.executeUpdate();
        }
        LOGGER.info("Deleted {} note entries for record {}", rowsUpdated, recordId);
        return rowsUpdated;
    }

    private static void updateRecordNotes(int recordId, String newNotes) throws SQLException {
        deleteNotesForRecord(recordId);
        saveNotesForRecord(recordId, newNotes);
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
    private static boolean deleteRecord(Integer id, int userId) throws SQLException {
        // TODO : audit log

        Record record = getRecordById(id);

        if (record == null) {
            throw new NoResultsFoundException(String.format("Record %d does not exist", id));
        }

        if (!Authenticator.isUserAuthenticatedForLocation(userId, record.getLocationId())) {
            throw new AuthenticationException(String.format("User %d is not authenticated to delete record under localtion %d", userId, record.getLocationId()));
        }

        LOGGER.info("About to delete record {}", id);

        // 1. Delete from records
        // 2. Classifications are deleted because of database constraint
        int rowsModified = 0;
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_RECORD_BY_ID)){
            ps.setInt(1, id);
            rowsModified = ps.executeUpdate();
        }

        // 3. Delete notes
        deleteNotesForRecord(id);

        return rowsModified == 1;
    }

    public static BatchResponse deleteRecords(int userId, DeleteRecordsForm form) throws SQLException {
        BatchResponse response = new BatchResponse();

        if (form.getRecordIds().isEmpty()) {
            LOGGER.info("No record ids found in delete records form. Returning true");
            return response;
        }

        if (!Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("User %d is not authenticated to delete record", userId));
        }

        for (int recordId : form.getRecordIds()) {
            try {
                if (deleteRecord(recordId, userId)) {
                    response.addResponse(recordId, "", true);
                } else {
                    response.addResponse(recordId, "", false);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                response.addResponse(recordId, e.getMessage(), false);
            }
        }
        return response;
    }

    /**
     * Update a record
     *
     * @param id
     * @param userId
     * @param updateForm
     * @throws SQLException
     */
    private static final String UPDATE_RECORD = "UPDATE records " +
            "SET Title=?, ScheduleId=?, StateId=?, ConsignmentCode=?,ContainerId=?, UpdatedAt=NOW() " +
            "WHERE Id= ?";
    public static void updateRecord(Integer id, int userId, UpdateRecordForm updateForm) throws SQLException {
        if (!Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("User %d is not authenticated to update record", userId));
        }

        Record record = getRecordById(id);
        if (record == null) {
            throw new NoResultsFoundException(String.format("Record %d does not exist", id));
        }

        if (!Authenticator.isUserAuthenticatedForLocation(userId, record.getLocationId())) {
            throw new AuthenticationException(String.format("User %d is not authenticated to update record under localtion %d", userId, record.getLocationId()));
        }

        // RMC can't move a record to a location tht they're not a part of
        if (!Authenticator.isUserAuthenticatedForLocation(userId, record.getLocationId())) {
            throw new AuthenticationException(String.format("User %d is not authenticated to update record under localtion %d", userId, record.getLocationId()));
        }

        // Only certain types of states are valid for certain retention schedules
        if (!RecordState.fromId(updateForm.getStateId()).isValidforRetentionSchedule(updateForm.getScheduleId() > 0)) {
            throw new IllegalArgumentException(String.format("State %d is not valid for retention schedule %d", updateForm.getStateId(), updateForm.getScheduleId()));
        }

        // Validate classifications
        if (!Classification.validateClassification(updateForm.getClassifications())) {
            throw new IllegalArgumentException(String.format("Classification %s is not valid", updateForm.getClassifications()));
        }

        LOGGER.info("About to update record {}", id);

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_RECORD)) {
            ps.setString(1, updateForm.getTitle());
            if (updateForm.getScheduleId() <= 0) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, updateForm.getScheduleId());
            }
            ps.setInt(3, updateForm.getStateId());
            ps.setString(4, updateForm.getConsignmentCode() == null? "" : updateForm.getConsignmentCode());
            if (updateForm.getContainerId() <= 0) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, updateForm.getContainerId());
            }
            ps.setInt(6, id);

            ps.executeUpdate();
        }

        // Update classifications if need to
        if (!updateForm.getClassifications().equals(record.getClassifications())) {
            updateRecordClassifications(id, updateForm.getClassifications());
        }

        // Update notes if need to
        if (StringUtils.isEmpty(updateForm.getNotes())) {
            deleteNotesForRecord(id);
        }else if(!updateForm.getNotes().equals(record.getNotes())) {
            updateRecordNotes(id, updateForm.getNotes());
        }

        //TODO audit logs
    }

    /**
     * Delete old classifications and insert new ones
     * Assuming classification string is valid
     *
     * @param recordId
     * @param newClassifications
     */
    private static void updateRecordClassifications(int recordId, String newClassifications) throws SQLException {
        deleteRecordClassfications(recordId);
        saveClassificationForRecord(recordId, newClassifications);
    }


    /**
     * Delete all classifications for a record
     *
     * @param recordId
     * @throws SQLException
     */
    private static final String DELETE_RECORD_CLASSIFICATIONS = "DELETE " +
            "FROM recordclassifications " +
            "WHERE RecordId=?";
    private static void deleteRecordClassfications(int recordId) throws SQLException {
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_RECORD_CLASSIFICATIONS)){
            ps.setInt(1, recordId);
            ps.executeUpdate();
        }
    }

    /**
     * Create a new volume
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    private static final String CHECK_LATEST_VOLUME =
        "SELECT r.Id = s.Id as latestVolume " +
        "FROM (SELECT * " +
                "FROM records " +
                "WHERE Id = ?) AS r " +
        "JOIN (SELECT Id, Number " +
                "FROM records " +
                      "WHERE Number LIKE BINARY ? " +
                      "ORDER BY Number DESC " +
                      "LIMIT 1) AS s; ";
    private static final String UPDATE_LATEST_VOLUME =
            "UPDATE records " +
            "SET Number=?, StateId=?, UpdatedAt=NOW() " +
            "WHERE Id= ?";;
    private static final String CREATE_NEW_VOLUME =
            "INSERT INTO records (Number, Title, ScheduleId, TypeId, ConsignmentCode, StateId, ContainerId, LocationId, CreatedAt, UpdatedAt, ClosedAt) " +
            "SELECT ?, Title, ScheduleId, TypeId, ConsignmentCode, StateId, ContainerId, LocationId, NOW(), NOW(), ClosedAt " +
            "FROM records " +
            "WHERE Id = ?";
    private static final String COPY_NOTES =
            "INSERT INTO notes (TableId, RowId, Chunk, Text) " +
            "SELECT TableId, ?, Chunk, Text " +
            "FROM notes " +
            "WHERE TableId = ? AND RowId = ?";
    // todo use @Transactional
    public static Record createVolume(Integer id, int userId, Boolean copyNotes) throws SQLException{
        if (!Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("User %d is not authenticated to create volume", userId));
        }

        Record baseRecord = getRecordById(id);
        if (baseRecord == null) {
            throw new IllegalArgumentException(String.format("Unable to create new volume from record id %d. Record does not exist.", id));
        }

        if (!Authenticator.isUserAuthenticatedForLocation(userId, baseRecord.getLocationId())) {
            throw new AuthenticationException(String.format("User %d is not authenticated to create volume under location %d", userId, baseRecord.getLocationId()));
        }

        // Check colon count to increment volume
        String number = baseRecord.getNumber();
        String baseNumber;

        int colonCount = StringUtils.countOccurrencesOf(number, ":");
        if (colonCount > 1) {
            throw new IllegalArgumentException(String.format("Unsupported volume format for create volume %s.", baseRecord.getNumber()));
        } else if (colonCount == 1) {
            String[] pieces = baseRecord.getNumber().split(":");
            baseNumber = pieces[0];
            int newVolume = Integer.parseInt(pieces[1]) + 1;
            if (newVolume > 99) {
                throw new IllegalArgumentException(String.format(
                        "Unable to create volume %d for record %s. Volume numbers over 99 currently not supported.",
                        newVolume, number));
            }
            number = String.format("%s:%02d", baseNumber, newVolume);
        } else {
            baseNumber = number;
            number = number + ":02";
        }

        // Check if it's the latest volume
        try (Connection conn = DbConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(CHECK_LATEST_VOLUME)){
            ps.setInt(1, id);
            ps.setString(2, baseNumber + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!rs.getBoolean("latestVolume")) {
                        throw new IllegalArgumentException(String.format(
                                "Unable to create new volume from record number %s. New volumes can only be created from latest existing volume.",
                                baseRecord.getNumber()));
                    }
                }
            }
        }

        int newRecordId = -1;
        try (Connection conn = DbConnect.getConnection()){
            conn.setAutoCommit(false);

            // Create the new volume
            PreparedStatement ps = conn.prepareStatement(CREATE_NEW_VOLUME, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, number);
            ps.setInt(2, id);
            int rowsModified = ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    newRecordId =  rs.getInt(1);
                }
            }

            if (newRecordId < 0 || rowsModified != 1) {
                LOGGER.error(String.format("Failed to save new volume to DB. Rows modified: %d.", rowsModified));
                throw new SQLException(String.format("Failed to save new volume to DB. Rows modified: %d.", rowsModified));
            }

            // Copy the notes
            if (copyNotes) {
                ps = conn.prepareStatement(COPY_NOTES);
                ps.setInt(1, newRecordId);
                ps.setInt(2, NoteTable.RECORDS.id);
                ps.setInt(3, id);
                ps.executeUpdate();
            }

            // Update first volume
            ps = conn.prepareStatement(UPDATE_LATEST_VOLUME);
            ps.setString(1, colonCount == 1 ? baseRecord.getNumber() : baseRecord.getNumber() + ":01");
            ps.setInt(2, RecordState.INACTIVE.getId());
            ps.setInt(3, id);
            rowsModified = ps.executeUpdate();

            if (rowsModified != 1) {
                throw new SQLException(String.format("Could not update base volume. Updated %d records.", rowsModified));
            }

            conn.commit();
        }

        LOGGER.info("Updated record. Record Id {}", id);
        LOGGER.info("Created record. Record Id {}", newRecordId);
        // TODO audit log
        return getRecordById(newRecordId);
    }

}
