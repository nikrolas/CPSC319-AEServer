package com.discovery.channel.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.List;

@Getter
@Setter
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
    private Date destructionDate;
    private String notes;

    public Container(int containerId,
                     String containerNumber,
                     String title,
                     Date createdAt,
                     Date updatedAt,
                     List<Integer> childRecordIds){
        this.containerId = containerId;
        this.containerNumber = containerNumber;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.childRecordIds = childRecordIds;
    }

    @JsonCreator
    public Container(@JsonProperty(value = "containerNumber", required = true) String number,
                     @JsonProperty(value = "title", required = true) String title,
                     @JsonProperty("notes") String notes) {
        this.containerNumber = number;
        this.title = title;
        this.notes = notes;
    }
}
