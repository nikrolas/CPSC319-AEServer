package com.discovery.channel.database;

import com.discovery.channel.model.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class ContainerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerController.class);

    /**
     * Parse result set from containers table
     * @param resultSet
     * @return
     * @throws SQLException
     */
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
    private static final List<Integer> getRecordIdsInContainer(int containerId) throws SQLException{
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
            rs.next();
            return parseResultSet(rs);
        }
    }
}
