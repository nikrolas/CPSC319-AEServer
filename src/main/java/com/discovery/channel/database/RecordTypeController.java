package com.discovery.channel.database;

import com.discovery.channel.model.RecordType;

import javax.ws.rs.GET;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecordTypeController {


    private static String GET_ALL_TYPES =
            "SELECT rt.Id, rt.Name, rt.NumberPattern, rs.Name AS Schedule " +
            "FROM recordtypes rt " +
            "LEFT JOIN retentionschedules rs ON rt.DefaultScheduleId = rs.Id";
    public static List<RecordType> getAllRecordTypes() throws SQLException {
        List<RecordType> recordTypes = new ArrayList<>();
        try(Connection conn = DbConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(GET_ALL_TYPES)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recordTypes.add(new RecordType(
                            rs.getInt("Id"),
                            rs.getString("Name"),
                            rs.getString("NumberPattern"),
                            rs.getString("Schedule")));
                }
            }
        }
        return recordTypes;
    }

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


    private static final String GET_NUMBER_PATTERN_BY_ID =
            "SELECT NumberPattern " +
                    "FROM recordtypes " +
                    "WHERE Id = ?";
    public static String getNumberPattern(int typeId) throws SQLException {
        try (Connection con = DbConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_NUMBER_PATTERN_BY_ID)) {
            ps.setInt(1, typeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("NumberPattern");
                }
            }
        }
        return null;
    }
}
