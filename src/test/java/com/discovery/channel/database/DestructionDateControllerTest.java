package com.discovery.channel.database;

import com.discovery.channel.model.Record;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DestructionDateControllerTest {

    @Test
    void getDestructionDateFromOneRecord() throws SQLException {

        Date date = calculateDate(51);
        ResponseEntity<?>  entity = DestructionDateController.getDestructionDate("51");
        assertEquals(date.getTime(), entity.getBody());
    }


    @Test
    void getDestructionDateFromMultipleRecordWithSameSchedule() throws SQLException {

        Date date = calculateDate(51);
        ResponseEntity<?>  entity = DestructionDateController.getDestructionDate("51,157,156");
        assertEquals(date.getTime(), entity.getBody());
    }

    @Test
    void getDestructionDateFromMultipleRecordWithDifferentSchedule() throws SQLException {

        Date date = calculateDate(51);
        ResponseEntity<?>  entity = DestructionDateController.getDestructionDate("51,56,156");
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    @Test
    void getDestructionDateWithRecordWithoutClosedAt() throws SQLException {

        ResponseEntity<?>  entity = DestructionDateController.getDestructionDate("191,190");
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    @Test
    void getDestructionDateWithAllRecordWithoutClosedAt() throws SQLException {

        ResponseEntity<?>  entity = DestructionDateController.getDestructionDate("357,358,359");
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
