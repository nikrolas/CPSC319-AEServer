package com.discovery.channel.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LocationController {


    /**
     * Load Location Code given Location Id
     *
     * @param id
     * @return Location Code
     * @throws SQLException
     */
    private static final String GET_LOCATION_CODE_BY_ID = "SELECT Code " +
            "FROM locations " +
            "WHERE Id=?";
    public static String getLocationCodeById(int id) throws SQLException {
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


    /**
     * Load Location Name given Location Id
     *
     * @param id
     * @return Location Name
     * @throws SQLException
     */
    private static final String GET_LOCATION_NAME_BY_LOCATION_ID = "SELECT Name " +
            "FROM locations " +
            "WHERE Id=?";

    public static String getLocationNameByLocationId(int id) throws SQLException{
       try (Connection conn = DbConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(GET_LOCATION_NAME_BY_LOCATION_ID)){
           ps.setInt(1, id);
           try (ResultSet rs = ps.executeQuery()) {
               if (rs.next()) {
                   return rs.getString("Name");
               }
           }
       }
       return "";
   }
}
