package com.discovery.channel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RecordType {
    private int typeId;
    private String typeName;
    private String numberPattern;
    private String defaultSchedule;
}
