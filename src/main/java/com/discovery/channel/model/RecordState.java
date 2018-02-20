package com.discovery.channel.model;

public enum  RecordState {
    ACTIVE(1), INACTIVE(2), ARCHIVED_LOCAL(3), ARCHIVED_INTERIM(4), ARCHIVED_PERMANENT(5), DESTRYOED(6);

    int id;
    RecordState(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
}
