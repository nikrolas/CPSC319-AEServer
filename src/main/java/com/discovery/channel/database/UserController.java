package com.discovery.channel.database;

import com.discovery.channel.authenticator.Role;
import com.discovery.channel.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordController.class);


    /**
     * Parse result set from user table
     * @param resultSet
     * @return user
     * @throws SQLException
     */
    private static User parseResultSet(ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt("Id");
        String userId = resultSet.getString("UserId");
        String firstName = resultSet.getString("FirstName");
        String lastName = resultSet.getNString("LastName");

        return new User(
                id,
                userId,
                firstName,
                lastName);
    }


    /**
     * Get user by given id in user table
     * @param id
     * @return user
     * @throws SQLException
     */

    public static final String GET_USER_BY_USER_TABLE_ID =
            "SELECT * " +
            "FROM users WHERE Id = ?";

    public static User getUserById(Integer id) throws SQLException{

        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_USER_BY_USER_TABLE_ID)) {
            ps.setInt(1, id);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    User user = parseResultSet(resultSet);
                    loadUserDetails(user);
                    return user;
                }
            }
        }

        LOGGER.info("User {} does not exist", id);
        return null;
    }

    /**
     * Load user roleId, role, locationId, location
     *
     * @param user
     * @throws SQLException
     */
    private static void loadUserDetails(User user) throws SQLException{

        user.setRoleId(getRoleIdByUserTableId(user.getId()));
        user.setRole(Role.fromRoleId(user.getRoleId()).getRoleName());
        user.setLocationId(getLocationIdByUserTableId(user.getId()));
        user.setLocation(LocationController.getLocationNameByLocationId(user.getLocationId()));

    }


    /**
     * Load user roleId given id in user table
     *
     * @param id
     * @return RoleId
     * @throws SQLException
     */
    public static final String GET_ROLE_ID_BY_USER_TABLE_ID =
            "SELECT RoleId" + " FROM userroles WHERE UserId = ?";
    private static int getRoleIdByUserTableId(Integer id) throws SQLException{

        try (Connection con = DbConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_ROLE_ID_BY_USER_TABLE_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("RoleId");
                }
            }
        }

        LOGGER.info("User {} does not exist", id);
        return 0;

    }


    /**
     * Load user locationId given id in user table
     *
     * @param id
     * @return LocationId
     * @throws SQLException
     */
    public static final String GET_LOCATION_ID_BY_USER_TABLE_ID =
            "SELECT LocationId " + "FROM userlocations WHERE UserId = ?";
    private static int getLocationIdByUserTableId(Integer id) throws SQLException{

        try (Connection con = DbConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_LOCATION_ID_BY_USER_TABLE_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("LocationId");
                }
            }
        }

        LOGGER.info("User {} does not exist", id);
        return 0;

    }

}
