package com.discovery.channel.database;

import com.discovery.channel.database.RecordController;
import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.form.RecordsForm;
import com.discovery.channel.form.UpdateRecordForm;
import com.discovery.channel.model.Container;
import com.discovery.channel.model.Record;
import com.discovery.channel.model.RecordState;
import com.discovery.channel.response.BatchResponse;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RecordControllerTest {
    private static final int RESTRICTED_LOCATION_ID = 84;
    private static final int FULL_PRIV_RMC = 600;
    private static final int RMC = 500; // authorized for locations 8 and 5


    @Test
    public void testGetRecordById() throws SQLException {
        Record record = RecordController.getRecordById(51, FULL_PRIV_RMC);
        assertNotNull(record);
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
    public void testGetRecordWithRestrictedLocation() throws SQLException {

        Record r = createNewRecordWithoutContainer("TESTING-Restricted-Location", "EDM-2018", RESTRICTED_LOCATION_ID, 26, 3);
        Record record = RecordController.createRecord(r, FULL_PRIV_RMC);
        assertNotNull(record);
        // non owner cannot view records of restricted locations
        assertThrows(AuthenticationException.class, () -> RecordController.getRecordById(record.getId(), RMC));

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());
        RecordsForm recordForDeletion = new RecordsForm();
        recordForDeletion.setRecordIds(recordIds);
        RecordController.deleteRecords(FULL_PRIV_RMC, recordForDeletion);
    }

    @Test
    public void testGetNonExistentRecord() throws SQLException{

        NoResultsFoundException e = assertThrows(NoResultsFoundException.class, () -> {
            RecordController.getRecordById(50, FULL_PRIV_RMC);
        });

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
        RecordsForm recordForDeletion = new RecordsForm();
        recordForDeletion.setRecordIds(recordIds);
        RecordController.deleteRecords(500, recordForDeletion);

        NoResultsFoundException e = assertThrows(NoResultsFoundException.class, () -> {
            RecordController.getRecordById(id, FULL_PRIV_RMC);
        });

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

        List<Integer> classIds = new ArrayList<>();
        classIds.add(1052);
        classIds.add(144);

        UpdateRecordForm form = new UpdateRecordForm("TESTING-UPDATE", 10,
                classIds,
                "445810223",
                "updating a record",
                1,
                0);

        RecordController.updateRecord(record.getId(), 500, form);
        Record updatedRecord = RecordController.getRecordById(recordId, FULL_PRIV_RMC);

        assertEquals("TESTING-UPDATE", updatedRecord.getTitle());
        assertEquals(1, updatedRecord.getScheduleYear());
        assertTrue(updatedRecord.getNotes().contains("updating a record"));

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(updatedRecord.getId());
        RecordsForm recordForDeletion = new RecordsForm();
        recordForDeletion.setRecordIds(recordIds);
        RecordController.deleteRecords(500, recordForDeletion);

        NoResultsFoundException e = assertThrows(NoResultsFoundException.class, () -> {
            RecordController.getRecordById(updatedRecord.getId(), FULL_PRIV_RMC);
        });

    }


    @Test
    public void testUpdateWithBadRole() throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-BAD-ROLES", "EDM-2018", 5, 26, 3);
        Record record = RecordController.createRecord(r, 500);

        List<Integer> classIds = new ArrayList<>();
        classIds.add(1052);
        classIds.add(144);

        UpdateRecordForm form = new UpdateRecordForm("TESTING-UPDATE", 10,
                classIds,
                "anConsignmentCode",
                "updating a record",
                1,
                24372);

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> {
            RecordController.updateRecord(record.getId(), 43, form);
        });

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());
        RecordsForm recordForDeletion = new RecordsForm();
        recordForDeletion.setRecordIds(recordIds);
        RecordController.deleteRecords(500, recordForDeletion);

        NoResultsFoundException em = assertThrows(NoResultsFoundException.class, () -> {
            RecordController.getRecordById(record.getId(), FULL_PRIV_RMC);
        });

    }


    @Test
    public void testUpdateWithBadLocation() throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-BAD-Location", "EDM-2018", 5, 26, 3);
        Record record = RecordController.createRecord(r, 500);

        List<Integer> classIds = new ArrayList<>();
        classIds.add(1052);
        classIds.add(144);

        UpdateRecordForm form = new UpdateRecordForm("TESTING-UPDATE", 10,
                classIds,
                "anConsignmentCode",
                "updating a record",
                1,
                24372);

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> {
            RecordController.updateRecord(record.getId(), 478, form);
        });

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());
        RecordsForm recordForDeletion = new RecordsForm();
        recordForDeletion.setRecordIds(recordIds);
        RecordController.deleteRecords(500, recordForDeletion);

        NoResultsFoundException em = assertThrows(NoResultsFoundException.class, () -> {
            RecordController.getRecordById(record.getId(), FULL_PRIV_RMC);
        });

    }


    @Test
    public void testDeleteRecordWithBadRole() throws SQLException {
        Record r = createNewRecordWithoutContainer("TESTING-Deletion-with-bad-role", "EDM-2018", 5, 26, 3);
        Record record = RecordController.createRecord(r, 500);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());
        RecordsForm recordForDeletion = new RecordsForm();
        recordForDeletion.setRecordIds(recordIds);

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> {
            RecordController.deleteRecords(2, recordForDeletion);
        });

        RecordController.deleteRecords(500, recordForDeletion);

        NoResultsFoundException em = assertThrows(NoResultsFoundException.class, () -> {
            RecordController.getRecordById(record.getId(), FULL_PRIV_RMC);
        });
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
        RecordsForm recordsForDeletion = new RecordsForm();
        recordsForDeletion.setRecordIds(recordIds);

        RecordController.deleteRecords(135, recordsForDeletion);

        NoResultsFoundException em1 = assertThrows(NoResultsFoundException.class, () -> {
            RecordController.getRecordById(recordsForDeletion.getRecordIds().get(0), FULL_PRIV_RMC);
        });

        NoResultsFoundException em2 = assertThrows(NoResultsFoundException.class, () -> {
            RecordController.getRecordById(recordsForDeletion.getRecordIds().get(1), FULL_PRIV_RMC);
        });

        NoResultsFoundException em3 = assertThrows(NoResultsFoundException.class, () -> {
            RecordController.getRecordById(recordsForDeletion.getRecordIds().get(2), FULL_PRIV_RMC);
        });

    }


    @Test
    public void testDeleteMultipleRecordsWithNonExistentRecords() throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-Deletion-1", "EDM-2018", 51, 10, 3);
        Record record = RecordController.createRecord(r, 110);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());
        recordIds.add(50);
        recordIds.add(49);
        RecordsForm recordForDeletion = new RecordsForm();
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
        RecordsForm recordForDeletion = new RecordsForm();
        recordForDeletion.setRecordIds(recordIds);

        BatchResponse response = RecordController.deleteRecords(500, recordForDeletion);
        assertTrue(response.getResponseList().isEmpty());

    }

    @Test
    public void testDestroyOneRecord() throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-Destroy", "EDM-2018", 8, 209, 3);
        Record newRecord = RecordController.createRecord(r, RMC);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(newRecord.getId());

        Container container = createValidNewContainerWithRecords("Test-destroy", "2018", recordIds);
        Container newContainer = ContainerController.createContainer(container, RMC);

        // check record does have closedat
        Record record = RecordController.getRecordById(newRecord.getId(), RMC);
        assertNotNull(record.getClosedAt());
        // check initial state
        assertEquals(RecordState.ARCHIVED_LOCAL.getId(), record.getStateId());

        // create form for multiple destroy and delete
        RecordsForm recordForm = new RecordsForm();
        recordForm.setRecordIds(recordIds);

        // destroy
        ResponseEntity response = RecordController.prepareToDestroyRecords(recordForm, RMC);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Record updatedRecord = RecordController.getRecordById(newRecord.getId(), RMC);
        // check state equals to destroyed
        assertEquals(RecordState.DESTROYED.getId(), updatedRecord.getStateId());

        // remove test record
        RecordController.deleteRecords(RMC, recordForm);

        // remove test container
        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(newContainer.getContainerId());
        ContainerController.deleteContainers(listOfContainerIds, RMC);

    }

    @Test
    public void testDestroyMultipleRecords()throws SQLException{
        Record r1 = createNewRecordWithoutContainer("TESTING-Destroy", "EDM-2018", 8, 209, 3);
        Record newRecord1 = RecordController.createRecord(r1, RMC);

        Record r2 = createNewRecordWithoutContainer("TESTING-Destroy-2", "EDM-2018", 8, 209, 3);
        Record newRecord2 = RecordController.createRecord(r2, RMC);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(newRecord1.getId());
        recordIds.add(newRecord2.getId());

        Container container = createValidNewContainerWithRecords("Test-destroy", "2018", recordIds);
        Container newContainer = ContainerController.createContainer(container, RMC);

        Record record1 = RecordController.getRecordById(newRecord1.getId(), RMC);
        Record record2 = RecordController.getRecordById(newRecord2.getId(), RMC);
        assertNotNull(record1.getClosedAt());
        assertNotNull(record2.getClosedAt());
        assertEquals(RecordState.ARCHIVED_LOCAL.getId(), record1.getStateId());
        assertEquals(RecordState.ARCHIVED_LOCAL.getId(), record2.getStateId());

        RecordsForm recordForm = new RecordsForm();
        recordForm.setRecordIds(recordIds);


        ResponseEntity response = RecordController.prepareToDestroyRecords(recordForm, RMC);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Record updatedRecord1 = RecordController.getRecordById(record1.getId(), RMC);
        Record updatedRecord2 = RecordController.getRecordById(record2.getId(), RMC);

        assertEquals(RecordState.DESTROYED.getId(), updatedRecord1.getStateId());
        assertEquals(RecordState.DESTROYED.getId(), updatedRecord2.getStateId());


        RecordController.deleteRecords(RMC, recordForm);
        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(newContainer.getContainerId());
        ContainerController.deleteContainers(listOfContainerIds, RMC);

    }


    @Test
    public void testDestroyRecordWithFutureDestructionDate() throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-Destroy", "EDM-2018", 5, 10, 3);
        Record newRecord = RecordController.createRecord(r, RMC);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(newRecord.getId());

        Container container = createValidNewContainerWithRecords("Test-destroy", "2018", recordIds);
        Container newContainer = ContainerController.createContainer(container, RMC);

        Record record = RecordController.getRecordById(newRecord.getId(), RMC);
        assertNotNull(record.getClosedAt());
        assertEquals(RecordState.ARCHIVED_LOCAL.getId(), record.getStateId());

        RecordsForm recordform = new RecordsForm();
        recordform.setRecordIds(recordIds);

        ResponseEntity<?> response = RecordController.prepareToDestroyRecords(recordform, RMC);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Record updatedRecord = RecordController.getRecordById(newRecord.getId(), RMC);
        assertEquals(RecordState.ARCHIVED_LOCAL.getId(), updatedRecord.getStateId());


        RecordController.deleteRecords(RMC, recordform);

        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(newContainer.getContainerId());
        ContainerController.deleteContainers(listOfContainerIds, RMC);

    }

    @Test
    public void testDestroyRecordWithBadLocation() throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-Destroy", "EDM-2018", 51, 209, 3);
        Record newRecord = RecordController.createRecord(r, 110);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(newRecord.getId());

        Container container = createValidNewContainerWithRecords("Test-destroy", "2018", recordIds);
        Container newContainer = ContainerController.createContainer(container, 110);

        Record record = RecordController.getRecordById(newRecord.getId(), RMC);
        assertNotNull(record.getClosedAt());
        assertEquals(RecordState.ARCHIVED_LOCAL.getId(), record.getStateId());

        RecordsForm recordform = new RecordsForm();
        recordform.setRecordIds(recordIds);

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> {
            RecordController.prepareToDestroyRecords(recordform, 400);
        });

        Record updatedRecord = RecordController.getRecordById(newRecord.getId(), RMC);
        assertEquals(RecordState.ARCHIVED_LOCAL.getId(), updatedRecord.getStateId());

        RecordController.deleteRecords(RMC, recordform);

        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(newContainer.getContainerId());
        ContainerController.deleteContainers(listOfContainerIds, RMC);
    }

    @Test
    public void testDestroyRecordWithBadRole() throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-Destroy", "EDM-2018", 8, 209, 3);
        Record newRecord = RecordController.createRecord(r, 400);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(newRecord.getId());

        Container container = createValidNewContainerWithRecords("Test-destroy", "2018", recordIds);
        Container newContainer = ContainerController.createContainer(container, RMC);

        Record record = RecordController.getRecordById(newRecord.getId(), RMC);
        assertNotNull(record.getClosedAt());
        assertEquals(RecordState.ARCHIVED_LOCAL.getId(), record.getStateId());

        RecordsForm recordform = new RecordsForm();
        recordform.setRecordIds(recordIds);

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> {
            RecordController.prepareToDestroyRecords(recordform, 99);
        });

        Record updatedRecord = RecordController.getRecordById(newRecord.getId(), RMC);
        assertEquals(RecordState.ARCHIVED_LOCAL.getId(), updatedRecord.getStateId());

        RecordController.deleteRecords(RMC, recordform);

        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(newContainer.getContainerId());
        ContainerController.deleteContainers(listOfContainerIds, RMC);
    }

    @Test
    public void testDestroyOneRecordWtihNoClosedAt()throws SQLException{
        Record r = createNewRecordWithoutContainer("TESTING-Destroy", "EDM-2018", 51, 10, 3);
        Record record = RecordController.createRecord(r, 110);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(record.getId());

        RecordsForm recordform = new RecordsForm();
        recordform.setRecordIds(recordIds);

        ResponseEntity<?> response = RecordController.prepareToDestroyRecords(recordform, 110);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        RecordsForm recordForDeletion = new RecordsForm();
        recordForDeletion.setRecordIds(recordIds);
        RecordController.deleteRecords(110, recordForDeletion);
    }

    @Test
    public void testDestroyRecordsWithNoClosedAtndSuccessCase() throws SQLException{
        Record r1 = createNewRecordWithoutContainer("TESTING-Destroy", "EDM-2018", 5, 209, 3);
        Record newRecord1 = RecordController.createRecord(r1, RMC);

        Record r2 = createNewRecordWithoutContainer("TESTING-Destroy-2", "EDM-2018", 5, 209, 3);
        Record newRecord2 = RecordController.createRecord(r2, RMC);

        List<Integer> recordIds = new ArrayList<>();
        recordIds.add(newRecord2.getId());

        Container container = createValidNewContainerWithRecords("Test-destroy", "2018", recordIds);
        Container newContainer = ContainerController.createContainer(container, RMC);


        Record failedRecord = RecordController.getRecordById(newRecord1.getId(), RMC);
        Record goodRecord = RecordController.getRecordById(newRecord2.getId(), RMC);
        assertTrue(failedRecord.getClosedAt() == null);
        assertNotNull(goodRecord.getClosedAt());
        assertEquals(RecordState.ACTIVE.getId(), failedRecord.getStateId());
        assertEquals(RecordState.ARCHIVED_LOCAL.getId(), goodRecord.getStateId());

        recordIds.add(newRecord1.getId());
        RecordsForm recordform = new RecordsForm();
        recordform.setRecordIds(recordIds);

        ResponseEntity<?> response = RecordController.prepareToDestroyRecords(recordform, RMC);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map map = new HashMap<>();
        map.putAll((Map) response.getBody());

        assertEquals("The record is not closed.",map.get("error"));

        Record updatedFailedRecord = RecordController.getRecordById(failedRecord.getId(), RMC);
        Record updatedGoodRecord = RecordController.getRecordById(goodRecord.getId(), RMC);
        assertEquals(RecordState.ACTIVE.getId(), updatedFailedRecord.getStateId());
        assertEquals(RecordState.ARCHIVED_LOCAL.getId(), updatedGoodRecord.getStateId());

        RecordController.deleteRecords(RMC, recordform);

        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(newContainer.getContainerId());
        ContainerController.deleteContainers(listOfContainerIds, RMC);

    }

    private Record createNewRecordWithoutContainer(String title, String recordNumber,int locationId, int scheduleId, int typeId) {

        List<Integer> classIds = new ArrayList<>();
        classIds.add(1052);
        classIds.add(14);

        return new Record(title, recordNumber, scheduleId, typeId, "RF011329724",
                0, locationId, classIds,
                "CREATED FOR TESTING -- testCreateRecord");
    }

    private Container createValidNewContainerWithRecords(String title, String number, List<Integer> listOfRecordIds) throws SQLException {

        return new Container(0, number, title, null, null, null,
                1, 5, 1, 8, null, listOfRecordIds, null);
    }

}
