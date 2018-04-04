package com.discovery.channel.model;

import com.discovery.channel.database.DbConnect;
import com.discovery.channel.database.LocationController;
import com.discovery.channel.exception.IllegalArgumentException;
import com.discovery.channel.exception.ValidationException;
import jdk.management.resource.ResourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class RecordNumber {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordNumber.class);

    private static final Random RAN = new Random(System.currentTimeMillis());

    public enum NUMBER_PATTERN{
        CLIENT("KKK-CLIENT.ggg", "[a-zA-Z]{3}-CLIENT"),
        TASK("KKK-TASK-XXXX", "[a-zA-Z]{3}-TASK-[a-zA-Z0-9]{4}"),
        ACCOUNTING("KKK-XXXX-XXXX.XXX", "[a-zA-Z]{3}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}\\.[a-zA-Z0-9]{3}"),
        CASE_RECORDS__22CHAR("KKK-XXXXXXXXXXXXXXXXXXXXXX", "[a-zA-Z]{3}-[a-zA-Z0-9]{22}"),
        CASE_RECORDS__23CHAR("KKK-XXXXXXXXXXXXXXXXXXXXXXX", "[a-zA-Z]{3}-[a-zA-Z0-9]{23}"),
        SUBJECT("KKK-yyyy/ggg", "[a-zA-Z]{3}-[0-9]{4}"),
        PROPOSAL("KKK_P_yyyy.ggg", "[a-zA-Z]{3}_P_[0-9]{4}"),
        PROJECT("nnnnzzzz.nn.a.nn.nn[:nn]", "[a-zA-Z0-9]{8}\\.[a-zA-Z0-9]{2}\\.[a-zA-Z0-9]\\.[a-zA-Z0-9]{2}\\.[a-zA-Z0-9]{2}");

        static final String VOLUME_SEPARATOR= ":";
        static final String VOLUME_REGEX = ":[0-9]{2}";
        static final String AUTO_GEN_PLACE_HOLDER = "ggg";

        public final String patternStr;
        public final String regex;
        NUMBER_PATTERN(String patternStr, String regex) {
            this.patternStr = patternStr;
            this.regex = regex;
        }

        public static NUMBER_PATTERN fromString(String patternStr) {
            switch (patternStr) {
                case "KKK-CLIENT.gggg":
                    return CLIENT;
                case "KKK-TASK-XXX":
                    return TASK;
                case "KKK-XXXX-XXXX.XXX":
                    return ACCOUNTING;
                case "KKK-XXXXXXXXXXXXXXXXXXXXXX":
                    return CASE_RECORDS__22CHAR;
                case "KKK-XXXXXXXXXXXXXXXXXXXXXXX":
                    return CASE_RECORDS__23CHAR;
                case "KKK-yyyy/ggg":
                    return SUBJECT;
                case "KKK_P_yyyy.ggg":
                    return PROPOSAL;
                case "nnnnzzzz.nn.a.nn.nn[:nn]":
                    return PROJECT;
                default:
                    throw new IllegalArgumentException("Invalid number pattern : " + patternStr);

            }
        }

        /**
         * Test if a string matches this Number Pattern
         *
         * @param toMatch
         * @return
         */
        public boolean match(String toMatch) {
            // might contain volume number
            LOGGER.info("Validating record number {} against number pattern {}", toMatch, patternStr);
            String[] components = toMatch.split(VOLUME_SEPARATOR);
            String regexPattern = regex;
            if (components.length > 1) {
                // There is a volume component, so append the volume suffix
                regexPattern = regex + VOLUME_REGEX;
            }
            return toMatch.matches(regexPattern);
        }


        /**
         * Some patterns contain "ggg" auto filled fields; Populate it with a random numeric string
         *
         * @param recordNumber
         * @return
         */
        public String fillAutoGenField(String recordNumber) throws SQLException {
            int index = patternStr.indexOf(AUTO_GEN_PLACE_HOLDER);
            if (index < 0 ) {
                return recordNumber;
            }
            char separator = patternStr.charAt(index - 1);
            int digit = patternStr.length() - index;

            String maxNum = findMaxRecordNum( recordNumber + separator);
            int nextAvailableNum = 0;
            if (maxNum != null) {
                nextAvailableNum =  Integer.parseInt(maxNum.substring(index, index + digit)) + 1;
            }

            String genNum = "";
            if (digit == 3) {
                if (nextAvailableNum > 999) {
                    throw new ValidationException(String.format("Could not create record. Max number of records with number %s.",
                            recordNumber));
                }
                genNum = String.format("%03d", nextAvailableNum);
            }else {
                if (nextAvailableNum > 9999) {
                    throw new ValidationException(String.format("Could not create record. Max number of records with number %s.",
                            recordNumber));
                }
                genNum = String.format("%04d", nextAvailableNum);
            }
            return recordNumber + separator + genNum;
        }

        /**
         * Find record number with maximum auto generated number
         *
         * @param recordNumber
         * @return
         * @throws SQLException
         */
        private static final String FIND_MAX_RECORD_NUMBER = "SELECT Number " +
                "FROM records " +
                "WHERE Number LIKE ? " +
                "ORDER BY Number DESC";
        private static String findMaxRecordNum(String recordNumber) throws SQLException {
            try(Connection conn = DbConnect.getConnection();
                PreparedStatement ps = conn.prepareStatement(FIND_MAX_RECORD_NUMBER)) {
                ps.setString(1, "%" + recordNumber + "%");
                try (ResultSet resultSet = ps.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("Number");
                    }
                }
            }
            return null;
        }

        /**
         * For patterns contain location, check if it matches the record's set location
         *
         * @param locationCode
         * @param number
         * @return
         */
        public boolean matchLocation(String locationCode, String number) {
            if (this.patternStr.contains("KKK")) {
                return locationCode.toUpperCase().equals(number.substring(0, 3));
            }
            return true;
        }

       public String updateWithNewLocationCode(String recordNumber, String newLocationCode) {
           if (this.patternStr.contains("KKK")) {
               return  newLocationCode.toUpperCase() + recordNumber.substring(3, recordNumber.length());
           }
           LOGGER.debug("This number pattern does not contain location code. Return old record number");
           return recordNumber;
       }
    }
}
