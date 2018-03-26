package com.discovery.channel.database;

import com.discovery.channel.model.Classification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClassificationController {

    private static final String FIND_ROOT_CLASSIFICATIONS =
            "SELECT Id, Name, Keyword " +
            "FROM classifications " +
            "WHERE KeyWord = ? " +
            "ORDER BY Name ASC";
    public static List<Classification> getRootClassifications() throws SQLException {
        List<Classification> classifications = new ArrayList<>();
        try(Connection conn = DbConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(FIND_ROOT_CLASSIFICATIONS)) {
            ps.setString(1, Classification.CLASSIFICATION_TYPE.T.name());
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    classifications.add(new Classification(rs.getInt("Id"),
                            rs.getString("Name"),
                            Classification.CLASSIFICATION_TYPE.fromName(rs.getString("KeyWord"))));
                }
            }
        }
        return classifications;
    }

    /**
     * Find classification by id
     *
     * @param id
     * @return
     * @throws SQLException
     */
    private static final String FIND_CLASSIFICATION_BY_ID =
            "SELECT Id, Name, KeyWord " +
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
    private static final String FIND_CHILDREN_CLASS_ID =
            "SELECT ChildId " +
            "FROM classhierarchy " +
            "WHERE ParentId = ? ";
    public static List<Integer> findChildrenClassificationIds(int classId) throws SQLException {
        List<Integer> childIds = new ArrayList<>();
        try(Connection conn = DbConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(FIND_CHILDREN_CLASS_ID)) {
            ps.setInt(1, classId);
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    childIds.add(rs.getInt("ChildId"));
                }
            }
        }
        return childIds;
    }

    /**
     * Find valid children classification Ids
     * @param clasId
     * @return
     */
    private static final String FIND_CHILDREN_CLASS =
            "SELECT c.Id AS Id, Name, KeyWord " +
            "FROM classhierarchy ch " +
            "INNER JOIN  classifications c " +
            "ON ch.childId = c.Id " +
            "WHERE ch.ParentId = ? " +
            "ORDER BY Name ASC";
    public static List<Classification> findChildrenClassifications(int parentId) throws SQLException {
        List<Classification> children = new ArrayList<>();
        try(Connection conn = DbConnect.getConnection();
            PreparedStatement ps = conn.prepareStatement(FIND_CHILDREN_CLASS)) {
            ps.setInt(1, parentId);
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    children.add(new Classification(rs.getInt("Id"),
                            rs.getString("Name"),
                            Classification.CLASSIFICATION_TYPE.fromName(rs.getString("KeyWord"))));
                }
            }
        }
        return children;
    }
}
