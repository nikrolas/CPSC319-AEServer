package com.discovery.channel.database;


import com.discovery.channel.model.Record;
import com.discovery.channel.properties.DefaultProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Created by Qiushan on 2018/1/20.
 */
public class DBUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBUtil.class);

    private static DefaultProperties PROPERTIES = DefaultProperties.getInstance();
    private static final String HOST = PROPERTIES.getProperty("DATABASE.HOST");
    private static final int PORT = PROPERTIES.getIntProperty("DATABASE.PORT");
    private static final String USERNAME = PROPERTIES.getProperty("DATABASE.USERNAME");
    private static final String PASSWORD = PROPERTIES.getProperty("DATABASE.PASSWORD");
    private static final String DEFAULT_DATABASE = PROPERTIES.getProperty("DATABASE");

    private static final String JDBC_URL = String.format(PROPERTIES.getProperty("DATABASE.JDBC.TEMPLATE"), HOST, PORT, DEFAULT_DATABASE);


    // Find record with give ID; If not, return null
    private static String GET_RECORD_BY_ID_SQL = "SELECT * " +
            "FROM records " +
            "WHERE Id = ?";
    public static Record getRecordById(int recordId){
        LOGGER.info(String.format(GET_RECORD_BY_ID_SQL + ": Id = %d", recordId));
        try(Connection conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            PreparedStatement ps = conn.prepareStatement(GET_RECORD_BY_ID_SQL)) {
            ps.setInt(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // TODO need a way to unify access to table fields
                    return new Record(recordId, rs.getString("Title"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }
}
