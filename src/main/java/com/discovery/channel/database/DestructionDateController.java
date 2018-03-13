package com.discovery.channel.database;

import com.discovery.channel.model.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class DestructionDateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordController.class);


    /**
     * Get destruction date
     *
     * @param ids
     * @return Destruction date in millisecond if success, error message otherwise
     * @throws SQLException
     */
    public static final ResponseEntity<?> getDestructionDate(String ids) throws SQLException {

        String[] listOfRecordIds = ids.split(",");
        LOGGER.info("Getting a destruction date");
        return getTheLatestClosetAt(listOfRecordIds);

    }


    /**
     * Get the latest ClosedAt given a list of record ids
     *
     * @param listOfRecordIds
     * @return Destruction date in millisecond if success, error message otherwise
     * @throws SQLException
     */
    private static ResponseEntity<?> getTheLatestClosetAt(String[] listOfRecordIds) throws SQLException {

        Date currentClosedAt;
        Date theLatestClosedAt = null;
        int scheduleYear = 0;

        LOGGER.info("Getting the latest closure date given ids {}", listOfRecordIds);

        for (String id : listOfRecordIds){
            LOGGER.info("Getting a record by id {}", id);

            try{
                Record record = RecordController.getRecordById(Integer.valueOf(id));
                currentClosedAt = record.getClosedAt();
                if(currentClosedAt == null){
                    LOGGER.info("ClosedAt for record id {} is null", id);
                    String output = String.format("ClosedAt for record %s is null", id);
                    return new ResponseEntity<>(output, HttpStatus.BAD_REQUEST);
                }

                if (theLatestClosedAt == null) {
                    theLatestClosedAt = currentClosedAt;
                    scheduleYear = record.getScheduleYear();
                } else {
                    if (currentClosedAt.compareTo(theLatestClosedAt) == 1) {
                        theLatestClosedAt = currentClosedAt;
                    }
                }

                if(scheduleYear != record.getScheduleYear()){
                    LOGGER.info("ScheduleId for record id {} is different", id);
                    String output = String.format("ScheduleId for record id %s is different", id);
                    return new ResponseEntity<>(output, HttpStatus.BAD_REQUEST);
                }

            }catch (NullPointerException e){
                LOGGER.info("Record id {} does not exist", id);
                String output = String.format("Record id %s does not exist", id);
                return new ResponseEntity<>(output, HttpStatus.BAD_REQUEST);
            }
        }

        return calculateDestructionDate(scheduleYear, theLatestClosedAt);

    }


    /**
     * Calculate destruction date given schedule year and the latest ClosedAt
     *
     * @param theLatestClosureDate
     * @return Destruction date in millisecond if success, error message otherwise
     * @throws SQLException
     */
    private static ResponseEntity<?> calculateDestructionDate(int year, Date theLatestClosureDate){

        LOGGER.info("Calculating a destruction date");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(theLatestClosureDate);
        calendar.add(Calendar.YEAR, year);

        java.util.Date date = calendar.getTime();

        return new ResponseEntity<>(date.getTime(), HttpStatus.OK);

    }

}
