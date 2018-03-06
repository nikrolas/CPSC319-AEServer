package com.discovery.channel.database;

import com.discovery.channel.model.Classification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClassificationController {


    /**
     * Find classification by name
     *
     * @param name
     * @return
     * @throws SQLException
     */
    private static final String FIND_CLASSIFICATION_BY_NAME =
            "SELECT * " +
                    "FROM classifications " +
                    "WHERE Name = ?";
    public static Classification findClassificationByName(String name) throws SQLException {
        try(Connection conn = DbConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(FIND_CLASSIFICATION_BY_NAME)) {
            ps.setString(1, name);
            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Classification(rs.getInt("Id"),
                            rs.getString("Name"),
                            Classification.CLASSIFICATION_TYPE.fromName(rs.getString("KeyWord")));
                }
            }
        }
        return null;
    }

    /**
     * Find classification by name
     *
     * @param name
     * @return
     * @throws SQLException
     */
    private static final String FIND_CLASSIFICATION_BY_ID=
            "SELECT * " +
                    "FROM classifications " +
                    "WHERE Id = ?";
    public static Classification findClassificationById(int id) throws SQLException {
        try(Connection conn = DbConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(FIND_CLASSIFICATION_BY_ID)) {
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Classification(rs.getInt("Id"),
                            rs.getString("Name"),
                            Classification.CLASSIFICATION_TYPE.fromName(rs.getString("KeyWord")));
                }
            }
        }
        return null;
    }

    /**
     * Find valid children classification Ids
     * @param clasId
     * @return
     */
    private static final String FIND_CHILDREN_CLASS = "SELECT ChildId " +
            "FROM classhierarchy " +
            "WHERE ParentId = ?";
    public static List<Integer> findChildrenClassifications(int classId) throws SQLException {
        List<Integer> childIds = new ArrayList<>();
        try(Connection conn = DbConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(FIND_CHILDREN_CLASS)) {
            ps.setInt(1, classId);
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    childIds.add(rs.getInt("ChildId"));
                }
            }
        }
        return childIds;
    }

}
