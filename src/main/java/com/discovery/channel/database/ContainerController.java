package com.discovery.channel.database;

import com.discovery.channel.model.Container;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class ContainerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerController.class);

    private static Container parseResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("Id");
        String number = resultSet.getString("Number");
        String title = resultSet.getString("Title");
        Date createdAt = resultSet.getDate("CreatedAt");
        Date updatedAt = resultSet.getDate("UpdatedAt");
        List<Integer> childRecordIds = getRecordIdsInContainer(id);
        return new Container(id,
                number,
                title,
                createdAt,
                updatedAt,
                childRecordIds);
    }

    private static final String GET_RECORD_IDS_IN_CONTAINER =
            "SELECT Id FROM records " +
            "WHERE ContainerId = ?";
    static final List<Integer> getRecordIdsInContainer(int containerId) throws SQLException{
        List<Integer> recordIds = new LinkedList<>();
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_RECORD_IDS_IN_CONTAINER)) {
            ps.setInt(1, containerId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                recordIds.add(rs.getInt("Id"));
            }
            return recordIds;
        }
    }

    private static final String GET_CONTAINER_BY_ID =
            "SELECT * FROM containers " +
            "WHERE Id = ?";
    public static final Container getContainerById(int id) throws SQLException{
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_CONTAINER_BY_ID)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            verifyResultNotEmpty(rs);
            rs.next();
            return parseResultSet(rs);
        }
    }

    //todo: consider moving this to a more general location to be used by other controllers
    public static void verifyResultNotEmpty(ResultSet rs) throws SQLException {
        if (!rs.isBeforeFirst()){
            throw new NoResultsFoundException("The query returned no results");
        }
    }

    public static final Container createAndReturnNewContainer(String requestBody) throws SQLException{
        Container c = new Gson().fromJson(requestBody, Container.class);
        Date createdAt = new Date(Calendar.getInstance().getTimeInMillis());
        int id = getNewContainerId();
        createNewContainer(c, createdAt, id);
        return getContainerById(id);
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
    private static void createNewContainer(Container c, Date createdAt, int id) throws SQLException {
        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(CREATE_CONTAINER)) {
            ps.setInt(1, id);
            ps.setString(2, c.getNumber());
            ps.setString(3, c.getTitle());
            ps.setString(4, "temporaryCode"); //todo: I think it would make more sense to have this be nullable
            ps.setDate(5, createdAt);
            ps.setDate(6, createdAt);
            ps.executeUpdate();
        }
    }
}
