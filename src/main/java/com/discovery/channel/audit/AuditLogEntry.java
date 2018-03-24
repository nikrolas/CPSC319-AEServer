package com.discovery.channel.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
public class AuditLogEntry {
    private int id;
    private int userId;
    private String action;
    private String target;
    private int targetId;
    private Timestamp timestamp;
}
