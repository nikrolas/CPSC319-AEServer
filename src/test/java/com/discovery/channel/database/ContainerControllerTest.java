package com.discovery.channel.database;

import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.form.RecordsForm;
import com.discovery.channel.model.Container;
import com.discovery.channel.model.Record;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.omg.CORBA.INTERNAL;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ContainerControllerTest {

    private static final int RMC_USER_ID = 500;
    private static final int NON_RMC_USER_ID = 100;

    private static final int FULL_PRIVILEGE_RMC = 600;

    private static final String TITLE = "RECORD FOR UNIT TESTING";
    private static final String NUMBER = "EDM-2018";
    private static final int SCHEDULE_ID = 26;
    private static final int SCHEDULE_ID_2 = 27;
    private static final int TYPE_ID = 8;
    private static final int TYPE_ID_2 = 11;
    private static final String CONSIGNMENT_CODE = "TEST-CODE";
    private static final String CONSIGNMENT_CODE_2 = "TEST-CODE-2";
    private static final int LOCATION_ID = 5;
    private static final int LOCATION_ID_2 = 8;
    private static final List<Integer> CLASS_IDS = new LinkedList<>(Arrays.asList(1052,14));
    private static final String NOTES = "THIS RECORD WAS CREATED FOR UNIT TESTING PURPOSES ONLY";


    private static Record r1;
    private static Record r2;
    private static Record recordWithDifferentLocation;
    private static Record recordWithDifferentConsignmentCode;
    private static Record recordWithDifferentScheduleId;
    private static Record recordWithDifferentTypeId;

    @BeforeAll
    static void setupTestRecords() throws SQLException {
        r1 = RecordController.createRecord(new Record(TITLE, NUMBER, SCHEDULE_ID, TYPE_ID, CONSIGNMENT_CODE, 0, LOCATION_ID, CLASS_IDS, NOTES), RMC_USER_ID);
        r2 = RecordController.createRecord(new Record(TITLE + 2, NUMBER, SCHEDULE_ID, TYPE_ID, CONSIGNMENT_CODE, 0, LOCATION_ID, CLASS_IDS, NOTES + 2), RMC_USER_ID);
        recordWithDifferentScheduleId = RecordController.createRecord(new Record(TITLE, NUMBER, SCHEDULE_ID_2, TYPE_ID, CONSIGNMENT_CODE, 0, LOCATION_ID, CLASS_IDS, NOTES), RMC_USER_ID);
        recordWithDifferentTypeId = RecordController.createRecord(new Record(TITLE, NUMBER, SCHEDULE_ID, TYPE_ID_2, CONSIGNMENT_CODE, 0, LOCATION_ID, CLASS_IDS, NOTES), RMC_USER_ID);
        recordWithDifferentConsignmentCode = RecordController.createRecord(new Record(TITLE, NUMBER, SCHEDULE_ID, TYPE_ID, CONSIGNMENT_CODE_2, 0, LOCATION_ID, CLASS_IDS, NOTES), RMC_USER_ID);
        recordWithDifferentLocation = RecordController.createRecord(new Record(TITLE, NUMBER, SCHEDULE_ID, TYPE_ID, CONSIGNMENT_CODE, 0, LOCATION_ID_2, CLASS_IDS, NOTES), RMC_USER_ID);
    }

    @AfterAll
    static void removeTestRecords() throws SQLException {
        RecordsForm recordsToBeDeleted = new RecordsForm();
        List<Integer> recordIds = new LinkedList<>();
        recordIds.add(r1.getId());
        recordIds.add(r2.getId());
        recordIds.add(recordWithDifferentLocation.getId());
        recordIds.add(recordWithDifferentConsignmentCode.getId());
        recordIds.add(recordWithDifferentScheduleId.getId());
        recordIds.add(recordWithDifferentTypeId.getId());
        recordsToBeDeleted.setRecordIds(recordIds);
        RecordController.deleteRecords(RMC_USER_ID, recordsToBeDeleted);
    }

    @Test
    void getMultipleRecordsInContainer() throws SQLException{
        List<Integer> ids = ContainerController.getRecordIdsInContainer(11125);
        assertEquals (ids.size(), 13);
    }

    //todo: consider adding a test case for getting 0 records from a container

    @Test
    void getNonExistantContainer() {
        NoResultsFoundException e = assertThrows(NoResultsFoundException.class, () -> {
            ContainerController.getContainerById(-1, FULL_PRIVILEGE_RMC);
        });
        assertTrue(e.getMessage().contains("The query returned no results"));
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

        //todo: this test does not check retrieval of state/stateId, location/locationId, type/typeId, schedule/scheduleId
    }

    @Test
    void createNewContainerHappyPathNoRecords() throws SQLException, JSONException {
        Container sampleContainer = createValidNewContainerRequest("TEST - HappyPathTitle","TEST - HappyPathNumber", new LinkedList<>());
        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);
        assertTrue(c.getContainerNumber().equals("TEST - HappyPathNumber"));
        assertTrue(c.getTitle().equals("TEST - HappyPathTitle"));
        assertTrue(c.getUpdatedAt().toString().equals(LocalDate.now().toString()));
        assertTrue(c.getCreatedAt().toString().equals(LocalDate.now().toString()));
        assertTrue(c.getChildRecordIds().size() == 0);
    }

