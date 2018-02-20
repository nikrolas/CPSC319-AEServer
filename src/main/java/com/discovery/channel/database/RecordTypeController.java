package com.discovery.channel.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecordTypeController {
    /**
     * get type name by ID
     *
     * @param typeId
     * @return type name
     */
    private static final String GET_TYPE_BY_ID = "SELECT Name " +
            "FROM recordtypes " +
            "WHERE Id = ?";
    public static String getTypeName(int typeId) throws SQLException {
        try (Connection con = DbConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_TYPE_BY_ID)) {
            ps.setInt(1, typeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
            }
        }
        return null;
    }
}
