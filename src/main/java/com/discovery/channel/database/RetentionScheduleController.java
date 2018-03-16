package com.discovery.channel.database;

import com.discovery.channel.model.RetentionSchedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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



    /**
     * retentionschedules table to get schedule name and years by Id
     *
     * @param retention schedule id
     * @return schedule name and years
     */
    private static final String GET_RECORD_SCHEDULE = "SELECT * " +
            "FROM retentionschedules " +
            "WHERE Id=?";
    public static Map<String, String> getRetentionSchedule(int id) throws SQLException {
        Map<String, String> schedule = new HashMap<String, String>();
        try (Connection con = DbConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_RECORD_SCHEDULE)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    schedule.put("Name", rs.getString("Name"));
                    schedule.put("Years", String.valueOf(rs.getInt("Years")));
                }
            }
        }
        return schedule;
    }
}
