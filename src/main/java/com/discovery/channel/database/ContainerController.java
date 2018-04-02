package com.discovery.channel.database;

import com.discovery.channel.audit.AuditLogger;
import com.discovery.channel.authenticator.Authenticator;
import com.discovery.channel.authenticator.Role;
import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.exception.IllegalArgumentException;
import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.exception.ValidationException;
import com.discovery.channel.model.Container;
import com.discovery.channel.model.Record;
import com.discovery.channel.model.RecordState;
import com.discovery.channel.model.RetentionSchedule;
import com.mysql.jdbc.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ContainerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerController.class);

    private static Container parseResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("Id");
        String number = resultSet.getString("Number");
        String title = resultSet.getString("Title");
        String consignmentCode = resultSet.getString("ConsignmentCode");
        Date createdAt = resultSet.getDate("CreatedAt");
        Date updatedAt = resultSet.getDate("UpdatedAt");
        int stateId = resultSet.getInt("stateId");
        int locationId = resultSet.getInt("locationId");
        int scheduleId = resultSet.getInt("scheduleId");
        int typeId = resultSet.getInt("typeId");
        Date destructionDate = resultSet.getDate("DestructionDate");

        List<Integer> childRecordIds = getRecordIdsInContainer(id);
        String notes = NoteTableController.getContainerNotes(id);

        Container c = new Container(id,
                number,
                title,
                consignmentCode,
                createdAt,
                updatedAt,
                stateId,
                locationId,
                scheduleId,
                typeId,
                destructionDate,
                childRecordIds,
                notes);
        loadContainerDetail(c);
        return c;
    }

    private static void loadContainerDetail(Container container) throws SQLException {
        container.setNotes(NoteTableController.getContainerNotes(container.getContainerId()));
        container.setLocationName(LocationController.getLocationNameByLocationId(container.getLocationId()));
        container.setState(StateController.getStateName(container.getStateId()));
        try {
            container.setType(RecordTypeController.getTypeName(container.getTypeId()));
            RetentionSchedule schedule = RetentionScheduleController.getRetentionSchedule(container.getScheduleId());
            container.setScheduleName(schedule.getName());
            container.setScheduleYear(schedule.getYears());
        } catch (Exception e) {

        }
    }


    private static final String GET_RECORD_IDS_IN_CONTAINER =
            "SELECT Id FROM records " +
            "WHERE ContainerId = ?";
    static final List<Integer> getRecordIdsInContainer(int containerId) throws SQLException{
        List<Integer> recordIds = new LinkedList<>();
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_RECORD_IDS_IN_CONTAINER)) {
            ps.setInt(1, containerId);
            try (ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    recordIds.add(rs.getInt("Id"));
                }
                return recordIds;
            }
        }
    }

    private static final String GET_CONTAINER_BY_ID =
            "SELECT * FROM containers " +
            "WHERE Id = ?";
    public static final Container getContainerById(int id, int userId) throws SQLException{
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_CONTAINER_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()){
                verifyResultNotEmpty(rs);
                rs.next();
                Container container =  parseResultSet(rs);
                if (!Authenticator.canUserViewLocation(userId, container.getLocationId())) {
                    throw new AuthenticationException(String.format("You do not have permission to view containers at %s. ",
                            container.getLocationName()));
                }
                return container;
            }
        }
    }

    /**
     * Get containers by ids
     *
     * @param ids
     * @return List of containers
     */
    public static List<Container> getContainersByIds(List<Integer> ids) throws SQLException {
        List<Container> containers = new ArrayList<>();

        if (ids == null || ids.isEmpty()) {
            return containers;
        }

        String str = "SELECT * FROM containers WHERE Id IN (";

        String query = completeIdsInQuery(ids, str);

        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    Container container = parseResultSet(resultSet);
                    loadContainerDetail(container);
                    containers.add(container);
                }
            }
        }
        return containers;
    }

    //todo: consider moving this to a more general location to be used by other controllers
    public static void verifyResultNotEmpty(ResultSet rs) throws SQLException {
        if (!rs.isBeforeFirst()){
            throw new NoResultsFoundException("This container does not exist.");
        }
    }

    private static String completeIdsInQuery(List<Integer> ids, String str){
        Iterator<Integer> idsIterator = ids.iterator();
        while(idsIterator.hasNext())
        {
            str = str + idsIterator.next().toString();
            if(idsIterator.hasNext()){
                str = str + ",";
            }
        }
        return str + ")";
    }

    /**
     * Create a new container
     *
     * @param container the request body translated to a container object
     * @param userId the id of the user submitting the request
     * @throws SQLException rethrows any SQLException
     * @throws AuthenticationException AuthenticationException thrown if the user does not have RMC rights
     */
    public static final Container createContainer(Container container, int userId) throws SQLException, AuthenticationException{
        if (!Authenticator.authenticate(userId, Role.ADMINISTRATOR) && !Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("You do not have permission to create containers."));
        }

        if (container.getChildRecordIds().size() < 1){
            throw new IllegalArgumentException("Containers must be created with at least one child record.");
        }

        if (container.getChildRecordIds().size() >= 1){
            try {
                validateRecordsCanBeAddedToSameContainer(container.getChildRecordIds(), userId);
            } catch (ValidationException e){
                String recordNumbers = "";
                List<Record> listOfRecords = RecordController.getRecordsByIds(container.getChildRecordIds(), false);
                for(Record r : listOfRecords){
                    if(recordNumbers == ""){
                        recordNumbers = r.getNumber();
                    }else{
                        recordNumbers = recordNumbers.concat(",") + r.getNumber();
                    }
                }
                throw new ValidationException("Could not create container with the following records: "
                        + recordNumbers + ". Reason: "+ e.getMessage());
            }
        }

        String locationCode = LocationController.getLocationCodeById(container.getLocationId()).toUpperCase();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String maxNumber = getMaxContainerNumber(year, locationCode);
        int maxNumberParsed = maxNumber != null ?
                Integer.parseInt(
                        maxNumber.substring(maxNumber.indexOf("/") + 1,
                                maxNumber.indexOf("-"))) + 1 :
                1;
        if (maxNumberParsed > 999) {
            throw new ValidationException(String.format("Could not create container in %s. Max number of containers reached. Please wait until next year.",
                    LocationController.getLocationNameByLocationId(container.getLocationId())));
        }

        Record baseRecord = RecordController.getRecordById(container.getChildRecordIds().get(0), userId);
        container.setConsignmentCode(baseRecord.getConsignmentCode());
        container.setScheduleId(baseRecord.getScheduleId());
        container.setTypeId(baseRecord.getTypeId());
        String ggg = String.format("%03d", maxNumberParsed);
        container.setContainerNumber(year + "/" + ggg + "-" + locationCode);
        container.setStateId(RecordState.ARCHIVED_LOCAL.getId());

        LOGGER.info("Passed all validation checks. Creating container {}", container);

        int newContainerId = saveContainerToDb(container);
        if (newContainerId < 0) {
            throw new SQLException("Unable to save container to database.");
        }

        if (!StringUtils.isEmpty(container.getNotes())){
            NoteTableController.saveNotesForContainer(newContainerId, container.getNotes());
        }

        LOGGER.info("Created container. Container Id {}", newContainerId);
        AuditLogger.log(userId, AuditLogger.Target.CONTAINER, newContainerId, AuditLogger.ACTION.CREATE);

        // Update records to point to the new container
        for (int recordId : container.getChildRecordIds()){
            RecordController.setRecordContainer(recordId, newContainerId);
        }

        return getContainerById(newContainerId, userId);
    }

    private static void validateRecordsCanBeAddedToSameContainer(List<Integer> recordIds, int userId) throws SQLException, ValidationException {
        List<Record> records = new LinkedList<>();
        for (Integer recordId : recordIds) {
            records.add(RecordController.getRecordById(recordId, userId));
        }

        // Validate all records are not already contained
        for (Record r : records) {
            LOGGER.info(String.format("Record is in container: %d", r.getContainerId()));
            if (r.getContainerId() != 0) {
                throw new ValidationException("Record '" + r.getNumber() +
                        "' is already contained in Container '" + r.getContainerId() + "'.");
            }
        }

        // Validate record has retention schedule
        int scheduleId = records.get(0).getScheduleId();
        if (scheduleId == 0) {
            throw new ValidationException(String.format("Record %s has no retention schedule.",
                    records.get(0).getNumber()));
        }

        if (records.size() <= 1) return;

        // Validate all records have the same consignmentCode
        String consignmentCode = records.get(0).getConsignmentCode();
        for (Record r : records){
            if (!r.getConsignmentCode().equals(consignmentCode)){
                throw new ValidationException("Record '" + r.getNumber() +
                        "' has a consignmentCode that differs from at least one other record.");
            }
        }
        // Validate all records have the same locationId
        int locationId = records.get(0).getLocationId();
        for (Record r : records){
            if (r.getLocationId() != locationId){
                throw new ValidationException("Record '" + r.getNumber() +
                        "' has a locationId that differs from at least one other record.");
            }
        }
        // Validate all records have the same typeId
        int typeId = records.get(0).getTypeId();
        for (Record r : records){
            if (r.getTypeId() != typeId){
                throw new ValidationException("Record '" + r.getNumber() +
                        "' has a typeId that differs from at least one other record.");
            }
        }
        // Validate all records have the same scheduleId
        for (Record r : records){
            if (r.getScheduleId() != scheduleId){
                throw new ValidationException("Record '" + r.getNumber() +
                        "' has a scheduleId that differs from at least one other record.");
            }
        }
    }

    private static final String GET_MAX_CONTAINER_NUMBER =
            "SELECT Number " +
            "FROM containers " +
            "WHERE Number LIKE ? " +
            "ORDER BY Number DESC";
    private static String getMaxContainerNumber(int year, String locationCode) throws SQLException {
        try (Connection connection = DbConnect.getConnection();
            PreparedStatement ps = connection.prepareStatement(GET_MAX_CONTAINER_NUMBER)) {
            ps.setString(1, year + "/___-" + locationCode);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) {
                    return rs.getString("Number");
                } else {
                    return null;
                }
            }
        }
    }

    private static final String CREATE_CONTAINER =
            "INSERT INTO containers " +
                    "(Number, Title, ConsignmentCode, StateId, LocationId, ScheduleId, TypeId, CreatedAt, UpdatedAt) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
    private static int saveContainerToDb(Container c) throws SQLException {
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(CREATE_CONTAINER, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getContainerNumber());
            ps.setString(2, c.getTitle());
            ps.setString(3, c.getConsignmentCode());
            ps.setInt(4, c.getStateId());
            ps.setInt(5, c.getLocationId());
            ps.setInt(6, c.getScheduleId());
            ps.setInt(7, c.getTypeId());
            ps.executeUpdate();

            int newContainerId = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    newContainerId = rs.getInt(1);
                }
            }

            if (newContainerId < 0) {
                LOGGER.error("Failed to save new container to DB. Returning -1");
                return -1;
            }

            return newContainerId;
        }
    }

    public static void validateContainerChangeForRecord(Record record, Container destinationContainer) throws SQLException {
        if (!destinationContainer.getConsignmentCode().equals(record.getConsignmentCode())) {
            throw new ValidationException(String.format(
                    "Tried to add record to container with consignmentCode: '%s', but record has consignment code of '%s'.",
                    destinationContainer.getConsignmentCode(), record.getConsignmentCode()));
        }
        if (isContainerEmpty(destinationContainer)) return;

        if (destinationContainer.getTypeId() != record.getTypeId())
            throw new ValidationException(String.format(
                    "Tried to add record to container with record type: '%s', but record has record type of '%s'.",
                    destinationContainer.getType(), record.getType()));
        if (destinationContainer.getScheduleId() != record.getScheduleId())
            throw new ValidationException(String.format(
                    "Tried to add record to container with schedule: '%s', but record has schedule '%s'.", destinationContainer.getScheduleName(), record.getSchedule()));
    }

    private static boolean isContainerEmpty(Container container) {
        return container.getChildRecordIds().isEmpty();
    }

    private static final String UPDATE_CONTAINER_RECORD_INFORMATION =
            "UPDATE containers " +
            "SET ScheduleId = ?, TypeId = ?, UpdatedAt = NOW() " +
            "WHERE Id = ?";
    public static void updateContainerRecordInformation(Container container, Record record) throws SQLException {
        // no need to update container information if adding a record to a container that already has records
        if (container.getChildRecordIds().size() == 0) {

            try (Connection connection = DbConnect.getConnection();
                 PreparedStatement ps = connection.prepareStatement(UPDATE_CONTAINER_RECORD_INFORMATION)) {

                ps.setInt(1, record.getScheduleId());
                ps.setInt(2, record.getTypeId());
                ps.setInt(3, container.getContainerId());
                ps.executeUpdate();
            }
        }
    }

    private static final String REMOVE_CONTAINER_RECORD_INFORMATION =
            "UPDATE containers " +
            "SET ScheduleId = ?, TypeId = ?, UpdatedAt = NOW() " +
            "WHERE Id = ?";
    /**
     * Clear a information about the kinds of records in the container
     *
     * @param containerId the id of the container that should have its container information cleared
     * @throws SQLException rethrows any SQLException
     */
    public static void clearContainerRecordInformation(int containerId) throws SQLException {
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(REMOVE_CONTAINER_RECORD_INFORMATION)) {
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setNull(2, java.sql.Types.INTEGER);
            ps.setInt(3, containerId);
            ps.executeUpdate();
        }
    }

    private static final String UPDATE_CONTAINER =
            "UPDATE containers " +
            "SET Title = ?, StateId = ?, LocationId = ?, ConsignmentCode = ?, UpdatedAt = NOW() " +
            "WHERE Id = ?";
    /**
     * Update a container
     *
     * @param container the request body translated to a container object, containing the updated container information
     * @param userId the id of the user submitting the request
     * @throws SQLException rethrows any SQLException
     * @throws AuthenticationException AuthenticationException thrown if the user does not have RMC rights
     */
    public static Container updateContainer(int containerId, Container container, int userId) throws SQLException{
        if (!Authenticator.authenticate(userId, Role.ADMINISTRATOR) && !Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException("You do not have permission to update containers.");
        }

        Container baseContainer = getContainerById(containerId, userId);

        if (!Authenticator.isUserAuthenticatedForLocation(userId, baseContainer.getLocationId())) {
            throw new AuthenticationException(String.format("You do not have permission to delete records in %s.",
                    baseContainer.getLocationName()));
        }

        if (!Authenticator.isUserAuthenticatedForLocation(userId, container.getLocationId())) {
            throw new AuthenticationException(String.format("You do not have permission to delete records in %s.",
                    LocationController.getLocationNameByLocationId(container.getLocationId())));
        }

        LOGGER.info("Passed all validation checks. Updating Container {}", container); //todo this message could be better

        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_CONTAINER)) {

            ps.setString(1, container.getTitle());
            ps.setInt(2, container.getStateId());
            ps.setInt(3, container.getLocationId());
            ps.setString(4, container.getConsignmentCode());
            ps.setInt(5, containerId);
            ps.executeUpdate();

            // Update container notes
            if (!StringUtils.isEmpty(container.getNotes())){
                NoteTableController.updateContainerNotes(containerId, container.getNotes());
            }

            // Update record fields to follow container
            RecordController.updateRecordContainer(containerId);

            AuditLogger.log(userId, AuditLogger.Target.CONTAINER, containerId, AuditLogger.ACTION.UPDATE);

            return getContainerById(containerId, userId);
        }
    }

    /**
     * Retrieve containers filtered by container number
     *
     * @param containerNumber
     * @return a list of containers
     */
    private static final String GET_CONTAINER_BY_NUMBER =
            "SELECT * FROM containers " +
            "WHERE (LocationId IN " +
                "(SELECT LocationId  " +
                "FROM locations l  LEFT JOIN userlocations ul ON (ul.LocationId = l.Id ) " +
                "WHERE l.Restricted = false OR ul.UserId = ?) " +
            "OR LocationId IS NULL) " +
            "AND Number LIKE ? " +
            "ORDER BY Number ASC " +
            "LIMIT ?, ?";
    public static List<Container> getContainerPageByNumber(String number, int userId,
                                                           int page, int pageSize, int offset) throws SQLException {
        List<Container> containers = new ArrayList<>();
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_CONTAINER_BY_NUMBER)) {
            ps.setInt(1, userId);
            ps.setString(2, "%" + number + "%");
            ps.setInt(3, (page - 1) * pageSize + offset);
            ps.setInt(4, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Container container = parseResultSet(rs);
                    containers.add(container);
                }
            }
        }
        return containers;
    }

    private static final String GET_CONTAINER_COUNT_BY_NUMBER =
            "SELECT COUNT(*) FROM containers " +
            "WHERE (LocationId IN" +
                "(SELECT LocationId  " +
                "FROM locations l  LEFT JOIN userlocations ul ON (ul.LocationId = l.Id ) " +
                "WHERE l.Restricted = false OR ul.UserId = ?) " +
            "OR LocationId IS NULL) " +
            "AND Number LIKE ? ";
    public static int getContainerCountByNumber(String number, int userId) throws SQLException {
        try (Connection connection = DbConnect.getConnection();
            PreparedStatement pst = connection.prepareStatement(GET_CONTAINER_COUNT_BY_NUMBER)) {
            pst.setInt(1, userId);
            pst.setString(2, "%" + number + "%");
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    LOGGER.error(String.format("Could not get count of containers: %s.", number));
                    throw new SQLException(String.format("Could not get count of containers: %s.", number));
                }
            }
        }
    }


    /**
     * Delete one container by id
     *
     * @param id
     *
     */
    private static final String DELETE_CONTAINERS＿BY_ID =
            "DELETE FROM containers" + " WHERE Id = ?";

    private static final void deleteOneContainer(Integer id) throws SQLException {

        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE_CONTAINERS＿BY_ID)) {
            LOGGER.info("Deleting container {}", id);
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Delete containers by ids
     *
     * @param ids
     * @return Http Status Code
     */
    public static final ResponseEntity<?> deleteContainers(List<Integer> ids, Integer userId) throws SQLException{
        if (!Authenticator.authenticate(userId, Role.ADMINISTRATOR) && !Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("You do not have permission to delete containers."));
        }
        Map<String, Object> errorResponse = new HashMap<>();
        List<String> listOfContainerNumbers = new ArrayList<>();

        for (Integer id : ids) {
            Container container = ContainerController.getContainerById(id, userId);
            if(!Authenticator.isUserAuthenticatedForLocation(userId, container.getLocationId())){
                throw new AuthenticationException(String.format("You do not have permission to delete container %s from your location.",
                        container.getContainerNumber()));
            }
            if (!getRecordIdsInContainer(id).isEmpty()) {
                listOfContainerNumbers.add(container.getContainerNumber());
            }
        }

        if(!listOfContainerNumbers.isEmpty()){
            errorResponse.put("containerNumber", listOfContainerNumbers);
        }

        if(errorResponse.isEmpty()) {
            LOGGER.info("Passed all validation checks. Deleting container {}", ids);
            for (Integer id : ids) {
                deleteOneContainer(id);
                NoteTableController.deleteNotesForContainer(id);
                AuditLogger.log(userId, AuditLogger.Target.CONTAINER, id, AuditLogger.ACTION.DELETE);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }else{
            errorResponse.put("error", "The container(s) are not empty and still contain records");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

}
