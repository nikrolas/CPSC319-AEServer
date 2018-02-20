package com.discovery.channel.database;

import com.discovery.channel.exception.NoResultsFoundException;
import com.discovery.channel.model.Container;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.SQLException;
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

}
