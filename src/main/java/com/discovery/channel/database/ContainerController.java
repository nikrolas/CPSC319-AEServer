package com.discovery.channel.database;

import com.discovery.channel.authenticator.Authenticator;
import com.discovery.channel.authenticator.Role;
import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.model.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class ContainerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerController.class);

    private static Container parseResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("Id");
        String number = resultSet.getString("Number");
        String title = resultSet.getString("Title");
        String consignmentCode = resultSet.getString("ConsignmentCode");
        Date createdAt = resultSet.getDate("CreatedAt");
        Date updatedAt = resultSet.getDate("UpdatedAt");
        Date destructionDate = resultSet.getDate("DestructionDate");
        List<Integer> childRecordIds = getRecordIdsInContainer(id);
        String notes = "Container notes"; //TODO: get container notes
        return new Container(id,
                number,
                title,
                consignmentCode,
                createdAt,
                updatedAt,
                destructionDate,
                childRecordIds,
                notes);
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
        LOGGER.info("Passed all validation checks. Creating container {}", container);

        Date createdAt = new Date(Calendar.getInstance().getTimeInMillis());
        container.setCreatedAt(createdAt);
        container.setUpdatedAt(createdAt);
        int newContainerId = saveContainerToDb(container);

        LOGGER.info("Created record. Record Id {}", newContainerId);

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
            "INSERT INTO containers(Id, Number, Title, ConsignmentCode, CreatedAt, UpdatedAt)" +
                    "VALUES(?, ?, ?, ?, ?, ?)";
    private static int saveContainerToDb(Container c) throws SQLException {
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(CREATE_CONTAINER)) {

            int id = getNewContainerId() + 1;

            ps.setInt(1, id);
            ps.setString(2, c.getContainerNumber());
            ps.setString(3, c.getTitle());
            ps.setString(4, "temporaryCode"); //todo: I think it would make more sense to have this be nullable
            ps.setDate(5, c.getCreatedAt());
            ps.setDate(6, c.getUpdatedAt());
            ps.executeUpdate();
            //todo save notes to db
            return id;
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
            throw new AuthenticationException(String.format("User %d is not authenticated to create record", userId));
        }
        LOGGER.info("Passed all validation checks. Updating Container {}", container); //todo this message could be better

        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_CONTAINER)) {

            ps.setString(1, container.getContainerNumber());
            ps.setString(2, container.getTitle());
            ps.setInt(3, containerId);

            ps.executeUpdate();
            //todo save notes to db
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
            throw new AuthenticationException(String.format("User %d is not authenticated to create record", userId));
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
            }
            return ResponseEntity.status(HttpStatus.OK).build();
        }else{
            return new ResponseEntity(failed, HttpStatus.PRECONDITION_FAILED);
        }


    }
}
