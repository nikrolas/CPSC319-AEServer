package com.discovery.channel.database;

import com.discovery.channel.model.Record;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DestructionDateControllerTest {



    @Test
    void getDestructionDateFromOneRecord() throws SQLException {

        Date date = calculateDate(51);

        ArrayList<Integer> listOfRecordIds = new ArrayList<>();
        listOfRecordIds.add(51);

        ResponseEntity<?>  entity = DestructionDateController.calculateDestructionDate(listOfRecordIds);
        assertEquals(date.getTime(), entity.getBody());
    }


    @Test
    void getDestructionDateFromMultipleRecordWithSameSchedule() throws SQLException {

        Date date = calculateDate(51);
        ArrayList<Integer> listOfRecordIds = new ArrayList<>();
        listOfRecordIds.add(51);
        listOfRecordIds.add(157);
        listOfRecordIds.add(156);
        ResponseEntity<?>  entity = DestructionDateController.calculateDestructionDate(listOfRecordIds);
        assertEquals(date.getTime(), entity.getBody());
    }

    @Test
    void getDestructionDateFromMultipleRecordWithDifferentSchedule() throws SQLException {

        Date date = calculateDate(51);
        ArrayList<Integer> listOfRecordIds = new ArrayList<>();
        listOfRecordIds.add(51);
        listOfRecordIds.add(56);
        listOfRecordIds.add(156);
        ResponseEntity<?>  entity = DestructionDateController.calculateDestructionDate(listOfRecordIds);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    @Test
    void getDestructionDateWithRecordWithoutClosedAt() throws SQLException {

        ArrayList<Integer> listOfRecordIds = new ArrayList<>();
        listOfRecordIds.add(191);
        listOfRecordIds.add(190);
        ResponseEntity<?>  entity = DestructionDateController.calculateDestructionDate(listOfRecordIds);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    @Test
    void getDestructionDateWithAllRecordWithoutClosedAt() throws SQLException {

        ArrayList<Integer> listOfRecordIds = new ArrayList<>();
        listOfRecordIds.add(357);
        listOfRecordIds.add(358);
        listOfRecordIds.add(359);
        ResponseEntity<?>  entity = DestructionDateController.calculateDestructionDate(listOfRecordIds);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }


    private Date calculateDate(int id) throws SQLException {

        Record record = RecordController.getRecordById(id);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(record.getClosedAt());
        calendar.add(Calendar.YEAR, record.getScheduleYear());
        Date date = calendar.getTime();

        return date;

    }

}
