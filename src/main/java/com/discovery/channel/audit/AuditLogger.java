package com.discovery.channel.audit;

import com.discovery.channel.database.DbConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuditLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogger.class);
    public enum Target {
        CONTAINER("CONTAINER"), RECORD("RECORD");

        String name;
        Target(String name) {
            this.name = name;
        }

        protected String getName() {
            return name;
        }
    }

    public enum ACTION {
        UPDATE("UPDATE"), DELETE("DELETE"), CREATE("CREATE");

        String name;
        ACTION(String name) {
            this.name = name;
        }

        protected String getName() {
            return name;
        }
    }

    private static final String INSERT_TO_AUDIT_LOG =
            "INSERT INTO auditlogs (UserId, Action, Target, TargetId) " +
            "VALUES (?,?,?,?)";
    public static void log(int userId, Target target, int targetId, ACTION action){
        LOGGER.info("Auditing action user Id {} target {} targetId {} action {}", userId, target.getName(), targetId, action.getName());
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_TO_AUDIT_LOG)){
            ps.setInt(1, userId);
            ps.setString(2, action.getName());
            ps.setString(3, target.getName());
            ps.setInt(4, targetId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to audit action", e);
        }
    }

    private static final String GET_AUDIT_LOGS = "SELECT * " +
            "FROM auditlogs " +
            "ORDER BY CreatedAt ASC";
    public static List<AuditLogEntry> getLogs() throws SQLException {
        List<AuditLogEntry> logs = new ArrayList<>();
        try (Connection conn = DbConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(GET_AUDIT_LOGS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                logs.add(new AuditLogEntry(rs.getInt("Id"),
                        rs.getInt("UserId"),
                        rs.getString("Action"),
                        rs.getString("Target"),
                        rs.getInt("TargetId"),
                        rs.getTimestamp("CreatedAt")));
            }
        }
        return logs;
    }
}
