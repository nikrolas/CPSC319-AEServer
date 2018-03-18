package com.discovery.channel.database;

import com.discovery.channel.model.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.sql.SQLException;
import java.util.*;

public class DestructionDateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordController.class);



    /**
     * Calculate a destruction date based on the latest ClosedAt given a list of record ids plus the retention schedule
     *
     * @param listOfRecordIds
     * @return Destruction date in millisecond if success, error message otherwise
     * @throws SQLException
     */
    public static ResponseEntity<?> calculateDestructionDate(List<Integer> listOfRecordIds) throws SQLException {

        LOGGER.info("Calculating a destruction date");

        Date currentClosedAt;
        Date theLatestClosedAt = null;
        int scheduleYear = 0;

        List<Record> listOfRecords = RecordController.getRecordsByIds(listOfRecordIds, true);
        Map<String, List<String>> listOfRecordsWithoutClosureDate = checkRecordsClosedAt(listOfRecords);
        List<String> temp = listOfRecordsWithoutClosureDate.get("id");

        if(temp.isEmpty() && !listOfRecords.isEmpty()){


            LOGGER.info("Getting the latest closure date given ids {}", listOfRecordIds);

            for (Record record : listOfRecords){
                LOGGER.info("Getting a record by id {}", record.getId());


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
                        LOGGER.info("ScheduleId for record id {} is different", record.getId());
                        Map<String, Object> response = new HashMap<>();
                        response.put("number", record.getNumber());
                        response.put("id", record.getId());
                        response.put("error", "ScheduleId is different");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
            }

            LOGGER.info("Passing all the validation");
            return new ResponseEntity<>(addYearToTheLatestClosureDate(scheduleYear, theLatestClosedAt), HttpStatus.OK);

        }else{

            Object recordIds = listOfRecordsWithoutClosureDate.get("id");
            LOGGER.info("Records id {} do not have ClosedAt", recordIds);
            Map<String, Object> response = new HashMap<>();
            response.put("number", listOfRecordsWithoutClosureDate.get("number"));
            response.put("id", recordIds);
            response.put("error", "Record(s) do not have ClosedAt");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
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
     * Check ClosedAt given records
     *
     * @param listOfRecords
     * @return Record ids that do not meet requirements
     * @throws SQLException
     */
    public static Map<String, List<String>> checkRecordsClosedAt(List<Record> listOfRecords) throws SQLException {

        Map<String, List<String>> response = new HashMap<>();

        List<String> recordNumbers = new ArrayList<>();
        List<String> recordIds = new ArrayList<>();

        for (Record record : listOfRecords){
            if(record != null){
                if(record.getClosedAt() == null) {
                    recordNumbers.add(record.getNumber());
                    recordIds.add(String.valueOf(record.getId()));
                }
            }
        }

        response.put("number", recordNumbers);
        response.put("id", recordIds);

        return response;
    }

}
