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
}
