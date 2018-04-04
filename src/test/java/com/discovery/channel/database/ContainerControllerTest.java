package com.discovery.channel.database;

import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.form.RecordsForm;
import com.discovery.channel.model.Container;
import com.discovery.channel.model.Record;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ContainerControllerTest {

    private static final int RMC_USER_ID = 500;
    private static final int NON_RMC_USER_ID = 100;

    private static final int FULL_PRIVILEGE_RMC = 600;

    @Test
    void getMultipleRecordsInContainer() throws SQLException{
        List<Integer> ids = ContainerController.getRecordIdsInContainer(11125);
        assertEquals (ids.size(), 13);
    }


    @Test
    void getNonExistantContainer() {
        NoResultsFoundException e = assertThrows(NoResultsFoundException.class, () -> {
            ContainerController.getContainerById(-1, FULL_PRIVILEGE_RMC);
        });
        assertTrue(e.getMessage().contains("This container does not exist."));
    }

    @Test
    void getContainerHappyPath() throws SQLException {
        Container c = ContainerController.getContainerById(11125, FULL_PRIVILEGE_RMC);
        assertEquals(c.getContainerId(), 11125);
        assertEquals(c.getContainerNumber(),"2006/002-EDM");
        assertEquals(c.getTitle(), "Vel itaque vitae repellendus architecto");

        assertEquals(c.getConsignmentCode(), "362817351");
        assertEquals(c.getCreatedAt(), new Date(1142841600000L));
        assertEquals(c.getUpdatedAt(), new Date(1453881600000L));

        List<Integer> expected = Arrays.asList(274,275,276,313,314,315,316,317,318,319,320,321,3211);
        assertTrue(expected.containsAll(c.getChildRecordIds()) && c.getChildRecordIds().containsAll(expected));

        assertEquals(c.getStateId(), 6);
        assertEquals(c.getLocationId(), 5);
        assertEquals(c.getTypeId(), 32);
        assertEquals(c.getScheduleId(), 44);

    }

    @Test
    void createNewContainerHappyPath() throws SQLException, JSONException {

        List<Integer> listOfRecordIds = new ArrayList<>();
        Record r = createOneRecord();
        listOfRecordIds.add(r.getId());

        Container sampleContainer = createValidNewContainerWithRecords("TEST - HappyPathTitle","", listOfRecordIds);
        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);
        assertNotNull(c.getContainerNumber());
        assertNotNull(c.getTitle());
        assertNotNull(c.getUpdatedAt());
        assertNotNull(c.getCreatedAt());

        RecordsForm rf = new RecordsForm();
        rf.setRecordIds(listOfRecordIds);
        RecordController.deleteRecords(RMC_USER_ID, rf);

        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(c.getContainerId());
        ContainerController.deleteContainers(listOfContainerIds, RMC_USER_ID);

    }

    @Test
    void createNewContainerMissingTitle() throws SQLException, JSONException {
        List<Integer> listOfRecordIds = new ArrayList<>();
        Record r = createOneRecord();
        listOfRecordIds.add(r.getId());

        Container sampleContainer = createValidNewContainerWithRecords("","",listOfRecordIds);
        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);
        sampleContainer.setTitle(null);

        Exception e = assertThrows(Exception.class, () -> {
            ContainerController.createContainer(c, RMC_USER_ID);
        });

        RecordsForm rf = new RecordsForm();
        rf.setRecordIds(listOfRecordIds);
        RecordController.deleteRecords(RMC_USER_ID, rf);

        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(c.getContainerId());
        ContainerController.deleteContainers(listOfContainerIds, RMC_USER_ID);

    }


    @Test
    void createNewContainerUnauthorizedUser() throws SQLException, JSONException {
        List<Integer> listOfRecordIds = new ArrayList<>();
        Record r = createOneRecord();
        listOfRecordIds.add(r.getId());

        Container sampleContainer = createValidNewContainerWithRecords("InvalidUserTitle", "", listOfRecordIds);

        Exception e = assertThrows(AuthenticationException.class, () -> {
            ContainerController.createContainer(sampleContainer, NON_RMC_USER_ID);
        });

        assertTrue(e.getMessage().contains("You do not have permission to create containers."));

        RecordsForm rf = new RecordsForm();
        rf.setRecordIds(listOfRecordIds);
        RecordController.deleteRecords(RMC_USER_ID, rf);

    }

    @Test
    void deleteOneContainer() throws SQLException{
        // create list of records
        List<Integer> listOfRecordIds = new ArrayList<>();
        Record r = createOneRecord();
        listOfRecordIds.add(r.getId());

        // create container contain at least one record
        Container sampleContainer = createValidNewContainerWithRecords("TESTING_FOR_DELETE", "2018", listOfRecordIds);
        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);

        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(c.getContainerId());

        RecordsForm rf = new RecordsForm();
        rf.setRecordIds(listOfRecordIds);

        // destroy records
        RecordController.prepareToDestroyRecords(rf, RMC_USER_ID);

        // container should be empty
        assertTrue(ContainerController.getRecordIdsInContainer(c.getContainerId()).isEmpty());

        // delete container
        ResponseEntity responseStatus = ContainerController.deleteContainers(listOfContainerIds, RMC_USER_ID);
        assertEquals(HttpStatus.OK, responseStatus.getStatusCode());

        // should not find container
        Exception e = assertThrows(NoResultsFoundException.class, () -> {
            ContainerController.getContainerById(c.getContainerId(), RMC_USER_ID);
        });

        // should not find container note
        assertTrue(NoteTableController.getContainerNotes(c.getContainerId()) == null);


        // clean up
        RecordController.deleteRecords(RMC_USER_ID, rf);
    }

    @Test
    void deleteMultipuleContainers() throws SQLException{

        List<Integer> onelistOfRecordIds = new ArrayList<>();
        Record r = createOneRecord();
        onelistOfRecordIds.add(r.getId());

        List<Integer> seclistOfRecordIds = new ArrayList<>();
        Record r2 = createOneRecord();
        seclistOfRecordIds.add(r2.getId());

        Container sampleContainer = createValidNewContainerWithRecords("TESTING_FOR_DELETE", "2018", onelistOfRecordIds);
        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);

        Container sampleContainer2 = createValidNewContainerWithRecords("TESTING_FOR_DELETE_2", "2018-2", seclistOfRecordIds);
        Container c2 = ContainerController.createContainer(sampleContainer2, RMC_USER_ID);

        RecordsForm rf = new RecordsForm();
        rf.setRecordIds(onelistOfRecordIds);

        RecordController.prepareToDestroyRecords(rf, RMC_USER_ID);

        RecordsForm rf2 = new RecordsForm();
        rf2.setRecordIds(seclistOfRecordIds);

        RecordController.prepareToDestroyRecords(rf2, RMC_USER_ID);

        assertTrue(ContainerController.getRecordIdsInContainer(c.getContainerId()).isEmpty());
        assertTrue(ContainerController.getRecordIdsInContainer(c2.getContainerId()).isEmpty());

        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(c.getContainerId());
        listOfContainerIds.add(c2.getContainerId());

        ResponseEntity responseStatus = ContainerController.deleteContainers(listOfContainerIds, RMC_USER_ID);
        assertEquals(HttpStatus.OK, responseStatus.getStatusCode());

        Exception e = assertThrows(NoResultsFoundException.class, () -> {
            ContainerController.getContainerById(c.getContainerId(), RMC_USER_ID);
        });

        assertTrue(NoteTableController.getContainerNotes(c.getContainerId()) == null);

        Exception e1 = assertThrows(NoResultsFoundException.class, () -> {
            ContainerController.getContainerById(c2.getContainerId(), RMC_USER_ID);
        });

        assertTrue(NoteTableController.getContainerNotes(c2.getContainerId()) == null);

        RecordController.deleteRecords(RMC_USER_ID, rf);
        RecordController.deleteRecords(RMC_USER_ID, rf2);

    }

    @Test
    void  deleteOneContainerWithBadRole() throws SQLException{

        List<Integer> listOfRecordIds = new ArrayList<>();
        Record r = createOneRecord();
        listOfRecordIds.add(r.getId());

        Container sampleContainer = createValidNewContainerWithRecords("TESTING_FOR_DELETE_BAD_ROLE", "2018", listOfRecordIds);
        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);

        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(c.getContainerId());

        RecordsForm rf = new RecordsForm();
        rf.setRecordIds(listOfRecordIds);

        RecordController.prepareToDestroyRecords(rf, RMC_USER_ID);

        assertTrue(ContainerController.getRecordIdsInContainer(c.getContainerId()).isEmpty());

        Exception e = assertThrows(AuthenticationException.class, () -> {
            ContainerController.deleteContainers(listOfContainerIds, NON_RMC_USER_ID);
        });

        ContainerController.deleteContainers(listOfContainerIds, RMC_USER_ID);
        RecordController.deleteRecords(RMC_USER_ID, rf);
    }

    @Test
    void deleteOneContainerWithRecords() throws SQLException{
        List<Integer> listOfRecordIds = new ArrayList<>();
        Record r = createOneRecord();
        Record r2 = createOneRecord();
        listOfRecordIds.add(r.getId());
        listOfRecordIds.add(r2.getId());

        Container sampleContainer = createValidNewContainerWithRecords("TESTING_FOR_DELETE_CONTAINER_CONTAINING_RECORDS", "2018", listOfRecordIds);
        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);

        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(c.getContainerId());

        assertTrue(!ContainerController.getRecordIdsInContainer(c.getContainerId()).isEmpty());
        ResponseEntity responseStatus = ContainerController.deleteContainers(listOfContainerIds, RMC_USER_ID);
        assertEquals(HttpStatus.BAD_REQUEST, responseStatus.getStatusCode());


        RecordsForm rf = new RecordsForm();
        rf.setRecordIds(listOfRecordIds);

        RecordController.prepareToDestroyRecords(rf, RMC_USER_ID);
        ContainerController.deleteContainers(listOfContainerIds, RMC_USER_ID);

        RecordController.deleteRecords(RMC_USER_ID, rf);

    }

    @Test
    void deleteContainerWithBadLocation() throws  SQLException{
        List<Integer> listOfRecordIds = new ArrayList<>();
        Record r = createOneRecord();
        Record r2 = createOneRecord();
        listOfRecordIds.add(r.getId());
        listOfRecordIds.add(r2.getId());

        Container sampleContainer = createValidNewContainerWithRecords("TESTING_FOR_DELETE_CONTAINER_BAD_LOCATION", "2018", listOfRecordIds);
        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);

        List<Integer> listOfContainerIds = new ArrayList<>();
        listOfContainerIds.add(c.getContainerId());

        RecordsForm rf = new RecordsForm();
        rf.setRecordIds(listOfRecordIds);

        RecordController.prepareToDestroyRecords(rf, RMC_USER_ID);

        assertTrue(ContainerController.getRecordIdsInContainer(c.getContainerId()).isEmpty());

        Exception e = assertThrows(AuthenticationException.class, () -> {
            ContainerController.deleteContainers(listOfContainerIds, 390);
        });

        ContainerController.deleteContainers(listOfContainerIds, RMC_USER_ID);
        RecordController.deleteRecords(RMC_USER_ID, rf);

    }



    private Container createValidNewContainerRequest(String title, String number)  {

        return new Container(0, number, title, null, null, null,
                1, 5, 1, 8, null, null, null);
    }

    private Container createValidNewContainerWithRecords(String title, String number, List<Integer> listOfRecordIds) throws SQLException {

        return new Container(0, number, title, null, null, null,
                1, 5, 1, 8, null, listOfRecordIds, null);
    }

    private  Record createOneRecord() throws SQLException{
        List<Integer> classIds = new ArrayList<>();
        classIds.add(1052);
        classIds.add(14);

        // schedule id 209 is 0 year
        Record r = new Record("test", "EDM-2018", 209, 3, "RF011329724",
                0, 5, classIds,
                "CREATED FOR TESTING -- testDeleteContainer");

        Record record = RecordController.createRecord(r, RMC_USER_ID);

        return record;
    }


}
