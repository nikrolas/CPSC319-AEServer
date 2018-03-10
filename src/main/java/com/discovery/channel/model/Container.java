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
    private Date createdAt;
    private Date updatedAt;
    private List<Integer> childRecordIds;

    private String consignmentCode;
    private int stateId;
    private String stateName;
    private int locationId;
    private String locationName;
    private int scheduleId;
    private String scheduleName;
    private int type;

    public Container(int id,
                     String number,
                     String title,
                     Date createdAt,
                     Date updatedAt,
                     List<Integer> childRecordIds){
        this.containerId = id;
        this.containerNumber = number;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.childRecordIds = childRecordIds;
    }

}
