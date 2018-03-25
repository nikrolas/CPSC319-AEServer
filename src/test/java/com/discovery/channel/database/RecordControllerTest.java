package com.discovery.channel.database;

import com.discovery.channel.database.RecordController;
import com.discovery.channel.exception.AuthenticationException;
import com.discovery.channel.model.Record;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

public class RecordControllerTest {
    private static final int RESTRICTED_LOCATION_ID = 84;
    private static final int FULL_PRIV_RMC = 600;
    private static final int RMC = 500; // authorized for locations 8 and 5

    @Test
    public void testGetRecordById() throws SQLException {
        Record record = RecordController.getRecordById(51, FULL_PRIV_RMC);
        assertNotNull(record);
        // Make Sure all the details are loaded
        assertTrue(StringUtils.isNotBlank(record.getLocation()));
        assertTrue(StringUtils.isNotBlank(record.getType()));
        assertTrue(StringUtils.isNotBlank(record.getState()));
        assertTrue(StringUtils.isNotBlank(record.getContainerNumber()));
        assertTrue(StringUtils.isNotBlank(record.getClassifications()));
        assertTrue(StringUtils.isNotBlank(record.getNotes()));
        assertNotNull(record.getSchedule());
    }

    @Test
    public void testGetRecordWithRestrictedLocation() throws SQLException {
        // TODO Better to first create a record of the location and test against that
        Record record = RecordController.getRecordById(35801, FULL_PRIV_RMC);
        assertNotNull(record);
        // non owner cannot view records of restricted locations
        assertThrows(AuthenticationException.class, () -> RecordController.getRecordById(35801, RMC));
    }
}
