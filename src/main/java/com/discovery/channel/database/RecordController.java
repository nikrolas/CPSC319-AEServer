package com.discovery.channel.database;

import com.discovery.channel.audit.AuditLogger;
import com.discovery.channel.authenticator.Authenticator;
import com.discovery.channel.authenticator.Role;
import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.exception.IllegalArgumentException;
import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.exception.ValidationException;
import com.discovery.channel.form.RecordsForm;
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
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class RecordController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordController.class);

    /**
     * Retrieve records filtered by record number
     *
     * @param recordNumber
     * @return a list of records
     */
    private static final String GET_RECORD_BY_NUMBER =
            "SELECT * FROM records " +
            "WHERE (LocationId IN " +
                "(SELECT LocationId  " +
                "FROM locations l  LEFT JOIN userlocations ul ON (ul.LocationId = l.Id ) " +
                "WHERE l.Restricted = false OR ul.UserId = ?) " +
            "OR LocationId IS NULL) " +
            "AND records.Number LIKE ? " +
            "ORDER BY Number ASC " +
            " LIMIT ?, ?";
    public static List<Record> getRecordPageByNumber(String number, int userId,
                                                      int page, int pageSize) throws SQLException{
        List<Record> records = new ArrayList<>();
        try (Connection connection = DbConnect.getConnection();
            PreparedStatement ps = connection.prepareStatement(GET_RECORD_BY_NUMBER)) {
            ps.setInt(1, userId);
            ps.setString(2, "%" + number + "%");
            ps.setInt(3, (page - 1) * pageSize);
            ps.setInt(4, pageSize);
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

    private static final String GET_RECORD_COUNT_BY_NUMBER =
            "SELECT COUNT(*) FROM records " +
            "WHERE (LocationId IN " +
                "(SELECT LocationId  " +
                "FROM locations l  LEFT JOIN userlocations ul ON (ul.LocationId = l.Id ) " +
                "WHERE l.Restricted = false OR ul.UserId = ?) " +
            "OR LocationId IS NULL) " +
            "AND Number LIKE ?";
    public static int getRecordCountByNumber(String number, int userId) throws SQLException {
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_RECORD_COUNT_BY_NUMBER)) {
            ps.setInt(1, userId);
            ps.setString(2, "%" + number + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    LOGGER.error(String.format("Could not get count of records: %s", number));
                    throw new SQLException(String.format("Could not get count of records: %s.", number));
                }
            }
        }
    }

    public static PagedResults<Document> getByNumber(String number,
                                                   Boolean searchRecord, Boolean searchContainer,
                                                   int page, int pageSize,
                                                   int userId) throws SQLException {
        if (page < 1 || pageSize < 1) {
            throw new IllegalArgumentException("Page number and results per page must be at least 1.");
        }
        if (!(searchRecord || searchContainer)) {
            throw new IllegalArgumentException("Search must include at least one of the following: {record, container}.");
        }

        int recordCount = 0;
        int containerCount = 0;
        List<Document> documents;

        if (searchRecord && searchContainer) {
            recordCount = getRecordCountByNumber(number, userId);
            containerCount = ContainerController.getContainerCountByNumber(number, userId);

            if (recordCount >= page * pageSize) {
                documents = (List)getRecordPageByNumber(number, userId, page, pageSize);
            }
            else if (recordCount > (page - 1) * pageSize &&
                     recordCount < page * pageSize) {
                documents = new ArrayList<>(pageSize);
                documents.addAll((List)getRecordPageByNumber(number, userId, page, pageSize));
                documents.addAll((List)ContainerController.getContainerPageByNumber(number, userId,
                        1, pageSize - (recordCount - (page - 1) * pageSize), 0));
            }
            else { // recordCount < (page - 1) * pageSize
                documents = (List)ContainerController.getContainerPageByNumber(number, userId,
                        page - (recordCount + pageSize - 1) / pageSize, pageSize, recordCount % pageSize);
            }
        }
        else if (searchRecord) {
            recordCount = getRecordCountByNumber(number, userId);
            documents = (List)RecordController.getRecordPageByNumber(number, userId, page, pageSize);
        }
        else { // only searchContainer
            containerCount = ContainerController.getContainerCountByNumber(number, userId);
            documents = (List)ContainerController.getContainerPageByNumber(number, userId, page, pageSize, 0);
        }

        documents = scrubDocuments(documents, userId);

        return new PagedResults<>(page, (recordCount + containerCount + pageSize - 1) / pageSize, documents);
    }

    /**
     * Retrieve all records
     *
     * @return a list of records, currently limit 20 order by UpdatedAt
     */
    private static final String GET_ALL_RECORDS = "SELECT * " +
            "FROM records  " +
            "WHERE LocationId IN ( SELECT LocationId  " +
            "FROM locations l  LEFT JOIN userlocations ul ON (ul.LocationId = l.Id ) " +
            "WHERE l.Restricted = false OR ul.UserId = ?) " +
            "ORDER BY UpdatedAt LIMIT 20";
    public static List<Record> getAllRecords(int userId) throws SQLException {
        List<Record> records = new ArrayList<>();
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_ALL_RECORDS)) {
            ps.setInt(1, userId);
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
    public static Record getRecordById(Integer id, int userId) throws SQLException {
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_RECORD_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    Record record = parseResultSet(resultSet);
                    loadRecordDetail(record);
                    if (!Authenticator.canUserViewLocation(userId, record.getLocationId())) {
                        throw new AuthenticationException("User " + userId + " is not allowed to view records on location " + record.getLocation());
                    }
                    return record;
                } else {
                    LOGGER.info("Record {} does not exist", id);
                    throw new NoResultsFoundException(String.format("Record %d does not exist.", id));
                }
            }
        }
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

        String idStr = completeIdsInQuery(ids, query);

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

        try {
            RetentionSchedule schedule = RetentionScheduleController.getRetentionSchedule(record.getScheduleId());
            record.setSchedule(schedule.getName());
            record.setScheduleYear(schedule.getYears());
        } catch (Exception e) {}

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
     *
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

        if (!Authenticator.authenticate(userId, Role.ADMINISTRATOR) && !Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("You do not have permission to create records."));
        }

        if (!Authenticator.isUserAuthenticatedForLocation(userId, record.getLocationId())) {
            throw new AuthenticationException(String.format("You do not have permission to create records in %s.",
                    LocationController.getLocationNameByLocationId(record.getLocationId())));
        }


        String pattern = RecordTypeController.getNumberPattern(record.getTypeId());
        RecordNumber.NUMBER_PATTERN numberPattern = RecordNumber.NUMBER_PATTERN.fromString(pattern);
        if (!numberPattern.match(record.getNumber()) ||
                numberPattern.matchLocation(
                        LocationController.getLocationCodeById(record.getLocationId()),
                        record.getNumber())) {
            throw new IllegalArgumentException(String.format("Invalid record number: %s, for record type %s.",
                    record.getNumber(), record.getType()));
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
        return getRecordById(newRecordId, userId);
    }


    private static final String CREATE_RECORD_SQL =
            "INSERT INTO records (Number, Title, ScheduleId, TypeId, ConsignmentCode, StateId, ContainerId, LocationId, CreatedAt, UpdatedAt) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

    private static int saveRecordToDb(Record record) throws SQLException {
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(CREATE_RECORD_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, record.getNumber());
            ps.setString(2, record.getTitle());
            ps.setInt(3, record.getScheduleId());
            ps.setInt(4, record.getTypeId());
            ps.setString(5, record.getConsignmentCode() == null ? "" : record.getConsignmentCode());
            ps.setInt(6, record.getStateId());
            if (record.getContainerId() <= 0) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, record.getContainerId());
            }
            ps.setInt(8, record.getLocationId());
            ps.executeUpdate();

            int newRecordId = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    newRecordId = rs.getInt(1);
                }
            }

            if (newRecordId < 0) {
                LOGGER.error("Failed to save new record to DB. Returning -1");
                return -1;
            }
            saveClassificationForRecord(newRecordId, record.getClassIds());
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
     * @param recordId
     * @param classIds
     * @throws SQLException
     */
    private static void saveClassificationForRecord(int recordId, List<Integer> classIds) throws SQLException {
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_RECORD_CLASSIFICATION)){
            for (int i = 0; i < classIds.size(); i++) {
                ps.setInt(1, recordId);
                ps.setInt(2, classIds.get(i));
                ps.setInt(3, i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        LOGGER.info("Saved classifications {} for record {}", classIds, recordId);
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

    private static boolean deleteRecord(Record record, int userId) throws SQLException {
        // TODO : audit log

        if (!Authenticator.isUserAuthenticatedForLocation(userId, record.getLocationId())) {
            throw new AuthenticationException(String.format("You do not have permission to delete records in %s.",
                    record.getLocation()));
        }

        LOGGER.info("About to delete record {}", record.getId());

        // 1. Delete from records
        // 2. Classifications are deleted because of database constraint
        int rowsModified = 0;
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_RECORD_BY_ID)) {
            ps.setInt(1, record.getId());
            rowsModified = ps.executeUpdate();
        }

        // 3. Delete notes
        NoteTableController.deleteNotesForRecord(record.getId());

        AuditLogger.log(userId, AuditLogger.Target.RECORD, record.getId(), AuditLogger.ACTION.DELETE);

        return rowsModified == 1;
    }

    public static BatchResponse deleteRecords(int userId, RecordsForm form) throws SQLException {
        BatchResponse response = new BatchResponse();

        if (form.getRecordIds().isEmpty()) {
            LOGGER.info("No record ids found in delete records form. Returning true");
            return response;
        }

        if (!Authenticator.authenticate(userId, Role.ADMINISTRATOR) && !Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("You do not have permission to delete records."));
        }

        for (int recordId : form.getRecordIds()) {
            Record record = null;
            try {
                record = getRecordById(recordId, userId);
                response.addResponse(recordId, record.getNumber(), "", deleteRecord(record, userId));
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                response.addResponse(recordId, record == null ? "" : record.getNumber(), e.getMessage(), false);
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
    private static final String UPDATE_RECORD =
            "UPDATE records " +
            "SET Title = ?, ScheduleId = ?, StateId = ?, " +
            "ConsignmentCode = ?,ContainerId = ?, UpdatedAt = NOW(), " +
            "ClosedAt = CASE " +
                "WHEN (? = 'close') THEN NOW() " +  // Just closed
                "WHEN (? = 'open') THEN NULL " +  // Opened
                "ELSE ClosedAt END " + // Already closed, stays closed
            "WHERE Id= ?";

    public static void updateRecord(Integer id, int userId, UpdateRecordForm updateForm) throws SQLException {

        Record record = getRecordById(id, userId);

        if (!Authenticator.authenticate(userId, Role.ADMINISTRATOR) && !Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("You do not have permission to update records."));
        }

        if (record == null) {
            throw new NoResultsFoundException(String.format("Record %s does not exist.", record.getNumber()));
        }

        if (!Authenticator.isUserAuthenticatedForLocation(userId, record.getLocationId())) {
            throw new AuthenticationException(String.format("You do not have permission to update records at %s.",
                    record.getLocation()));
        }

        // Only certain types of states are valid for certain retention schedules
        if (!RecordState.fromId(updateForm.getStateId()).isValidforRetentionSchedule(updateForm.getScheduleId() > 0)) {
            throw new IllegalArgumentException(String.format("State %s is not valid for retention schedule %s.",
                    StateController.getStateName(updateForm.getStateId()),
                    RetentionScheduleController.getRetentionSchedule(updateForm.getScheduleId()).getName()));
        }

        // Validate classifications
        if (!Classification.validateClassification(updateForm.getClassifications())) {
            throw new IllegalArgumentException(String.format("Classification %s is not valid.", updateForm.getClassifications()));
        }

        // Validate container update
        Container destinationContainer = updateForm.getContainerId() <= 0 ?
                null : ContainerController.getContainerById(updateForm.getContainerId(), userId);
        if (destinationContainer != null && isContainerChanged(record, updateForm.getContainerId())) {
            ContainerController.validateContainerChangeForRecord(record, destinationContainer);
        }

        // Check and validate if set to destroyed
        if (updateForm.getStateId() == RecordState.DESTROYED.getId() &&
                record.getStateId() != RecordState.DESTROYED.getId()) {
            if (record.getStateId() != RecordState.ARCHIVED_LOCAL.getId() ||
                record.getStateId() != RecordState.ARCHIVED_INTERIM.getId() ||
                record.getStateId() != RecordState.ARCHIVED_PERMANENT.getId()) {
                throw new ValidationException(String.format("Cannot destroy record %s. It has not been closed yet.", record.getNumber()));
            } else {
                if (DestructionDateController.addYearToTheLatestClosureDate(record.getScheduleYear(), record.getClosedAt()) <=
                        System.currentTimeMillis()) {
                    throw new ValidationException(
                            String.format("Cannot destroy record %s. Destruction date has not passed yet.", record.getNumber()));
                }
            }
        }

        LOGGER.info("About to update record {}", id);
        String closeStatus = "";

        // Determine new close status
        // Update to opened
        if (updateForm.getStateId() == RecordState.ACTIVE.getId() ||
            updateForm.getStateId() == RecordState.INACTIVE.getId()) {
            closeStatus = "open";
        } else { // Update to closed
            // Not closed, close it now
            if (record.getStateId() == RecordState.ACTIVE.getId() ||
                record.getStateId() == RecordState.INACTIVE.getId()) {
                closeStatus = "close";
            }
            // Already closed, stay closed
        }

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_RECORD)) {
            ps.setString(1, updateForm.getTitle());
            if (updateForm.getScheduleId() <= 0) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, updateForm.getScheduleId());
            }
            ps.setInt(3, updateForm.getStateId());
            ps.setString(4, updateForm.getConsignmentCode() == null ? "" : updateForm.getConsignmentCode());
            if (updateForm.getContainerId() <= 0) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, updateForm.getContainerId());
            }
            ps.setString(6, closeStatus);
            ps.setString(7, closeStatus);

            ps.setInt(8, id);
            ps.executeUpdate();
        }

        // Update classifications if need to
        if (!updateForm.getClassifications().equals(record.getClassifications())) {
            updateRecordClassifications(id, updateForm.getClassifications());
        }

        // Update notes if need to
        if (StringUtils.isEmpty(updateForm.getNotes())) {
            NoteTableController.deleteNotesForRecord(id);
        } else if (!updateForm.getNotes().equals(record.getNotes())) {
            NoteTableController.updateRecordNotes(id, updateForm.getNotes());
        }

        // Update container information and/or set record closedAt date if need to
        if (isContainerChanged(record, updateForm.getContainerId())) {
            if (destinationContainer != null) {
                ContainerController.updateContainerRecordInformation(destinationContainer, record);
                setRecordContainer(id, destinationContainer.getContainerId());
            } else if (ContainerController.getContainerById(record.getContainerId(), userId).getChildRecordIds().size() == 0) {
                ContainerController.clearContainerRecordInformation(record.getContainerId());
            }
        }

        AuditLogger.log(userId, AuditLogger.Target.RECORD, id, AuditLogger.ACTION.UPDATE);
    }

    private static boolean isContainerChanged(Record record, int containerId) {
        return record.getContainerId() != containerId;
    }

    private static final String SET_RECORD_CONTAINER =
            "UPDATE records AS R, " +
                "(SELECT StateId, LocationId " +
                "FROM containers WHERE Id = ?) AS C " +
            "SET R.StateId = C.StateId, " +
                "R.LocationId = C.LocationId, " +
                "ClosedAt = NOW(), UpdatedAt = NOW(), ContainerId = ? " +
            "WHERE R.Id = ?";

    public static void setRecordContainer(int recordId, int containerId) throws SQLException {
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(SET_RECORD_CONTAINER)) {
            ps.setInt(1, containerId);
            ps.setInt(2, containerId);
            ps.setInt(3, recordId);
            ps.executeUpdate();
        }
    }

    private static final String UPDATE_RECORD_CONTAINER =
            "UPDATE records AS R, " +
                "(SELECT StateId, LocationId, ConsignmentCode " +
                "FROM containers WHERE Id = ?) AS C " +
            "SET R.StateId = C.StateId, " +
                "R.LocationId = C.LocationId, " +
                "R.ConsignmentCode = C.ConsignmentCode, " +
                "UpdatedAt = NOW() " +
            "WHERE R.ContainerId = ?";

    public static void updateRecordContainer(int containerId) throws SQLException {
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_RECORD_CONTAINER)) {
            ps.setInt(1, containerId);
            ps.setInt(2, containerId);
            ps.executeUpdate();
        }
    }

    /**
     * Delete old classifications and insert new ones
     * Assuming classification string is valid
     *
     * @param recordId
     * @param newClassifications
     */
    private static void updateRecordClassifications(int recordId, List<Integer> newClassifications) throws SQLException {
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
             PreparedStatement ps = conn.prepareStatement(DELETE_RECORD_CLASSIFICATIONS)) {
            ps.setInt(1, recordId);
            ps.executeUpdate();
        }
    }

    /**
     * Find all volumes related to a record Number
     *
     * @param String recordNumber
     * @throws SQLException
     */
    private static final String FIND_VOLUMES_BY_NUMBER =
            "SELECT * " +
            "FROM records " +
            "WHERE LocationId IN " +
                "(SELECT LocationId  " +
                "FROM locations l  LEFT JOIN userlocations ul ON (ul.LocationId = l.Id ) " +
                "WHERE l.Restricted = false OR ul.UserId = ?) " +
            "AND (Number LIKE BINARY ? " +
            "OR Number LIKE BINARY ?)";
    public static List<Record> getVolumesByNumber(String recordNumber, int userId) throws SQLException {
        int colonIndex = recordNumber.indexOf(":");
        if (colonIndex != -1)
            recordNumber = recordNumber.substring(0, colonIndex);

        List<Record> records = new ArrayList<>();

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_VOLUMES_BY_NUMBER)){
            ps.setInt(1, userId);
            ps.setString(2, recordNumber);
            ps.setString(3, recordNumber + ":%");
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

    private static List<Document> scrubDocuments(List<Document> documentList, int userId) throws SQLException {
        List<Location> locations = LocationController.getUserLocations(userId);
        List<Integer> locationIds = locations.stream()
                .map(elt -> elt.getLocationId())
                .collect(Collectors.toList());
        documentList.forEach(new Consumer<Document>() {
            public void accept(Document document) {
                if (!locationIds.contains(document.getLocationId())) {
                    document.setConsignmentCode("***");
                }

            }
        });
        return documentList;
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
    private static final String COPY_CLASSIFICATION =
            "INSERT INTO recordclassifications (RecordId, ClassId, Ordinal) " +
            "SELECT ?, ClassId, Ordinal " +
            "FROM recordclassifications " +
            "WHERE RecordId = ?";
    private static final String COPY_NOTES =
            "INSERT INTO notes (TableId, RowId, Chunk, Text) " +
            "SELECT TableId, ?, Chunk, Text " +
            "FROM notes " +
            "WHERE TableId = ? AND RowId = ?";
    // todo use @Transactional
    public static Record createVolume(Integer id, int userId, Boolean copyNotes) throws SQLException{
        if (!Authenticator.authenticate(userId, Role.ADMINISTRATOR) &&
                !Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("You do not have permission to create volumes."));
        }

        Record baseRecord = getRecordById(id, userId);
        if (baseRecord == null) {
            throw new IllegalArgumentException(String.format("Unable to create new volume from record %s. Record does not exist.",
                    baseRecord.getNumber()));
        }

        if (!Authenticator.isUserAuthenticatedForLocation(userId, baseRecord.getLocationId())) {
            throw new AuthenticationException(String.format("You do not have permission to create volumes in %s.",
                    baseRecord.getLocation()));
        }

        // Check colon count to increment volume
        String number = baseRecord.getNumber();
        String baseNumber;

        int colonCount = StringUtils.countOccurrencesOf(number, ":");
        if (colonCount > 1) {
            throw new IllegalArgumentException(String.format("Unsupported volume format for volume creation: %s.", number));
        } else if (colonCount == 1) {
            String[] pieces = baseRecord.getNumber().split(":");
            baseNumber = pieces[0];
            int newVolume = Integer.parseInt(pieces[1]) + 1;
            if (newVolume > 99) {
                throw new IllegalArgumentException(String.format(
                        "Unable to create volume :%d for record %s. Volume numbers over 99 currently not supported.",
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
                                "Unable to create new volume from record %s. New volumes can only be created from latest existing volume.",
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

            // Copy the classifications
            ps = conn.prepareStatement(COPY_CLASSIFICATION);
            ps.setInt(1, newRecordId);
            ps.setInt(2, id);
            ps.executeUpdate();

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
        return getRecordById(newRecordId, userId);
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
    public static ResponseEntity<?> prepareToDestroyRecords(RecordsForm ids, int userId) throws SQLException {


        if (!Authenticator.authenticate(userId, Role.RMC) && !Authenticator.authenticate(userId, Role.ADMINISTRATOR)) {
            throw new AuthenticationException(String.format("You do not have permission to destroy records."));
        }

        List<Integer> failedIds = new ArrayList<>();
        List<String> failedNumbers = new ArrayList<>();
        Date currentDate = new Date(Calendar.getInstance().getTimeInMillis());
        HashMap<String, Object> errorResponse = new HashMap<>();

        List<Record> listOfRecords = getRecordsByIds(ids.getRecordIds(), true);
        Map<String, Object> noClosedAt = DestructionDateController.checkRecordsClosedAt(listOfRecords);

        if(!noClosedAt.isEmpty()){
            LOGGER.info("Record id(s) do not have ClosedAt", noClosedAt.get("id"));
            errorResponse.put("id", noClosedAt.get("id"));
            errorResponse.put("number", noClosedAt.get("number"));
            errorResponse.put("error", RECORD_IS_NOT_CLOSED);

        }else{
            if(listOfRecords.size() == ids.getRecordIds().size()) {
                for (Record record : listOfRecords) {
                    if (record != null) {
                        if (record.getStateId() != RecordState.DESTROYED.getId()) {
                            if(!Authenticator.isUserAuthenticatedForLocation(userId, record.getLocationId())){
                                throw new AuthenticationException(String.format("You do not have permission to destroy record %s from your location %s.",
                                        record.getNumber(), record.getLocation()));
                            }
                            Date destructionDate = new Date(DestructionDateController.addYearToTheLatestClosureDate(record.getScheduleYear(), record.getClosedAt()));
                            if (destructionDate.compareTo(currentDate) >= 0) {
                                failedIds.add(record.getId());
                                failedNumbers.add(record.getNumber());
                            }
                        }
                    } else {
                        LOGGER.info("Record id {} does not exist", record.getId());
                        String output = String.format("Record %s does not exist.", record.getNumber());
                        new ResponseEntity<>(output, HttpStatus.BAD_REQUEST);
                    }
                }

                if (failedIds.isEmpty()) {
                    LOGGER.info("Records passed all the checking");
                    destroyRecords(ids.getRecordIds());
                    return new ResponseEntity<>(HttpStatus.OK);

                } else {
                    LOGGER.info("Records destruction date(s) not passed yet");
                    errorResponse.put("id", failedIds);
                    errorResponse.put("number", failedNumbers);
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
        String query = "UPDATE records "
                + "SET StateId = " + RecordState.DESTROYED.getId()
                + " , UpdatedAt = now() "
                + " , ContainerId = null WHERE Id IN (";
        String destroyRecordsQuery = completeIdsInQuery(ids, query);

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(destroyRecordsQuery)){
            ps.executeUpdate();
        }
    }

    private static String completeIdsInQuery(List<Integer> ids, String query){
        Iterator<Integer> idsIterator = ids.iterator();
        while(idsIterator.hasNext())
        {
            query += idsIterator.next().toString();
            if(idsIterator.hasNext()){
                query += ",";
            }
        }
        return query + ")";
    }
}
