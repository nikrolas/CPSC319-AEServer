package com.discovery.channel.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LocationController {
    private static final String GET_LOCATION_CODE_BY_ID = "SELECT Code " +
            "FROM locations " +
            "WHERE Id=?";
    public static String getGetLocationCodeById(int id) throws SQLException {
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(GET_LOCATION_CODE_BY_ID)){
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Code");
                }
            }
        }
        return "";
    }
}
