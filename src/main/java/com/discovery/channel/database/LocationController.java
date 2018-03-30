package com.discovery.channel.database;

import com.discovery.channel.model.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                } else {
                    throw new SQLException(String.format("Unable to retrieve location code for location id: %d.", id));
                }
            }
        }
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

   private static final String GET_USER_LOCATIONS = "SELECT * " +
           "FROM locations " +
           "WHERE Id IN " +
           "(SELECT LocationId FROM userlocations WHERE UserId = ?)";
    public static List<Location> getUserLocations(int userId) throws SQLException {
        List<Location> locations = new ArrayList<>();
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(GET_USER_LOCATIONS)){
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    locations.add(new Location(rs.getInt("Id"), rs.getString("Name"), rs.getString("Code")));
                }
            }
        }
        return locations;
    }
}
