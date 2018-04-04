package com.discovery.channel.model;

import com.discovery.channel.exception.IllegalArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class RecordNumber {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordNumber.class);

    private static final Random RAN = new Random(System.currentTimeMillis());

    /**
     * Generate three digit random number and convert it to a string
     * @return
     */
    public static String generateRandomNumber(int digit){
        int randomNum = RAN.nextInt(999);
        LOGGER.debug("Generated number " + randomNum);
        if (digit == 3) {
            return String.format("%03d", randomNum);
        }else {
            return String.format("%04d", randomNum);
        }
    }

    public enum NUMBER_PATTERN{
        CLIENT("KKK-CLIENT.ggg", "[a-zA-Z]{3}-CLIENT"),
        TASK("KKK-TASK-XXXX", "[a-zA-Z]{3}-TASK-[a-zA-Z0-9]{4}"),
        ACCOUNTING("KKK-XXXX-XXXX.XXX", "[a-zA-Z]{3}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}\\.[a-zA-Z0-9]{3}"),
        CASE_RECORDS__22CHAR("KKK-XXXXXXXXXXXXXXXXXXXXXX", "[a-zA-Z]{3}-[a-zA-Z0-9]{22}"),
        CASE_RECORDS__23CHAR("KKK-XXXXXXXXXXXXXXXXXXXXXXX", "[a-zA-Z]{3}-[a-zA-Z0-9]{23}"),
        PERSONNEL_CASE_RECORDS("XXX-ZZZ/NN","[a-zA-Z0-9]{3}-[a-zA-Z0-9]{3}/[0-9]{2}"),
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
        public String fillAutoGenField(String recordNumber) {
            int index = patternStr.indexOf(AUTO_GEN_PLACE_HOLDER);
            if (index < 0 ) {
                return recordNumber;
            }
            char separator = patternStr.charAt(index - 1);
            int digit = patternStr.length() - index;
            return recordNumber + separator + generateRandomNumber(digit);
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
                return locationCode.equals(number.substring(0, 3));
            }
            return true;
        }
    }
}
