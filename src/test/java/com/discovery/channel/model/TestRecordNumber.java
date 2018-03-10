package com.discovery.channel.model;

import com.discovery.channel.model.RecordNumber;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRecordNumber {
    // type id 70
    private static final String VALID_CLIENT = "edm-CLIENT";

    // type id 73
    private static final String VALID_TASK = "edm-TASK-N0G9";

    // type id 26
    private static final String VALID_ACCOUNTING = "edm-1234-ABCD.FFF";

    // type id 9
    private static final String VALID_CASE_RECORDS__22CHAR = "edm-XXXXXXXXaXXX0XX9XXXCXX";

    // type id 25
    private static final String VALID_CASE_RECORDS__23CHAR = "edm-XXXXXXXXaXXX0XX9XXXCXXY";

    // type id subject
    private static final String VALID_SUBJECT = "edm-2017";

    // type id 32
    private static final String VALID_PROPOSAL = "edm_P_1092";

    // type id 83
    private static final String VALID_PROJECT = "20232535.00.E.01.00";

    @Test
    public void testValidateRecordNumber(){
        assertTrue(RecordNumber.NUMBER_PATTERN.CLIENT.match(VALID_CLIENT));
        assertTrue(RecordNumber.NUMBER_PATTERN.TASK.match(VALID_TASK));
        assertTrue(RecordNumber.NUMBER_PATTERN.ACCOUNTING.match(VALID_ACCOUNTING));
        assertTrue(RecordNumber.NUMBER_PATTERN.CASE_RECORDS__22CHAR.match(VALID_CASE_RECORDS__22CHAR));
        assertTrue(RecordNumber.NUMBER_PATTERN.CASE_RECORDS__23CHAR.match(VALID_CASE_RECORDS__23CHAR));
        assertTrue(RecordNumber.NUMBER_PATTERN.SUBJECT.match(VALID_SUBJECT));
        assertTrue(RecordNumber.NUMBER_PATTERN.PROPOSAL.match(VALID_PROPOSAL));
        assertTrue(RecordNumber.NUMBER_PATTERN.PROJECT.match(VALID_PROJECT));
    }

    @Test
    public void testFillAutoGenField() {
        String filledNum = RecordNumber.NUMBER_PATTERN.CLIENT.fillAutoGenField(VALID_CLIENT);
        assertTrue(filledNum.matches(RecordNumber.NUMBER_PATTERN.CLIENT.regex + "\\.[0-9]{3}"));

        filledNum = RecordNumber.NUMBER_PATTERN.SUBJECT.fillAutoGenField(VALID_SUBJECT);
        assertTrue(filledNum.matches(RecordNumber.NUMBER_PATTERN.SUBJECT.regex + "/[0-9]{3}"));

    }
}
