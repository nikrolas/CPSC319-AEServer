package com.discovery.channel.database;

import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.model.Container;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ContainerControllerTest {

    private static final int RMC_USER_ID = 500;
    private static final int NON_RMC_USER_ID = 100;

    @Test
    void getMultipleRecordsInContainer() throws SQLException{
        List<Integer> ids = ContainerController.getRecordIdsInContainer(11125);
        assertEquals (ids.size(), 13);
    }

    //todo: consider adding a test case for getting 0 records from a container

    @Test
    void getNonExistantContainer() {
        NoResultsFoundException e = assertThrows(NoResultsFoundException.class, () -> {
            ContainerController.getContainerById(-1);
        });
        assertTrue(e.getMessage().contains("The query returned no results"));
    }

    @Test
    void getContainerHappyPath() throws SQLException {
        Container c = ContainerController.getContainerById(11125);
        assertEquals(c.getContainerId(), 11125);
        assertEquals(c.getContainerNumber(),"2006/002-EDM");
        assertEquals(c.getTitle(), "Vel itaque vitae repellendus architecto");

        assertEquals(c.getConsignmentCode(), "362817351");
        assertEquals(c.getCreatedAt(), new Date(1142841600000L));
        assertEquals(c.getUpdatedAt(), new Date(1453881600000L));

        List<Integer> expected = Arrays.asList(274,275,276,313,314,315,316,317,318,319,320,321,3211);
        assertTrue(expected.containsAll(c.getChildRecordIds()) && c.getChildRecordIds().containsAll(expected));
    }

    @Test
    void createNewContainerHappyPath() throws SQLException, JSONException {
        Container sampleContainer = createValidNewContainerRequest("TEST - HappyPathTitle","TEST - HappyPathNumber");
        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);
        assertTrue(c.getContainerNumber().equals("TEST - HappyPathNumber"));
        assertTrue(c.getTitle().equals("TEST - HappyPathTitle"));
        assertTrue(c.getUpdatedAt().toString().equals(LocalDate.now().toString()));
        assertTrue(c.getCreatedAt().toString().equals(LocalDate.now().toString()));
    }

    @Test
    void createNewContainerMissingTitle() throws SQLException, JSONException {
        Container sampleContainer = createValidNewContainerRequest("","NotMissingNumber");
        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);
        sampleContainer.setTitle(null);

        Exception e = assertThrows(Exception.class, () -> {
            ContainerController.createContainer(c, RMC_USER_ID);
        });
        assertTrue(e.getMessage().contains("Column 'Title' cannot be null"));
    }

    @Test
    void createNewContainerMissingNumber() throws SQLException, JSONException {
        Container sampleContainer = createValidNewContainerRequest("NotMissingTitle","");
        Container c = ContainerController.createContainer(sampleContainer, RMC_USER_ID);
        sampleContainer.setContainerNumber(null);

        Exception e = assertThrows(Exception.class, () -> {
            ContainerController.createContainer(c, RMC_USER_ID);
        });
        assertTrue(e.getMessage().contains("Column 'Number' cannot be null"));
    }

    @Test
    void createNewContainerUnauthorizedUser() throws SQLException, JSONException {
        Container sampleContainer = createValidNewContainerRequest("InvalidUserTitle", "InvalidUserNumber");
        Container c = ContainerController.createContainer(sampleContainer, NON_RMC_USER_ID);

        Exception e = assertThrows(AuthenticationException.class, () -> {
            ContainerController.createContainer(c, RMC_USER_ID);
        });
        assertTrue(e.getMessage().contains("Column 'Number' cannot be null"));
    }

    private Container createValidNewContainerRequest(String title, String number) throws JSONException{
        Container c = new Container(0, number, title, null, null, null, null, null);
        return c;
    }
}
