package com.discovery.channel.authenticator;

import com.discovery.channel.database.DbConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Authenticator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Authenticator.class);

    /**
     * Return true iff the user has the expected role
     * @param userId
     * @param expectedRole
     * @return
     */
    public static boolean authenticate(int userId, Role expectedRole) {
        try {
            Role userRole = getUserRole(userId);
            return userRole == expectedRole;
        } catch (SQLException e) {
            LOGGER.info("Failed to query user role for user " + userId, e);
            return false;
        }
    }

    /**
     * Query user role by user Id
     * @param userId
     * @return
     * @throws SQLException
     */
    private static final String GET_USER_ROLE_BY_USER_ID ="SELECT RoleId " +
            "FROM userroles " +
            "WHERE UserId=?";
    private static Role getUserRole(int userId) throws SQLException {
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(GET_USER_ROLE_BY_USER_ID)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Role.fromRoleId(rs.getInt("RoleId"));
                }
            }
        }
        return null;
    }

    /**
     * Return true iff the user belongs to locationId
     * @param userId
     * @param locationId
     * @return
     * @throws SQLException
     */
    private static final String GET_USER_LOCATIONS =
            "SELECT * FROM userlocations " +
                    "WHERE UserId = ?" +
                    " AND LocationId = ?";
    public static boolean isUserAuthenticatedForLocation(int userId, int locationId) throws SQLException {
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(GET_USER_LOCATIONS)) {
            ps.setInt(1, userId);
            ps.setInt(2, locationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final String IS_LOCATION_RESTRICTED = "SELECT Restricted FROM locations WHERE Id = ?";
    public static boolean canUserViewLocation(int userId, int locationId) throws SQLException {
        // Containers may have null location id
        if (locationId == 0) {
            return true;
        }
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(IS_LOCATION_RESTRICTED)) {
            ps.setInt(1, locationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getBoolean("Restricted")) {
                        return isUserAuthenticatedForLocation(userId, locationId);
                    }
                }
            }
        }
        return true;
    }
}
