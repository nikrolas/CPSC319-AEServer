package com.discovery.channel.audit;

import com.discovery.channel.database.DbConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogger.class);
    public enum Target {
        CONTAINER("CONTAINER"), RECORD("RECORD"), VOLUME("VOLUME");

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
        LOGGER.info("Auditting action user Id {} target {} targetId {} action {}", userId, target.getName(), targetId, action.getName());
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_TO_AUDIT_LOG)){
            ps.setInt(1, userId);
            ps.setString(2, target.getName());
            ps.setInt(3, targetId);
            ps.setString(4, action.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to audit action", e);
        }
    }
}
