package model;

import com.discovery.channel.model.Classification;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClassificationTest {
    private static final String INVALID_CLASS_HIERARCHY = "PROJECT MANAGEMENT/Budgets and Schedules";
    private static final String INVALID_CLASSIFICATION_SHORT = "PROJECT MANAGEMENT";
    private static final String INVALID_CLASSIFICATION_WRONG_ROOT = "Budgets and Schedules/PROJECT MANAGEMENT";
    private static final String INVALID_CLASSIFICATION_NOT_EXIST = "PROJECT MANAGEMENT/A NON-EXISTENT CLASSIFICATION";

    private static final String VALID_CLASSIFICATION = "Advisory Services/Advice/Reports";

    @Test
    public void testValidateClassifications() throws SQLException {
        List<String> invalidStrings = Arrays.asList(INVALID_CLASSIFICATION_NOT_EXIST, INVALID_CLASSIFICATION_SHORT, INVALID_CLASS_HIERARCHY, INVALID_CLASSIFICATION_WRONG_ROOT);
        for (String invalidClass : invalidStrings) {
            assertFalse(Classification.validateClassification(invalidClass));
        }
        assertTrue(Classification.validateClassification(VALID_CLASSIFICATION));
    }
}
