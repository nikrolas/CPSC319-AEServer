package com.discovery.channel.database;

import com.discovery.channel.audit.AuditLogger;
import com.discovery.channel.authenticator.Authenticator;
import com.discovery.channel.authenticator.Role;
import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.exception.ValidationException;
import com.discovery.channel.model.Container;
import com.discovery.channel.model.Record;
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
        if (isContainerEmpty(container)) return;
        container.setType(RecordTypeController.getTypeName(container.getTypeId()));
        container.setLocationName(LocationController.getLocationNameByLocationId(container.getLocationId()));
        Map<String, String> schedule = RetentionScheduleController.getRetentionSchedule(container.getScheduleId());
        container.setScheduleName(schedule.get("Name"));
        container.setState(StateController.getStateName(container.getStateId()));
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
    public static final Container getContainerById(int id) throws SQLException{
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_CONTAINER_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()){
                verifyResultNotEmpty(rs);
                rs.next();
                return parseResultSet(rs);
            }
        }
    }

    //todo: consider moving this to a more general location to be used by other controllers
    public static void verifyResultNotEmpty(ResultSet rs) throws SQLException {
        if (!rs.isBeforeFirst()){
            throw new NoResultsFoundException("The query returned no results");
        }
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
        if (!Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("User %d is not authenticated to create record", userId));
        }
        //todo: validate if all the records have the same consignmentCode, typeid, scheuldeid, lcationid, stateid
        LOGGER.info("Passed all validation checks. Creating container {}", container);

        Date createdAt = new Date(Calendar.getInstance().getTimeInMillis());
        container.setCreatedAt(createdAt);
        container.setUpdatedAt(createdAt);
        int newContainerId = saveContainerToDb(container);

        if (!StringUtils.isEmpty(container.getNotes())){
            NoteTableController.saveNotesForContainer(newContainerId, container.getNotes());
        }

        LOGGER.info("Created container. Container Id {}", newContainerId);
        AuditLogger.log(userId, AuditLogger.Target.CONTAINER, newContainerId, AuditLogger.ACTION.CREATE);

        return getContainerById(newContainerId);
    }

    private static final String GET_MAX_CONTAINER_ID =
            "SELECT MAX(Id) FROM containers";
    private static int getNewContainerId() throws SQLException {
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_MAX_CONTAINER_ID)) {
            try (ResultSet rs = ps.executeQuery()){
                rs.next();
                return rs.getInt("MAX(Id)") + 1;
            }
        }
    }

    private static final String CREATE_CONTAINER =
            "INSERT INTO containers(Id, Number, Title, CreatedAt, UpdatedAt)" +
                    "VALUES(?, ?, ?, ?, ?)";
    private static int saveContainerToDb(Container c) throws SQLException {
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(CREATE_CONTAINER)) {

            int id = getNewContainerId() + 1;

            ps.setInt(1, id);
            ps.setString(2, c.getContainerNumber());
            ps.setString(3, c.getTitle());
            ps.setDate(4, c.getCreatedAt());
            ps.setDate(5, c.getUpdatedAt());
            ps.executeUpdate();
            return id;
        }
    }

    private void validateRecordsCanBeAddedToSameContainer(List<Integer> recordIds) throws SQLException {
        //todo
    }

    public static void validateContainerChangeForRecord(Record record, Container destinationContainer) throws SQLException {
        if (isContainerEmpty(destinationContainer)) return;
        if (!destinationContainer.getConsignmentCode().equals(record.getConsignmentCode()))
            throw new ValidationException("tried to add record to container with consignmentCode: '" +
                    destinationContainer.getConsignmentCode() + "', but record has consignment code of '" +
                    record.getConsignmentCode() + "'");
        if (destinationContainer.getTypeId() != record.getTypeId())
            throw new ValidationException("tried to add record to container with type: '" +
                    destinationContainer.getConsignmentCode() + "', but record has consignment code of '" +
                record.getConsignmentCode() + "'");
        if (destinationContainer.getLocationId() != record.getLocationId())
            throw new ValidationException("tried to add record to container with type: '" +
                    destinationContainer.getConsignmentCode() + "', but record has consignment code of '" +
                    record.getConsignmentCode() + "'");
        if (destinationContainer.getScheduleId() != record.getScheduleId())
            throw new ValidationException("tried to add record to container with type: '" +
                    destinationContainer.getConsignmentCode() + "', but record has consignment code of '" +
                    record.getConsignmentCode() + "'");
        if (destinationContainer.getStateId() != record.getStateId())
            throw new ValidationException("tried to add record to container with type: '" +
                    destinationContainer.getConsignmentCode() + "', but record has consignment code of '" +
                    record.getConsignmentCode() + "'");
    }

    private static boolean isContainerEmpty(Container container) throws SQLException {
        return container.getChildRecordIds().isEmpty();
    }

    private static final String UPDATE_CONTAINER_RECORD_INFORMATION =
            "UPDATE containers " +
            "SET StateId = ?, LocationId = ?, ScheduleId = ?, TypeId = ?, ConsignmentCode = ?, UpdatedAt = NOW() " +
            "WHERE Id = ?";
    public static void addRecordToContainer(Container container, Record record) throws SQLException {
        if (container.getChildRecordIds().size() == 0){

            try (Connection connection = DbConnect.getConnection();
                 PreparedStatement ps = connection.prepareStatement(UPDATE_CONTAINER_RECORD_INFORMATION)) {

                ps.setInt(1, record.getStateId());
                ps.setInt(2, record.getLocationId());
                ps.setInt(3, record.getScheduleId());
                ps.setInt(4, record.getTypeId());
                ps.setString(5, record.getConsignmentCode());
                ps.setInt(6, container.getContainerId());
                ps.executeUpdate();
            }
        }
    }


    private static final String REMOVE_CONTAINER_RECORD_INFORMATION =
            "UPDATE containers " +
            "SET StateId = ?, LocationId = ?, ScheduleId = ?, TypeId = ?, ConsignmentCode = ?, UpdatedAt = NOW() " +
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
            ps.setNull(3, java.sql.Types.INTEGER);
            ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, null);
            ps.setInt(6, containerId);
            ps.executeUpdate();
        }
    }

    private static final String UPDATE_CONTAINER =
            "UPDATE containers " +
                    "SET Number = ?, Title = ?, UpdatedAt = NOW() " +
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
        if (!Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("User %d is not authenticated to update record", userId));
        }
        LOGGER.info("Passed all validation checks. Updating Container {}", container); //todo this message could be better

        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_CONTAINER)) {

            ps.setString(1, container.getContainerNumber());
            ps.setString(2, container.getTitle());
            ps.setInt(3, containerId);
            ps.executeUpdate();

            if (!StringUtils.isEmpty(container.getNotes())){
                NoteTableController.updateContainerNotes(containerId, container.getNotes());
            }
            AuditLogger.log(userId, AuditLogger.Target.CONTAINER, containerId, AuditLogger.ACTION.UPDATE);

            return getContainerById(containerId);
        }
    }

    /**
     * Retrieve containers filtered by container number
     *
     * @param containerNumber
     * @return a list of containers
     */
    private static final String GET_CONTAINER_BY_NUMBER = "SELECT * FROM containers " +
            "WHERE Number LIKE ? " +
            "ORDER BY UpdatedAt LIMIT 20";
    public static List<Container> getContainerByNumber(String containerNumber) throws SQLException {
        List<Container> containers = new ArrayList<>();
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_CONTAINER_BY_NUMBER)) {
            ps.setString(1, "%" + containerNumber + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Container container = parseResultSet(rs);
                    containers.add(container);
                }
            }
        }
        return containers;
    }



    /**
     * Delete one container by id
     *
     * @param id
     *
     */
    private static final String DELETE_CONTAINERS＿BY_ID =
            "DELETE FROM containers" + " WHERE Id = ?";

    public static final void deleteOneContainer(String id) throws SQLException {

        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE_CONTAINERS＿BY_ID)) {
            LOGGER.info("Deleting container {}", id);
            ps.setInt(1, Integer.valueOf(id));
            ps.executeUpdate();
        }
    }

    /**
     * Delete containers by ids
     *
     * @param ids
     * @return Http Status Code
     */
    public static final ResponseEntity<?> deleteContainers(String ids, Integer userId) throws SQLException{
        if (!Authenticator.authenticate(userId, Role.RMC)) {
            throw new AuthenticationException(String.format("User %d is not authenticated to delete record", userId));
        }
        List<String> failed = new ArrayList<>();
        String[] listOfIds = ids.split(",");
        for (String id : listOfIds) {
            if (!getRecordIdsInContainer(Integer.valueOf(id)).isEmpty()) {
                failed.add(id);
            }
        }
        if(failed.isEmpty()) {
            LOGGER.info("Passed all validation checks. Deleting container {}", ids);
            for (String id : listOfIds) {
                deleteOneContainer(id);
                AuditLogger.log(userId, AuditLogger.Target.CONTAINER, Integer.valueOf(id), AuditLogger.ACTION.DELETE);
            }
            return ResponseEntity.status(HttpStatus.OK).build();
        }else{
            return new ResponseEntity(failed, HttpStatus.PRECONDITION_FAILED);
        }
    }
}
