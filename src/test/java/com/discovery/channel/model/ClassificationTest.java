package com.discovery.channel.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClassificationTest {
    private static final List<Integer> INVALID_CLASS_HIERARCHY =  Arrays.asList(1066, 1365); //"PROJECT MANAGEMENT/Budgets and Schedules";
    private static final List<Integer> INVALID_CLASSIFICATION_SHORT = Arrays.asList(1066); //"PROJECT MANAGEMENT";
    private static final List<Integer> INVALID_CLASSIFICATION_WRONG_ROOT = Arrays.asList(1365, 1066); //"Budgets and Schedules/PROJECT MANAGEMENT";
    private static final List<Integer> INVALID_CLASSIFICATION_NOT_EXIST = Arrays.asList(1306, 4); //"PROJECT MANAGEMENT/A NON-EXISTENT CLASSIFICATION";

    private static final List<Integer> VALID_CLASSIFICATION = Arrays.asList(1063, 1212, 1047); // "Advisory Services/Advice/Reports";

    @Test
    public void testValidateClassifications() throws SQLException {
        List<List<Integer>> invalidStrings = Arrays.asList(INVALID_CLASSIFICATION_NOT_EXIST, INVALID_CLASSIFICATION_SHORT, INVALID_CLASS_HIERARCHY, INVALID_CLASSIFICATION_WRONG_ROOT);
        for (List<Integer> invalidClass : invalidStrings) {
            Assertions.assertFalse(Classification.validateClassification(invalidClass));
        }
        assertTrue(Classification.validateClassification(VALID_CLASSIFICATION));
    }
}
