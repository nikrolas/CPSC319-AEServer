package com.discovery.channel.model;

import com.discovery.channel.exception.IllegalArgumentException;

import java.util.Arrays;
import java.util.List;

public enum  RecordState {
    ACTIVE(1), INACTIVE(2), ARCHIVED_LOCAL(3), ARCHIVED_INTERIM(4), ARCHIVED_PERMANENT(5), DESTROYED(6);

    int id;
    RecordState(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public static RecordState fromId(int id) {
        switch (id) {
            case 1:
                return ACTIVE;
            case 2:
                return INACTIVE;
            case 3:
                return ARCHIVED_LOCAL;
            case 4:
                return ARCHIVED_INTERIM;
            case 5:
                return ARCHIVED_PERMANENT;
            case 6:
                return DESTROYED;
            default:
                throw new IllegalArgumentException("Record state with Id " + id + " does not exist");
        }
    }

    private static List<RecordState> VALID_STATES_FOR_RETENTION_SCHEDULE = Arrays.asList(ACTIVE, INACTIVE, ARCHIVED_LOCAL, ARCHIVED_INTERIM, DESTROYED);
    private static List<RecordState> VALID_STATES_FOR_NULL_RETENTION_SCHEDULE = Arrays.asList(ACTIVE, INACTIVE, ARCHIVED_LOCAL, ARCHIVED_PERMANENT);
    public boolean isValidforRetentionSchedule(boolean hasRetentionSchedule) {
        if (hasRetentionSchedule) {
            return VALID_STATES_FOR_RETENTION_SCHEDULE.contains(this);
        } else {
            return VALID_STATES_FOR_NULL_RETENTION_SCHEDULE.contains(this);
        }
    }

}
