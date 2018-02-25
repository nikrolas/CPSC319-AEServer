package com.discovery.channel.database;

import com.discovery.channel.model.Container;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContainerControllerTest {

    @Test
    void getMultipleRecordsInContainer() throws SQLException{
        List<Integer> ids = ContainerController.getRecordIdsInContainer(11125);
        assertThat(ids.size(), is(equalTo(13)));
    }

    //todo: consider adding a test case for getting 0 records from a container

    @Test
    void getNonExistantContainer() {
        NoResultsFoundException e = assertThrows(NoResultsFoundException.class, () -> {
            ContainerController.getContainerById(-1);
        });
        assertThat(e.getMessage(), containsString("The query returned no results"));
    }

    @Test
    void getContainerHappyPath() throws SQLException {
        Container c = ContainerController.getContainerById(11125);
        assertThat(c.getId(), is(equalTo(11125)));
        assertThat(c.getNumber(), is(equalTo("2006/002-EDM")));
        assertThat(c.getTitle(), is(equalTo("Vel itaque vitae repellendus architecto")));
        assertThat(c.getCreatedAt(), is(equalTo(new Date(1142841600000L))));
        assertThat(c.getUpdatedAt(), is(equalTo(new Date(1453881600000L))));
        assertThat(c.getChildRecordIds(), is(equalTo(new LinkedList<>(Arrays.asList(274,275,276,313,314,315,316,317,318,319,320,321,3211)))));
    }

    @Test
    void createNewContainerHappyPath() throws SQLException {
        JsonObject testRequest = createValidNewContainerRequest("HappyPathTitle","HappyPathNumber");

        Container c = ContainerController.createAndReturnNewContainer(testRequest.toString());
        assertThat(c.getNumber(), is(equalTo("TEST - HappyPathNumber")));
        assertThat(c.getTitle(), is(equalTo("TEST - HappyPathTitle")));
        assertThat(c.getUpdatedAt().toString(), is(equalTo(LocalDate.now().toString())));
        assertThat(c.getCreatedAt().toString(), is(equalTo(LocalDate.now().toString())));
    }

    @Test
    void createNewContainerMissingTitle() throws SQLException {
        JsonObject testRequest = createValidNewContainerRequest("","NotMissingNumber");
        testRequest.remove("title");

        Exception e = assertThrows(Exception.class, () -> {
            ContainerController.createAndReturnNewContainer(testRequest.toString());
        });
        assertThat(e.getMessage(), containsString("Column 'Title' cannot be null"));
    }

    @Test
    void createNewContainerMissingNumber() throws SQLException {
        JsonObject testRequest = createValidNewContainerRequest("NotMissingTitle","");
        testRequest.remove("number");

        Exception e = assertThrows(Exception.class, () -> {
            ContainerController.createAndReturnNewContainer(testRequest.toString());
        });
        assertThat(e.getMessage(), containsString("Column 'Number' cannot be null"));
    }

    private JsonObject createValidNewContainerRequest(String title, String number){
        JsonObject testRequest = new JsonObject();
        testRequest.addProperty("title", "TEST - " + title);
        testRequest.addProperty("number", "TEST - " + number);
        JsonArray recordsArray = new JsonArray();
        testRequest.add("records", recordsArray);
        return testRequest;
    }
}
