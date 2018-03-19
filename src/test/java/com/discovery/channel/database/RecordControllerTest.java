package com.discovery.channel.database;

import com.discovery.channel.database.RecordController;
import com.discovery.channel.model.Record;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

public class RecordControllerTest {

    @Test
    public void testGetRecordById() throws SQLException {
        Record record = RecordController.getRecordById(51);
        // Make Sure all the details are loaded
        assertTrue(StringUtils.isNotBlank(record.getLocation()));
        assertTrue(StringUtils.isNotBlank(record.getType()));
        assertTrue(StringUtils.isNotBlank(record.getState()));
        assertTrue(StringUtils.isNotBlank(record.getContainerNumber()));
        assertTrue(StringUtils.isNotBlank(record.getClassifications()));
        assertTrue(StringUtils.isNotBlank(record.getNotes()));
        assertNotNull(record.getSchedule());
    }
}
