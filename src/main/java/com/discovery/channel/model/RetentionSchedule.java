package com.discovery.channel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RetentionSchedule {
    private int id;
    private String name;
    private String code;
    private int years;
}
