package com.discovery.channel.database;

import com.discovery.channel.model.NoteTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NoteTableController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoteTableController.class);

    /**
     * Save notes to db
     *
     * @param recordId
     * @param notes
     * @throws SQLException
     */
    private static final int MAX_NOTE_LEN = Integer.MAX_VALUE;
    private static final String INSERT_RECORD_NOTE = "INSERT INTO notes (TableId, RowId, Chunk, Text) " +
            "VALUES(?, ? , ? , ?)";
    private static void saveNotes(NoteTable noteTable, int recordId, String notes) throws SQLException {
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_RECORD_NOTE)){
            int chunkNum = 0;
            int startIndex = 0;
            while (startIndex < notes.length()) {
                ps.setInt(1, noteTable.id);
                ps.setInt(2, recordId);
                ps.setInt(3, chunkNum);
                ps.setString(4, notes.substring(startIndex,
                        startIndex + MAX_NOTE_LEN >= notes.length()? notes.length() : startIndex + MAX_NOTE_LEN));
                ps.addBatch();
                startIndex = startIndex + MAX_NOTE_LEN;
                chunkNum = chunkNum + 1;
            }
            ps.executeBatch();
        }
        LOGGER.info("Saved notes {} for record {}", notes, recordId);
    }

    public static void saveNotesForRecord(int recordId, String notes) throws SQLException {
        saveNotes(NoteTable.RECORDS, recordId, notes);
    }

    public static void saveNotesForContainer(int containerId, String notes) throws SQLException {
        saveNotes(NoteTable.CONTAINERS, containerId, notes);
    }

    /**
     * Delete all chunks of notes for a record
     * @param recordId
     * @return
     * @throws SQLException
     */
    private static final String DELETE_NOTE_FOR_RECORD = "DELETE FROM notes " +
            "WHERE TableId=? AND RowId = ?";
    private static int deleteNotes(NoteTable noteTable, int recordId) throws SQLException {
        int rowsUpdated = 0;
        try(Connection connection = DbConnect.getConnection();
            PreparedStatement ps = connection.prepareStatement(DELETE_NOTE_FOR_RECORD)) {
            ps.setInt(1, noteTable.id);
            ps.setInt(2, recordId);
            rowsUpdated = ps.executeUpdate();
        }
        LOGGER.info("Deleted {} note entries for record {}", rowsUpdated, recordId);
        return rowsUpdated;
    }

    public static int deleteNotesForRecord(int recordId) throws SQLException {
        return deleteNotes(NoteTable.RECORDS, recordId);
    }

    public static int deleteNotesForContainer(int containerId) throws SQLException {
        return deleteNotes(NoteTable.CONTAINERS, containerId);
    }

    public static void updateRecordNotes(int recordId, String newNotes) throws SQLException {
        deleteNotesForRecord(recordId);
        saveNotesForRecord(recordId, newNotes);
    }

    public static void updateContainerNotes(int containerId, String newNotes) throws SQLException {
        deleteNotesForContainer(containerId);
        saveNotesForContainer(containerId, newNotes);
    }
}
