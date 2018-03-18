package com.discovery.channel.database;



import com.discovery.channel.model.State;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StateController {
    private static final String GET_ALL_STATES = "SELECT * " +
            "FROM recordstates";
    public static List<State> getAllStates() throws SQLException {
        List<State> states = new ArrayList<>();
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(GET_ALL_STATES)){
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()) {
                    states.add(new State(rs.getInt("Id"), rs.getString("Name")));
                }
            }
        }
        return states;
    }

    /**
     * Get state name by id
     *
     * @param stateId
     * @return state name
     */
    private static final String GET_STATE_BY_ID = "SELECT Name " +
            "FROM recordstates " +
            "WHERE Id = ?";
    public static String getStateName(int stateId) throws SQLException {
        try (Connection con = DbConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(GET_STATE_BY_ID)) {
            ps.setInt(1, stateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
            }
        }
        return null;
    }
}
