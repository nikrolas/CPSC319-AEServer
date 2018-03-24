package com.discovery.channel.database;

import com.discovery.channel.audit.AuditLogger;
import com.discovery.channel.authenticator.Authenticator;
import com.discovery.channel.authenticator.Role;
import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.exception.IllegalArgumentException;
import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.form.DeleteRecordsForm;
import com.discovery.channel.form.UpdateRecordForm;
import com.discovery.channel.model.*;
import com.discovery.channel.response.BatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.sql.Date;
import java.util.*;
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
     * @param ids
     * @param verbose
     * @return List of records
     */
    public static List<Record> getRecordsByIds(List<Integer> ids, boolean verbose) throws SQLException {
        List<Record> records = new ArrayList<>();

        if (ids == null || ids.isEmpty()) {
            return records;
        }

        String query = "SELECT * FROM records WHERE Id IN (";

        String idStr = buildString(ids, query);

        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(idStr)) {
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
        record.setLocation(LocationController.getLocationNameByLocationId(record.getLocationId()));
        record.setType(RecordTypeController.getTypeName(record.getTypeId()));
        record.setState(StateController.getStateName(record.getStateId()));
        record.setContainerNumber(getContainerNumber(record.getContainerId()));

        Map<String, String> schedule = RetentionScheduleController.getRetentionSchedule(record.getScheduleId());
        record.setSchedule(schedule.get("Name"));
        record.setScheduleYear(Integer.valueOf(schedule.get("Years")));

        // Load classifications
        List<Integer> classIds = getRecordClassifications(record.getId());
        record.setClassIds(classIds);
        List<Classification> classifications = new ArrayList<>();
        for (int classId : classIds) {
            classifications.add(ClassificationController.findClassificationById(classId));
        }
        record.setClassifications(Classification.buildClassificationString(classifications));

        // Load notes
        record.setNotes(NoteTableController.getRecordNotes(record.getId()));
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
            throw new AuthenticationException(String.format("User %d is not authenticated to create record under localtion %d", userId, record.getLocationId()));
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
        AuditLogger.log(userId, AuditLogger.Target.RECORD, newRecordId, AuditLogger.ACTION.CREATE);
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
                NoteTableController.saveNotesForRecord(newRecordId, record.getNotes());
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
            throw new AuthenticationException(String.format("User %d is not authenticated to delete record under location %d", userId, record.getLocationId()));
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

        AuditLogger.log(userId, AuditLogger.Target.RECORD, id, AuditLogger.ACTION.UPDATE);

        // 3. Delete notes
        NoteTableController.deleteNotesForRecord(id);

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
            throw new AuthenticationException(String.format("User %d is not authenticated to update record under location %d", userId, record.getLocationId()));
        }

        // RMC can't move a record to a location tht they're not a part of
        if (!Authenticator.isUserAuthenticatedForLocation(userId, record.getLocationId())) {
            throw new AuthenticationException(String.format("User %d is not authenticated to update record under location %d", userId, record.getLocationId()));
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
            NoteTableController.deleteNotesForRecord(id);
        }else if(!updateForm.getNotes().equals(record.getNotes())) {
            NoteTableController.updateRecordNotes(id, updateForm.getNotes());
        }

        AuditLogger.log(userId, AuditLogger.Target.RECORD, id, AuditLogger.ACTION.DELETE);
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
     * Prepare to destroy records
     *
     * @param ids
     * @param userId
     * @throws SQLException
     */
    private static final String RECORD_IS_NOT_CLOSED = "The record is not closed.";
    private static final String RETENTION_NOT_END = "The record's retention period has not ended yet.";
    public static ResponseEntity<?> prepareToDestroyRecords(List<Integer> ids, int userId) throws SQLException {

        User user = UserController.getUserByUserTableId(userId);

        if (!Authenticator.authenticate(userId, Role.RMC) && !Authenticator.authenticate(userId, Role.ADMINISTRATOR)) {
            throw new AuthenticationException(String.format("User %s is not authenticated to destroy record(s)", user.getUserId()));
        }

        List<Integer> failedIds = new ArrayList<>();
        List<String> failedNmbers = new ArrayList<>();
        Date currentDate = new Date(Calendar.getInstance().getTimeInMillis());
        HashMap<String, Object> errorResponse = new HashMap<>();

        List<Record> listOfRecords = getRecordsByIds(ids, true);
        Map<String, List<String>> noClosedAt = DestructionDateController.checkRecordsClosedAt(listOfRecords);

        if(!noClosedAt.get("id").isEmpty()){
            LOGGER.info("Record id(s) do not have ClosedAt", noClosedAt.get("id"));
            errorResponse.put("id", noClosedAt.get("id"));
            errorResponse.put("number", noClosedAt.get("number"));
            errorResponse.put("error", RECORD_IS_NOT_CLOSED);

        }else{
            if(listOfRecords.size() == ids.size()) {
                for (Record record : listOfRecords) {
                    if (record != null) {
                        if (record.getStateId() != RecordState.DESTROYED.getId()) {
                            Date destructionDate = new Date(DestructionDateController.addYearToTheLatestClosureDate(record.getScheduleYear(), record.getClosedAt()));
                            if (destructionDate.compareTo(currentDate) > 0) {
                                failedIds.add(record.getId());
                                failedNmbers.add(record.getNumber());
                            }
                        }
                    } else {
                        LOGGER.info("Record id {} does not exist", record.getId());
                        String output = String.format("Record %s does not exist", record.getNumber());
                        new ResponseEntity<>(output, HttpStatus.BAD_REQUEST);
                    }
                }

                if (failedIds.isEmpty()) {
                    LOGGER.info("Records passed all the checking");
                    destroyRecords(ids);
                    return new ResponseEntity<>(HttpStatus.OK);

                } else {
                    LOGGER.info("Records destruction date(s) not passed yet");
                    errorResponse.put("id", failedIds);
                    errorResponse.put("number", failedNmbers);
                    errorResponse.put("error", RETENTION_NOT_END);
                }
            }else{
                return new ResponseEntity<>("One or more record(s) do not exist",HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    /**
     * Destroy record(s) given ids
     *
     * @param ids
     * @throws SQLException
     */
    public static void destroyRecords(List<Integer> ids) throws SQLException {

        String query = "UPDATE records" + " SET StateId = " + RecordState.DESTROYED.getId() + " , UpdatedAt = now() " + "WHERE Id IN (";
        String destroyRecordsQuery = buildString(ids, query);

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(destroyRecordsQuery)){
            ps.executeUpdate();

        }
    }


    /**
     * build sql statement
     *
     * @param ids
     * @param str
     * @return sql statement
     */
    private static String buildString(List<Integer> ids, String str){

        Iterator<Integer> idsIterator = ids.iterator();
        while(idsIterator.hasNext())
        {
            str = str + idsIterator.next().toString();
            if(idsIterator.hasNext()){
                str = str + ",";
            }
        }

        str = str + ")";

        return str;
    }

}
