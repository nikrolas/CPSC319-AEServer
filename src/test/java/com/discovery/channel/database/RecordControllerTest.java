package com.discovery.channel.database;

import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.form.DeleteRecordsForm;
import com.discovery.channel.form.UpdateRecordForm;
import com.discovery.channel.model.Record;
import com.discovery.channel.response.BatchResponse;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;



import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
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
    public void testGetNonExistentRecord() throws SQLException{

        Record record = RecordController.getRecordById(50);
        assertNull(record);

    }

    @Test
    public void testGetRecordByIdWithBadLocation(){
        // Todo
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
        assertNull(removedRecord);
    }

    @Test
    public void testCreateRecordWithBadRole(){
        Record r = createNewRecordWithoutContainer("TESTING-2", "EDM-2018", 5, 26, 3);
        AuthenticationException e = assertThrows(AuthenticationException.class, () -> {
            RecordController.createRecord(r, 10);
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
        assertNull(removedRecord);
    }


    @Test
    public void testUpdateWithBadRole() throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-BAD-ROLES", "EDM-2018", 5, 26, 3);
        Record record = RecordController.createRecord(r, 500);

        UpdateRecordForm form = new UpdateRecordForm("TESTING-UPDATE", 10,
                "ADVISORY SERVICES/ADVICE",
                "anConsignmentCode",
                "updating a record",
                1,
                24372);

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> {
            RecordController.updateRecord(record.getId(), 43, form);
        });

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());
        DeleteRecordsForm recordForDeletion = new DeleteRecordsForm();
        recordForDeletion.setRecordIds(recordIds);
        RecordController.deleteRecords(500, recordForDeletion);

        Record removedRecord = RecordController.getRecordById(record.getId());
        assertNull(removedRecord);

    }


    @Test
    public void testUpdateWithBadLocation() throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-BAD-Location", "EDM-2018", 5, 26, 3);
        Record record = RecordController.createRecord(r, 500);

        UpdateRecordForm form = new UpdateRecordForm("TESTING-UPDATE", 10,
                "ADVISORY SERVICES/ADVICE",
                "anConsignmentCode",
                "updating a record",
                1,
                24372);

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> {
            RecordController.updateRecord(record.getId(), 478, form);
        });

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());
        DeleteRecordsForm recordForDeletion = new DeleteRecordsForm();
        recordForDeletion.setRecordIds(recordIds);
        RecordController.deleteRecords(500, recordForDeletion);

        Record removedRecord = RecordController.getRecordById(record.getId());
        assertNull(removedRecord);

    }


    @Test
    public void testDeleteRecordWithBadRole() throws SQLException {
        Record r = createNewRecordWithoutContainer("TESTING-Deletion-with-bad-role", "EDM-2018", 5, 26, 3);
        Record record = RecordController.createRecord(r, 500);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());
        DeleteRecordsForm recordForDeletion = new DeleteRecordsForm();
        recordForDeletion.setRecordIds(recordIds);

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> {
            RecordController.deleteRecords(2, recordForDeletion);
        });

        RecordController.deleteRecords(500, recordForDeletion);
    }


    @Test
    public void testDeleteRecordWithBadLocation()throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-Deletion-with-bad-location", "EDM-2018", 5, 26, 3);
        Record record = RecordController.createRecord(r, 598);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());
        DeleteRecordsForm recordForDeletion = new DeleteRecordsForm();
        recordForDeletion.setRecordIds(recordIds);

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> {
            RecordController.deleteRecords(475, recordForDeletion);
        });

        RecordController.deleteRecords(598, recordForDeletion);

    }


    @Test
    public void testDeleteMultipleRecords() throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-Deletion-1", "EDM-2018", 51, 10, 3);
        Record record = RecordController.createRecord(r, 110);

        Record r2 = createNewRecordWithoutContainer("TESTING-Deletion-2", "EDM-2018", 51, 24, 3);
        Record record2 = RecordController.createRecord(r2, 111);

        Record r3 = createNewRecordWithoutContainer("TESTING-Deletion-3", "EDM-2018", 51, 79, 3);
        Record record3 = RecordController.createRecord(r3, 112);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());
        recordIds.add(record2.getId());
        recordIds.add(record3.getId());
        DeleteRecordsForm recordForDeletion = new DeleteRecordsForm();
        recordForDeletion.setRecordIds(recordIds);

        RecordController.deleteRecords(135, recordForDeletion);

    }


    @Test
    public void testDeleteMultipleRecordsWithNonExistentRecords() throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-Deletion-1", "EDM-2018", 51, 10, 3);
        Record record = RecordController.createRecord(r, 110);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());
        recordIds.add(50);
        recordIds.add(49);
        DeleteRecordsForm recordForDeletion = new DeleteRecordsForm();
        recordForDeletion.setRecordIds(recordIds);

        BatchResponse response = RecordController.deleteRecords(110, recordForDeletion);
        List<BatchResponse.Response> responses = response.getResponseList();
        assertTrue(responses.get(0).isStatus());
        assertFalse(responses.get(1).isStatus());
        assertFalse(responses.get(2).isStatus());

    }

    @Test
    public void testDeleteRecordsWithEmptyInput()throws SQLException{

        List<Integer> recordIds = new ArrayList<>();
        DeleteRecordsForm recordForDeletion = new DeleteRecordsForm();
        recordForDeletion.setRecordIds(recordIds);

        BatchResponse response = RecordController.deleteRecords(500, recordForDeletion);
        assertTrue(response.getResponseList().isEmpty());

    }


    @Test
    public void testDestroyOneRecord() throws SQLException, ParseException {
        // Todo
    }


    @Test
    public void testDestroyOneRecordWtihNoClosedAt()throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-Destroy", "EDM-2018", 51, 10, 3);
        Record record = RecordController.createRecord(r, 110);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());

        ResponseEntity<?> response = RecordController.prepareToDestroyRecords(recordIds, 110);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        DeleteRecordsForm recordForDeletion = new DeleteRecordsForm();
        recordForDeletion.setRecordIds(recordIds);
        RecordController.deleteRecords(110, recordForDeletion);
    }



    private Record createNewRecordWithoutContainer(String title, String recordNumber,int locationId, int scheduleId, int typeId) {
        return new Record(title, recordNumber, scheduleId, typeId, "RF011329724",
                0, locationId, "Advisory Services/Advice/Reports",
                "CREATED FOR TESTING -- testCreateRecord");
    }

}
