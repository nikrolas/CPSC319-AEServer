package com.discovery.channel.model;

public enum NoteTable {
    RECORDS(26), RecordTypes(29), RetentionSchedule(32), CLASSIFICATIONS(36), LOGINS(78), CONTAINERS(79);

    public final int id;
    NoteTable(int id) {
        this.id = id;
    }
}
