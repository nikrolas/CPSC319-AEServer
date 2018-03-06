package com.discovery.channel.database;

import com.discovery.channel.model.RetentionSchedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RetentionScheduleController {

    /**
     * Get all retention schedules
     * @return
     * @throws SQLException
     */
    private static final String GET_ALL_SCHEDULES = "SELECT * FROM retentionschedules";
    public static List<RetentionSchedule> getAllRetentionSchedules() throws SQLException {
        List<RetentionSchedule> schedules = new ArrayList<>();
        try(Connection conn = DbConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(GET_ALL_SCHEDULES)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    schedules.add(new RetentionSchedule(rs.getInt("Id"),
                            rs.getString("Name"),
                            rs.getString("Code"),
                            rs.getInt("Years")));
                }
            }
        }
        return schedules;
    }
}
