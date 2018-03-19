package com.discovery.channel.database;

import com.discovery.channel.database.RecordController;
import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.form.DeleteRecordsForm;
import com.discovery.channel.form.UpdateRecordForm;
import com.discovery.channel.model.Record;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.test.context.jdbc.Sql;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RecordControllerTest {

    @Test
    public void testGetRecordById() throws SQLException {
        Record record = RecordController.getRecordById(51);
        // Make Sure all the details are loaded
        assertTrue(StringUtils.isNotBlank(record.getLocation()));
        assertTrue(StringUtils.isNotBlank(record.getType()));
        assertTrue(StringUtils.isNotBlank(record.getState()));
        assertTrue(StringUtils.isNotBlank(record.getContainerNumber()));
        assertTrue(StringUtils.isNotBlank(record.getClassifications()));
        assertTrue(StringUtils.isNotBlank(record.getNotes()));
        assertNotNull(record.getSchedule());
    }


    @Test
    public void testCreateRecord() throws SQLException{

        Record r = createNewRecordWithoutContainer("TESTING", "EDM-2018", 5, 26, 3);
        Record record = RecordController.createRecord(r, 500);

        assertNotNull(record.getTitle());
        assertNotNull(record.getNumber());
        assertEquals(1, record.getStateId());
        assertEquals("Edmonton", record.getLocation());
        assertEquals(5, record.getScheduleYear());
        assertTrue(record.getId() != 0);
        assertEquals("Subject", record.getType());
        assertNotNull(record.getClassifications());
        int id = record.getId();
        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());
        DeleteRecordsForm recordForDeletion = new DeleteRecordsForm();
        recordForDeletion.setRecordIds(recordIds);
        RecordController.deleteRecords(500, recordForDeletion);

        Record removedRecord = RecordController.getRecordById(id);
        assertTrue(removedRecord == null);
    }

    @Test
    public void testCreateRecordWithBadRole(){
        Record r = createNewRecordWithoutContainer("TESTING-2", "EDM-2018", 5, 26, 3);
        AuthenticationException e = assertThrows(AuthenticationException.class, () -> {
            RecordController.createRecord(r, 400);
        });
    }

    @Test
    public void testCreateRecordwithBadLocation(){
        Record r = createNewRecordWithoutContainer("TESTING-2", "EDM-2018", 51, 26,9);
        AuthenticationException e = assertThrows(AuthenticationException.class, () -> {
            RecordController.createRecord(r, 500);
        });
    }


    @Test
    public void testUpdateRecord() throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING", "EDM-2018", 5, 26, 3);
        Record record = RecordController.createRecord(r, 500);
        int recordId = record.getId();
        UpdateRecordForm form = new UpdateRecordForm("TESTING-UPDATE", 10,
                "ADVISORY SERVICES/ADVICE",
                "anConsignmentCode",
                "updating a record",
                1,
                24372);

        RecordController.updateRecord(record.getId(), 500, form);
        Record updatedRecord = RecordController.getRecordById(recordId);

        assertEquals("TESTING-UPDATE", updatedRecord.getTitle());
        assertEquals(1, updatedRecord.getScheduleYear());
        assertTrue(updatedRecord.getContainerId() != 0);
        assertTrue(updatedRecord.getNotes().contains("updating a record"));

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(updatedRecord.getId());
        DeleteRecordsForm recordForDeletion = new DeleteRecordsForm();
        recordForDeletion.setRecordIds(recordIds);
        RecordController.deleteRecords(500, recordForDeletion);

        Record removedRecord = RecordController.getRecordById(updatedRecord.getId());
        assertTrue(removedRecord == null);
    }




    private Record createNewRecordWithoutContainer(String title, String recordNumber,int locationId, int scheduleId, int typeId) {
        return new Record(title, recordNumber, scheduleId, typeId, "RF011329724",
                0, locationId, "Advisory Services/Advice/Reports",
                "CREATED FOR TESTING -- testCreateRecord");
    }



}
