package com.discovery.channel.database;

import com.discovery.channel.model.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
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
        LOGGER.info("Calculating a destruction date");
        return calculateDestructionDate(listOfRecordIds);

    }


    /**
     * Calculate a destruction date based on the latest ClosedAt given a list of record ids plus the retention schedule
     *
     * @param listOfRecordIds
     * @return Destruction date in millisecond if success, error message otherwise
     * @throws SQLException
     */
    private static ResponseEntity<?> calculateDestructionDate(String[] listOfRecordIds) throws SQLException {

        Date currentClosedAt;
        Date theLatestClosedAt = null;
        int scheduleYear = 0;

        if(checkRecordsClosedAt(listOfRecordIds).isEmpty()){


            LOGGER.info("Getting the latest closure date given ids {}", listOfRecordIds);

            for (String id : listOfRecordIds){
                LOGGER.info("Getting a record by id {}", id);

                Record record = RecordController.getRecordById(Integer.valueOf(id));
                if (record != null){
                    currentClosedAt = record.getClosedAt();

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
                }else{
                    LOGGER.info("Records id {} does not exist", id);
                    String output = String.format("Records id %s does not exist", id);
                    return new ResponseEntity<>(output, HttpStatus.BAD_REQUEST);
                }
            }

            LOGGER.info("Passing all the validation");
            return new ResponseEntity<>(addYearToTheLatestClosureDate(scheduleYear, theLatestClosedAt), HttpStatus.OK);

        }else{

            LOGGER.info("Records id {} do not have ClosedAt");
            return new ResponseEntity<>(checkRecordsClosedAt(listOfRecordIds), HttpStatus.BAD_REQUEST);
        }

    }


    /**
     * Calculate the destruction date given schedule year and the latest ClosedAt
     *
     * @param theLatestClosureDate
     * @return Destruction date in millisecond if success, error message otherwise
     * @throws SQLException
     */
    public static long addYearToTheLatestClosureDate(int year, Date theLatestClosureDate){

        LOGGER.info("Calculating a destruction date");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(theLatestClosureDate);
        calendar.add(Calendar.YEAR, year);

        java.util.Date date = calendar.getTime();

        return date.getTime();

    }


    /**
     * Check ClosedAt given record ids
     *
     * @param listOfRecordIds
     * @return Record ids that do not meet requirements
     * @throws SQLException
     */
    public static ArrayList<String> checkRecordsClosedAt(String[] listOfRecordIds) throws SQLException {

        ArrayList<String> noClosureDate = new ArrayList<>();

        for (String id : listOfRecordIds){
            Record record = RecordController.getRecordById(Integer.valueOf(id));
            if(record.getClosedAt() == null) noClosureDate.add(id);
        }

        return noClosureDate;
    }

}