//    @Test
//    void createNewContainerHappyPathWithRecords() throws SQLException, JSONException {
//
//
//        Container sampleContainer = createValidNewContainerRequest("TEST - HappyPathTitle","TEST - HappyPathNumber", new LinkedList<>());
//        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);
//        assertTrue(c.getContainerNumber().equals("TEST - HappyPathNumber"));
//        assertTrue(c.getTitle().equals("TEST - HappyPathTitle"));
//        assertTrue(c.getUpdatedAt().toString().equals(LocalDate.now().toString()));
//        assertTrue(c.getCreatedAt().toString().equals(LocalDate.now().toString()));
//        assertTrue(c.getChildRecordIds().size() == 0);
//    }

    @Test
    void createNewContainerUnauthorizedUser() throws SQLException, JSONException {
        Container sampleContainer = createValidNewContainerRequest("InvalidUserTitle", "InvalidUserNumber", new LinkedList<>());

        Exception e = assertThrows(AuthenticationException.class, () -> {
            ContainerController.createContainer(sampleContainer, NON_RMC_USER_ID);
        });
        assertTrue(e.getMessage().contains("User 100 is not authenticated to create container"));
    }



//    @Test
//    void deleteOneContainer() throws SQLException, JSONException{
//        Container sampleContainer = createValidNewContainerRequest("TESTING_FOR_DELETE", "2018");
//        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);
//
//        ResponseEntity responseStatus = ContainerController.deleteContainers(String.valueOf(c.getContainerId()), RMC_USER_ID);
//        assertEquals(responseStatus.getStatusCode(), HttpStatus.OK);
//    }

//    @Test
//    void deleteMultipuleContainers() throws SQLException, JSONException{
//        Container sampleContainer = createValidNewContainerRequest("TESTING_FOR_DELETE", "2018");
//        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);
//
//        Container sampleContainer2 = createValidNewContainerRequest("TESTING_FOR_DELETE_2", "2018-2");
//        Container c2 = ContainerController.createContainer(sampleContainer2, RMC_USER_ID);
//
//        String ids = String.valueOf(c.getContainerId()) + "," + String.valueOf(c2.getContainerId());
//        ResponseEntity responseStatus = ContainerController.deleteContainers(ids, RMC_USER_ID);
//        assertEquals(responseStatus.getStatusCode(), HttpStatus.OK);
//    }
//
//    @Test
//    void  deleteOneContainerWithRecords() throws SQLException{
//        Container c = ContainerController.getContainerById(16348, RMC_USER_ID);
//        ResponseEntity responseStatus = ContainerController.deleteContainers(String.valueOf(c.getContainerId()), RMC_USER_ID);
//        assertEquals(responseStatus.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
//    }
//
//    @Test
//    void deleteContainersWithRecords() throws SQLException{
//        Container c1 = ContainerController.getContainerById(16348, RMC_USER_ID);
//        Container c2 = ContainerController.getContainerById(16349, RMC_USER_ID);
//        String ids = String.valueOf(c1.getContainerId()) + "," + String.valueOf(c2.getContainerId());
//        ResponseEntity responseStatus = ContainerController.deleteContainers(ids, RMC_USER_ID);
//        assertEquals(responseStatus.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
//    }

    private Container createValidNewContainerRequest(String title, String number, List<Integer> recordIds) throws JSONException{
        return new Container(0, number, title, null, null, null, 1, 5, 1, 8, null, recordIds, null);
    }


}
