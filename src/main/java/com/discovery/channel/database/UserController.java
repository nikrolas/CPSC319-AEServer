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
     * Get user by given id
     * @return user
     * @throws SQLException
     */

    public static final String GET_USER_BY_ID =
            "SELECT * " +
            "FROM users WHERE Id = ?";

    public static User getUserById(Integer Id) throws SQLException{

        try (Connection connection = DbConnect.getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_USER_BY_ID)) {
            ps.setInt(1, Id);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    User user = parseResultSet(resultSet);
                    loadUserDetails(user);
                    return user;
                }
            }
        }

        LOGGER.info("User {} does not exist", Id);
        return null;
    }

    /**
     * Load user RoleId and Role
     *
     * @param user
     * @throws SQLException
     */
    private static void loadUserDetails(User user) throws SQLException{

        user.setRoleId(getRoleIdById(user.getId()));
        user.setRole(Role.fromRoleId(user.getRoleId()).getRoleName());

    }


    /**
     * Load user RoleId given UserId
     *
     * @param userId
     * @throws SQLException
     */
    public static final String GET_ROLEID_BY_ID =
            "SELECT RoleId " + "FROM userroles WHERE UserId = ?";
    private static int getRoleIdById(Integer id) throws SQLException{

        try (Connection con = DbConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_ROLEID_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("RoleId");
                }
            }
        }

        LOGGER.info("RoleId {} does not exist", id);
        return 0;

    }

}
