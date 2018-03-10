package com.discovery.channel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class Container {

    private int containerId;
    private String containerNumber;
    private String title;
    private String consignmentCode;
    private Date createdAt;
    private Date updatedAt;
    private Date destructionDate;
    private List<Integer> childRecordIds;
    private String notes;
}
