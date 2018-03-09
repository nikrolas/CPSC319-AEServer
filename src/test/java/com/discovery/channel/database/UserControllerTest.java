package com.discovery.channel.database;

import com.discovery.channel.model.User;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserControllerTest {

    @Test
    void getUserById() throws SQLException{

        User user = UserController.getUserByUserTableId(1);
        assertEquals(1, user.getId());
        assertEquals("reichertb", user.getUserId());
        assertEquals("Blaise", user.getFirstName());
        assertEquals("Reichert", user.getLastName());

        assertEquals(1, user.getRoleId());
        assertEquals("Administrator", user.getRole());

        assertFalse(user.getLocations().isEmpty());
        assertEquals(51, user.getLocations().get(0).getLocationId());
        assertEquals("Toronto", user.getLocations().get(0).getLocationName());

    }


}
