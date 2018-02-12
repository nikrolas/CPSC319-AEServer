package com.discovery.channel.database;

import com.discovery.channel.model.Record;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;


/**
 * Created by Qiushan on 2018/1/20.
 */
public class DBUtil {
    // TODO move to config file
    public static String HOSTNAME = "cs319-discoverychannel.csgbwdrdx2ye.us-east-2.rds.amazonaws.com";
    public static int PORT = 3306;
    public static String DRIVER ="mysql";
    public static String USERNAME = "cs319_rds";
    public static String PASSWORD = "discoverychannel";

    public static String DEFAULT_DATABASE = "recordr";

    private static String getJdbcUrl(String database){
        // TODO move to config file
        //String template = "jdbc:%s://%s:%s/%s?useSSL=false";
        //jdbc:driver://hostname:port/dbName?user=userName&password=password
        String template = "jdbc:%s://%s:%d/%s?user=%s&password=%s&useSSL=false";
        return String.format(template, DRIVER, HOSTNAME, PORT, database, USERNAME, PASSWORD);
    }

    // Find record with give ID; If not, return null
    private static String GET_RECORD_BY_ID_SQL = "SELECT * " +
            "FROM records " +
            "WHERE Id = ?";

    public static Record getRecordById(int recordId){
        try(Connection conn = DriverManager.getConnection(getJdbcUrl(DEFAULT_DATABASE), USERNAME, PASSWORD);
            PreparedStatement ps = conn.prepareStatement(GET_RECORD_BY_ID_SQL)) {
            ps.setInt(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // TODO need a way to unify access to table fields
                    return new Record(recordId, rs.getString("Title"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
